package niva.aquamonitor.data;

import niva.geotools.referencing.CRS;

import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.store.ContentFeatureCollection;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.process.vector.PointStackerProcess;
import org.junit.Ignore;
import org.junit.Test;

public class PointStackProcessTest {


	@Test
	@Ignore
	public void stackLoimpStations() throws Exception {
		ProjectUserDataStore dataStore = new ProjectUserDataStore("LOIMP");
		ContentFeatureSource stationSource = dataStore.getFeatureSource(ProjectUserDataStore.DEFAULT_LAYERS[0]);
		ContentFeatureCollection collection = stationSource.getFeatures(Query.ALL);
		System.out.println(collection.size());
		
		PointStackerProcess process = new PointStackerProcess();
		
		ReferencedEnvelope outputEnv = new ReferencedEnvelope(2.9, 38.3, 57.2, 79.2, CRS.getBreddeLengdegrad());
		
		SimpleFeatureCollection stacked = process.execute(collection, 20, false, false, PointStackerProcess.PreserveLocation.Superimposed, outputEnv, 600, 600, null);
		
		System.out.println(stacked.size());
		
	}
}
