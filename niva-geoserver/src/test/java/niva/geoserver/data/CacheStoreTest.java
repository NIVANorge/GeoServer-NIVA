package niva.geoserver.data;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerInfo;
import org.geotools.data.DataAccess;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.FileDataStore;
import org.geotools.data.Query;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.util.logging.Logging;
import org.junit.Assert;
import org.junit.Test;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import niva.geotools.data.CacheDataStore;
import niva.geotools.data.CacheFeatureStore;



public class CacheStoreTest extends NivaTestSupport {
    
    private static Logger LOGGER = Logging.getLogger(CacheStoreTest.class);
	
	public static Map<String, Serializable> getTestParametersProjectShapefile(String user, File shpDir) throws Exception {
		Map<String, Serializable> params = getTestParametersShapefile(shpDir);
		params.put("backend", "dbtype=aquamonitor;user=" + user);
		return params;
	}
	
	public static Map<String, Serializable> getTestParametersSiteShapefile(String site, File shpDir) throws Exception {
		Map<String, Serializable> params = getTestParametersShapefile(shpDir);
		params.put("backend", "dbtype=aquamonitor-site;host=https://test-aquamonitor.niva.no/;site=" + site);
		return params;
	}
	
	private static Map<String, Serializable> getTestParametersShapefile(File shpDir) throws Exception {
		Map<String, Serializable> params = new HashMap<>();
		params.put("dbtype", "cache");
		params.put("namespace", "http://www.aquamonitor.no/");
		params.put("cache", "dbtype=shapefile;url=file:" + shpDir.getAbsolutePath());
		params.put("update", 0);
		
		return params;
	}
	
   private Map<String, Serializable> getTestParametersPostgis() throws Exception {
        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put("dbtype", "cache");
        params.put("namespace", "http://www.aquamonitor.no/");
        params.put("backend", "dbtype=aquamonitor;user=Mjøsa");
        params.put("cache", "dbtype=postgis;host=etna.niva.no;port=5432;database=temp_1;schema=public;user=nivakart;passwd=316miljo");
        params.put("cache-name", "MJOSA_%s");
        params.put("update", 0);
        
        return params;
    }

	
	@Test
	public void testCreateShapefileCacheStore() throws Exception {
		Catalog catalog = getCatalog();
		
        File shpDir = new File(new File(testData.getDataDirectoryRoot(), "data"), "cache");
        shpDir.mkdir();
       
		addAquaMonitorStore("ShapeCache", getTestParametersProjectShapefile("Mjøsa", shpDir));
		
		DataStoreInfo storeCat = catalog.getDataStoreByName("no.niva.aquamonitor", "ShapeCache");
		
		Assert.assertNotNull(storeCat);

		DataAccess<? extends FeatureType, ? extends Feature> dataStoreObj = storeCat.getDataStore(null);
		
		Assert.assertNotNull(dataStoreObj);
		Assert.assertTrue(dataStoreObj instanceof CacheDataStore);
		
		CacheDataStore cache = (CacheDataStore)dataStoreObj;
		FileDataStore shpStore = null;
		try {
	        int cnt = addStationPointsLayer(catalog, cache, storeCat, "Cache_points_shape").getFeatures().size();
	         
			ShapefileDataStoreFactory shpFact = new ShapefileDataStoreFactory();
			shpStore = shpFact.createDataStore(new URL("file:" + new File(shpDir, "STATION_POINTS.shp").getAbsolutePath()));

			Assert.assertTrue(cnt == shpStore.getFeatureSource().getCount(Query.ALL));
		}
		finally {
		    cache.dispose();
		    if (shpStore != null) {
		        shpStore.dispose();
		    }
		}
	}
	

