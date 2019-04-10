package niva.aquamonitor.data.ws;


/**
 * Base class for all webservices.
 * @author Roar Brænden, NIVA
 *
 */
public abstract class AquaWebService {


	private static final String NAMESPACE = "http://www.aquamonitor.no/";
	protected static final String HOST_ADDRESS = "http://www.aquamonitor.no/";
	//protected static final String HOST_ADDRESS = "https://test-aquamonitor.niva.no/";
	protected static final String DEFAULT_SITE = "AquaServices";
	
	private String url;
	
	/**
	 * Init with an url.
	 * 
	 * @param url
	 */
	public AquaWebService(String url)   {
		this.url = url;
	}

	String getUrl()   {
		return this.url;
	}
	
	String getNamespace() {
		return NAMESPACE;
	}
	
}
