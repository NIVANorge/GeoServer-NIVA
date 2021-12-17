package niva.aquamonitor.data.ws;

import java.io.IOException;
import java.util.Map;

public class DatatypeReader extends AquaReader<DatatypeCargo>{

	public DatatypeReader(AquaWebService webservice, String function) {
		super(webservice, function);
	}
	
	DatatypeReader(AquaWebService webservice, String path, String token) {
	    super(webservice, path, token);
	}
	
	@Override
	public CloseableIterator<DatatypeCargo> iterator() throws IOException {
		return new DatatypeIterator(callJsonService());
	}
	
	class DatatypeIterator extends JsonMapper<DatatypeCargo>{

		DatatypeIterator(JsonStreamIterator iter) {
			super(iter);
		}
		
		public DatatypeCargo mapCargo(Map<String, Object> curr) {

			final DatatypeCargo cargo = new DatatypeCargo();
			cargo.stationId = getInteger(curr, "StationId");
			cargo.latitude = getDouble(curr, "Latitude");
			cargo.longitude = getDouble(curr, "Longitude");
			cargo.stationTypeId = getInteger(curr, "StationTypeId");
			cargo.stationType = getString(curr, "StationType");
			cargo.datatypes = getStringArr(curr, "Datatypes");
			
			return cargo;
		}
	}
	
}
