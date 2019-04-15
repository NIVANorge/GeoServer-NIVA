package niva.geoserver.query;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;


import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.NameImpl;
import org.geotools.referencing.CRS;
import org.geotools.util.logging.Logging;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.rest.RestBaseController;
import org.geoserver.rest.RestException;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.springframework.http.HttpStatus;

import com.vividsolutions.jts.geom.Geometry;



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

	
	public QueryBaseController(Catalog catalog) {
		this.catalog = catalog;
	}


	protected SimpleFeatureSource extractSourceFromPathVariable(String workspace, String layer) throws RestException {
		
		if (workspace==null || layer == null) {
			throw new RestException("workspace and layer must be specified", HttpStatus.BAD_REQUEST);
		}

		try {
			
			SimpleFeatureSource source = getFeatureSource(workspace, layer);

			if (source == null) {
				throw new RestException("Missing layer: " + workspace + ":" + layer, HttpStatus.NOT_FOUND);
			}

			return source;
		}
		catch (IOException ie) {
			LOGGER.severe(ie.getMessage());
			throw new RestException("Exception when reading layer.", HttpStatus.INTERNAL_SERVER_ERROR);
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
			throw new RestException("GeoServer can't handle EPSG-code", HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (FactoryException e) {
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
	

	
	protected HashMap<String, Object> createResultMap(SimpleFeatureCollection coll) {
		HashMap<String, Object> features = new HashMap<String, Object>();
		
		SimpleFeatureIterator iter = coll.features();
		ArrayList<HashMap<String, Object>> arr = new ArrayList<HashMap<String, Object>>();
		Collection<PropertyDescriptor> descs = coll.getSchema().getDescriptors();
		String geometry = coll.getSchema().getGeometryDescriptor().getLocalName();
		
		while (iter.hasNext()) {
			SimpleFeature feat = iter.next();
			
			HashMap<String, Object> map = new HashMap<String, Object>();
			
			for (PropertyDescriptor desc: descs) {
				String name = desc.getName().getLocalPart();
				if (!name.equals(geometry)) {
					Object value;
					value = feat.getAttribute(desc.getName());
					map.put(name, value);
				}
			}
			arr.add(map);
		}
		
		iter.close();
		
		features.put("features", arr);
		
		return features;
	}
	
	protected HashMap<String, Object> createResultMapWithArea(SimpleFeatureCollection coll) {
		HashMap<String, Object> features = new HashMap<String, Object>();
		
		SimpleFeatureIterator iter = coll.features();
		ArrayList<HashMap<String, Object>> arr = new ArrayList<HashMap<String, Object>>();
		Collection<PropertyDescriptor> descs = coll.getSchema().getDescriptors();
		String geometry = coll.getSchema().getGeometryDescriptor().getLocalName();
		
		while (iter.hasNext()) {
			SimpleFeature feat = iter.next();
			
			HashMap<String, Object> map = new HashMap<String, Object>();
			
			for (PropertyDescriptor desc: descs) {
				String name = desc.getName().getLocalPart();
				if (!name.equals(geometry)) {
					Object value;
					value = feat.getAttribute(desc.getName());
					map.put(name, value);
				}
				else {
					map.put("area", ((Geometry)feat.getAttribute(geometry)).getArea());
				}
			}
			arr.add(map);
		}
		
		iter.close();
		
		features.put("features", arr);
		
		return features;
	}
	
	protected HashMap<String, Object> createEmptyResult() {
		HashMap<String, Object> features = new HashMap<String, Object>();
		features.put("features", null);
		return features;
	}

}
