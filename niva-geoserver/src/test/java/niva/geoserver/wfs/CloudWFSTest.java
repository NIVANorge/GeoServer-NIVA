package niva.geoserver.wfs;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.impl.DataStoreInfoImpl;
import org.geotools.test.TestData;
import org.geotools.util.logging.Logging;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import niva.geoserver.data.NivaTestSupport;

/**
 * These tests are resembling a WFS store in Cloud calling a restricted layer at AquaMonitor Geoserver.
 * 
 * Before running we should set up the System Property AQUAMONITOR_TEST_USER. It should have access to the layer at AquaMonitor Geoserver.
 * 
 * @author Roar Brænden
 *
 */
public class CloudWFSTest extends NivaTestSupport {
	

	private static Logger LOGGER = Logging.getLogger(CloudWFSTest.class);
	
	private final static String TEST_WFS_URL = "https://test-aquamonitor.niva.no/geoserver/no.niva.aquamonitor/Intern_stations/wfs";
	
	private final static String TEST_USER;
	private final static String TEST_PWD;
	static {
		String usrpwd = System.getProperty("AQUAMONITOR_TEST_USER");
		if (usrpwd != null) {
			int inx = usrpwd.indexOf(':');
			if (inx == -1) {
				throw new IllegalArgumentException("Not properly configured AQUAMONITOR_TEST_USER");
			}
			TEST_USER = usrpwd.substring(0, inx);
			TEST_PWD = usrpwd.substring(inx + 1);
		} else {
			TEST_USER = null;
			TEST_PWD = null;
		}
	}
	
	@Before
	public void setupInternWFSProxy() throws Exception {
		Logging.getLogger("org.geotools.http").setLevel(Level.FINE);
		Logging.getLogger("org.geotools.data.wfs").setLevel(Level.FINE);
		Assume.assumeNotNull(TEST_USER, TEST_PWD);
		Catalog catalog = getCatalog();
		
		StoreInfo store = new DataStoreInfoImpl(catalog);
		store.setName("Intern_WFS");
		store.setWorkspace(catalog.getWorkspaceByName("no.niva.aquamonitor"));
		
		Map<String, Serializable> params = store.getConnectionParameters();
		params.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", new URL(TEST_WFS_URL));
		params.put("WFSDataStoreFactory:USERNAME", TEST_USER);
		params.put("WFSDataStoreFactory:PASSWORD", TEST_PWD);
		
		addAquaMonitorStore("Intern_WFS", params);
		FeatureTypeInfo featureInfo = addFeatureLayer(catalog.getDataStoreByName("no.niva.aquamonitor", "Intern_WFS"),
						"Intern_wfs_stations",
						"no.niva.aquamonitor_Intern_stations",
						"EPSG:4326");
		
		addLayer(featureInfo, null);
		
	}
	
	@After
	public void cleanUp() throws Exception {
		Catalog catalog = getCatalog();
		LayerInfo layer = catalog.getLayerByName("Intern_wfs_stations");
		catalog.remove(layer);
		
		FeatureTypeInfo feature = catalog.getFeatureTypeByName("Intern_wfs_stations");
		catalog.remove(feature);
		
		StoreInfo store = catalog.getStoreByName("Intern_WFS", StoreInfo.class);
		catalog.remove(store);
	}
	
	
	@Test
	public void testWFSGetFeatureFromRemoteInternCache() throws Exception {

		String path = "no.niva.aquamonitor/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=no.niva.aquamonitor%3AIntern_wfs_stations&maxFeatures=10&outputFormat=application%2Fgml%2Bxml%3B%20version%3D3.2";
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(get(path)))) {
			StringBuilder resp = new StringBuilder();
			String next = reader.readLine();
			while (next != null) {
				resp.append(next).append('\n');
				next = reader.readLine();
			}
			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.fine("Response from test geoserver: " + resp);
			}
			Assert.assertFalse("The response contained a ServiceException", resp.toString().contains("ServiceException"));
		}
	}
	
	@Test
	public void testWMS_1_3_0_GetMapFullExtentNECoordinates() throws Exception {

		String path = "no.niva.aquamonitor/ows?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap&FORMAT=image%2Fpng&TRANSPARENT=true&STYLES&LAYERS=Intern_wfs_stations&SRS=EPSG%3A4326&WIDTH=506&HEIGHT=768&BBOX=37.79296875%2C-24.08203125%2C82.265625%2C43.41796875";
		BufferedImage img = this.getAsImage(path, "image/png");
		ImageIO.write(img, "png", new File(TestData.file(this, null), "intern_stations.png"));
	}
}
