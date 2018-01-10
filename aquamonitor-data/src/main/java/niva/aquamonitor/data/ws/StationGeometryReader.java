package niva.aquamonitor.data.ws;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;


public class StationGeometryReader extends AquaReader<StationGeometryCargo> {

	StationGeometryReader(AquaWebService webservice, String function) {
		super(webservice, function);
	}

	@Override
	public Iterator<StationGeometryCargo> iterator() throws IOException {
		return new StationGeometryIterator(callJsonService());
	}
	
	class StationGeometryIterator extends JsonMapper<StationGeometryCargo> {
		
		StationGeometryIterator(Iterator<Object> iter) {
			super(iter);
		}
		
		
		public StationGeometryCargo mapCargo(Map<String, Object> curr) {
			final StationGeometryCargo spc = new StationGeometryCargo();
			spc.geometryId = getInteger(curr, "GeometryId");
			spc.geometryTypeId = getInteger(curr, "GeometryTypeId");
			spc.geometryType = getString(curr, "GeometryType");
			spc.layer = getString(curr, "Layer");
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
