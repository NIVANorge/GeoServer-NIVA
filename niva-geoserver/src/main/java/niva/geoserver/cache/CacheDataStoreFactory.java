package niva.geoserver.cache;

import java.awt.RenderingHints.Key;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.Collections;
import niva.geotools.convert.StringToParams;
import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFactorySpi;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.util.logging.Logging;

/**
 * Cache with datastores
 * 
 * @author Roar Brænden, NIVA
 *
 */
public class CacheDataStoreFactory implements DataStoreFactorySpi {
	
	public static final Param DBTYPE_PARAM = new Param("dbtype", String.class, "fixed value. Must be \"cache\"", true, "cache");
	
	public static final Param NAMESPACE_PARAM = new Param("namespace", String.class, "namespace", true, null);
	
	public static final Param BACKEND_PARAM = new Param("backend", String.class, "connection-string to original data store.", true, null);
	
	public static final Param CACHE_PARAM = new Param("cache", String.class, "connection-string to cache store. null - memory based cache", false, null);
	
	public static final Param INTERVAL_PARAM = new Param("update", Integer.class, "update interval (min), 0 - manual update", false, 0);
	
	public static final Param CACHE_TYPE_NAME_PARAM = new Param("cache-name", String.class, "format of cache typeName, with backend typeName as input", false, "%s");
	
	public static final Map<String, Class<?>> knownNamespaceType = new HashMap<>();
	static {
		knownNamespaceType.put("shapefile", ShapefileDataStoreFactory.NAMESPACEP.type);
		knownNamespaceType.put(PostgisNGDataStoreFactory.DBTYPE.key, PostgisNGDataStoreFactory.NAMESPACE.type);
	}
	
	private static Logger LOGGER = Logging.getLogger(CacheDataStoreFactory.class);
	
	@SuppressWarnings("rawtypes") 
	@Override
	public boolean canProcess(final Map params) {

		return (DBTYPE_PARAM.sample.equals(params.get(DBTYPE_PARAM.key)));
	}

	@Override
	public DataStore createNewDataStore(Map<String, ?> params)
			throws IOException {
		throw new UnsupportedOperationException("We doesn't allow this at the moment.");
	}
	
	@Override
	public CacheDataStore createDataStore(Map<String, ?> params) throws IOException {
		
		final String ns = (String)params.get(NAMESPACE_PARAM.key);
		if (ns == null) {
			throw new IOException("Missing namespace");
		}
		LOGGER.info("Creating CacheDataStore with backend: " + params.get(BACKEND_PARAM.key));
		final Map<String, ?> backendParams = StringToParams.createParams((String)params.get(BACKEND_PARAM.key));
		createNamespaceParameter(backendParams, ns);
		final DataStore backend = DataStoreFinder.getDataStore(backendParams);
		if (backend == null) {
			throw new IOException("Passed backendparams {" + backendParams + "} doesn't give a DataStore.");
		}
		
		final String cacheParamString = (String)params.get(CACHE_PARAM.key);
		final DataStore cache;
		if (cacheParamString != null) {
			final Map<String, ?> cacheParams = StringToParams.createParams(cacheParamString);
			createNamespaceParameter(cacheParams, ns);
			
			cache = DataStoreFinder.getDataStore(cacheParams);
			if (cache == null) {
				throw new IOException("Passed cacheparams {" + cacheParams + "} doesn't give a DataStore.");
			}
		}
		else {
			cache = new MemoryDataStore();
		}
			
		final Object obj = params.get(INTERVAL_PARAM.key);
		final int interval = (obj.getClass() == String.class ? Integer.parseInt((String)obj) : (Integer)obj);        
		final String cacheNameFormat = (String)params.get(CACHE_TYPE_NAME_PARAM.key);
		final CacheDataStore cacheDataStore = new CacheDataStore(backend, cache, cacheNameFormat, interval);
		return cacheDataStore;
	}

	/**
	 * The parameter namespace are in certain cases expected to be of type URI, while in others it must be a String.
	 * We use the variable knownNamespaceType to find out which type to use.
	 *  
	 * @param params
	 * @param namespace
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
    private void createNamespaceParameter(Map<String, ?> params, String namespace) throws IOException {
		
		final String dbtype = (String)params.get(DBTYPE_PARAM.key);
		if (dbtype != null && knownNamespaceType.containsKey(dbtype)) {
			final Class<?> namespaceType = knownNamespaceType.get(dbtype);
			if (namespaceType == URI.class) {
				try {
					((Map<String, Object>)params).put(NAMESPACE_PARAM.key, new URI(namespace));
				} catch (URISyntaxException e) {
					throw new IOException("Wrong namespace uri: " + namespace, e);
				}
			}
			else if (namespaceType == String.class) {
			    ((Map<String, Object>)params).put(NAMESPACE_PARAM.key, namespace);
			}
			else {
				LOGGER.warning("Unknown type for namespace parameter {" + namespaceType.getName() + "}");
			}
		}
	}
	
	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	public Map<Key, ?> getImplementationHints() {
		return Collections.emptyMap();
	}

	
	@Override
	public String getDisplayName() {
		return "Cache data store";
	}

	@Override
	public String getDescription() {
		return "Makes a cache of an existsing data store.";
	}

	@Override
	public Param[] getParametersInfo() {
		return new Param[] {DBTYPE_PARAM, NAMESPACE_PARAM, BACKEND_PARAM, CACHE_PARAM, INTERVAL_PARAM, CACHE_TYPE_NAME_PARAM};
	}
	
}