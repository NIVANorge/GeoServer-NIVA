package niva.geoserver.renderer;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.wms.DefaultWebMapService;
import org.geoserver.wms.GetLegendGraphic;
import org.geoserver.wms.GetLegendGraphicRequest;
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

	@Test
	public void testgetLegendGraphic() throws Exception {
		
		DefaultWebMapService reflector = new DefaultWebMapService(getWMS());
		GetLegendGraphic glg = new GetLegendGraphic(getWMS());
		reflector.setGetLegendGraphic(glg);
		FeatureTypeInfo pointsLayer = getWMS().getCatalog().getFeatureTypeByName("Points");
		assertNotNull(pointsLayer);
		assertNotNull(pointsLayer.getFeatureType());
		
		StyleFactory sf = new StyleFactoryImpl();
		ExternalGraphic eg = sf.createExternalGraphic("http://aquamonitor?cnt=2&cols=56ddff", "application/chart");

		FilterFactory ff = new FilterFactoryImpl();
		Graphic graph = sf.createGraphic(new ExternalGraphic[] {eg}, null, null, null, ff.literal(40), null);

		
		PointSymbolizer symb = sf.createPointSymbolizer();
		symb.setGraphic(graph);
		
		Rule rule = sf.createRule();
		rule.symbolizers().add(symb);
		rule.setLegend(graph);
		
		FeatureTypeStyle fts = sf.createFeatureTypeStyle();
		fts.rules().add(rule);

		Style symbolStyle = sf.createStyle();
		symbolStyle.featureTypeStyles().add(fts);
								
		GetLegendGraphicRequest request = new GetLegendGraphicRequest();
		request.setFormat("image/png");
		request.setLayer(pointsLayer.getFeatureType());
		request.setStyle(symbolStyle);
		request.setWidth(50);
		request.setHeight(50);
		
		
		Object graphic = reflector.getLegendGraphic(request);
		
		assertNotNull(graphic);
		assertTrue(graphic instanceof BufferedImageLegendGraphic);

		//ImageIO.write(((BufferedImageLegendGraphic)graphic).getLegend(), "png", new File("C:\\temp\\legend.png"));
		
		testImage(((BufferedImageLegendGraphic)graphic).getLegend());
	}

	private void testImage(BufferedImage generatedImage) throws Exception {
		File testFile = TestData.file(LegendWithExternalSymbolTest.class, "legend.png");
		
		BufferedImage testImage = ImageIO.read(testFile);
		assertTrue("The two pictures doesn't have the same dimension.", generatedImage.getWidth() == testImage.getWidth() && generatedImage.getHeight() == testImage.getHeight());
		
		int width = generatedImage.getWidth();
		int height = generatedImage.getHeight();
		
		for (int y = 0; y < height; y++){
			for (int x = 0; x < width; x++){
				if (generatedImage.getRGB(x, y) != testImage.getRGB(x, y)) {
					fail("The two pictures doesn't match.");
				}
			}
		}
	}
}
