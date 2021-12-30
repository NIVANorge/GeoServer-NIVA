package niva.aquamonitor.data.ws;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;
import org.geotools.util.logging.Logging;

/**
 * Methods for calling Niva.AquaMonitor.Map.Web.GeographyController
 * 
 * The final address is <aquamonitor-site>/api/geography/<function-path>
 * 
 * Each function call's it's own Reader with the rest of the path
 * 
 * @author Roar Brænden, NIVA
 *
 */
public class GeographyController extends AquaWebService {
    /** Logger. */
    private final static Logger LOGGER = Logging.getLogger(GeographyController.class);
    private static final String TOKEN_PROPERTY = "AQUAMONITOR_SECRET_TOKEN";
    
    private static String defaultToken = null;
    static {
        lookupSecretToken();
    }
    
    private GeographyController(String url) {
        super(url);
    }

    public static GeographyController createService() {
        return createService(getHostAddress(), DEFAULT_SITE);
    }
    

    public static GeographyController createService(String host, String site) {
        return new GeographyController(host + site + "/api/geography");
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
            
            defaultToken = value;
        }
        
        if (Objects.isNull(defaultToken)) {
            LOGGER.warning("No AquaMonitor secret token found.");
        }
    }
    
    private void checkToken() throws IOException {
        if (defaultToken == null) {
            throw new IOException("AQUAMONITOR_SECRET_TOKEN is not set.");
        }
    }


    

    public StationPointReader getProjectUserStationReader(String username) throws IOException {
        checkToken();
        LOGGER.fine("username:" + username);
        return new StationPointReader(this, 
                String.format("projectuser/%s/stationpoints", username), 
                defaultToken);
    }
    

    public StationPointReader getAllStationReader() throws IOException {
        checkToken();
        return new StationPointReader(this, "stationpoints", defaultToken);
    }

    
    public StationPointReader getCurrentStationReader(String userkey) throws IOException {
        checkToken();
        LOGGER.fine("userkey:" + userkey);
        return new StationPointReader(this, 
                String.format("user/%s/stationpoints", userkey), 
                defaultToken);
    }
    

    
    public StationPointReader getAdminStationReader(String userkey) throws IOException {
        checkToken();
        LOGGER.fine("userkey:" + userkey);
        return new StationPointReader(this, 
                String.format("admin/%s/stationpoints", userkey), 
                defaultToken);
    }
    

    
    public StringReader getAllDatatypesReader() throws IOException {
        checkToken();
        return new StringReader(this, "datatypes", defaultToken);
    }
    
    
    
    public DatatypeReader getAllDatatypePointsReader() throws IOException {
        checkToken();
        final DatatypeReader reader = new DatatypeReader(this, "datatypepoints", defaultToken);
        reader.setTimeout(10);
        return reader;
    }
    
    
    public DatatypeReader getCurrentDatatypePointsReader(String userkey) throws IOException {
        checkToken();
        LOGGER.fine("userkey:" + userkey);
        final DatatypeReader reader = new DatatypeReader(this, 
                String.format("user/%s/datatypepoints", userkey), 
                defaultToken);
        reader.setTimeout(5);
        
        return reader;
    }
}
