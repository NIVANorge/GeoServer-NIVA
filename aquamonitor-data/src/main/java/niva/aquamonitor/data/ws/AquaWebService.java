package niva.aquamonitor.data.ws;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.geotools.util.logging.Logging;

/**
 * Base class for all webservices. Functionality to create address for aquamonitor web server.
 * @author Roar Brænden, NIVA
 *
 */
public abstract class AquaWebService {


	private static final String NAMESPACE = "http://www.aquamonitor.no/";
	private static final String HOST_ADDRESS = "http://www.aquamonitor.no/";
	protected static final String DEFAULT_SITE = "AquaServices";
	
	private static String defaultHostAddress = null;
	private static Map<ServletContext, String> hostAddresses = new HashMap<ServletContext, String>();
	
	private String url;
	

	/** Logger. */
	private final static Logger LOGGER = Logging.getLogger(AquaWebService.class);
	

	
	protected static String getHostAddress() {
		return (defaultHostAddress != null ? defaultHostAddress : lookupHostAddress(null));
	}
	
	
	
	protected static String getHostAddress(ServletContext servContext) {
		return (hostAddresses.containsKey(servContext) ? hostAddresses.get(servContext) : lookupHostAddress(servContext));
	}
	
	
    private static String lookupHostAddress(ServletContext servContext) {
    	

        final String[] typeStrs = {
            "Java environment variable ",
            "Servlet context parameter ",
            "System environment variable "
        };

        final String varStr = "AQUAMONITOR_HOST_ADDRESS";

        String hostAddress = null;

        // Loop over variable access methods
        for (int j = 0; j < typeStrs.length; j++) {
            String value = null;
             
            // Lookup section
            switch (j) {
                case 0:
                    value = System.getProperty(varStr);
                    break;
                case 1:
                    value = (servContext != null ? servContext.getInitParameter(varStr) : null);
                    break;
                case 2:
                    value = System.getenv(varStr);
                    break;
            }

            if (value == null || value.equalsIgnoreCase("")) {
                continue;
            }

            LOGGER.fine("Found AquaMonitor host address " + value + " within " + typeStrs[j]);
            
            if (servContext == null) {
            	defaultHostAddress = value;
            }
            else {
            	hostAddresses.put(servContext,  value);
            }
            
            hostAddress = value;
            break;
        }
        return (hostAddress == null ? HOST_ADDRESS : hostAddress);
    }

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
