package niva.aquamonitor.data.ws;

import java.io.IOException;
import java.util.logging.Logger;


import org.geotools.util.logging.Logging;

/**
 * 
 * @author Roar Brænden, NIVA
 *
 */
public class GeographyWebService extends AquaWebService {
	
	private static final String SERVICE_ADDRESS = "/WebServices/GeographyService.asmx";
	
	
	/** Logger. */
	private final static Logger LOGGER = Logging.getLogger(GeographyWebService.class);

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
	

	public StationPointReader getProjectUserStationReader(String username) throws IOException {
		LOGGER.fine("GeographyWebService.getProjectUserStationReader. username:" + username);
		StationPointReader reader = new StationPointReader(this, "GetProjectUserStationPoints");
		reader.addArgument("token", "system");
		reader.addArgument("username", username);

		return reader;
	}
	


	
	public StationPointReader getAllStationReader() throws IOException {
		LOGGER.fine("GeographyWebService.getAllStationReader.");
		StationPointReader reader = new StationPointReader(this, "GetAllStationPoints");
		reader.addArgument("token", "system");

		return reader;
	}
	

	
	public StationPointReader queryAllStationReader(String where) throws IOException {
		LOGGER.fine("GeographyWebService.queryAllStationReader. where:" + where);
		StationPointReader reader = new StationPointReader(this, "QueryAllStationPoints");
		reader.addArgument("token", "system");
		reader.addArgument("where", "where");

		return reader;
	}
	


	
	public StationPointReader getCurrentStationReader(String userkey) throws IOException {
		
		LOGGER.fine("GeographyWebService.getCurrentStationReader. userkey:" + userkey);
		StationPointReader reader = new StationPointReader(this, "GetCurrentStationPoints");
		reader.addArgument("token", "system");
		reader.addArgument("userkey", userkey);
		
		return reader;
	}
	

	
	public StationPointReader getAdminStationReader(String userkey) throws IOException {
		
		LOGGER.fine("GeographyWebService.getAdminStationReader. userkey:" + userkey);
		StationPointReader reader = new StationPointReader(this, "GetAdminStationPoints");
		reader.addArgument("token", "system");
		reader.addArgument("userkey", userkey);
		
		return reader;
	}
	

	
	public StringReader getAllDatatypesReader() throws IOException {
		LOGGER.fine("GeographyWebService.getAllDatatypesReader.");
		StringReader reader = new StringReader(this, "GetAllDatatypes");
		reader.addArgument("token", "system");
		return reader;
	}
	
	
	
	public DatatypeReader getAllDatatypePointsReader() throws IOException {
		LOGGER.fine("GeographyWebService.getAllDatatypePointsReader.");
		DatatypeReader reader = new DatatypeReader(this, "GetAllDatatypePoints");
		reader.addArgument("token", "system");
		reader.setTimeout(10);
		return reader;
	}
	
	
	public DatatypeReader getCurrentDatatypePointsReader(String userkey) throws IOException {
		LOGGER.fine("GeographyWebService.getCurrentDatatypePointsReader. userkey:" + userkey);
		DatatypeReader reader = new DatatypeReader(this, "GetCurrentDatatypePoints");
		reader.addArgument("token", "system");
		reader.addArgument("userkey", userkey);
		reader.setTimeout(5);
		
		return reader;
	}


}
