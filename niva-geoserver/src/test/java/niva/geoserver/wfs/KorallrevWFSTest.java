package niva.geoserver.wfs;

import java.util.Map;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.impl.DataStoreInfoImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import niva.geoserver.data.NivaTestSupport;

public class KorallrevWFSTest extends NivaTestSupport {
	
	private static final String WFS_URL = "https://wfs.geonorge.no/skwms1/wfs.korallrev?";
	@Before
	public void setupKorallrevWFSProxy() throws Exception {
		Catalog catalog = getCatalog();
		
		StoreInfo store = new DataStoreInfoImpl(catalog);
		store.setName("Korallrev_WFS");
		store.setWorkspace(catalog.getWorkspaceByName("no.norgedigitalt"));
		store.setEnabled(true);
		
		Map<String, Serializable> params = store.getConnectionParameters();
		params.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", WFS_URL);
		params.put("WFSDataStoreFactory:WFS_STRATEGY", "auto");
		params.put("WFSDataStoreFactory:GML_COMPLIANCE_LEVEL", 0);
		params.put("usedefaultsrs", false);
		catalog.add(store);
		
		FeatureTypeInfo featureInfo = addFeatureLayer(
				catalog.getDataStoreByName("no.norgedigitalt", "Korallrev_WFS"),
				"no.norgedigitalt",
				"korallrev",
				"app_Korallrev",
				"EPSG:4258");

		addLayer(featureInfo, null);
	}
	
	@After
	public void cleanUp() throws Exception {
		Catalog catalog = getCatalog();
		LayerInfo layer = catalog.getLayerByName("korallrev");
		catalog.remove(layer);
		
		FeatureTypeInfo feature = catalog.getFeatureTypeByName("korallrev");
		catalog.remove(feature);
		
		StoreInfo store = catalog.getStoreByName("Korallrev_WFS", StoreInfo.class);
		catalog.remove(store);
	}
	
	@Test
	public void testQGISGetFeatureCall() throws Exception {
		String path = "wfs?SERVICE=WFS&REQUEST=GetFeature&VERSION=2.0.0&TYPENAMES=no.norgedigitalt:korallrev&STARTINDEX=0&COUNT=1&SRSNAME=urn:ogc:def:crs:EPSG::4258&BBOX=-90,-180,90,180,urn:ogc:def:crs:EPSG::4258";
		//String path = "wfs?SERVICE=WFS&REQUEST=GetCapabilities&VERSION=2.0.0";
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(get(path)))) {
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}
		}
	}

}
