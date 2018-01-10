package niva.geoserver.process;


import org.opengis.feature.simple.SimpleFeature;

public interface AggregateFeatures {
	void add(SimpleFeature feature);
	
	SimpleFeature aggregate();
	
	void clear();
	
	boolean isEmpty();
}
