package niva.geoserver.query;

import java.io.IOException;
import java.util.HashMap;

import org.geoserver.catalog.Catalog;
import org.geoserver.rest.RestException;

import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.BasicFactories;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;

import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.primitive.Point;
import org.opengis.geometry.primitive.PrimitiveFactory;
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

		final SimpleFeatureSource source = this.extractSourceFromPathVariable(workspace, layer);
		final CoordinateReferenceSystem crs = this.extractCRSFromPathVariable(epsg);
		final PrimitiveFactory gFact = BasicFactories.getDefault().getPrimitiveFactory(crs);
		
		Point pnt = gFact.createPoint( new double[] {east, north} );
		try {
			if ( !crs.equals(source.getSchema().getCoordinateReferenceSystem()) ) {
				pnt = (Point)pnt.transform( source.getSchema().getCoordinateReferenceSystem() ); 
			}
			
			final DirectPosition dp = pnt.getDirectPosition();

			final GeometryDescriptor geometry = source.getSchema().getGeometryDescriptor();
			

			final Filter withinFilt = CQL.toFilter("DWITHIN( " + geometry.getLocalName() 
					+ ", POINT(" + Double.toString(dp.getOrdinate(0)) + " " + Double.toString(dp.getOrdinate(1)) + ")"
					+ "," + Double.toString(dist) + ",meters)");
			
			return createResultMap(source.getFeatures(withinFilt));
		}
		catch (TransformException | CQLException | IOException ex) {
			throw new RestException(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
