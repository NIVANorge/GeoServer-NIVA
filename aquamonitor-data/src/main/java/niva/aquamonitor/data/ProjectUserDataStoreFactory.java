package niva.aquamonitor.data;

import java.awt.RenderingHints.Key;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;


/**
 * Oppretter UsersDataStore basert på username.
 * 
 * Litt usikker på om vi skal bruke username eller userid.
 * 
 * @author Roar Brænden
 *
 */
public class ProjectUserDataStoreFactory implements DataStoreFactorySpi {


	public static final Param NAMESPACE_PARAM = new Param("namespace", URI.class, "namespace", true, null);
	public static final Param DBTYPE_PARAM = new Param("dbtype", String.class, "dbtype må være aquamonitor", true, "aquamonitor");
    public static final Param USER_PARAM = new Param("user", String.class, "name of AquaMonitor project-user", true, null);

	
	/**
	 */
	public DataStore createDataStore(Map<String, Serializable> params) throws IOException {
        String dbtype = (String) params.get(DBTYPE_PARAM.key);
        if (dbtype.equals(DBTYPE_PARAM.sample)) {

        	String username;
        	username = (String) params.get(USER_PARAM.key);
        	
			ProjectUserDataStore store = new ProjectUserDataStore(username);
			
			Object namespace = params.get(NAMESPACE_PARAM.key);
			if (namespace != null)
				store.setNamespaceURI(namespace.toString());

			return store;
        }
        else {
        	return null;
        }
	}


	/**
	 * 
	 */
	public String getDescription() {
		return "Kartlag basert på Aquamonitor prosjekt-bruker";
	}

	public String getDisplayName() {
		return "Aquamonitor user map";
	}

	public Param[] getParametersInfo() {
		return new Param[] { NAMESPACE_PARAM, DBTYPE_PARAM, USER_PARAM};
	}
	
	@SuppressWarnings("rawtypes") 
	@Override
	public boolean canProcess(final Map params) {
		return (DBTYPE_PARAM.sample.equals(params.get(DBTYPE_PARAM.key)));
	}


	@Override
	public DataStore createNewDataStore(Map<String, Serializable> params)
			throws IOException {
		throw new UnsupportedOperationException("This isn't supported.");
	}


	@Override
	public boolean isAvailable() {
		return true;
	}


	@SuppressWarnings("unchecked")
	@Override
	public Map<Key, ?> getImplementationHints() {
		return Collections.EMPTY_MAP;
	}

}
