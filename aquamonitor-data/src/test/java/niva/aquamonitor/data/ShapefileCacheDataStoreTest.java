package niva.aquamonitor.data;


import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.geotools.TestData;
import org.geotools.api.data.Query;
import org.geotools.data.memory.MemoryFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.data.store.ContentFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.util.logging.Logging;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.Name;
import org.geotools.api.filter.Filter;
import niva.geotools.data.CacheDataStore;
import niva.geotools.data.CacheDataStoreFactory;
import niva.geotools.data.CacheFeatureStore;
import org.junit.Test;
import org.junit.Assert;

public class ShapefileCacheDataStoreTest {
    private static final Logger LOGGER = Logging.getLogger(ShapefileCacheDataStoreTest.class);


	@Test
	public void stationPointToShapeTest() throws Exception {
	    File cacheFolder = new File(TestData.file(this, null), "cache_1");
	    cacheFolder.mkdir();
	    try {
    		final HashMap<String, Serializable> params = new HashMap<>();
    		params.put(CacheDataStoreFactory.NAMESPACE_PARAM.key, "http://www.aquamonitor.no/");
    		params.put(CacheDataStoreFactory.DBTYPE_PARAM.key, (Serializable) CacheDataStoreFactory.DBTYPE_PARAM.sample);
    		params.put(CacheDataStoreFactory.BACKEND_PARAM.key, "dbtype=aquamonitor;user=Ostfold");
    		params.put(CacheDataStoreFactory.CACHE_PARAM.key, "dbtype=shapefile;url=file:" + cacheFolder.getAbsolutePath());
    		params.put(CacheDataStoreFactory.INTERVAL_PARAM.key, (Serializable) CacheDataStoreFactory.INTERVAL_PARAM.sample);
    		
    		final CacheDataStore store = new CacheDataStoreFactory().createDataStore(params);
    		final List<Name> names = store.getNames();
    		CacheFeatureStore source = store.getFeatureSource(names.get(0));
    		
    		int i = source.getFeatures(Query.ALL).size();
    		Assert.assertTrue("Cache is empty.", i > 0);
	    }
	    finally {
	        FileUtils.deleteDirectory(cacheFolder);
	    }
	}
	
	@Test
	public void datatypePointToShapeTest() throws Exception {
		HashMap<String, Serializable> params = new HashMap<String, Serializable>();
		
		File cacheFolder = new File(TestData.file(this, null), "cache_2");
        cacheFolder.mkdir();
		params.put(CacheDataStoreFactory.NAMESPACE_PARAM.key, "http://www.aquamonitor.no/");	
		params.put(CacheDataStoreFactory.DBTYPE_PARAM.key, (Serializable) CacheDataStoreFactory.DBTYPE_PARAM.sample);
		params.put(CacheDataStoreFactory.BACKEND_PARAM.key, "dbtype=aquamonitor-site;site=Intern;host=https://test-aquamonitor.niva.no/");
		params.put(CacheDataStoreFactory.CACHE_PARAM.key, "dbtype=shapefile;url=file:" + cacheFolder.getAbsolutePath());
		params.put(CacheDataStoreFactory.INTERVAL_PARAM.key, (Serializable) CacheDataStoreFactory.INTERVAL_PARAM.sample);
		params.put(CacheDataStoreFactory.CACHE_TYPE_NAME_PARAM.key, "INTERN_%1s");
		
		CacheDataStore store = new CacheDataStoreFactory().createDataStore(params);
		try {
    		List<Name> names = store.getNames();
    		
    		Name pointsName = null;
    		for (Name n : names) {
    			if ("STATION_DATATYPE_POINTS".equals(n.getLocalPart())) {
    				pointsName = n;
    				break;
    			}		
    		}
    		Assert.assertNotNull(pointsName);
    		
    		CacheFeatureStore source = store.getFeatureSource(pointsName);
    		Assert.assertNotNull(source.getBounds());
		}
		finally {
		    store.dispose();
		    FileUtils.deleteDirectory(cacheFolder);
		}
	}
	
	
	@Test
	public void stationPointToMemoryTest() throws Exception {
		CacheDataStoreFactory factory = new CacheDataStoreFactory();
		HashMap<String, Serializable> params = new HashMap<String, Serializable>();
		params.put(CacheDataStoreFactory.NAMESPACE_PARAM.key, "http://www.aquamonitor.no/");		
		params.put(CacheDataStoreFactory.DBTYPE_PARAM.key, (Serializable) CacheDataStoreFactory.DBTYPE_PARAM.sample);
		params.put(CacheDataStoreFactory.BACKEND_PARAM.key, "dbtype=aquamonitor;user=Ostfold");
		params.put(CacheDataStoreFactory.INTERVAL_PARAM.key, 0);
		

        CacheDataStore store = factory.createDataStore(params);
        try {
            List<Name> names = store.getNames();            
            CacheFeatureStore source = store.getFeatureSource(names.get(0));
            int i = source.getCount(Query.ALL);
            
            Assert.assertTrue(i > 0);
    	}
    	finally {
    	    store.dispose();
    	}
	}
	
