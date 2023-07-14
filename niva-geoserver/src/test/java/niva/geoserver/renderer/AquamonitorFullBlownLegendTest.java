package niva.geoserver.renderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.xml.namespace.QName;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.data.test.SystemTestData;
import org.geoserver.data.test.SystemTestData.LayerProperty;
import org.geoserver.ows.util.KvpUtils;
import org.geoserver.wms.WMSTestSupport;
import org.geoserver.wms.legendgraphic.BufferedImageLegendGraphicBuilder;
import org.geoserver.wms.legendgraphic.GetLegendGraphicKvpReader;
import org.geotools.test.TestData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import niva.geoserver.data.CacheStoreTest;
import niva.geoserver.data.NivaTestSupport;

/**
 * Tests to check that the legends used within AquaMonitor is correct.
 * Based on the SLD that we're using.
 * Use niva.geotools.renderer.CreateAquaMonitorSLD in niva-geotools to generate files for test-data.
 * 
 * @author Roar Brænden, NIVA
 *
 */
public class AquamonitorFullBlownLegendTest extends WMSTestSupport {
	
	private static final String PREFIX = "no.niva.aquamonitor";
	private static final String NAMESPACE = "http://www.aquamonitor.no/";

    private BufferedImageLegendGraphicBuilder legendProducer;
	
    @SuppressWarnings("rawtypes")
    @Override
    protected void onSetUp(SystemTestData testData) throws Exception {
        super.onSetUp(testData);
        final Catalog catalog = getCatalog();
        
        testData.addWorkspace(PREFIX, NAMESPACE, catalog);
        
        final QName internLayer = new QName(NAMESPACE,
        								  "Intern_station_datatype",
        								  PREFIX
        								);

        setupAquamonitorAggregationStyle(testData, catalog);
        
        final Map<LayerProperty, Object> props = new HashMap<>();
        props.put(SystemTestData.LayerProperty.STYLE, "AquaMonitor_aggregation");
        
        testData.addVectorLayer(internLayer,
        		                props,
        						getClass(),
        						catalog);        
    }
    
    public static void setupAquamonitorAggregationStyle(SystemTestData testData, Catalog catalog) throws URISyntaxException, IOException {
    	
    	testData.addStyle("AquaMonitor_aggregation",
				  "test-data/AquaMonitor_Aggragation.sld",
				  AquamonitorFullBlownLegendTest.class,
				  catalog);

		String iconsRoot = "styles/aqm_icons/";
		URL iconsDir = TestData.url(AquamonitorFullBlownLegendTest.class, "aqm_icons");
		new File(testData.getDataDirectoryRoot(), iconsRoot).mkdir();
		Files.walk(Paths.get(iconsDir.toURI()))
		  .filter(Files::isRegularFile)
		  .map(f -> f.toFile().getName())
		  .forEach(n -> {
		      try {
		          testData.copyTo(
		                  TestData.openStream(AquamonitorFullBlownLegendTest.class, "aqm_icons/" + n),
		                  iconsRoot + n);
		      } catch (IOException e) {
		          throw new Error("Trouble setting up this test. " + e.getMessage());
		      }
		  });

    }
    
    
    @Before
    public void setupLegendProducer() throws Exception {
        this.legendProducer = new BufferedImageLegendGraphicBuilder();
    }

    @SuppressWarnings("rawtypes")
    @Test
	public void testLegendInternal() throws Exception {

		final String requestURL = "wms?LAYERS=no.niva.aquamonitor:Intern_station_datatype&"
								+ "FORMAT=image/png&TRANSPARENT=TRUE&SERVICE=WMS&"
								+ "VERSION=1.1.1&REQUEST=GetLegendGraphic&STYLES=&"
								+ "SRS=EPSG:32633&LAYER=no.niva.aquamonitor:Intern_station_datatype"
								+ "&legend_options=rescaleSymbols:off";
		
        final Map<String, Object> rawKvp = caseInsensitiveKvp(KvpUtils.parseQueryString(requestURL));
        final GetLegendGraphicKvpReader reader = new GetLegendGraphicKvpReader(getWMS());
        final BufferedImage image = legendProducer.buildLegendGraphic(reader.read(reader.createRequest(),
        																		  (Map)parseKvp(rawKvp),
        																		  (Map)rawKvp));
        
        //ImageIO.write(image, "png", new File("C:\\temp\\Internal_legend.png"));
        
        ResourceImageTester.assertImage("Internal_legend.png", image);   
	}
    
    
    /** Tests for a problem that want be fixed in GeoTools, but that we fix in NIVANorge. 
     *  https://github.com/geotools/geotools/pull/4073 */
	@Test
	public void generateMapWithTransformCachedShapefile() throws Exception {
        File shpDir = new File(new File(testData.getDataDirectoryRoot(), "data"), "vannmiljo_cache");
        Assert.assertTrue("Couldn't create temp vannmiljo_cache", shpDir.mkdirs());
        Catalog catalog = getCatalog();
		
		StoreInfo store = NivaTestSupport.addAquaMonitorStore(catalog, "VannmiljoCache",
								CacheStoreTest.getTestParametersSiteShapefile("Vannmiljo", shpDir));
		
		StyleInfo style = catalog.getStyleByName("AquaMonitor_aggregation");
		
		NivaTestSupport.addStationDatatypesLayer(catalog, store, style, "Vannmiljo_stations");
		
		BufferedImage image = this.getAsImage("wms?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap&FORMAT=image%2Fpng&TRANSPARENT=true&LAYERS=no.niva.aquamonitor%3AVannmiljo_stations&CRS=EPSG%3A32633&STYLES=&WIDTH=1800&HEIGHT=1200&BBOX=127723.21822606487%2C7090357.020734268%2C943538.0989709899%2C7634233.607897551", "image/png");
		
		Assert.assertNotNull(image);
		//ImageIO.write(image, "png", new File("C:\\temp\\Vannmiljo_map.png"));
	}
    
}
