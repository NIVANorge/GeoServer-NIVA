package niva.aquamonitor.data;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;


import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import niva.aquamonitor.data.ws.StationPointCargo;
import niva.aquamonitor.data.ws.StationPointReader;
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
	
	private StationPointReader reader;
	
	
	public StationPointSource(ContentEntry entry, StationPointReader reader) {
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
		CoordinateReferenceSystem crs = CRS.getLengdeBreddegrad();
		
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
			return new ReferencedEnvelope(reader.getEnvelope(), CRS.getLengdeBreddegrad());
		}
		
		FeatureReader<SimpleFeatureType, SimpleFeature> reader = getReaderInternal(query);
		try {
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
			return bounds;
		}
		finally {
			reader.close();
		}
	}


	@Override
	protected int getCountInternal(Query query) throws IOException {
		if (query.equals(Query.ALL)) {
			return reader.getCount();
		}
		
		FeatureReader<SimpleFeatureType, SimpleFeature> reader = getReaderInternal(query);
		try {
			int i = 0;
			while (reader.hasNext()) {
				reader.next();
				i++;
			}
			
			return i;
		}
		finally {
			reader.close();
		}
	}

	/**
	 * Try to report a user-friendly exception.
	 */
	@Override
	protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(Query query) throws IOException {
		try {
			return new StationPointFeatureReader();
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
	
	/**
	 * Internal class to wrap a Iterator on the json response from NIVA.
	 * 
	 * @author Roar Brænden, NIVA
	 *
	 */
	private class StationPointFeatureReader implements FeatureReader<SimpleFeatureType, SimpleFeature> {

		final private Iterator<StationPointCargo> iter;
		
		final private GeometryFactory fact = JTSFactoryFinder.getGeometryFactory();
		
		private int actual = 0;
		
		
		public StationPointFeatureReader() throws IOException {
			this.iter = reader.iterator();
		}
		

		@Override
		public SimpleFeatureType getFeatureType() {
			return StationPointSource.this.getSchema();
		}

		@Override
		public SimpleFeature next() throws IOException, NoSuchElementException {
			
			final StationPointCargo spc = iter.next();
			final SimpleFeatureBuilder builder = new SimpleFeatureBuilder(getFeatureType());

			builder.add(this.fact.createPoint(new Coordinate(spc.longitude, spc.latitude)));
			builder.add(spc.samplePointId);
			builder.add(spc.latitude);
			builder.add(spc.longitude);
			builder.add(spc.projectId);
			builder.add(spc.projectName);
			builder.add(spc.stationId);
			builder.add(spc.stationTypeId);
			builder.add(spc.stationType);
			builder.add(spc.stationCode);
			builder.add(spc.stationName);
			builder.add(spc.fullStationName);
			return builder.buildFeature(String.valueOf(++actual));
		}

		@Override
		public boolean hasNext() throws IOException {
			return iter.hasNext();
		}

		@Override
		public void close() throws IOException {
		}
	}
}
