package niva.geoserver.gwc;


import static org.junit.Assert.assertNotNull;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.ProjectionPolicy;
import org.geoserver.catalog.impl.LayerInfoImpl;
import org.geoserver.catalog.impl.WMSLayerInfoImpl;
import org.geoserver.catalog.impl.WMSStoreInfoImpl;
import org.geoserver.data.test.SystemTestData;

import niva.geoserver.data.NivaTestSupport;



import niva.geotools.referencing.CRS;

import org.junit.Test;


public class NorgeDigitaltWMSLayerTest extends NivaTestSupport {
	
	@Override
	protected void onSetUp(SystemTestData testData) throws Exception {
		super.onSetUp(testData);
		Catalog catalog = getCatalog();
		
		WMSStoreInfoImpl store = new WMSStoreInfoImpl(catalog);
		store.setName("Toporaster");
		store.setWorkspace(catalog.getWorkspaceByName("no.norgedigitalt"));
		store.setCapabilitiesURL("http://openwms.statkart.no/skwms1/wms.toporaster4?service=wms&version=1.3.0");
		store.setMaxConnections(6);
		store.setReadTimeout(120);
		store.setConnectTimeout(60);
		store.getMetadata().put("useConnectionPooling", true);
		store.setEnabled(true);

		catalog.add(store);

		NamespaceInfo namespace = catalog.getNamespaceByPrefix("no.norgedigitalt");
		
		WMSLayerInfoImpl resourceCat = new WMSLayerInfoImpl(catalog);
		resourceCat.setStore(store);
		resourceCat.setNamespace(namespace);
		resourceCat.setName("toporaster");
		resourceCat.setNativeName("toporaster");
		resourceCat.setNativeCRS(CRS.getUtm33());
		resourceCat.setSRS("EPSG:32633");
		resourceCat.setProjectionPolicy(ProjectionPolicy.FORCE_DECLARED);
		resourceCat.setEnabled(true);
		
		catalog.add(resourceCat);
		
		LayerInfo layerCat = new LayerInfoImpl();
		layerCat.setResource(resourceCat);
		layerCat.setName("toporaster");
		layerCat.setEnabled(true);
		
		catalog.add(layerCat);
	}
	

	
	/**
	 * Fetches a map of Norway.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTopokartWMS() throws Exception {
		BufferedImage bi = this.getAsImage("wms?service=WMS&version=1.1.0&request=GetMap&layers=no.norgedigitalt:toporaster&styles=&bbox=-127998.0,6377920.0,1145510.0,7976800.0&width=611&height=768&srs=EPSG:32633&format=image%2Fpng", "image/png");
		assertNotNull(bi);
		
		ImageIO.write(bi, "png", new File("C:/temp/nd.png"));
		
	}

}
