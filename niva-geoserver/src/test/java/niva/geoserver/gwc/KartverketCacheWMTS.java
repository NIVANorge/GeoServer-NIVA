package niva.geoserver.gwc;

import static org.junit.Assert.assertNotNull;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import javax.imageio.ImageIO;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.ProjectionPolicy;
import org.geoserver.catalog.impl.LayerInfoImpl;
import org.geoserver.catalog.impl.WMTSLayerInfoImpl;
import org.geoserver.catalog.impl.WMTSStoreInfoImpl;
import org.geoserver.data.test.SystemTestData;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.ows.wms.CRSEnvelope;
import org.geotools.ows.wmts.WebMapTileServer;
import org.geotools.ows.wmts.model.WMTSLayer;
import org.junit.Assert;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import niva.geoserver.data.NivaTestSupport;
import niva.geotools.referencing.CRS;

/**
 * Kartverket cache tjeneste brukt i Aquamonitor
 * 
 * @author Roar Brænden, NIVA
 *
 */
public class KartverketCacheWMTS extends NivaTestSupport {
    
    private static final String CAPABILITIES_URL = "https://opencache.statkart.no/gatekeeper/gk/gk.open_wmts?Version=1.0.0&service=wmts&request=getcapabilities";
    
    private static final ReferencedEnvelope UTM33_BOUNDINGBOX = new ReferencedEnvelope(0, 84.01, -11.57, 41.57, null);
    @Override
    protected void onSetUp(SystemTestData testData) throws Exception {
        super.onSetUp(testData);
        
        final Catalog catalog = getCatalog();
        WMTSStoreInfoImpl store = new WMTSStoreInfoImpl(catalog);
        store.setName("KartverketCache");
        store.setWorkspace(catalog.getWorkspaceByName("no.norgedigitalt"));
        store.setCapabilitiesURL(CAPABILITIES_URL);
        store.setMaxConnections(6);
        store.setReadTimeout(120);
        store.setConnectTimeout(60);
        store.getMetadata().put("useConnectionPooling", true);
        store.setEnabled(true);
        
        catalog.add(store);
        
        NamespaceInfo namespace = catalog.getNamespaceByPrefix("no.norgedigitalt");
        WMTSLayerInfoImpl resourceCat = new WMTSLayerInfoImpl(catalog);
        resourceCat.setStore(store);
        resourceCat.setNamespace(namespace);
        resourceCat.setName("europa");
        resourceCat.setNativeName("egk");
        resourceCat.setNativeCRS(CRS.getUtm33());
        resourceCat.setSRS("EPSG:32633");
        resourceCat.setProjectionPolicy(ProjectionPolicy.FORCE_DECLARED);
        resourceCat.setLatLonBoundingBox(UTM33_BOUNDINGBOX);
        resourceCat.setEnabled(true);
        
        catalog.add(resourceCat);
        
        LayerInfo layerCat = new LayerInfoImpl();
        layerCat.setResource(resourceCat);
        layerCat.setName("europa");
        layerCat.setEnabled(true);
        
        catalog.add(layerCat);
    }

    @Test
    public void testConfiguration() throws Exception {
        
        final String layerName = "egk";
        
        final WebMapTileServer tileServer = new WebMapTileServer(new URL(CAPABILITIES_URL));
        final WMTSLayer layer = tileServer.getCapabilities().getLayer(layerName);
        layer.setLatLonBoundingBox(new CRSEnvelope(UTM33_BOUNDINGBOX));
        Assert.assertNotNull("Missing europa from capabilities", layer);
        
        CRSEnvelope envelope33 = layer.getBoundingBoxes().get("EPSG:32633");
        Assert.assertNotNull("Missing boundingBox for utm 33", envelope33);
    }
    
    /**
     * Fetches a map of Norway.
     * 
     * @throws Exception
     */
    @Test
    public void testEuropaWMS() throws Exception {
        /* Something is missing from the initialisation of this layer.
         * 
         */ 
        BufferedImage bi = this.getAsImage("wms?service=WMS&version=1.1.0&request=GetMap&layers=no.norgedigitalt:europa&styles=&bbox=-127998.0,6377920.0,1145510.0,7976800.0&width=611&height=768&srs=EPSG:32633&format=image%2Fpng", "image/png");
        assertNotNull(bi);
        
        ImageIO.write(bi, "png", new File("C:/temp/europa_1.png"));

    }
}
