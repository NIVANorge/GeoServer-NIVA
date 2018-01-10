package niva.aquamonitor.data.ws;



public abstract class AquaWebService {


	private static final String NAMESPACE = "http://www.aquamonitor.no/";
	protected static final String HOST_ADDRESS = "http://www.aquamonitor.no/";
	//protected static final String HOST_ADDRESS = "http://localhost/";
	protected static final String DEFAULT_SITE = "AquaServices";
	
	private String url;
	
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
