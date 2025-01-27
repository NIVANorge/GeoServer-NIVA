package niva.geoserver.printing;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Subpart of the json coming from the web client.
 * Used by both OSM and WMS.
 * 
 * @author Roar Brænden, NIVA
 *
 */
@Deprecated
@XStreamAlias("LayerSpecification")
public class LayerSpecification {

	public String type;
	public String baseURL;
	public String layer;
	public boolean transparent;
}
