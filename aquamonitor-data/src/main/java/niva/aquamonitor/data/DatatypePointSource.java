package niva.aquamonitor.data;

import java.io.IOException;

import niva.aquamonitor.data.ws.DatatypeReader;
import niva.geotools.referencing.CRS;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;

import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;


import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.BoundingBox;

import org.locationtech.jts.geom.Point;

/**
 * A Feature Source for points that represents a station with the datatypes that are handled at the location.
 * @author Roar Brænden, NIVA
 *
 */
public class DatatypePointSource extends ContentFeatureSource {

	
	private DatatypeReader reader;
	private String[] datatypes;
	
	/**
	 * The constructor takes a ContentEntry, a reader and the name of all datatypes.
	 * @param entry
	 * @param reader
	 * @param datatypes
	 */
	public DatatypePointSource(ContentEntry entry, DatatypeReader reader, String[] datatypes) {
		super(entry, Query.ALL);
		this.reader = reader;
		
		this.datatypes = datatypes;
	}

	/**
	 * Creates a schema for this feature source.
	 */
	@Override
	protected SimpleFeatureType buildFeatureType() throws IOException {
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName(entry.getName());
		
		builder.setCRS(CRS.getLengdeBreddegrad());
		builder.add("the_geom", Point.class);
		
		builder.add("STATION_ID", Integer.class);
		builder.add("STATION_TYPE_ID", Integer.class);
		builder.add("STATION_TYPE", String.class);
		
		for (int i = 0; i < this.datatypes.length; i++)
			builder.add(this.datatypes[i], Integer.class);
		
		return builder.buildFeatureType();
	}
	

	/**
	 * Returns the bounds
	 */
	@Override
	protected ReferencedEnvelope getBoundsInternal(Query query) throws IOException {
		if (query.equals(Query.ALL)) {
			return new ReferencedEnvelope(reader.getEnvelope(), CRS.getLengdeBreddegrad());
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
			return (bounds == null ? ReferencedEnvelope.EVERYTHING : bounds);
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

	@Override
	protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(Query query) throws IOException {
		return  new DatatypePointReader(buildFeatureType(), reader);
	}
	
}
