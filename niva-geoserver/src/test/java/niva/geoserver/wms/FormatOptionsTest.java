package niva.geoserver.wms;

import java.io.Serializable;
import java.util.Map;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.WMSLayerInfo;
import org.geoserver.catalog.WMSStoreInfo;
import org.geoserver.catalog.impl.LayerInfoImpl;
import org.geoserver.catalog.impl.WMSLayerInfoImpl;
import org.geoserver.catalog.impl.WMSStoreInfoImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import niva.geoserver.data.NivaTestSupport;

/**
 * Testing calls with FormatOptions as query parameter.
 */
public class FormatOptionsTest extends NivaTestSupport {
	
	@Before
	public void setupKartdataWMS() throws Exception {
		Catalog catalog = getCatalog();
		WMSStoreInfo store = new WMSStoreInfoImpl(catalog);
		store.setName("Kartdata WMS");
		store.setWorkspace(catalog.getWorkspaceByName("geonorge"));
		store.setCapabilitiesURL("https://wms.geonorge.no/skwms1/wms.kartdata?");
		store.setEnabled(true);
		catalog.add(store);
		
		WMSLayerInfo resourceInfo = new WMSLayerInfoImpl(catalog);
		resourceInfo.setName("kd_stedsnavn");
		store = catalog.getStoresByWorkspace("geonorge", WMSStoreInfo.class)
						.stream()
						.filter(s -> s.getName().equals("Kartdata WMS"))
						.findFirst()
						.get();
		
		resourceInfo.setStore(store);
		resourceInfo.setNamespace(catalog.getNamespaceByPrefix("geonorge"));
		resourceInfo.setNativeName("kd_stedsnavn");
		resourceInfo.setEnabled(true);
		catalog.add(resourceInfo);
		
		LayerInfo layerInfo = new LayerInfoImpl();
		layerInfo.setResource(resourceInfo);
		layerInfo.setName("kd_stedsnavn");
		layerInfo.setEnabled(true);
		layerInfo.setAdvertised(true);
		catalog.add(layerInfo);
	}
	
	@After
	public void cleanUp() throws Exception {
		Catalog catalog = getCatalog();
		LayerInfo layer = catalog.getLayerByName("kd_stedsnavn");
		catalog.remove(layer);
		WMSLayerInfo resource = catalog.getResourceByName("kd_stedsnavn", WMSLayerInfo.class);
		catalog.remove(resource);
		
		StoreInfo store = catalog.getWMSStoreByName("Kartdata WMS");
		catalog.remove(store);
	}
	
	@Test
	public void testFormatOptionsCalls() throws Exception {
		String url = "wms?REQUEST=GetMap&SERVICE=WMS&VERSION=1.3.0&FORMAT=image%2Fpng&STYLES=&TRANSPARENT=TRUE&LAYERS=geonorge%3Akd_stedsnavn&CRS=EPSG%3A32633&FORMAT_OPTIONS=dpi%3A135&WIDTH=2862&HEIGHT=1324&BBOX=176202.29974955012%2C6544505.893805489%2C332198.4496635176%2C6616671.827588191";
		this.getAsImage(url, "image/png");
	}

}
