package niva.aquamonitor.data;


import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import niva.aquamonitor.data.ws.DatatypeCargo;
import niva.aquamonitor.data.ws.DatatypeReader;

import org.geotools.data.FeatureReader;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
/**
 * A feature reader for points that have attributes with information about datatype handled at the given location.
 * 
 * @author Roar Brænden, NIVA
 *
 */
public class DatatypePointReader implements FeatureReader<SimpleFeatureType, SimpleFeature> {
	
	private SimpleFeatureType schema;
	
	private Iterator<DatatypeCargo> iter;
	
	private SimpleFeatureBuilder builder;
	
	private int actual = 0;
	
	private GeometryFactory fact;
	
	private String[] datatypes;

	
	/**
	 * A constructor that takes the final schema and a reader into a json.
	 * @param schema
	 * @param reader
	 * @throws IOException
	 */
	public DatatypePointReader(SimpleFeatureType schema, DatatypeReader reader) throws IOException {
		this.schema = schema;
		this.iter = reader.iterator();
		this.fact = JTSFactoryFinder.getGeometryFactory();
		
		this.builder = new SimpleFeatureBuilder(schema);
		
		this.datatypes = new String[schema.getAttributeCount() - 4];
		for (int i = 4; i < schema.getAttributeCount(); i++) {
			this.datatypes[i - 4] = schema.getDescriptor(i).getLocalName();
		}
	}
	

	/**
	 * Returns the schema for the features of this reader.
	 */
	@Override
	public SimpleFeatureType getFeatureType() {
		return this.schema;
	}

	/**
	 * Gets the next element of the json, and returns a feature.
	 */
	@Override
	public SimpleFeature next() throws IOException, NoSuchElementException {

		DatatypeCargo dpc = iter.next();

		synchronized (builder) {
			builder.add(this.fact.createPoint(new Coordinate(dpc.longitude, dpc.latitude)));
	
			builder.add(dpc.stationId);
			builder.add(dpc.stationTypeId);
			builder.add(dpc.stationType);
			
			for (int i = 0; i < this.datatypes.length; i++) {
				builder.add(hasDatatype(dpc, this.datatypes[i]));
			}
			final SimpleFeature feature = builder.buildFeature(String.valueOf(++actual));
			builder.notify();
			
			return feature;
		}
	}
	
	private int hasDatatype(DatatypeCargo d, String datatype)  {
		int i = 0;
		while (i < d.datatypes.length) {
			if (d.datatypes[i++].equals(datatype))
				return 1;
		}
		return 0;
	}

	/**
	 * Returns the hasNext from the json-reader.
	 */
	@Override
	public boolean hasNext() throws IOException {
		return this.iter.hasNext();
	}


	/**
	 * Must be implemented, but no need for it here.
	 */
	@Override
	public void close() throws IOException {

	}

}
