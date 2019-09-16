package niva.aquamonitor.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import niva.aquamonitor.data.ws.DatatypeReader;
import niva.aquamonitor.data.ws.GeographyWebService;
import niva.aquamonitor.data.ws.StationGeometryReader;
import niva.aquamonitor.data.ws.StationPointReader;
import niva.aquamonitor.data.ws.StringReader;

import org.geotools.data.DataStore;
import org.geotools.data.DefaultServiceInfo;
import org.geotools.data.ServiceInfo;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.FeatureTypes;
import org.geotools.feature.NameImpl;
import org.geotools.util.logging.Logging;

import org.opengis.feature.type.Name;



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
	
	public static final String[] DEFAULT_LAYERS = new String[] { "STATION_POINTS"
																, "ADMIN_STATION_POINTS"
																, "STATION_DATATYPE_POINTS"
																, "STATION_SECTORS" };
	
	
	private static final Logger LOGGER = Logging.getLogger(SiteDataStore.class);

	private String host;
	private String site;
	private String key = null;
	
	private DataStore geometryStore = null;
	

	public SiteDataStore(String host, String site) {
    	this.host = host;
    	this.site = site;
	}
	
	public SiteDataStore(String host, String site, String key) {
		this(host, site);
		this.key = key;
	}

	private String getHost() {
		return this.host;
	}
	
	private String getSite()  {
		return this.site;
	}
	
	
	private String getKey() {
		return this.key;
	}
	
	public DataStore getGeometryStore() {
		return this.geometryStore;
	}
	
	
	public void setGeometryStore(DataStore geometryStore) {
		this.geometryStore = geometryStore;
	}
	
	
	@Override
	public void dispose() {
		
		if (this.geometryStore != null) {
			try {
				this.geometryStore.dispose();
			}
			finally {
			}
		}
			
		super.dispose();
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
	 * Which layers do exist for this DataStore.
	 * 
	 */
	@Override
	protected List<Name> createTypeNames() throws IOException {
		
		List<Name> list = new ArrayList<Name>();
		
		list.add(new NameImpl(getNamespaceURI(), DEFAULT_LAYERS[0]));
		
		if (getKey() != null) {
			list.add(new NameImpl(getNamespaceURI(), DEFAULT_LAYERS[1]));
		}
		
		list.add(new NameImpl(getNamespaceURI(), DEFAULT_LAYERS[2]));
		
		return list;
	}

	/***
	 * Returns a suitable FeatureSource
	 */
	@Override
	protected ContentFeatureSource createFeatureSource(ContentEntry entry) throws IOException {
		
		GeographyWebService ws = GeographyWebService.createService(getHost(), getSite());
		String typeName = entry.getTypeName();
		
		final String key = getKey();
		
		
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
			DatatypeReader reader;
			
			if (key == null)
				reader = ws.getAllDatatypePointsReader();
			else
				reader =  ws.getCurrentDatatypePointsReader(key);
			
			StringReader datatypesReader = ws.getAllDatatypesReader();
			
			ArrayList<String> list = new ArrayList<String>();
			Iterator<String> iter = datatypesReader.iterator();
			while(iter.hasNext())
				list.add(iter.next());
		
			String[] datatypes = new String[list.size()];
			list.toArray(datatypes);
			
			return new DatatypePointSource(entry, reader, datatypes);
		}
		else if (entry.getTypeName().equals(DEFAULT_LAYERS[3])) {
			StationGeometryReader reader;
			
			if (key == null) {
				reader = ws.getAllStationSectorsReader();
			}
			else {
				reader = ws.getCurrentStationSectorsReader(key);
			}
			LOGGER.warning("This layer uses a type that will not be supported in the future: " + entry.getName());
			return new StationSectorSource(entry, geometryStore, reader);
		}
		else {
			throw new IllegalArgumentException("Unknown typeName");
		}
	}
}
