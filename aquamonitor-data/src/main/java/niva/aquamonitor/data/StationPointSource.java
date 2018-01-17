package niva.aquamonitor.data;

import java.io.IOException;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;


import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Point;

import niva.geotools.referencing.CRS;

/**
 * Represents the Stations as a point with attributes: sample_point_id, longitude, latitude, project_id, project_name, station_id,
 * station_type_id, station_type, station_code and station_name.
 * 
 * 
 * @author Roar Brænden, NIVA
 *
 */
public class StationPointSource extends ContentFeatureSource {
	
	private niva.aquamonitor.data.ws.StationPointReader reader;
	
	
	public StationPointSource(ContentEntry entry, niva.aquamonitor.data.ws.StationPointReader reader) {
		super(entry, Query.ALL);
		
		this.reader = reader;
	}
	

	/**
	 * Bygger opp struktur for en enkel SamplePoint
	 */
	@Override
	protected SimpleFeatureType buildFeatureType() throws IOException {
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName(entry.getName());
		CoordinateReferenceSystem crs = CRS.getBreddeLengdegrad();
		
		builder.setCRS(crs);
		builder.add("the_geom", Point.class);
		
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
		
		return builder.buildFeatureType();
	}
	
	@Override
	protected ReferencedEnvelope getBoundsInternal(Query query) throws IOException {		
		
		if (query.equals(Query.ALL)) {
			ReferencedEnvelope env = new ReferencedEnvelope(reader.getEnvelope(), CRS.getBreddeLengdegrad());
			
			return env;
		}
		else {
			FeatureReader<SimpleFeatureType, SimpleFeature> reader = getReaderInternal(query);
			ReferencedEnvelope bounds = null;
			while (reader.hasNext()) {
				SimpleFeature feature = reader.next();
	            BoundingBox fb = feature.getBounds();
	            if(fb != null) {
	            	if (bounds == null)
	            		bounds = ReferencedEnvelope.reference(fb);
	            	else 
	            		bounds.expandToInclude(ReferencedEnvelope.reference(fb));
	            }
			}
			reader.close();
			return bounds;
		}
	}


	@Override
	protected int getCountInternal(Query query) throws IOException {
		if (query.equals(Query.ALL)) {
			return reader.getCount();
		}
		else {
			FeatureReader<SimpleFeatureType, SimpleFeature> reader = getReaderInternal(query);
			int i = 0;
			while (reader.hasNext()) {
				reader.next();
				i++;
			}
			reader.close();
			return i;
		}
	}

	/**
	 * Try to report a user-friendly exception.
	 */
	@Override
	protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(Query query) throws IOException {
		try {
			return new StationPointReader(buildFeatureType(), reader);
		}
		catch (IOException ie) {
			String message = ie.getMessage();
			
			if (message != null && message.startsWith("Given key") && message.endsWith("is missing.")) {
				throw new IOException("Store " + this.getEntry().getTypeName() + " represents a session that has expired.", ie);
			}
			else {
				throw ie;
			}
		}
	}
}
