package niva.geoserver.query;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geoserver.catalog.Catalog;
import org.geoserver.rest.RestException;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.logging.Logging;
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
 * /query/{workspace}/{layer}/extent.json
 * 
 * @author Roar Brænden
 *
 */
@RestController
@RequestMapping(path=QueryBaseController.QUERY_ROOT_PATH + "/extent.json", 
        produces= {MediaType.APPLICATION_JSON_VALUE})
public class ExtentController extends QueryBaseController {
	
	private static final Logger LOGGER = Logging.getLogger(ExtentController.class);

	@Autowired
	public ExtentController(@Qualifier("secureCatalog") Catalog catalog) {
		super(catalog);
	}


	@SuppressWarnings("rawtypes")
	@GetMapping()
	public HashMap get(@PathVariable String workspace, @PathVariable String layer) {
		SimpleFeatureSource source = this.extractSourceFromPathVariable(workspace, layer);
		try {
			ReferencedEnvelope env = source.getBounds();
			
			HashMap<String, Double> extent = new HashMap<String, Double>();
			
			extent.put("minX", env.getMinX());
			extent.put("maxX", env.getMaxX());
			extent.put("minY", env.getMinY());
			extent.put("maxY", env.getMaxY());
			
			return extent;
		}
		catch (IOException ex) {
			LOGGER.log(Level.SEVERE, "Get extent of layer.", ex);
			throw new RestException("Exception while getting extent of layer:" + layer + ".", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
