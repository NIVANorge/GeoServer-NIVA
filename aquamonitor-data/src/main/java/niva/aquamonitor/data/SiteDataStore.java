package niva.aquamonitor.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import niva.aquamonitor.data.ws.DatatypeReader;
import niva.aquamonitor.data.ws.GeographyController;
import niva.aquamonitor.data.ws.StationPointReader;
import niva.aquamonitor.data.ws.ValuePointReader;
import org.geotools.data.DefaultServiceInfo;
import org.geotools.api.data.ServiceInfo;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.FeatureTypes;
import org.geotools.feature.NameImpl;
import org.geotools.util.logging.Logging;
import org.geotools.api.feature.type.Name;



/**
 * En SiteDataStore er basert på stasjonene én enkelt bruker har tilgang til gjennom Aquamonitor.
 * 
 * Det kan opprettes flere featurelayer's. F.eks kun stasjonene, temperaturmålinger eller ph-verdier.
 * 
 * 
 * @author Roar Brænden
 *
 */
public class SiteDataStore extends ContentDataStore {
	
	public static final String[] DEFAULT_LAYERS = new String[] {
	        "STATION_POINTS",
	        "ADMIN_STATION_POINTS", 
	        "STATION_DATATYPE_POINTS", 
	        "VALUE_POINTS"};
	
	private static final Logger LOGGER = Logging.getLogger(SiteDataStore.class);

	private String host;
	private String site;
	private String key;
	
	private String[] allDatatypes = null;


	public SiteDataStore(String host, String site) {
	    this(host, site, null);
	}
	
	public SiteDataStore(String host, String site, String key) {
	    this.host = host;
	    this.site = site;	
		this.key = key;
	}

	public String getHost() {
		return this.host;
	}
	
	public void setHost(String host) {
		this.host = host;
	}
	
	public String getSite()  {
		return this.site;
	}
	
	public void setSite(String site) {
		this.site = site;
	}
	
	
	public String getKey() {
		return this.key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public ServiceInfo getInfo() {
        DefaultServiceInfo info = new DefaultServiceInfo();
        info.setTitle("AquaMonitor Site " + this.getSite());
        info.setDescription("Kartlag basert på stasjoner i AquaMonitor-siten:" + this.getHost() + this.getSite());
        info.setSchema( FeatureTypes.DEFAULT_NAMESPACE );        
        return info;
	}

	/**
	 * Which layers do exist for this DataStore. Taking into account if it got a userKey.
	 */
	@Override
	protected List<Name> createTypeNames() throws IOException {
		List<Name> list = new ArrayList<>();
		list.add(new NameImpl(getNamespaceURI(), DEFAULT_LAYERS[0]));
		if (getKey() != null) {
			list.add(new NameImpl(getNamespaceURI(), DEFAULT_LAYERS[1]));
		}
		list.add(new NameImpl(getNamespaceURI(), DEFAULT_LAYERS[2]));
		if (getKey() != null) {
		    list.add(new NameImpl(getNamespaceURI(), DEFAULT_LAYERS[3]));
		}
		return list;
	}
	
	private String[] getAlldatatypes() {
		if (allDatatypes == null) {
			final String[] datatypes;

			final GeographyController ws = GeographyController.createService(getHost(), getSite());
			try (Stream<String> datatypesStream = ws.getAllDatatypesReader().stream()) {
                List<String> list = datatypesStream.collect(Collectors.toList());
                datatypes = list.toArray(new String[list.size()]);
			} catch (IOException e) {
				if ("Feil bruker".equals(e.getMessage())) {
					throw new RuntimeException("AQUAMONITOR_SECRET_TOKEN must be reconfigured.");
				}
				throw new RuntimeException("Failed to collect all datatypes from site: " + getSite(), e);
			}
			allDatatypes = datatypes;
		}
		return allDatatypes;
	}

	/***
	 * Returns a suitable FeatureSource
	 */
	@Override
	protected ContentFeatureSource createFeatureSource(ContentEntry entry) throws IOException {
		
		final GeographyController ws = GeographyController.createService(getHost(), getSite());
		final String typeName = entry.getTypeName();
		final String key = getKey();
		
		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.fine(String.format("Create feature source for %s with key %s.\n", typeName, key));
		}
		
		if (entry.getTypeName().equals(DEFAULT_LAYERS[0]) || entry.getTypeName().equals(DEFAULT_LAYERS[1])) {
			StationPointReader reader;
			
			if (key == null && typeName.equals(DEFAULT_LAYERS[0])) {
				reader =  ws.getAllStationReader();
			}
			else if (key != null && typeName.equals(DEFAULT_LAYERS[0])) {
				reader =  ws.getCurrentStationReader(key);
			}
			else if (key != null && typeName.equals(DEFAULT_LAYERS[1])) {
				reader =  ws.getAdminStationReader(key);
			}
			else {
				LOGGER.severe("Unknown typename specified: " + typeName);
				throw new IllegalArgumentException("Unknown TypeName");
			}
		
			return new StationPointSource(entry, reader);
		}
		else if (entry.getTypeName().equals(DEFAULT_LAYERS[2])) {
			DatatypeReader reader = (key == null ? ws.getAllDatatypePointsReader()
                    : ws.getCurrentDatatypePointsReader(key));
            return new DatatypePointSource(entry, reader, getAlldatatypes());
		}
		else if (entry.getTypeName().equals(DEFAULT_LAYERS[3])) {
		    ValuePointReader reader = ws.getCurrentValuePointsReader(key);
		    return new ValuePointSource(entry, reader);
		}
		else {
			throw new IllegalArgumentException("Unknown typeName");
		}
	}
}
