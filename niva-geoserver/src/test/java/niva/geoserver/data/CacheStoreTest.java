package niva.geoserver.data;


import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

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

import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;

import niva.geotools.data.CacheDataStore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class CacheStoreTest extends NivaTestSupport {
	
	private Map<String, Serializable> getTestParameters(File shpDir) throws Exception {
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("dbtype", "cache");
		params.put("namespace", "http://www.aquamonitor.no/");
		params.put("backend", "dbtype=aquamonitor;user=Mjøsa");
		params.put("cache", "dbtype=shapefile;url=file:" + shpDir.getAbsolutePath());
		params.put("update", 0);
		
		return params;
	}
	

	
	@Test
	public void testCreateCacheStore() throws Exception {
		Catalog catalog = getCatalog();
		
        File shpDir = new File(new File(testData.getDataDirectoryRoot(), "data"), "cache");
        shpDir.mkdir();
       
		addAquaMonitorStore("ShapeCache", getTestParameters(shpDir));
		
		DataStoreInfo storeCat = catalog.getDataStoreByName("no.niva.aquamonitor", "ShapeCache");
		
		assertNotNull(storeCat);

		DataAccess<? extends FeatureType, ? extends Feature> dataStoreObj = storeCat.getDataStore(null);
		
		assertNotNull(dataStoreObj);
		
		if (dataStoreObj instanceof CacheDataStore) {
			CacheDataStore cache = (CacheDataStore)dataStoreObj;
			try {
				String[] names = cache.getTypeNames();
				assertEquals("STATION_POINTS", names[0]);
				
				addStationLayer(storeCat, "Cache_points");
				
				LayerInfo layerCat = catalog.getLayerByName("Cache_points");
				assertNotNull(layerCat);
				
				FeatureTypeInfo featureInfo = (FeatureTypeInfo)layerCat.getResource();
				FeatureSource<? extends FeatureType, ? extends Feature> source = featureInfo.getFeatureSource(null, null);
				
				int cnt = source.getFeatures().size();
				assertNotNull(source);
				
				ShapefileDataStoreFactory shpFact = new ShapefileDataStoreFactory();
				FileDataStore shpStore = shpFact.createDataStore(new URL("file:" + new File(shpDir, "STATION_POINTS.shp").getAbsolutePath()));
				
				assertTrue(cnt == shpStore.getFeatureSource().getCount(Query.ALL));
			}
			finally {
			    cache.dispose();	
			}
		}
		else {
			fail("Ikke CacheDataStore");
		}
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
        
		addAquaMonitorStore("ShapeCache2", getTestParameters(shpDir));
		DataStoreInfo storeCat = catalog.getDataStoreByName("no.niva.aquamonitor", "ShapeCache2");
		
		addStationLayer(storeCat, "Cache_points_2");
		
		FeatureTypeInfo layerCat = catalog.getFeatureTypeByName("http://www.aquamonitor.no/", "Cache_points_2");
		
		SimpleFeatureStore cacheSource = (SimpleFeatureStore)layerCat.getFeatureSource(null, null);
		SimpleFeatureCollection coll = cacheSource.getFeatures();
		SimpleFeatureIterator iter = coll.features();
		
		assertTrue(iter.hasNext());
		String stid = iter.next().getAttribute("STATION_ID").toString();
		iter.close();
		
		Filter stFilt = CQL.toFilter("STATION_ID=" + stid);
		
		
		ShapefileDataStoreFactory shpFact = new ShapefileDataStoreFactory();
		FileDataStore shpStore = shpFact.createDataStore(new URL("file:" + new File(shpDir, "STATION_POINTS.shp").getAbsolutePath()));
		
		DefaultTransaction transaction = new DefaultTransaction();
		try
		{
			FeatureWriter<SimpleFeatureType, SimpleFeature> writer = shpStore.getFeatureWriter( stFilt, transaction);
			while(writer.hasNext()) {
				writer.next();				
				writer.remove();
			}
			writer.close();
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
		assertTrue(coll2.size() == 0);
		
		String xml;
		
		xml =  "<wfs:Transaction service=\"WFS\" version=\"1.0.0\""
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
		
		InputStream is = this.post("wfs", xml);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		
		String resp = "";
		String next = reader.readLine();
		while (next != null) {
			resp += next;
			next = reader.readLine();
		}
		is.close();
		
		System.out.println(resp);
		
		assertTrue(resp.contains("SUCCESS"));
		
		assertTrue(resp.contains("<wfs:InsertResult><ogc:FeatureId fid=\"none\"/></wfs:InsertResult>"));
		
		SimpleFeatureCollection coll3 = cacheSource.getFeatures(stFilt);
		assertTrue(coll3.size() > 0);
	}
	
	@Test
	public void testDeleteCache() throws Exception {
		Catalog catalog = getCatalog();
		
		File dataDir = new File(testData.getDataDirectoryRoot(), "data");
        File shpDir = new File(dataDir, "cache3");
        shpDir.mkdir();
        
		addAquaMonitorStore("ShapeCache3", getTestParameters(shpDir));
		DataStoreInfo storeCat = catalog.getDataStoreByName("no.niva.aquamonitor", "ShapeCache3");
		
		addStationLayer(storeCat, "Cache_points_3");
		
		FeatureTypeInfo layerCat = catalog.getFeatureTypeByName("http://www.aquamonitor.no/", "Cache_points_3");
		
		SimpleFeatureStore cacheSource = (SimpleFeatureStore)layerCat.getFeatureSource(null, null);

		SimpleFeatureCollection coll = cacheSource.getFeatures();
		SimpleFeatureIterator iter = coll.features();
		
		assertTrue(iter.hasNext());
		String stid = iter.next().getAttribute("STATION_ID").toString();
		iter.close();
		
		Filter stFilt = CQL.toFilter("STATION_ID=" + stid);
		
		String xml;
		
		xml =  "<wfs:Transaction service=\"WFS\" version=\"1.0.0\""
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
		
		InputStream is = this.post("wfs", xml);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		
		String resp = "";
		String next = reader.readLine();
		while (next != null) {
			resp += next;
			next = reader.readLine();
		}
		is.close();
		
		
		System.out.println(resp);
		
		assertTrue(resp.contains("SUCCESS"));
		
		ShapefileDataStoreFactory shpFact = new ShapefileDataStoreFactory();
		FileDataStore shpStore = shpFact.createDataStore(new URL("file:" + new File(shpDir, "STATION_POINTS.shp").getAbsolutePath()));
		
	
		SimpleFeatureCollection coll2 = shpStore.getFeatureSource().getFeatures(stFilt);
		assertTrue(coll2.size() == 0);

		
		SimpleFeatureCollection coll3 = cacheSource.getFeatures(stFilt);
		assertTrue(coll3.size() == 0);
	}
}
