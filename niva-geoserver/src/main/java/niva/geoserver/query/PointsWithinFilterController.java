package niva.geoserver.query;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;
import org.geoserver.catalog.Catalog;
import org.geoserver.rest.RestException;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.util.logging.Logging;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.locationtech.jts.geom.Geometry;

/**
 * Returns a collection of points that is within a given polygon.
 * Specification is as follows:
 * 
 * /query/{workspace}/{layer}/filter/{workspace2}/{layer2}/{filter}/features.json
 * 
 * Response is a json {features:[{...},{...}....]}
 * @author Roar Brænden
 *
 */
@RestController
@RequestMapping(path = QueryBaseController.QUERY_ROOT_PATH + "/filter/{workspace2}/{layer2}/{filter}/features.json",
				produces = { MediaType.APPLICATION_JSON_VALUE })
public class PointsWithinFilterController extends QueryBaseController {
	
	private static final Logger LOGGER = Logging.getLogger(PointsWithinFilterController.class);
	
	private final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
	


	@Autowired
	public PointsWithinFilterController(@Qualifier("catalog") Catalog catalog) {
		super(catalog);
	}

	

	@SuppressWarnings("rawtypes")
	@GetMapping
	public HashMap get(@PathVariable String workspace,
									@PathVariable String layer,
									@PathVariable String workspace2,
									@PathVariable String layer2,
									@PathVariable String filter) {
		
		final SimpleFeatureSource pointSource = this.extractSourceFromPathVariable(workspace, layer);
		final SimpleFeatureSource polySource = this.extractSourceFromPathVariable(workspace2, layer2);
		try {
			Filter filt = CQL.toFilter(filter);
			SimpleFeatureCollection coll = polySource.getFeatures(filt);
			Geometry uni = null;
            
			try (SimpleFeatureIterator iter = coll.features()) {

	            while (iter.hasNext()) {
	                final SimpleFeature feat = iter.next();
	                LOGGER.fine("Record:" + feat.getID());
	                final Geometry geom = (Geometry)feat.getDefaultGeometry();
	                
	                uni = (uni != null && geom != null ? uni.union(geom) : geom);
	            }
			}
			
			final Filter within = ff.within(ff.property(pointSource.getSchema().getGeometryDescriptor().getLocalName()),
			                                ff.literal(uni));

			return createResultMap(pointSource.getFeatures(within));
		}
		catch (CQLException | IOException ex) {
			throw new RestException(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
