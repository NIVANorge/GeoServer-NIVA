package niva.aquamonitor.data;

import java.io.IOException;

import niva.aquamonitor.data.ws.DatatypeCargo;
import niva.aquamonitor.data.ws.StationPointCargo;

@Deprecated
public interface AquaDataStore {

	@Deprecated
	StationPointCargo[] readStationPoints(String typeName) throws IOException;
	
	@Deprecated
	DatatypeCargo[] readDatatypePoints(String typeName) throws IOException;
}