    @Test
    public void testCreatePostgisCacheStore() throws Exception {
        Catalog catalog = getCatalog();
        addAquaMonitorStore("PostgisCache", getTestParametersPostgis());
        
        DataStoreInfo storeCat = catalog.getDataStoreByName("no.niva.aquamonitor", "PostgisCache");
        Assert.assertNotNull(storeCat);
        DataAccess<? extends FeatureType, ? extends Feature> dataStoreObj = storeCat.getDataStore(null);
        Assert.assertNotNull(dataStoreObj);
        Assert.assertTrue(dataStoreObj instanceof CacheDataStore);
        
        CacheDataStore cache = (CacheDataStore)dataStoreObj;
        try {
            addStationPointsLayer(catalog, cache, storeCat, "Cache_points_postgis");
            CacheFeatureStore featureStore = cache.getFeatureSource("STATION_POINTS");
            try {
                featureStore.generate();
            }
            catch (IOException e) {
                if (e.getMessage().contains("Calling generate on a store that is cached")) {
                    featureStore.refresh();
                }
                else {
                    throw e;
                }
            }
        }
        finally {
            cache.dispose();    
        }
    }
    
    private FeatureSource<? extends FeatureType, ? extends Feature> addStationPointsLayer(Catalog catalog, CacheDataStore cache, DataStoreInfo storeCat, String cacheName) throws Exception {
        String[] names = cache.getTypeNames();
        Assert.assertEquals("STATION_POINTS", names[0]);
        
        addStationLayer(storeCat, cacheName);
        
        LayerInfo layerCat = catalog.getLayerByName(cacheName);
        Assert.assertNotNull(layerCat);
        
        FeatureTypeInfo featureInfo = (FeatureTypeInfo)layerCat.getResource();
        FeatureSource<? extends FeatureType, ? extends Feature> source = featureInfo.getFeatureSource(null, null);
        Assert.assertNotNull(source);
        
        return source;
    }
	
	/**
	 * When an update is issued, we requery backend for the features specified by the request.
	 * @throws Exception
	 */
	@Test
	public void testInsertCache() throws Exception {
		Catalog catalog = getCatalog();
		
		File dataDir = new File(testData.getDataDirectoryRoot(), "data");
        File shpDir = new File(dataDir, "cache2");
        shpDir.mkdir();
        
		addAquaMonitorStore("ShapeCache2", getTestParametersProjectShapefile("Mjøsa", shpDir));
		DataStoreInfo storeCat = catalog.getDataStoreByName("no.niva.aquamonitor", "ShapeCache2");
		
		addStationLayer(storeCat, "Cache_points_2");
		
		FeatureTypeInfo layerCat = catalog.getFeatureTypeByName("http://www.aquamonitor.no/", "Cache_points_2");
		
		SimpleFeatureStore cacheSource = (SimpleFeatureStore)layerCat.getFeatureSource(null, null);
		try (SimpleFeatureIterator iter = cacheSource.getFeatures().features()) {
		    Assert.assertTrue(iter.hasNext());
    		String stid = iter.next().getAttribute("STATION_ID").toString();
    		Filter stFilt = CQL.toFilter("STATION_ID=" + stid);
            
            ShapefileDataStoreFactory shpFact = new ShapefileDataStoreFactory();
            FileDataStore shpStore = shpFact.createDataStore(new URL("file:" + new File(shpDir, "STATION_POINTS.shp").getAbsolutePath()));
            
            DefaultTransaction transaction = new DefaultTransaction();
            try (FeatureWriter<SimpleFeatureType, SimpleFeature> writer = shpStore.getFeatureWriter( stFilt, transaction)) {
                
                while(writer.hasNext()) {
                    writer.next();              
                    writer.remove();
                }
                transaction.commit();
            }
            catch (Exception e) {
                transaction.rollback();
                throw e;
            }
            finally {
                transaction.close();
            }
            
            SimpleFeatureCollection coll2 = cacheSource.getFeatures(stFilt);
            Assert.assertTrue(coll2.size() == 0);
            
            String xml =  "<wfs:Transaction service=\"WFS\" version=\"1.0.0\""
                    + " xmlns:ogc=\"http://www.opengis.net/ogc\""
                    + " xmlns:wfs=\"http://www.opengis.net/wfs\""
                    + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                    + " xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.0.0/WFS-transaction.xsd\">"
                    + "<wfs:Update typeName=\"no.niva.aquamonitor:Cache_points_2\">"
                    + "<wfs:Property><wfs:Name>STATION_ID</wfs:Name><wfs:Value>" + stid + "</wfs:Value></wfs:Property>"
                    + "<ogc:Filter>"
                    + "<ogc:PropertyIsEqualTo>"
                    + "<ogc:PropertyName>no.niva.aquamonitor:STATION_ID</ogc:PropertyName>"
                    + "<ogc:Literal>" + stid + "</ogc:Literal>"
                    + "</ogc:PropertyIsEqualTo>"
                    + "</ogc:Filter>"
                    + "</wfs:Update>"
                    + "</wfs:Transaction>";
            
            
            String resp = "";
            try (InputStream is = this.post("wfs", xml);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String next = reader.readLine();
                while (next != null) {
                    resp += next;
                    next = reader.readLine();
                }
            }

            Assert.assertTrue(resp.contains("SUCCESS"));
            Assert.assertTrue(resp.contains("<wfs:InsertResult><ogc:FeatureId fid=\"none\"/></wfs:InsertResult>"));
            Assert.assertTrue(cacheSource.getFeatures(stFilt).size() > 0);
		}
	}
	
