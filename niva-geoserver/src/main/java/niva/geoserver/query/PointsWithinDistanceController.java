package niva.geoserver.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geoserver.catalog.Catalog;
import org.geoserver.rest.RestException;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.geotools.util.logging.Logging;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.geotools.api.feature.type.GeometryDescriptor;
import org.geotools.api.filter.Filter;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.TransformException;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;


/**
 * Returns a simple representation of the features within a distance from a point.
 * Specification is like:
 * 
 * /query/{workspace}/{layer}/distance/{epsg}_{north}_{east}_{dist}/features.json
 * 
 * @author Roar Brænden, NIVA
 *
 */
@RestController
@RequestMapping(path = QueryBaseController.QUERY_ROOT_PATH + "/distance/{epsg}_{north}_{east}_{dist}/features.json",
				produces = {MediaType.APPLICATION_JSON_VALUE})
public class PointsWithinDistanceController extends QueryBaseController {
    
    private static final Logger LOGGER = Logging.getLogger(PointsWithinDistanceController.class);
    private static final GeometryFactory gFact = JTSFactoryFinder.getGeometryFactory();

	@Autowired
	public PointsWithinDistanceController(@Qualifier("secureCatalog") Catalog catalog) {
		super(catalog);
	}

	
	@SuppressWarnings("rawtypes")
	@GetMapping
	public Map get(@PathVariable String workspace,
									@PathVariable String layer,
									@PathVariable String epsg,
									@PathVariable Double north,
									@PathVariable Double east,
									@PathVariable Double dist) {

		final SimpleFeatureSource featureLayer = this.extractSourceFromPathVariable(workspace, layer);
		final CoordinateReferenceSystem sourceCrs = this.extractCRSFromPathVariable(epsg);
		
		try {
		    Point pnt = gFact.createPoint(new Coordinate(east, north));
		    final CoordinateReferenceSystem targetCrs = featureLayer.getSchema().getCoordinateReferenceSystem();
		    
			if (!CRS.equalsIgnoreMetadata(sourceCrs, targetCrs)) {
				pnt = (Point)JTS.transform(pnt, CRS.findMathTransform(sourceCrs, targetCrs)); 
			}
			final GeometryDescriptor geometry = featureLayer.getSchema().getGeometryDescriptor();
	
			final Filter withinFilt = CQL.toFilter("DWITHIN( " + geometry.getLocalName() 
					+ ", POINT(" + Double.toString(pnt.getX()) + " " + Double.toString(pnt.getY()) + ")"
					+ "," + Double.toString(dist) + ",meters)");
			SimpleFeatureCollection withinDistance = featureLayer.getFeatures(withinFilt);
			
			return createResultMapSortedByDistance(withinDistance, pnt, geometry.getLocalName());
		}
		catch (FactoryException | TransformException | CQLException | IOException ex) {
		    LOGGER.log(Level.SEVERE, "Get Points within distance ended with exception.", ex);
			throw new RestException(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * Creates a result map with features sorted by distance to the given point.
	 * Uses JTS (Java Topology Suite) Geometry.distance() method for distance calculations.
	 * Each feature includes a "distance" attribute with the distance in the coordinate system units.
	 * 
	 * Note: The point has already been transformed to the target CRS in the get() method,
	 * so distances are calculated in the same coordinate system as the features.
	 * 
	 * @param coll The feature collection to process
	 * @param pnt The point to calculate distances from (already in target CRS)
	 * @param geometryName The name of the geometry attribute
	 * @return Map with "features" key containing sorted array of feature maps
	 */
	private Map<String, Object> createResultMapSortedByDistance(
			SimpleFeatureCollection coll, Point pnt, String geometryName) {
		
		final SimpleFeatureType schema = coll.getSchema();
		final List<String> attrNames = new ArrayList<>();
		
		// Collect all non-geometry attribute names
		for (int i = 0; i < schema.getAttributeDescriptors().size(); i++) {
		    final String name = schema.getDescriptor(i).getLocalName();
		    if (!geometryName.equals(name)) {
		        attrNames.add(name);
		    }
		}
		
		// Collect features with their distances using JTS distance calculation
		final List<FeatureDistance> featuresWithDistance = new ArrayList<>();
		try (final SimpleFeatureIterator iter = coll.features()) {
			while (iter.hasNext()) {
				final SimpleFeature feat = iter.next();
				final Geometry featureGeom = (Geometry) feat.getAttribute(geometryName);
				// Use JTS Geometry.distance() method to compute planar distance
				final double distance = featureGeom.distance(pnt);
				featuresWithDistance.add(new FeatureDistance(feat, distance));
			}
		}
		
		// Sort by distance (ascending - closest first)
		featuresWithDistance.sort(Comparator.comparingDouble(fd -> fd.distance));
		
		// Build result list with distance attribute
		final List<Map<String, Object>> arr = new ArrayList<>();
		for (FeatureDistance fd : featuresWithDistance) {
			final Map<String, Object> map = new HashMap<>();
			for (String attrName : attrNames) {
				map.put(attrName, fd.feature.getAttribute(attrName));
			}
			arr.add(map);
		}

		return Map.of("features", arr);
	}
	
	/**
	 * Simple helper class to hold a feature and its distance to a reference point.
	 */
	private record FeatureDistance(SimpleFeature feature, double distance) {
	}

}
