package niva.aquamonitor.data.ws;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.geotools.util.logging.Logging;

/**
 * Base class for calling a AquaMonitor webservice.
 * Functionality to create the address for a AquaMonitor web server.
 * <br>
 * It could be configured by either:
 * <li>Java system property (-D)</li>
 * <li>Servlet context parameter</li>
 * <li>System environment variable</li>
 * <br>
 * The name for the variable should be: <b>AQUAMONITOR_HOST_ADDRESS</b>
 * <br>
 * Default is set to http://www.aquamonitor.no/
 * 
 * @author Roar Brænden, NIVA
 *
 */
public abstract class AquaWebService {


	private static final String NAMESPACE = "http://www.aquamonitor.no/";
	private static final String HOST_ADDRESS = "http://www.aquamonitor.no/";
	protected static final String DEFAULT_SITE = "AquaServices";

    private static final String HOST_ADDRESS_VARIABLE = "AQUAMONITOR_HOST_ADDRESS";
	
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
    	LOGGER.fine("Lookup AquaMonitor host address.");
    	
        final String[] typeStrs = {
            "Java system property ",
            "Servlet context parameter ",
            "System environment variable "
        };

        String hostAddress = null;

        // Loop over variable access methods
        for (int j = 0; j < typeStrs.length; j++) {
            String value = null;
             
            // Lookup section
            switch (j) {
                case 0:
                    value = System.getProperty(HOST_ADDRESS_VARIABLE);
                    break;
                case 1:
                    value = (servContext != null ? servContext.getInitParameter(HOST_ADDRESS_VARIABLE) : null);
                    break;
                case 2:
                    value = System.getenv(HOST_ADDRESS_VARIABLE);
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
