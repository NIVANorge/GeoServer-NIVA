package niva.geoserver.wps;

import java.io.InputStream;
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
import org.geoserver.util.IOUtils;
import org.geotools.TestData;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import niva.geoserver.data.NivaTestSupport;

/**
 * Test a WPS process used by Kantsonekartlegging that fails.
 * No NIVA-specific code, but practical use-case for debugging GeoServer.
 */
public class NveMalestasjonFeltClipTest extends NivaTestSupport {
	
	private static final String GEOSERVER_POSTGRES_HOST = "10.233.0.5";
	private static final String GEOSERVER_POSTGRES_PORT = "5432";
	private static final String GEOSERVER_POSTGRES_DB = "nivagis";
	
	private static final String WORKSPACE = "riverq";
	@Override
	protected void onSetUp(SystemTestData testData) throws Exception {
		String dbPassword = System.getenv("GEOSERVER_POSTGRES_PASSWORD");
		Assume.assumeNotNull("Environment variable GEOSERVER_POSTGRES_PASSWORD is not set, cannot run test", dbPassword);
		
		Catalog catalog = getCatalog();
		testData.addWorkspace(WORKSPACE, "http://niva.no/riverq", catalog);
		
        Map<String, Serializable> params = new HashMap<>();
        params.put("dbtype", "postgis");
        params.put("namespace", "http://niva.no/riverq");
        params.put("host", GEOSERVER_POSTGRES_HOST);
        params.put("port", GEOSERVER_POSTGRES_PORT);
        params.put("database", GEOSERVER_POSTGRES_DB);
        params.put("schema",  "public");
        params.put("user",  "geoserver");
        params.put("passwd",  dbPassword);
		StoreInfo store = new DataStoreInfoImpl(catalog);
		store.setName("NIVAGIS");
		store.setWorkspace(catalog.getWorkspaceByName(WORKSPACE));
		store.setEnabled(true);
		store.getConnectionParameters().putAll(params);
		catalog.add(store);
		DataStoreInfo storeCat = catalog.getDataStoreByName(WORKSPACE, "NIVAGIS");
		
		FeatureTypeInfo layer = addFeatureLayer(catalog, storeCat, WORKSPACE,
				"nve_nedborfelt_malestasjon_f",
				"nve_nedborfelt_malestasjon_f", "EPSG:25833");
		StyleInfo style = catalog.getStyleByName("polygon");
		addLayer(catalog, layer, style);		
	}

	@Test
	public void clipNedborfeltMalestasjon() throws Exception {
		String testXml = IOUtils.toString(TestData.openStream(this, "NveMalestasjonClip.xml"));
		try (InputStream response = this.post("ows", testXml)) {
			String responseStr = IOUtils.toString(response);
			Assert.assertFalse("Response should not contain ProcessFailed", responseStr.contains("wps:ProcessFailed"));
		}
	}
}
