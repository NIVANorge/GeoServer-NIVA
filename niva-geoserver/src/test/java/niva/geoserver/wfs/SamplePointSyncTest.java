package niva.geoserver.wfs;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.impl.DataStoreInfoImpl;
import org.geoserver.data.test.SystemTestData;
import org.geotools.api.data.Transaction;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.geotools.jdbc.JDBCDataStore;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import niva.geoserver.data.NivaTestSupport;

/**
 * Test for testing the synchronization of sample points that occur in the Nivadatabase.
 * Should be tested against a PostgreSQL database with a sample point table.
 * This should be a test-container, but for now it is set up to be run against the nivatest-1 database.
 * The password for the database is set by environment variable: GEOSERVER_POSTGRES_PASSWORD.
 */
public class SamplePointSyncTest extends NivaTestSupport {
	
	private static final String GEOSERVER_POSTGRES_HOST = "10.233.0.5";
	private static final String GEOSERVER_POSTGRES_PORT = "5432";
	private static final String GEOSERVER_POSTGRES_DB = "nivagis";
	
	
	@Override
	protected void onSetUp(SystemTestData testData) throws Exception {
		String dbPassword = System.getenv("GEOSERVER_POSTGRES_PASSWORD");
		Assume.assumeNotNull("Environment variable GEOSERVER_POSTGRES_PASSWORD is not set, cannot run test", dbPassword);
		
		Catalog catalog = getCatalog();
		testData.addWorkspace("aquamonitor", "http://www.aquamonitor.no/", catalog);
		
        Map<String, Serializable> params = new HashMap<>();
        params.put("dbtype", "postgis");
        params.put("namespace", "http://www.aquamonitor.no/");
        params.put("host", GEOSERVER_POSTGRES_HOST);
        params.put("port", GEOSERVER_POSTGRES_PORT);
        params.put("database", GEOSERVER_POSTGRES_DB);
        params.put("schema",  "public");
        params.put("user",  "geoserver");
        params.put("passwd",  dbPassword);
		StoreInfo store = new DataStoreInfoImpl(catalog);
		store.setName("NIVAGIS");
		store.setWorkspace(catalog.getWorkspaceByName("aquamonitor"));
		store.setEnabled(true);
		store.getConnectionParameters().putAll(params);
		catalog.add(store);
		DataStoreInfo storeCat = catalog.getDataStoreByName("aquamonitor", "NIVAGIS");
		
		FeatureTypeInfo layer = addFeatureLayer(catalog, storeCat, "aquamonitor", "sample_points", "sample_points", "EPSG:4326");
		StyleInfo style = catalog.getStyleByName("point");
		addLayer(catalog, layer, style);
		
		JDBCDataStore dbStore = new PostgisNGDataStoreFactory().createDataStore(params);
		try {
			dbStore.getConnection(Transaction.AUTO_COMMIT).prepareStatement("DELETE FROM sample_points WHERE sample_point_id = 201").execute();
		} finally {
			dbStore.dispose();
		}	
		
	}
	
	@Test
	public void testAquaMonitorSamplePointEdit() throws Exception {

		String insertXml = "<wfs:Transaction service=\"WFS\" version=\"2.0.0\" " +
				"xmlns:wfs=\"http://www.opengis.net/wfs/2.0\" xmlns:gml=\"http://www.opengis.net/gml/3.2\" " +
				"xmlns:aquamonitor=\"http://www.aquamonitor.no/\">" +
				"<wfs:Insert><aquamonitor:sample_points>" + 
				"<aquamonitor:sample_point_id>201</aquamonitor:sample_point_id>" + 
				"<aquamonitor:the_geom><gml:Point srsName=\"EPSG:4326\">" + 
				"<gml:pos>11.089 62.678</gml:pos></gml:Point>" + 
				"</aquamonitor:the_geom></aquamonitor:sample_points></wfs:Insert></wfs:Transaction>";
		
		try (InputStream response = post("wfs", insertXml)) {
			String responseStr = new BufferedReader(new InputStreamReader(response)).lines()
					.reduce("", (acc, line) -> acc + line);
			Assert.assertTrue("Insert ended with error:\n" + responseStr, responseStr.contains("<wfs:totalInserted>1</wfs:totalInserted>"));
		}
		
		String moveXml = "<wfs:Transaction service=\"WFS\" version=\"2.0.0\" xmlns:wfs=\"http://www.opengis.net/wfs/2.0\"" +
				" xmlns:fes=\"http://www.opengis.net/fes/2.0\" xmlns:gml=\"http://www.opengis.net/gml/3.2\"" +
				" xmlns:aquamonitor=\"http://www.aquamonitor.no/\"><wfs:Update typeName=\"aquamonitor:sample_points\">" +
				"<wfs:Property><wfs:ValueReference>the_geom</wfs:ValueReference><wfs:Value><gml:Point srsName=\"EPSG:4326\">" +
				"<gml:pos>11.987 63.678</gml:pos></gml:Point></wfs:Value></wfs:Property><fes:Filter><fes:PropertyIsEqualTo>" +
				"<fes:ValueReference>sample_point_id</fes:ValueReference><fes:Literal>201</fes:Literal></fes:PropertyIsEqualTo>" +
				"</fes:Filter></wfs:Update></wfs:Transaction>";
		
		try (InputStream response = post("wfs", moveXml)) {
			String responseStr = new BufferedReader(new InputStreamReader(response)).lines()
					.reduce("", (acc, line) -> acc + line);
			Assert.assertTrue("Update ended with error:\n" + responseStr, responseStr.contains("<wfs:totalUpdated>1</wfs:totalUpdated>"));
		}	
	}
}
