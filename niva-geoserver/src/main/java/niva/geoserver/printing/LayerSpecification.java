package niva.geoserver.printing;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("LayerSpecification")
public class LayerSpecification {

	public String type;
	public String baseURL;
	public String layer;
	public boolean transparent;
}
