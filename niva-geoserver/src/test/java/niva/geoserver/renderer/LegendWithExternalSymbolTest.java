package niva.geoserver.renderer;


import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.wms.DefaultWebMapService;
import org.geoserver.wms.GetLegendGraphic;
import org.geoserver.wms.GetLegendGraphicRequest;
import org.geoserver.wms.WMS;
import org.geoserver.wms.WMSTestSupport;
import org.geoserver.wms.legendgraphic.BufferedImageLegendGraphic;


import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Graphic;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;

import org.opengis.filter.FilterFactory;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import static niva.geoserver.renderer.ResourceImageTester.assertImage;

/**
 * Test for a problem with GeoServer handling of external symbols within the Legend part of a SLD.
 * 
 * The solution is to implement a function within org.geotools.renderer.style.SLDStyleFactory
 * 
 * createGraphicLegend()
 * 
 * That is called from org.geoserver.wms.legendgraphic.BufferedImageLegendGraphicBuilder
 * 
 * renderRules()
 * 
 * Another problem is that this class suppose that all symbols within legend should be equal sized.
 * We wan't them to be the size specified.
 * 
 * @author Roar Brænden, NIVA
 *
 */
public class LegendWithExternalSymbolTest extends WMSTestSupport {
	
	private static StyleFactory styleFact = CommonFactoryFinder.getStyleFactory();
	
	private static FilterFactory filterFact = CommonFactoryFinder.getFilterFactory();
	

	/**
	 * Represents the legends we are creating at NIVA.
	 * 
	 * @throws Exception
	 */
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
		
		final Rule rule = styleFact.createRule();
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
		request.setScale(0.0);
		
		final DefaultWebMapService reflector = new DefaultWebMapService(wms);
		reflector.setGetLegendGraphic(new GetLegendGraphic(wms));
		
		final Object legendGraphic = reflector.getLegendGraphic(request);
		
		assertNotNull(legendGraphic);
		assertTrue(legendGraphic instanceof BufferedImageLegendGraphic);
		
		final BufferedImage legendImage = ((BufferedImageLegendGraphic)legendGraphic).getLegend();
		ImageIO.write(legendImage, "png", new File("C:\\temp\\legend.png"));
        
		
		assertImage("legend.png", ((BufferedImageLegendGraphic)legendGraphic).getLegend());
	}

}
