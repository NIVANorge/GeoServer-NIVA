package niva.geoserver.renderer;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.wms.DefaultWebMapService;
import org.geoserver.wms.GetLegendGraphic;
import org.geoserver.wms.GetLegendGraphicRequest;
import org.geoserver.wms.WMS;
import org.geoserver.wms.WMSTestSupport;
import org.geoserver.wms.legendgraphic.BufferedImageLegendGraphic;

import org.geotools.TestData;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Graphic;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyleFactoryImpl;

import org.opengis.filter.FilterFactory;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for a problem with geoServer handling of external symbols within the Legend part of a SLD.
 * @author Roar Brænden, NIVA
 *
 */
public class LegendWithExternalSymbolTest extends WMSTestSupport {
	
	private static StyleFactory styleFact = new StyleFactoryImpl();
	
	private static FilterFactory filterFact = new FilterFactoryImpl();
	

	@Test
	public void testgetLegendGraphic() throws Exception {
		final WMS wms = getWMS();
		
		final FeatureTypeInfo pointsLayer = wms.getCatalog().getFeatureTypeByName("Points");
		
		assertNotNull(pointsLayer);
		assertNotNull(pointsLayer.getFeatureType());
		
		final Graphic symbolizerGraph = styleFact.createGraphic(new ExternalGraphic[] {
																	styleFact.createExternalGraphic("http://aquamonitor?cnt=2&cols=56ddff",
																									"application/chart")
																		}, // externalGraphics
																null, // marks
																null, // symbols
																null, // opacity
																filterFact.literal(40), // size
																null); // rotation

		final PointSymbolizer symbolizer = styleFact.createPointSymbolizer();
		symbolizer.setGraphic(symbolizerGraph);
		
		Rule rule = styleFact.createRule();
		rule.symbolizers().add(symbolizer);
		rule.setLegend(symbolizerGraph);
		
		final FeatureTypeStyle fts = styleFact.createFeatureTypeStyle();
		fts.rules().add(rule);

		final Style symbolStyle = styleFact.createStyle();
		symbolStyle.featureTypeStyles().add(fts);
								
		final GetLegendGraphicRequest request = new GetLegendGraphicRequest();
		request.setFormat("image/png");
		request.setLayer(pointsLayer.getFeatureType());
		request.setStyle(symbolStyle);
		request.setWidth(50);
		request.setHeight(50);
		
		final DefaultWebMapService reflector = new DefaultWebMapService(wms);
		reflector.setGetLegendGraphic(new GetLegendGraphic(wms));
		
		final Object legendGraphic = reflector.getLegendGraphic(request);
		
		assertNotNull(legendGraphic);
		assertTrue(legendGraphic instanceof BufferedImageLegendGraphic);
		
		final File tmpLegendFile = TestData.temp(LegendWithExternalSymbolTest.class, "legend.png");

		ImageIO.write(((BufferedImageLegendGraphic)legendGraphic).getLegend(), "png", tmpLegendFile);
		
		testImage(((BufferedImageLegendGraphic)legendGraphic).getLegend());
	}

	private void testImage(BufferedImage generatedImage) throws Exception {
		final File testFile = TestData.file(LegendWithExternalSymbolTest.class, "legend.png");
		
		final int width = generatedImage.getWidth();
		final int height = generatedImage.getHeight();
		
		final BufferedImage testImage = ImageIO.read(testFile);
		assertTrue("The two pictures doesn't have the same dimension.", width == testImage.getWidth() 
																		&& height == testImage.getHeight());

		for (int y = 0; y < height; y++){
			for (int x = 0; x < width; x++){
				if (generatedImage.getRGB(x, y) != testImage.getRGB(x, y)) {
					fail("The two legends doesn't match.");
				}
			}
		}
	}
}
