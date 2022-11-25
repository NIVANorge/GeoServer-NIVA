package niva.geoserver.renderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.xml.namespace.QName;
import org.geoserver.catalog.Catalog;
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


/**
 * Tests to check that the legends used within AquaMonitor is correct
 * 
 * @author Roar Brænden, NIVA
 *
 */
public class AquamonitorFullBlownLegendTest extends WMSTestSupport {

    private BufferedImageLegendGraphicBuilder legendProducer;
	
    @Override
    protected void onSetUp(SystemTestData testData) throws Exception {
        super.onSetUp(testData);
        final Catalog catalog = getCatalog();
        testData.addStyle("AquaMonitor_aggregation",
        				  "AquaMonitor_Aggragation.sld",
        				  AquamonitorFullBlownLegendTest.class,
        				  catalog);
        
        String iconsRoot = "styles/aqm_icons/";
        URL iconsDir = TestData.url(this, "aqm_icons");
        Files.walk(Paths.get(iconsDir.toURI()))
            .filter(Files::isRegularFile)
            .map(f -> f.toFile().getName())
            .forEach(n -> {
                try {
                    testData.copyTo(
                            TestData.openStream(this, "aqm_icons/" + n),
                            iconsRoot + n);
                } catch (IOException e) {
                    throw new Error("Trouble setting up this test.", e);
                }
            });
        
        final QName layerName = new QName("http://www.aquamonitor.no/",
        								  "Intern_station_datatype",
        								  "no.niva.aquamonitor"
        								);
        
        testData.addWorkspace(layerName.getPrefix(),
        				      layerName.getNamespaceURI(),
        				      catalog);
        
        
        final HashMap<LayerProperty, Object> props = new HashMap<>();
        props.put(SystemTestData.LayerProperty.STYLE, "AquaMonitor_aggregation");
        
        testData.addVectorLayer(layerName,
        		                props,
        						getClass(),
        						catalog);
    }
    
    @Before
    public void setupLegendProducer() throws Exception {
        this.legendProducer = new BufferedImageLegendGraphicBuilder();
    }
	
	@Test
	public void testLegendInternal() throws Exception {

		final String requestURL = "wms?LAYERS=no.niva.aquamonitor:Intern_station_datatype&"
								+ "FORMAT=image/png&TRANSPARENT=TRUE&SERVICE=WMS&"
								+ "VERSION=1.1.1&REQUEST=GetLegendGraphic&STYLES=&"
								+ "SRS=EPSG:32633&LAYER=no.niva.aquamonitor:Intern_station_datatype"
								+ "&legend_options=rescaleSymbols:off";
		
        final Map rawKvp = caseInsensitiveKvp(KvpUtils.parseQueryString(requestURL));
        final GetLegendGraphicKvpReader reader = new GetLegendGraphicKvpReader(getWMS());
        final BufferedImage image = legendProducer.buildLegendGraphic(reader.read(reader.createRequest(),
        																		  parseKvp(rawKvp),
        																		  rawKvp));
        
        ImageIO.write(image, "png", new File("C:\\temp\\Internal_legend.png"));
        
        ResourceImageTester.assertImage("Internal_legend.png", image);   
	}

}
