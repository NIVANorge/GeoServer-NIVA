package niva.geoserver.query;


import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geoserver.catalog.Catalog;
import org.geoserver.rest.RestException;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.geotools.util.logging.Logging;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.geotools.api.filter.Filter;
import org.geotools.api.filter.FilterFactory;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.TransformException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;



/**
 * Returns a collection of points that is within a given geometry.
 * Spesifikasjonen for GET er som følger:
 * 
 * /query/{workspace}/{layer}/geometry/{epsg}_{geometry}/features.json
 * 
 * Specification for POST is:
 * 
 * /query/{workspace}/{layer}/geometry/{epsg}/features.json
 * 
 * Resultatet returneres som et array med de resulterende features.
 * @author Roar Brænden
 *
 */
@RestController
@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
public class PointsWithinGeometryController extends QueryBaseController {
	
	private static final Logger LOGGER = Logging.getLogger(PointsWithinGeometryController.class);
	
	private static final FilterFactory FF = CommonFactoryFinder.getFilterFactory();


	@Autowired
	public PointsWithinGeometryController(@Qualifier("secureCatalog") Catalog catalog) {
		super(catalog);
	}

	@SuppressWarnings("rawtypes")
	@GetMapping(path = QueryBaseController.QUERY_ROOT_PATH + "/geometry/{epsg}_{geometry}/features.json")
	public Map get(@PathVariable String workspace,
									@PathVariable String layer,
									@PathVariable String epsg,
									@PathVariable String geometry) {
		LOGGER.fine("Query a geometry: " + geometry);
		
		SimpleFeatureSource source = this.extractSourceFromPathVariable(workspace, layer);
		String geomField = source.getSchema().getGeometryDescriptor().getLocalName();
		final CoordinateReferenceSystem sourceCrs = this.extractCRSFromPathVariable(epsg);
		final CoordinateReferenceSystem targetCrs = source.getSchema().getCoordinateReferenceSystem();
		final String decodedGeometry = this.extractGeometryFromPathVariable(geometry);

		try {
        	Geometry geom = parseGeometry(decodedGeometry);
        	if (!CRS.equalsIgnoreMetadata(sourceCrs, targetCrs)) {
        		geom = JTS.transform(geom,  CRS.findMathTransform(sourceCrs, targetCrs));
        	}
			final Filter within = FF.intersects(geomField, geom);
			return createResultMap(source.getFeatures(within));
		}
		catch (TransformException | FactoryException | IOException ex) {
		    LOGGER.log(Level.SEVERE, "Get Points within geometry.", ex);
			throw new RestException(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	

    @SuppressWarnings("rawtypes")
    @PostMapping(path = QueryBaseController.QUERY_ROOT_PATH + "/geometry/{epsg}/features.json")
    public Map post(@PathVariable String workspace,
                                    @PathVariable String layer,
                                    @PathVariable String epsg,                                   
                                    @RequestBody String geometry) {
        LOGGER.fine("Query a geometry.");
        
        SimpleFeatureSource source = this.extractSourceFromPathVariable(workspace, layer);
        String geomField = source.getSchema().getGeometryDescriptor().getLocalName();
        final CoordinateReferenceSystem targetCrs = this.extractCRSFromPathVariable(epsg);
        final CoordinateReferenceSystem sourceCrs = source.getSchema().getCoordinateReferenceSystem();
        
        try {
        	Geometry geom = parseGeometry(geometry);
        	if (!CRS.equalsIgnoreMetadata(sourceCrs, targetCrs)) {
        		geom = JTS.transform(geom,  CRS.findMathTransform(sourceCrs, targetCrs));
        	}
            final Filter within = FF.intersects(geomField, geom);
            
            SimpleFeatureCollection result = source.getFeatures(within);
        
            return createResultMap(result);
        }
        catch (TransformException | FactoryException | IOException ex) {
            LOGGER.log(Level.SEVERE, "Post Points within geometry.", ex);
            throw new RestException(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    private Geometry parseGeometry(String decodedGeom) {
        try {
			return new WKTReader(JTSFactoryFinder.getGeometryFactory()).read(decodedGeom);
		} catch (ParseException e) {
			throw new RestException(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
    }
}
