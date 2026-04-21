package niva.geoserver.query;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.feature.NameImpl;
import org.geotools.referencing.CRS;
import org.geotools.util.logging.Logging;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.rest.RestBaseController;
import org.geoserver.rest.RestException;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.PropertyDescriptor;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.NoSuchAuthorityCodeException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.springframework.http.HttpStatus;
import org.locationtech.jts.geom.Geometry;


/**
 * Abstract class that handles GET-requests that returns a key,value map.
 * The subclasses should implement doGetMap.
 * 
 * @author Roar Brænden, NIVA
 *
 */
public abstract class QueryBaseController extends RestBaseController {
	
	public static final String QUERY_ROOT_PATH = "/rest/query/{workspace}/{layer}";
	
	private Catalog catalog;
	
	private static final Logger LOGGER = Logging.getLogger(QueryBaseController.class);

	
	QueryBaseController(Catalog catalog) {
		this.catalog = catalog;
	}


	protected SimpleFeatureSource extractSourceFromPathVariable(String workspace, String layer) throws RestException {
		
		if (workspace==null || layer == null) {
			throw new RestException("workspace and layer must be specified", HttpStatus.BAD_REQUEST);
		}

		try {
			
			SimpleFeatureSource source = getFeatureSource(workspace, layer);

			if (source == null) {
				throw new RestException(String.format("Missing layer: %s:%s", workspace, layer), HttpStatus.NOT_FOUND);
			}

			return source;
		}
		catch (IOException ie) {
			LOGGER.log(Level.SEVERE, "Error while getting feature source " + workspace + ":" + layer, ie);
			throw new RestException(
					String.format("Exception when reading layer: %s:%s (%s)", workspace, layer, ie.getMessage()), 
			        HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		
	}
	
	protected CoordinateReferenceSystem extractCRSFromPathVariable(String epsg) throws RestException {

		if ( epsg == null ) {
			throw new RestException("Missing EPSG-code.", HttpStatus.BAD_REQUEST);
		}
		if ( !epsg.toUpperCase().startsWith("EPSG:") )
			epsg = "EPSG:" + epsg;
		
		try {
			CoordinateReferenceSystem crs = CRS.decode( epsg );
			
			return crs;
		} catch (NoSuchAuthorityCodeException e) {
			LOGGER.log(Level.SEVERE, "Problem with epsg code: " + epsg, e);
			throw new RestException("GeoServer can't handle EPSG-code", HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (FactoryException e) {
			LOGGER.log(Level.SEVERE, "Error with CRS factory.", e);
			throw new RestException("GeoServer isn't set up correctly", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	
	protected String extractGeometryFromPathVariable(String geometry) throws RestException {
		
		if ( geometry == null) {
			throw new RestException("Missing geometry.", HttpStatus.BAD_REQUEST);
		}
		try {
			geometry = URLDecoder.decode(geometry, "UTF-8");	
			return geometry;
		} catch (UnsupportedEncodingException e) {
			LOGGER.log(Level.SEVERE, "Error while extracting geometry: " + geometry, e);
			throw new RestException("GeoServer doesn't support UTF-8", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	
	protected Catalog getCatalog() {
		return this.catalog;
	}
	
	
	/**
	 * 
	 * @param workspace
	 * @param layer
	 * @return Null if layer is not found.
	 * @throws IOException
	 */
	protected SimpleFeatureSource getFeatureSource(String workspace, String layer) throws IOException {
		FeatureTypeInfo info = getCatalog().getFeatureTypeByName(new NameImpl(workspace, layer));
		if (info == null) {
			return null;
		}
		return (SimpleFeatureSource)info.getFeatureSource(null, null);
	}
	
	/**
	 * Creates a map with the features in the collection. The map has a key "features" with an array of maps as value.
	 * Each map in the array represents a feature, and has the attribute names as keys and the attribute values as values.
	 * The geometry attribute is not included in the result.
	 * @param coll
	 * @return
	 */
	protected Map<String, Object> createResultMap(SimpleFeatureCollection coll) {
		final SimpleFeatureType schema = coll.getSchema();
		final String geometry = schema.getGeometryDescriptor().getLocalName();
		final List<Pair<Integer, String>> descs = new ArrayList<>(schema.getAttributeCount());
		
		for (int i = 0; i < schema.getAttributeDescriptors().size(); i++) {
		    final String name = schema.getDescriptor(i).getLocalName();
		    if (!geometry.equals(name)) {
		        descs.add(new ImmutablePair<>(i, name));
		    }
		}
		
		final List<Map<String, Object>> arr = new ArrayList<>();
		try (final SimpleFeatureIterator iter = coll.features()) {
			while (iter.hasNext()) {
				final SimpleFeature feat = iter.next();
				final Map<String, Object> map = new HashMap<>();
				descs.forEach(desc -> map.put(desc.getRight(), feat.getAttribute(desc.getLeft())));
				arr.add(map);
			}
		}
		return Map.of("features", arr);
	}
	
	protected Map<String, Object> createResultMapWithArea(SimpleFeatureCollection coll) {
		List<Map<String, Object>> arr = new ArrayList<>();
		try (SimpleFeatureIterator iter = coll.features()) {
			Collection<PropertyDescriptor> descs = coll.getSchema().getDescriptors();
			String geometry = coll.getSchema().getGeometryDescriptor().getLocalName();
			
			while (iter.hasNext()) {
				SimpleFeature feat = iter.next();
				Map<String, Object> map = new HashMap<>();
				for (PropertyDescriptor desc: descs) {
					String name = desc.getName().getLocalPart();
					if (!name.equals(geometry)) {
						Object value = feat.getAttribute(desc.getName());
						map.put(name, value);
					}
					else {
						map.put("area", ((Geometry)feat.getAttribute(geometry)).getArea());
					}
				}
				arr.add(map);
			}	
		}
		return Map.of("features", arr);
	}
	
	protected Map<String, Object> createEmptyResult() {
		return Map.of("features", null);
	}

}
