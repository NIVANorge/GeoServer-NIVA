package niva.aquamonitor.data.ws;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;
import java.util.logging.Logger;


import org.geotools.util.logging.Logging;

/**
 * 
 * Calling the web service in AquaMonitor to support geographical elements.
 * 
 * We're using a secret token that connects as a System user within AquaMonitor.
 * It could be configured by:
 * <li>Java system property (-D)</li>
 * <li>System environment variable</li>
 * <br>
 * The name for the variable should be: <b>AQUAMONITOR_SECRET_TOKEN</b>
 * <br>
 * This must be set prior to any call.
 * 
 * @author Roar Brænden, NIVA
 *
 */
public class GeographyWebService extends AquaWebService {
	
	private static final String SERVICE_ADDRESS = "/WebServices/GeographyService.asmx";
	
	private static final String TOKEN_PROPERTY = "AQUAMONITOR_SECRET_TOKEN";

	/** Logger. */
	private final static Logger LOGGER = Logging.getLogger(GeographyWebService.class);
	
	private static String defaultToken = null;
	static {
		lookupSecretToken();
	}
	
	
	public static GeographyWebService createService(String host, String site)    {
		return new GeographyWebService(host + site + SERVICE_ADDRESS);
	}
	
	public static GeographyWebService createService(String host)    {
		return createService(host, AquaWebService.DEFAULT_SITE);
	}
	
	public static GeographyWebService createService() {
		return createService(getHostAddress(), AquaWebService.DEFAULT_SITE);
	}
	
	public GeographyWebService(String url)   {
		super(url);
	}
	
    private static void lookupSecretToken() {
    	LOGGER.fine("Lookup AquaMonitor secret token.");
    	
        final String[] typeStrs = {
            "Java system property ",
            "System environment variable "
        };

        // Loop over variable access methods
        for (int j = 0; j < typeStrs.length; j++) {
            String value = null;
             
            // Lookup section
            switch (j) {
                case 0:
                    value = System.getProperty(TOKEN_PROPERTY);
                    break;
                case 1:
                    value = System.getenv(TOKEN_PROPERTY);
                    break;
            }

            if (value == null || value.equalsIgnoreCase("")) {
                break;
            }

            LOGGER.fine(String.format("Found AquaMonitor secret token %s within %s", value, typeStrs[j]));

            try {
				defaultToken = URLEncoder.encode(value, "UTF-8");
	            break;
			} catch (UnsupportedEncodingException e) {
				LOGGER.warning("This platform doesn't support UTF-8");
			}
        }
        
        if (Objects.isNull(defaultToken)) {
            LOGGER.warning("No AquaMonitor secret token found.");
        }
    }


	public StationPointReader getProjectUserStationReader(String username) throws IOException {
	    checkToken();
		LOGGER.fine("GeographyWebService.getProjectUserStationReader. username:" + username);
		StationPointReader reader = new StationPointReader(this, "GetProjectUserStationPoints");
		reader.addArgument("token", defaultToken);
		reader.addArgument("username", username);

		return reader;
	}
	


	
	public StationPointReader getAllStationReader() throws IOException {
	    checkToken();
		LOGGER.fine("GeographyWebService.getAllStationReader.");
		final StationPointReader reader = new StationPointReader(this, "GetAllStationPoints");
		reader.addArgument("token", defaultToken);

		return reader;
	}
	

	
	public StationPointReader queryAllStationReader(String where) throws IOException {
	    checkToken();
		LOGGER.fine("GeographyWebService.queryAllStationReader. where:" + where);
		StationPointReader reader = new StationPointReader(this, "QueryAllStationPoints");
		reader.addArgument("token", defaultToken);
		reader.addArgument("where", where);

		return reader;
	}
	


	
	public StationPointReader getCurrentStationReader(String userkey) throws IOException {
	    checkToken();
		LOGGER.fine("GeographyWebService.getCurrentStationReader. userkey:" + userkey);
		StationPointReader reader = new StationPointReader(this, "GetCurrentStationPoints");
		reader.addArgument("token", defaultToken);
		reader.addArgument("userkey", userkey);
		
		return reader;
	}
	

	
	public StationPointReader getAdminStationReader(String userkey) throws IOException {
	    checkToken();
		LOGGER.fine("GeographyWebService.getAdminStationReader. userkey:" + userkey);
		StationPointReader reader = new StationPointReader(this, "GetAdminStationPoints");
		reader.addArgument("token", defaultToken);
		reader.addArgument("userkey", userkey);
		
		return reader;
	}
	

	
	public StringReader getAllDatatypesReader() throws IOException {
	    checkToken();
		LOGGER.fine("GeographyWebService.getAllDatatypesReader.");
		StringReader reader = new StringReader(this, "GetAllDatatypes");
		reader.addArgument("token", defaultToken);
		return reader;
	}
	
	
	
	public DatatypeReader getAllDatatypePointsReader() throws IOException {
	    checkToken();
		LOGGER.fine("GeographyWebService.getAllDatatypePointsReader.");
		DatatypeReader reader = new DatatypeReader(this, "GetAllDatatypePoints");
		reader.addArgument("token", defaultToken);
		reader.setTimeout(10);
		return reader;
	}
	
	
	public DatatypeReader getCurrentDatatypePointsReader(String userkey) throws IOException {
	    checkToken();
		LOGGER.fine("GeographyWebService.getCurrentDatatypePointsReader. userkey:" + userkey);
		DatatypeReader reader = new DatatypeReader(this, "GetCurrentDatatypePoints");
		reader.addArgument("token", defaultToken);
		reader.addArgument("userkey", userkey);
		reader.setTimeout(5);
		
		return reader;
	}
	
	private void checkToken() throws IOException {
	    if (defaultToken == null) {
	        throw new IOException("AQUAMONITOR_SECRET_TOKEN is not set.");
	    }
	}
}
