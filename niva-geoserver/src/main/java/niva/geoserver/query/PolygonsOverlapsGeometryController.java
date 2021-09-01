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
import org.geotools.process.vector.ClipProcess;
import org.geotools.referencing.CRS;
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

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;


/**
 * Returns a simple representation of the features within a distance from a point.
 * Specification is like:
 * 
 * /query/{workspace}/{layer}/overlaps/{epsg}_{geometry}/features.json
 * 
 * @author Roar Brænden, NIVA
 *
 */
@RestController
@RequestMapping(path = QueryBaseController.QUERY_ROOT_PATH + "/overlaps/{epsg}_{geometry}/features.json",
				produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
public class PolygonsOverlapsGeometryController extends QueryBaseController {

	private static final Logger LOGGER = Logging.getLogger(PolygonsOverlapsGeometryController.class);

	@Autowired
	public PolygonsOverlapsGeometryController(@Qualifier("catalog") Catalog catalog) {
		super(catalog);
	}


	@SuppressWarnings("rawtypes")
	@GetMapping
	public HashMap get(@PathVariable String workspace,
									 @PathVariable String layer,
									 @PathVariable String epsg,
									 @PathVariable String geometry) {
		
		final SimpleFeatureSource source = this.extractSourceFromPathVariable(workspace, layer);
		final CoordinateReferenceSystem targetCrs = this.extractCRSFromPathVariable(epsg);
		final CoordinateReferenceSystem sourceCrs = source.getSchema().getCoordinateReferenceSystem();
		final String decodedGeometry = extractGeometryFromPathVariable(geometry);
		try {
		
			Geometry geom = new WKTReader().read(decodedGeometry);
			
			SimpleFeatureCollection features = (CRS.equalsIgnoreMetadata(sourceCrs, targetCrs) 
			        ? source.getFeatures()
			        : new ReprojectingFeatureCollection(source.getFeatures(), targetCrs));
			
			return createResultMapWithArea(new ClipProcess().execute(features, geom, true));
		}
		catch (ParseException | FactoryException | SchemaException | IOException ex) {
			LOGGER.severe(ex.getMessage());
			throw new RestException(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
