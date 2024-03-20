package niva.geoserver.process;


import org.geotools.api.feature.simple.SimpleFeature;

public interface AggregateFeatures {
	void add(SimpleFeature feature);
	
	SimpleFeature aggregate();
	
	void clear();
	
	boolean isEmpty();
}
