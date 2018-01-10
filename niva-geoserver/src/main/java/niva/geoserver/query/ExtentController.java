package niva.geoserver.query;

import java.io.IOException;
import java.util.HashMap;

import org.geoserver.catalog.Catalog;
import org.geoserver.rest.RestException;
import org.geoserver.rest.wrapper.RestWrapper;

import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * Returnerer extent av et feature layer. Bruker getBounds().
 * Spesifikasjonen er som følger:
 * 
 * /query/{workspace}/{layer}/extent.{format}
 * 
 * @author Roar Brænden
 *
 */
@RestController
@RequestMapping(path=QueryBaseController.QUERY_ROOT_PATH + "/extent.{format}", produces= {MediaType.APPLICATION_JSON_VALUE})
public class ExtentController extends QueryBaseController {
	

	@Autowired
	public ExtentController(@Qualifier("catalog") Catalog catalog) {
		super(catalog);
	}


	@SuppressWarnings("rawtypes")
	@GetMapping()
	public RestWrapper<HashMap> get(@PathVariable String workspace, @PathVariable String layer) {
		SimpleFeatureSource source = this.extractSourceFromPathVariable(workspace, layer);
		try {
			ReferencedEnvelope env = source.getBounds();
			
			HashMap<String, Double> extent = new HashMap<String, Double>();
			
			extent.put("minX", env.getMinX());
			extent.put("maxX", env.getMaxX());
			extent.put("minY", env.getMinY());
			extent.put("maxY", env.getMaxY());
			
			return wrapObject(extent, HashMap.class);
		}
		catch (IOException ie) {
			throw new RestException("Exception while getting extent.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
