package niva.geoserver.query;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import org.geoserver.catalog.Catalog;
import org.geoserver.feature.ReprojectingFeatureCollection;
import org.geoserver.rest.RestException;
import org.geoserver.rest.wrapper.RestWrapper;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.SchemaException;
import org.geotools.process.vector.ClipProcess;
import org.geotools.util.logging.Logging;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;


/**
 * Returns a simple representation of the features within a distance from a point.
 * Specification is like:
 * 
 * /query/{workspace}/{layer}/overlaps/{epsg}_{geometry}/features.{format}
 * 
 * @author Roar Brænden, NIVA
 *
 */
@RestController
@RequestMapping(path = QueryBaseController.QUERY_ROOT_PATH + "/overlaps/{epsg}_{geometry}/features.{format}",
				produces = { MediaType.APPLICATION_JSON_VALUE })
public class PolygonsOverlapsGeometryController extends QueryBaseController {

	private static final Logger LOGGER = Logging.getLogger(PolygonsOverlapsGeometryController.class);

	@Autowired
	public PolygonsOverlapsGeometryController(@Qualifier("catalog") Catalog catalog) {
		super(catalog);
	}


	@GetMapping
	public  RestWrapper<HashMap> get(@PathVariable String workspace,
									 @PathVariable String layer,
									 @PathVariable String epsg,
									 @PathVariable String geometry,
									 @PathVariable String format) {
		
		SimpleFeatureSource source = this.extractSourceFromPathVariable(workspace, layer);
		CoordinateReferenceSystem crs = this.extractCRSFromPathVariable(epsg);
		geometry = extractGeometryFromPathVariable(geometry);
		try {
		
			WKTReader reader = new WKTReader();
			Geometry geom = reader.read(geometry);
			
			SimpleFeatureCollection features;
			
			if (crs.equals(source.getSchema().getCoordinateReferenceSystem())) {
				features = source.getFeatures();
			}
			else {
				features = new ReprojectingFeatureCollection(source.getFeatures(), crs);
			}
			
			ClipProcess cp = new ClipProcess();
			SimpleFeatureCollection result;
			
			result = cp.execute(features, geom, true);
			
			return wrapObject(createResultMapWithArea(result), HashMap.class);
		}
		catch (ParseException | FactoryException | SchemaException | IOException ex) {
			throw new RestException(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
