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
 * Oppretter SiteDataStore basert på name.
 * 
 * 
 * 
 * @author Roar Brænden
 *
 */
public class SiteDataStoreFactory implements DataStoreFactorySpi {


	public static final Param NAMESPACE_PARAM = new Param("namespace", URI.class, "namespace", false, null);
	public static final Param DBTYPE_PARAM = new Param("dbtype", String.class, "dbtype må være aquamonitor-site", true, "aquamonitor-site");
	public static final Param SITE_PARAM = new Param("site", String.class, "name of AquaMonitor site", true, null);
    public static final Param KEY_PARAM = new Param("key", String.class, "key for a given user", false, null);
    public static final Param HOST_PARAM = new Param("host", String.class, "host of AquaMonitor site", false, "http://www.aquamonitor.no/");
    
	
	/**
	 */
	public DataStore createDataStore(Map<String, Serializable> params) throws IOException {
        String dbtype = (String) params.get(DBTYPE_PARAM.key);
        if (dbtype.equals(DBTYPE_PARAM.sample)) {
        	
        	String host = params.get(HOST_PARAM.key).toString();
        	String site = params.get(SITE_PARAM.key).toString();

        	Object keyObj = params.get(KEY_PARAM.key);
        	SiteDataStore store = (keyObj == null ? 
        	        new SiteDataStore(host, site) : 
        	            new SiteDataStore(host, site, keyObj.toString()));
        	
        	Object namespace =  params.get(NAMESPACE_PARAM.key);
        	if (namespace != null) {
        		store.setNamespaceURI(namespace.toString());
        	}

			return store;
        }
        else {
        	return null;
        }
	}

	/**
	 * Dette skal vi vel ikke gjøre. Jo definitivt!!
	 */
	public DataStore createNewDataStore(Map<String, Serializable> params) throws IOException {
		throw new UnsupportedOperationException("Dette finnes det ikke støtte for.");
	}
	
	@SuppressWarnings("rawtypes") 
	@Override
	public boolean canProcess(final Map params) {
		return (DBTYPE_PARAM.sample.equals(params.get(DBTYPE_PARAM.key)));
	}
	

	/**
	 * 
	 */
	public String getDescription() {
		return "Kartlag basert på en Aquamonitor site. Kan kobles til en bruker-sesjon.";
	}

	public String getDisplayName() {
		return "Aquamonitor site map";
	}

	public Param[] getParametersInfo() {
		return new Param[] { NAMESPACE_PARAM, DBTYPE_PARAM, SITE_PARAM, KEY_PARAM, HOST_PARAM};
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
