package niva.aquamonitor.data;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import niva.aquamonitor.data.ws.StationPointCargo;

import org.geotools.data.FeatureReader;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;


public class StationPointReader implements FeatureReader<SimpleFeatureType, SimpleFeature> {
	
	final private SimpleFeatureType schema;
	final private Iterator<StationPointCargo> iter;
	final private SimpleFeatureBuilder builder;
	final private GeometryFactory fact;
	
	private int actual = 0;
	
	
	public StationPointReader(SimpleFeatureType schema, niva.aquamonitor.data.ws.StationPointReader reader) throws IOException {
		this.schema = schema;
		this.iter = reader.iterator();
		
		this.builder = new SimpleFeatureBuilder(schema);
		this.fact = JTSFactoryFinder.getGeometryFactory();
	}
	

	@Override
	public SimpleFeatureType getFeatureType() {
		return this.schema;
	}

	@Override
	public SimpleFeature next() throws IOException, NoSuchElementException {
		
		StationPointCargo spc = iter.next();

		synchronized (builder) {
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
	
			final SimpleFeature feature =  builder.buildFeature(String.valueOf(++actual));
			builder.notify();
			return feature;
		}
	}

	@Override
	public boolean hasNext() throws IOException {
		return iter.hasNext();
	}

	@Override
	public void close() throws IOException {
	}
}
