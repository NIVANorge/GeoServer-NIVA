package niva.geoserver.query;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geoserver.catalog.Catalog;
import org.geoserver.rest.RestException;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.geotools.util.logging.Logging;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
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
	public PointsWithinDistanceController(@Qualifier("catalog") Catalog catalog) {
		super(catalog);
	}

	
	@SuppressWarnings("rawtypes")
	@GetMapping
	public HashMap get(@PathVariable String workspace,
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
			
			return createResultMap(featureLayer.getFeatures(withinFilt));
		}
		catch (FactoryException | TransformException | CQLException | IOException ex) {
		    LOGGER.log(Level.SEVERE, "Get Points within distance ended with exception.", ex);
			throw new RestException(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