	@Test
	public void testDeleteCache() throws Exception {
		Catalog catalog = getCatalog();
		
		File dataDir = new File(testData.getDataDirectoryRoot(), "data");
        File shpDir = new File(dataDir, "cache3");
        shpDir.mkdir();
        
		addAquaMonitorStore("ShapeCache3", getTestParametersProjectShapefile("Mjøsa", shpDir));
		DataStoreInfo storeCat = catalog.getDataStoreByName("no.niva.aquamonitor", "ShapeCache3");
		addStationLayer(storeCat, "Cache_points_3");
		FeatureTypeInfo layerCat = catalog.getFeatureTypeByName("http://www.aquamonitor.no/", "Cache_points_3");
		SimpleFeatureStore cacheSource = (SimpleFeatureStore)layerCat.getFeatureSource(null, null);

		String stid;
		Filter stFilt;
		try (SimpleFeatureIterator iter = cacheSource.getFeatures().features()) {
		    Assert.assertTrue(iter.hasNext());
    		stid = iter.next().getAttribute("STATION_ID").toString();
    		
    	    stFilt = CQL.toFilter("STATION_ID=" + stid);
		}
		LOGGER.info("Using station id = " + stid + " as target.");
		
        String xml =  "<wfs:Transaction service=\"WFS\" version=\"1.0.0\""
                + " xmlns:ogc=\"http://www.opengis.net/ogc\""
                + " xmlns:no.niva.aquamonitor=\"http://www.aquamonitor.no/\""
                + " xmlns:gml=\"http://www.opengis.net/gml\""
                + " xmlns:wfs=\"http://www.opengis.net/wfs\""
                + " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                + " xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.0.0/WFS-transaction.xsd\">"
                + "<wfs:Delete typeName=\"no.niva.aquamonitor:Cache_points_3\">"
                + "<ogc:Filter>"
                + "<ogc:PropertyIsEqualTo>"
                + "<ogc:PropertyName>no.niva.aquamonitor:STATION_ID</ogc:PropertyName>"
                + "<ogc:Literal>" + stid + "</ogc:Literal>"
                + "</ogc:PropertyIsEqualTo>"
                + "</ogc:Filter>"
                + "</wfs:Delete>"
                + "</wfs:Transaction>";
	        
        try (InputStream is = this.post("wfs", xml);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            StringBuilder builder = new StringBuilder();
            String next = reader.readLine();
            while (next != null) {
                builder.append(next);
                next = reader.readLine();
            }
            String resp = builder.toString();
            LOGGER.info(resp);
            Assert.assertTrue(resp.contains("SUCCESS"));
        }

        SimpleFeatureCollection coll3 = cacheSource.getFeatures(stFilt);
        Assert.assertEquals(0, coll3.size());
	}
	

}
