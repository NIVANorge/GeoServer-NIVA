package niva.aquamonitor.data;


import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import org.geotools.api.data.Query;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.filter.text.cql2.CQL;
import niva.geotools.data.CacheDataStore;
import niva.geotools.data.CacheDataStoreFactory;
import niva.geotools.data.CacheFeatureStore;
import org.junit.Assert;
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
	
	private Map<String, ?> getTestParameters() {
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
        
        return params;
	}
	
	@Test
	public void stationPointToPostgisTest() throws Exception {
		final CacheDataStore store = new CacheDataStoreFactory().createDataStore(getTestParameters());
		assertNotNull(store);

		final CacheFeatureStore source = store.getFeatureSource(store.getNames().get(0));
		try (SimpleFeatureIterator iterator = source.getFeatures().features()){
			assertTrue(iterator.hasNext());
		}
		finally {
			store.dispose();
		}
	}
	
	/**
	 * Tests the functionality to update a record within the cache
	 * First it deletes all records for a given station.
	 * Then it will call modifyFeatures for the same station_id.
	 * @throws Exception
	 */
	@Test
	public void testModifyFeaturesUpdatingCache() throws Exception {
	    final String pgConnectionStr = "jdbc:postgresql://"+HOST+":"+PostgisNGDataStoreFactory.PORT.sample+"/temp_1";

	    try (Connection conn = DriverManager.getConnection(pgConnectionStr, USER, PASSWORD)) {
	        conn.createStatement().execute("delete from public.\"STATION_POINTS\" where \"STATION_ID\"=9456");
	    }
	    
	    final CacheDataStore store = new CacheDataStoreFactory().createDataStore(getTestParameters());
	    
        try {
            CacheFeatureStore featureStore = store.getFeatureSource("STATION_POINTS");
            
            Query query = new Query();
            query.setFilter(CQL.toFilter("STATION_ID=9456"));
            
            int ant = featureStore.getCount(query);
            Assert.assertEquals(0, ant);
            
            featureStore.modifyFeatures(query.getFilter());
            
            int ant2 = featureStore.getCount(query);
            Assert.assertTrue(ant2 > 0);
        }
        finally {
            store.dispose();
        }
	}
}
