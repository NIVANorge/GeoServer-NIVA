package niva.aquamonitor.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import niva.aquamonitor.data.ws.GeographyController;
import niva.aquamonitor.data.ws.StationPointReader;
import org.geotools.data.DefaultServiceInfo;
import org.geotools.api.data.ServiceInfo;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.FeatureTypes;
import org.geotools.feature.NameImpl;

import org.geotools.api.feature.type.Name;


/**
 * En UsersDataStore er basert på stasjonene én enkelt bruker har tilgang til gjennom Aquamonitor.
 * 
 * Det kan opprettes flere featurelayer's. F.eks kun stasjonene, temperaturmålinger eller ph-verdier.
 * 
 * Alle featurelayer's hentes gjennom en webservice i http://www.aquamonitor.no/aquaServices
 * Vi kan velge å mellomlagre dem temporært.
 * 
 * @author Roar Brænden
 *
 */
public class ProjectUserDataStore extends ContentDataStore {
	
	public static final String[] DEFAULT_LAYERS = new String[] { "STATION_POINTS"};
	
	
	private String username;
	
	private GeographyController controller;
	

	public ProjectUserDataStore(String username) {
		if (username==null) {
			throw new IllegalArgumentException("Username must be specified.");
		}
		
		this.username = username;
		this.controller = GeographyController.createService();
	}


	private String getUsername() {
		return this.username;
	}
	

	@Override
	public ServiceInfo getInfo() {
        DefaultServiceInfo info = new DefaultServiceInfo();
        info.setDescription("Kartlag basert på gitt prosjekt-bruker i AquaMonitor");
        info.setSchema( FeatureTypes.DEFAULT_NAMESPACE );        
        return info;
	}
	

	/**
	 * Noen typeNames er faste StationPoint f.eks.
	 * De andre er avhengige av at vi har en temporær lagring, som er populert gjennom createChema
	 */
	@Override
	protected List<Name> createTypeNames() throws IOException {
		List<Name> list = new ArrayList<Name>();
		for (String n : DEFAULT_LAYERS) {
			list.add(new NameImpl(getNamespaceURI(), n));
		}
		
		return list;
	}


	@Override
	protected ContentFeatureSource createFeatureSource(ContentEntry entry) throws IOException {
		if (entry.getTypeName().equals(DEFAULT_LAYERS[0])) {
			String username = getUsername();
			StationPointReader reader = this.controller.getProjectUserStationReader(username);
			
			return new StationPointSource(entry, reader);
		}
		else {
			throw new IllegalArgumentException("Unknown typeName");
		}
	}

}
