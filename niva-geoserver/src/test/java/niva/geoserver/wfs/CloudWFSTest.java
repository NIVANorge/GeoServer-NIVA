package niva.geoserver.wfs;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.Map;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.impl.DataStoreInfoImpl;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import niva.geoserver.data.NivaTestSupport;

/**
 * Proxying an internal WFS in the cloud have some problem's when there are access-restrictions on the DescribeLayer call.
 * 
 * @author Roar Brænden
 *
 */
public class CloudWFSTest extends NivaTestSupport {
	

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
	
	
	@Test
	public void testGetFeatureWithExtent() throws Exception {
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
		addFeatureLayer(catalog.getDataStoreByName("no.niva.aquamonitor", "Intern_WFS"),
						"Intern_wfs_stations",
						"no.niva.aquamonitor_Intern_stations",
						"EPSG:4326");
		
		String path = "no.niva.aquamonitor/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=no.niva.aquamonitor%3AIntern_wfs_stations&maxFeatures=10&outputFormat=application%2Fgml%2Bxml%3B%20version%3D3.2";
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(get(path)))) {
			StringBuilder resp = new StringBuilder();
			String next = reader.readLine();
			while (next != null) {
				resp.append(next).append('\n');
				next = reader.readLine();
			}
			Assert.assertFalse("The response contained a ServiceException", resp.toString().contains("ServiceException"));
		}
	}
}
