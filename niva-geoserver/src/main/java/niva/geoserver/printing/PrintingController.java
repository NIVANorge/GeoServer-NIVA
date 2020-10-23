package niva.geoserver.printing;


import java.io.IOException;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.geoserver.catalog.LayerInfo;
import org.geoserver.config.util.XStreamPersister;
import org.geoserver.platform.Operation;
import org.geoserver.rest.RestBaseController;
import org.geoserver.rest.converters.XStreamMessageConverter;
import org.geoserver.wms.GetMapRequest;
import org.geoserver.wms.MapLayerInfo;
import org.geoserver.wms.WMS;
import org.geoserver.wms.WMSMapContent;
import org.geoserver.wms.map.PNGMapResponse;
import org.geoserver.wms.map.RenderedImageMap;
import org.geoserver.wms.map.RenderedImageMapOutputFormat;

import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.referencing.CRS;
import org.geotools.util.logging.Logging;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.thoughtworks.xstream.XStream;

import niva.geotools.osm.OSMGridLayer;

/**
 * New approach for making static images server side.
 * Eliminating the original overhead with mapfish-print
 * 
 * @author Roar Brænden, NIVA
 *
 */
@RestController
@ControllerAdvice
public class PrintingController extends RestBaseController {
	
	public static final String PRINTING_ROOT_PATH = "/rest/printing";
	
	
	private static final Logger LOGGER = Logging.getLogger(PrintingController.class);
	
	private WMS wms;
	
	@Autowired
	public PrintingController(@Qualifier("wms") WMS wms) {
		this.wms = wms;
	}

	@PostMapping(path = PrintingController.PRINTING_ROOT_PATH, consumes = MediaType.APPLICATION_JSON)
	public void postSpec(@RequestBody PrintSpecification spec, HttpServletResponse response) throws IOException {
		
		CoordinateReferenceSystem crs;
		try {
			crs = CRS.decode(spec.srs);
		} catch (FactoryException ex) {
			throw new IllegalArgumentException("Uknown srs:" + spec.srs);
		}
		final WMSMapContent mapContent = new WMSMapContent(new GetMapRequest());
		mapContent.getViewport().setBounds(new ReferencedEnvelope(spec.bbox[0], spec.bbox[2], spec.bbox[1], spec.bbox[3], crs));
		mapContent.setMapHeight(spec.height);
		mapContent.setMapWidth(spec.width);
		mapContent.getRequest().setFormat("image/png");
		
		for(LayerSpecification layerSpec: spec.layers) {
			Layer layer;
			switch (layerSpec.type) {
				case "WMS" : {
					final LayerInfo i = this.wms.getLayerByName(layerSpec.layer);
					if (i == null) {
						throw new IllegalArgumentException("Unknown layer:" + layerSpec.layer);
					}
					final MapLayerInfo info = new MapLayerInfo(i);
					layer = new FeatureLayer(info.getFeatureSource(true, crs), info.getDefaultStyle());
					break;
				}
				case "OSM" : {

			        final GeneralEnvelope envelope = GeneralEnvelope.toGeneralEnvelope(mapContent.getRenderingArea());
			        envelope.setCoordinateReferenceSystem(mapContent.getCoordinateReferenceSystem());
			        
					layer = new OSMGridLayer(envelope);
					break;
				}
				default:{
					throw new IllegalArgumentException("Unknown type:" + layerSpec.type);
				}
			}
			mapContent.addLayer(layer);
		}
		LOGGER.fine("Ready to produce map.");
		final RenderedImageMapOutputFormat mapProducer = new RenderedImageMapOutputFormat(this.wms);
		final RenderedImageMap map = mapProducer.produceMap(mapContent);
		
		LOGGER.fine("Map produced - now sending response");
		Operation operation = null;
		PNGMapResponse mapResponse = new PNGMapResponse(this.wms);
		mapResponse.write(map, response.getOutputStream(), operation);
	}
	
	@Override
	public boolean supports(MethodParameter method, Type typ, Class<? extends HttpMessageConverter<?>> converter) {
		return typ.getTypeName().equals(PrintSpecification.class.getTypeName());
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void configurePersister(XStreamPersister persister, XStreamMessageConverter converter) {
		XStream xs = persister.getXStream();
		xs.allowTypes(new Class[] {PrintSpecification.class, LayerSpecification.class});
		xs.processAnnotations(new Class[] {PrintSpecification.class, LayerSpecification.class});
	}
}
