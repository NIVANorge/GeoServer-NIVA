package niva.aquamonitor.data.ws;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class StationPointReader extends AquaReader<StationPointCargo> {

	
	public StationPointReader(AquaWebService webservice, String function) {
		super(webservice, function);
	}

	@Override
	public Iterator<StationPointCargo> iterator() throws IOException {
		Iterator<Object> iter = callJsonService();
		
		return new StationCargoIterator(iter);
	}
	
	class StationCargoIterator extends JsonMapper<StationPointCargo> {

		StationCargoIterator(Iterator<Object> iter) {
			super(iter);
		}

		@Override
		public StationPointCargo mapCargo(Map<String, Object> curr) {
			final StationPointCargo spc = new StationPointCargo();
			spc.samplePointId = getInteger(curr, "SamplePointId");
			spc.latitude = getDouble(curr,"Latitude");
			spc.longitude = getDouble(curr, "Longitude");
			spc.projectId = getInteger(curr, "ProjectId");
			spc.projectName = getString(curr, "ProjectName");
			spc.stationId = getInteger(curr, "StationId");
			spc.stationTypeId = getInteger(curr, "StationTypeId");
			spc.stationType = getString(curr, "StationType");
			spc.stationCode = getString(curr, "StationCode");
			spc.stationName = getString(curr, "StationName");
			spc.fullStationName = getString(curr, "FullStationName");
			
			return spc;
		}
	}
}
