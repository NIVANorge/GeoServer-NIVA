package niva.aquamonitor.data;


import java.io.Serializable;
import java.util.HashMap;


import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureIterator;

import niva.geotools.data.CacheDataStore;
import niva.geotools.data.CacheDataStoreFactory;
import niva.geotools.data.CacheFeatureStore;


import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 
 * Tests for usage of Postgis as an CacheDataStore for AquaMonitor specific datastores.
 * @author Roar Brænden
 *
 */
public class PostgisCacheDataStoreTest {
	
	private final String HOST = "etna.niva.no";
	private final String USER  = "nivakart";
	private final String PASSWORD = "316miljo";
	
	
	@Test
	public void stationPointToPostgisTest() throws Exception {
		
		final HashMap<String, Serializable> params = new HashMap<String, Serializable>();
		params.put(CacheDataStoreFactory.NAMESPACE_PARAM.key, "http://www.aquamonitor.no/");
		params.put(CacheDataStoreFactory.DBTYPE_PARAM.key, (Serializable) CacheDataStoreFactory.DBTYPE_PARAM.sample);
		params.put(CacheDataStoreFactory.BACKEND_PARAM.key, "dbtype=aquamonitor;user=Ostfold");
		params.put(CacheDataStoreFactory.CACHE_PARAM.key, "dbtype=" + PostgisNGDataStoreFactory.DBTYPE.sample + 
														  ";host=" + HOST +
														  ";port=" + PostgisNGDataStoreFactory.PORT.sample +
														  ";user=" + USER +
														  ";passwd=" + PASSWORD +
														  ";database=temp_1" + 
														  ";create database=true");
		
		params.put(CacheDataStoreFactory.INTERVAL_PARAM.key, (Serializable) CacheDataStoreFactory.INTERVAL_PARAM.sample);
		
		
		final CacheDataStore store = new CacheDataStoreFactory().createDataStore(params);
		assertNotNull(store);

		
		final CacheFeatureStore source = store.getFeatureSource(store.getNames().get(0));
		final SimpleFeatureIterator iterator = source.getFeatures().features();
		try {
			assertTrue(iterator.hasNext());
		}
		finally {
			iterator.close();
		}
	}
}
