package niva.vannmiljo.data;

import static org.geotools.data.arcgisrest.ArcGISRestDataStoreFactory.NAMESPACE_PARAM;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.arcgisrest.ArcGISRestDataStore;

public class VannmiljoDataStoreFactory implements DataStoreFactorySpi {

	private static final String VANNMILJO_URL = "https://kart.miljodirektoratet.no/arcgis/rest/services/vannmiljo/FeatureServer";
	@Override
	public String getDisplayName() {
		return "Vannmiljø ArcGIS Rest";
	}

	@Override
	public String getDescription() {
		return "A DataStore wrapper on Vannmiljø's ArcGis rest service";
	}

	@Override
	public Param[] getParametersInfo() {
		return new Param[] {NAMESPACE_PARAM};
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	public DataStore createDataStore(Map<String, Serializable> params) throws IOException {
        return new ArcGISRestDataStore(
                (String) params.get(NAMESPACE_PARAM.key),
                VANNMILJO_URL,
                true,
                null,
                null);
	}

	@Override
	public DataStore createNewDataStore(Map<String, Serializable> params) throws IOException {
		throw new UnsupportedOperationException("Vi kan ikke lage nye DataStore.");
	}
}
