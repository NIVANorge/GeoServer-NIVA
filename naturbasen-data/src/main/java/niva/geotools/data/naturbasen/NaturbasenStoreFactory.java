package niva.geotools.data.naturbasen;

import java.awt.RenderingHints.Key;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.geotools.arcsde.ArcSDEDataStoreFactory;
import org.geotools.arcsde.data.ArcSDEDataStore;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;

import org.geotools.data.oracle.OracleNGDataStoreFactory;
import org.geotools.jdbc.JDBCDataStore;

import static org.geotools.arcsde.data.ArcSDEDataStoreConfig.DBTYPE_PARAM_NAME;
import static org.geotools.arcsde.session.ArcSDEConnectionConfig.USER_NAME_PARAM_NAME;
import static org.geotools.arcsde.session.ArcSDEConnectionConfig.INSTANCE_NAME_PARAM_NAME;
import static org.geotools.arcsde.session.ArcSDEConnectionConfig.MAX_CONNECTIONS_PARAM_NAME;
import static org.geotools.arcsde.session.ArcSDEConnectionConfig.MIN_CONNECTIONS_PARAM_NAME;
import static org.geotools.arcsde.session.ArcSDEConnectionConfig.PASSWORD_PARAM_NAME;
import static org.geotools.arcsde.session.ArcSDEConnectionConfig.PORT_NUMBER_PARAM_NAME;
import static org.geotools.arcsde.session.ArcSDEConnectionConfig.SERVER_NAME_PARAM_NAME;



public class NaturbasenStoreFactory implements DataStoreFactorySpi {
	public static final String DBTYPE_PARAM_VALUE = "naturbasen";

	private static final String USER_PARAM_VALUE = "BIOMANG";
	
	private static final String DBTYPE_PARAM_VALUE_ARCSDE = "arcsde";
	private static final String DBTYPE_PARAM_VALUE_DB = "Oracle";
	
	private static final int PORT_PARAM_VALUE_ARCSDE = 5151;
	private static final int PORT_PARAM_VALUE_DB = 1521;
	
	static final Param[] PARAMETERS = new Param[] {
		new Param(DBTYPE_PARAM_NAME, String.class, "fixed value. Must be \"" + DBTYPE_PARAM_VALUE + "\"", true, DBTYPE_PARAM_VALUE)
	    , new Param(SERVER_NAME_PARAM_NAME, String.class, "name of server <arcsde;oracle>.", true, null)
		, new Param(PASSWORD_PARAM_NAME, String.class, "password of user BIOMANG", true, null)
		, new Param(MAX_CONNECTIONS_PARAM_NAME, Integer.class, "max connections", true, 3)
		, new Param(MIN_CONNECTIONS_PARAM_NAME, Integer.class, "min conenctions", true, 1)
	};

	
	private static final ArcSDEDataStoreFactory arcFactory = new ArcSDEDataStoreFactory();
	private static final OracleNGDataStoreFactory dbFactory = new OracleNGDataStoreFactory();
	
	public DataStore createDataStore(Map<String, Serializable> params) throws IOException {
		if (params == null)
			throw new IllegalArgumentException("params kan ikke være null");
		
        String dbtype = (String) params.get(DBTYPE_PARAM_NAME);
        if (DBTYPE_PARAM_VALUE.equals(dbtype)) {
			ArcSDEDataStore arcStore = (ArcSDEDataStore) arcFactory.createDataStore(adjustParamsArcsde(params));			

			JDBCDataStore dbStore = dbFactory.createDataStore(adjustParamsDb(params));
			dbStore.setExposePrimaryKeyColumns(true);
			
			NaturbasenStore store = new NaturbasenStore(arcStore, dbStore);
		
			return store;
        }
        else {
        	throw new IllegalArgumentException("Wrong dbtype");
        }
	}

	public DataStore createNewDataStore(Map<String, Serializable> params) throws IOException {
		throw new UnsupportedOperationException("Dette gjør vi ikke.");
	}

	public boolean canProcess(Map<String, Serializable> params) {
        String dbtype = (String) params.get(DBTYPE_PARAM_NAME);
        if (DBTYPE_PARAM_VALUE.equals(dbtype)) {
        	boolean ret = true;
        	ret = arcFactory.canProcess(adjustParamsArcsde(params));
        	if (ret) {	
        		OracleNGDataStoreFactory dbFactory = new OracleNGDataStoreFactory();
        		ret = dbFactory.canProcess(adjustParamsDb(params));
        	}
        	return ret;
        }
        else
        	return false;
	}

	public String getDescription() {
		return "Kartlag basert på data i Biomangfold";
	}

	public String getDisplayName() {
		return "Naturbasen biomangfold";
	}

	public Param[] getParametersInfo() {
		return PARAMETERS;
	}

	public boolean isAvailable() {
		return arcFactory.isAvailable() && dbFactory.isAvailable();
	}

	public Map<Key, ?> getImplementationHints() {
		throw new UnsupportedOperationException("Dette er ikke implementert.");
	}

	
	private Map<String, Serializable> adjustParamsArcsde(Map<String, Serializable> params) {
		Map<String, Serializable> adjusted = new HashMap<String, Serializable>();
		
		adjusted.put(DBTYPE_PARAM_NAME, DBTYPE_PARAM_VALUE_ARCSDE);
		adjusted.put(SERVER_NAME_PARAM_NAME, getServerName(params, 0));
		adjusted.put(USER_NAME_PARAM_NAME, USER_PARAM_VALUE);
		adjusted.put(PASSWORD_PARAM_NAME, params.get(PASSWORD_PARAM_NAME));
		adjusted.put(PORT_NUMBER_PARAM_NAME, PORT_PARAM_VALUE_ARCSDE);
		adjusted.put(INSTANCE_NAME_PARAM_NAME, null);
		
		return adjusted;
	}
	
	private Map<String, Serializable> adjustParamsDb(Map<String, Serializable> params) {
		Map<String, Serializable> adjusted = new HashMap<String, Serializable>();
		
		adjusted.put(DBTYPE_PARAM_NAME, DBTYPE_PARAM_VALUE_DB);
		adjusted.put("host", getServerName(params, 1));			
		adjusted.put(PORT_NUMBER_PARAM_NAME, PORT_PARAM_VALUE_DB);
		adjusted.put("user", USER_PARAM_VALUE);
		adjusted.put("passwd", params.get(PASSWORD_PARAM_NAME));

		adjusted.put("schema", "BIOMANG");
		adjusted.put("loosebox", Boolean.TRUE);
		
		adjusted.put("database", "NIVABASE");
		adjusted.put("maxconn", params.get(MAX_CONNECTIONS_PARAM_NAME));
		adjusted.put("minconn", params.get(MIN_CONNECTIONS_PARAM_NAME));
		
		return adjusted;
	}
	
	private String getServerName(Map<String, Serializable> params, int index) {
		String param = (String)params.get(SERVER_NAME_PARAM_NAME);
		if (param.indexOf(";") > -1) {
			return param.split(";")[index];
		}
		else {
			return param;
		}
			
	}
}
