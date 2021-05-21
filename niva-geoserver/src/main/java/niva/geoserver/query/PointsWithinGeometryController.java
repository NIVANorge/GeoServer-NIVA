package niva.geoserver.query;


import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;
import org.geoserver.catalog.Catalog;
import org.geoserver.feature.ReprojectingFeatureCollection;
import org.geoserver.rest.RestException;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.SchemaException;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.referencing.CRS;
import org.geotools.util.logging.Logging;
import org.opengis.filter.Filter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;



/**
 * Returns a collection of points that is within a given geometry.
 * Spesifikasjonen er som følger:
 * 
 * /query/{workspace}/{layer}/geometry/{epsg}_{geometry}/features.json
 * 
 * Resultatet returneres som et array med de resulterende features.
 * @author Roar Brænden
 *
 */
@RestController
@RequestMapping(path = QueryBaseController.QUERY_ROOT_PATH + "/geometry/{epsg}_{geometry}/features.json",
				produces = {MediaType.APPLICATION_JSON_VALUE})
public class PointsWithinGeometryController extends QueryBaseController {
	
	private static final Logger LOGGER = Logging.getLogger(PointsWithinGeometryController.class);


	@Autowired
	public PointsWithinGeometryController(@Qualifier("catalog") Catalog catalog) {
		super(catalog);
	}

	

	@SuppressWarnings("rawtypes")
	@GetMapping
	public HashMap get(@PathVariable String workspace,
									@PathVariable String layer,
									@PathVariable String epsg,
									@PathVariable String geometry) {
		LOGGER.fine("Query a geometry.");
		
		SimpleFeatureSource source = this.extractSourceFromPathVariable(workspace, layer);
		String geomField = source.getSchema().getGeometryDescriptor().getLocalName();
		final CoordinateReferenceSystem targetCrs = this.extractCRSFromPathVariable(epsg);
		final CoordinateReferenceSystem sourceCrs = source.getSchema().getCoordinateReferenceSystem();
		final String decodedGeometry = this.extractGeometryFromPathVariable(geometry);

		try {
			
			final Filter within = CQL.toFilter("WITHIN( " + geomField	+ ", " + decodedGeometry + ")");
	
			SimpleFeatureCollection result = (CRS.equalsIgnoreMetadata(sourceCrs, targetCrs) ?
			        source.getFeatures(within) :
				    new ReprojectingFeatureCollection(source.getFeatures(), targetCrs).subCollection(within));
		
			return createResultMap(result);
		}
		catch (FactoryException | CQLException | SchemaException | IOException ex) {
			throw new RestException(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
