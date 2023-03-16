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
import org.junit.Assert;
import org.junit.Test;


public class NorgeDigitaltWMSLayerTest extends NivaTestSupport {
    

	@Override
	protected void onSetUp(SystemTestData testData) throws Exception {
		super.onSetUp(testData);
		final Catalog catalog = getCatalog();
        	
		WMSStoreInfoImpl store = createWMSStore(catalog, "Toporaster",
		        "http://openwms.statkart.no/skwms1/wms.toporaster4?service=wms&version=1.3.0");
		createWMSLayer(catalog, store, "toporaster");
		
		WMSStoreInfoImpl flyfotoStore = createWMSStore(catalog, "NorgeIBilder",
		        "http://wms.geonorge.no/skwms1/wms.nib?service=WMS&request=GetCapabilities&version=1.1.1");
		createWMSLayer(catalog, flyfotoStore, "ortofoto");
		
	}
	
	private WMSStoreInfoImpl createWMSStore(Catalog catalog, String name, String url) {
        WMSStoreInfoImpl store = new WMSStoreInfoImpl(catalog);
        store.setName(name);
        store.setWorkspace(catalog.getWorkspaceByName("no.norgedigitalt"));
        store.setCapabilitiesURL(url);
        store.setMaxConnections(6);
        store.setReadTimeout(120);
        store.setConnectTimeout(60);
        store.getMetadata().put("useConnectionPooling", true);
        store.setEnabled(true);
        catalog.add(store);
        return store;
	}
	
	private void createWMSLayer(Catalog catalog, WMSStoreInfoImpl wmsStore, String name) {
	    WMSLayerInfoImpl resourceCat = new WMSLayerInfoImpl(catalog);
	    final NamespaceInfo namespace = catalog.getNamespaceByPrefix("no.norgedigitalt");
        
        resourceCat.setStore(wmsStore);
        resourceCat.setNamespace(namespace);
        resourceCat.setName(name);
        resourceCat.setNativeName(name);
        resourceCat.setNativeCRS(CRS.getUtm33());
        resourceCat.setSRS("EPSG:32633");
        resourceCat.setProjectionPolicy(ProjectionPolicy.FORCE_DECLARED);
        resourceCat.setEnabled(true);
        
        catalog.add(resourceCat);
        
        LayerInfo layerCat = new LayerInfoImpl();
        layerCat.setResource(resourceCat);
        layerCat.setName(name);
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
	
	@Test
	public void testOrtofotoWMS() throws Exception {
	    BufferedImage bi = this.getAsImage("wms?service=WMS&version=1.1.0&request=GetMap&layers=no.norgedigitalt:ortofoto&bbox=-1038347.839073,949803.942783,586102.169247,3371048.853752&width=515&height=768&srs=EPSG:32633&styles=&format=image%2Fpng", "image/png");
	    Assert.assertNotNull(bi);
	    
	    ImageIO.write(bi, "png", new File("C:/temp/orto.png"));
	}

}