	@Test
	public void refreshShapefileCacheTest() throws Exception {
		
		File cacheFolder = new File(TestData.file(this, null), "cache_3");
        cacheFolder.mkdir();
		
		HashMap<String, Serializable> params = new HashMap<String, Serializable>();
		params.put(CacheDataStoreFactory.NAMESPACE_PARAM.key, "http://www.aquamonitor.no/");	
		params.put(CacheDataStoreFactory.DBTYPE_PARAM.key, (Serializable) CacheDataStoreFactory.DBTYPE_PARAM.sample);
		params.put(CacheDataStoreFactory.BACKEND_PARAM.key, "dbtype=aquamonitor;user=Ostfold");
		params.put(CacheDataStoreFactory.CACHE_PARAM.key, "dbtype=shapefile;url=file:" + cacheFolder.getAbsolutePath());
		params.put(CacheDataStoreFactory.INTERVAL_PARAM.key, (Serializable) CacheDataStoreFactory.INTERVAL_PARAM.sample);

		ShapefileDataStore shpDataStore = null;
		CacheDataStore store = new CacheDataStoreFactory().createDataStore(params);
        try {
    		
    		SimpleFeatureSource stationSource = store.getFeatureSource("STATION_POINTS");
    		int origCnt = stationSource.getCount(Query.ALL);
    		
    		SimpleFeatureCollection origColl = stationSource.getFeatures();
    		SimpleFeature firstFeature, secondFeature;
    		try (SimpleFeatureIterator iter = origColl.features()) {
        		firstFeature = (iter.hasNext() ? iter.next() : null);
        		secondFeature = (iter.hasNext() ? iter.next() : null);
    		}
    		
    		Assert.assertTrue(firstFeature != null && secondFeature != null);
    		
    		Filter firstFilter = CQL.toFilter("STATION_ID=" + firstFeature.getAttribute("STATION_ID") 
    		                                    + " AND PROJECT_ID=" + firstFeature.getAttribute("PROJECT_ID"));
    		String firstName = (String)firstFeature.getAttribute("STATION_NAME");
    		
    		LOGGER.info(firstFilter.toString());
    		
    		Filter secondFilter = CQL.toFilter("STATION_ID=" + secondFeature.getAttribute("STATION_ID") 
    		                                    + " AND PROJECT_ID=" + secondFeature.getAttribute("PROJECT_ID"));
    		Filter thirdFilter = CQL.toFilter("STATION_ID=1 AND PROJECT_ID=1");
    		
    		LOGGER.info(secondFilter.toString());
    	
    		shpDataStore = (ShapefileDataStore) new ShapefileDataStoreFactory().
    		        createDataStore(new URL("file:" + cacheFolder.getAbsolutePath() + 
    		                File.separator + "STATION_POINTS.shp"));
    		
    		ContentFeatureStore shpStore = (ContentFeatureStore) shpDataStore.getFeatureSource();
    		int shpCnt = shpStore.getCount(Query.ALL);
    		
    		Assert.assertEquals(origCnt, shpCnt);
    		
    		
			shpStore.modifyFeatures("STATION_NA", "TEST1", firstFilter);
			shpStore.removeFeatures(secondFilter);
			
			SimpleFeatureBuilder builder = new SimpleFeatureBuilder(shpStore.getSchema());
			builder.set("STATION_ID", 1);
			builder.set("PROJECT_ID", 1);
			builder.set("STATION_NA", "TEST2");
			
			MemoryFeatureCollection added = new MemoryFeatureCollection(shpStore.getSchema());
			added.add(builder.buildFeature("1"));
			
			shpStore.addFeatures((FeatureCollection<SimpleFeatureType,SimpleFeature>)added);

    		try (SimpleFeatureIterator firstIter = stationSource.getFeatures(firstFilter).features()) {
        		Assert.assertTrue(firstIter.hasNext());
        		Assert.assertEquals("TEST1", firstIter.next().getAttribute("STATION_NAME"));
    		}
    		
    		Assert.assertTrue(stationSource.getFeatures(secondFilter).isEmpty());
    		
    		try (SimpleFeatureIterator thirdIter = stationSource.getFeatures(thirdFilter).features()) {
        		Assert.assertTrue(thirdIter.hasNext());
        		Assert.assertEquals("TEST2", thirdIter.next().getAttribute("STATION_NAME"));
    		}
    		
    		((CacheFeatureStore)stationSource).refresh();
    		
    		try (SimpleFeatureIterator firstIter = stationSource.getFeatures(firstFilter).features()) {
        		Assert.assertTrue(firstIter.hasNext());
        		Assert.assertEquals(firstName, firstIter.next().getAttribute("STATION_NAME"));
    		}
    		Assert.assertFalse(stationSource.getFeatures(secondFilter).isEmpty());
    		Assert.assertTrue(stationSource.getFeatures(thirdFilter).isEmpty());
        }
        finally {
            if (shpDataStore != null) {
                shpDataStore.dispose();
            }
            store.dispose();
            FileUtils.deleteDirectory(cacheFolder);
        }
	}
}
