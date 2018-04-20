package niva.aquamonitor.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import niva.aquamonitor.data.StationPointSource;
import niva.aquamonitor.data.ProjectUserDataStore;
import niva.aquamonitor.data.ProjectUserDataStoreFactory;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.After;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentFeatureSource;

import org.geotools.filter.text.cql2.CQL;
import org.geotools.geometry.jts.ReferencedEnvelope;


@Ignore
public class StationPointSourceTest {
	
	private final String TEST_USER = "mjosutfylling";
	
	private ProjectUserDataStore dataStore;
	
	@Before
	public void setUp() throws IOException{
		ProjectUserDataStoreFactory factory = new ProjectUserDataStoreFactory();
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("dbtype", "aquamonitor");
		params.put("user", TEST_USER);
		this.dataStore = (ProjectUserDataStore)factory.createDataStore(params);
	}

	private ContentFeatureSource getStationPoints() throws IOException {
		return this.dataStore.getFeatureSource(ProjectUserDataStore.DEFAULT_LAYERS[0]);
	}
	
	@Test
	public void testGetBounds() throws Exception {
		StationPointSource source = (StationPointSource)getStationPoints();
		ReferencedEnvelope bounds = source.getBounds();
		assertNotNull(bounds);
	}
	
	
	@Test
	public void testReaderWithBBox() throws Exception {
		Query query = new Query();
		query.setFilter(CQL.toFilter("BBOX(the_geom, 11.2417, 60.5480, 11.2419, 60.5481)"));
		
		StationPointSource source = (StationPointSource)getStationPoints();
		FeatureReader<SimpleFeatureType, SimpleFeature> reader = source.getReader(query);
		
		int i = 0;
		while (reader.hasNext()) {
			reader.next();
			i++;
		}
		assertEquals(1, i);
		reader.close();
		
		reader = source.getReader(CQL.toFilter("BBOX(the_geom, 10.227497265625, 60.490836875, 10.227902734374998, 60.491243125)"));
		
		assertFalse("Filteret skal ikke finne noen lokaliteter.", reader.hasNext());
		
		reader.close();
	}
	
	@Test
	public void testCountWithFilter() throws Exception  {
		StationPointSource source = (StationPointSource) getStationPoints();
		Query query = new Query();
		query.setFilter(CQL.toFilter("STATION_CODE='FP3-Nord'"));
		int count = source.getCount(query);
		
		assertEquals(1, count);
	}
	
	
	@Test
	public void testBoundsWithFilter() throws Exception  {
		StationPointSource source = (StationPointSource) getStationPoints();
		Query query = new Query();
		query.setFilter(CQL.toFilter("STATION_CODE='FP3-Nord' AND BBOX(the_geom, 11.24, 60.54, 11.25, 60.55)"));
		ReferencedEnvelope env = source.getBounds(query);
		assertNotNull(env);
		assertEquals("ReferencedEnvelope[11.2418 : 11.2418, 60.54805 : 60.54805]", env.toString());
	}

	
	@After
	public void stop() throws IOException {
		if (this.dataStore != null ) {
			this.dataStore.dispose();
		}
	}
}
