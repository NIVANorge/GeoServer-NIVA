package niva.aquamonitor.data;

import java.io.IOException;
import org.geotools.data.store.ContentEntry;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.api.feature.simple.SimpleFeatureType;
import niva.aquamonitor.data.ws.CloseableIterator;
import niva.aquamonitor.data.ws.StationPointCargo;
import niva.aquamonitor.data.ws.StationPointReader;

/**
 * Represents the Stations as a point with attributes: sample_point_id, longitude, latitude, project_id, project_name, station_id,
 * station_type_id, station_type, station_code and station_name.
 * 
 * It takes a StationPointReader as input, and uses that repeatedly.
 * 
 * @author Roar Brænden, NIVA
 *
 */
public class StationPointSource extends AbstractPointSource<StationPointCargo> {
	

	public StationPointSource(ContentEntry entry, StationPointReader reader) {
		super(entry, reader);
	}
	

	/**
	 * Builds the structure for a simple station point
	 */
	@Override
	protected void addAttributes(SimpleFeatureTypeBuilder builder) {
        builder.nillable(false).add("SAMPLE_POINT_ID", Integer.class);
        builder.add("LATITUDE", Double.class);
        builder.add("LONGITUDE", Double.class);
        builder.add("PROJECT_ID", Integer.class);
        builder.add("PROJECT_NAME", String.class);
        builder.nillable(false).add("STATION_ID", Integer.class);
        builder.add("STATION_TYPE_ID", Integer.class);
        builder.add("STATION_TYPE", String.class);
        builder.add("STATION_CODE", String.class);
        builder.add("STATION_NAME", String.class);
        builder.add("FULL_STATION_NAME", String.class); 
	}
	
	

    @Override
    protected AbstractFeatureReader<StationPointCargo> createFeatureReader(SimpleFeatureType schema,
            CloseableIterator<StationPointCargo> iter) throws IOException {
        return new AbstractFeatureReader<StationPointCargo>(schema, iter) {

            @Override
            protected void addNextFeature(SimpleFeatureBuilder builder, StationPointCargo next) {
                builder.add(next.samplePointId);
                builder.add(next.latitude);
                builder.add(next.longitude);
                builder.add(next.projectId);
                builder.add(next.projectName);
                builder.add(next.stationId);
                builder.add(next.stationTypeId);
                builder.add(next.stationType);
                builder.add(next.stationCode);
                builder.add(next.stationName);
                builder.add(next.fullStationName);
            }
        };
    }
}
