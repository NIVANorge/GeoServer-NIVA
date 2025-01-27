package niva.geoserver.printing;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Content of json coming from web client.
 * 
 * @author Roar Brænden, NIVA
 *
 */
@Deprecated
@XStreamAlias("PrintSpecification")
@SuppressWarnings("unused")
public class PrintSpecification {

	public String srs;
	public int dpi;
	public int width;
	public int height;
	
	@XStreamImplicit
	public double[] bbox;
	
	@XStreamImplicit
	public LayerSpecification[] layers;

}
