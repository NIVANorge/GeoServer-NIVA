package niva.aquamonitor.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import org.junit.Assert;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.geometry.jts.ReferencedEnvelope;


/**
 * Tests for checking the StationPointSource used at a Project AquaMonitor site.
 * 
 * @author Roar Brænden, NIVA
 *
 */
public class ProjectStationPointSourceTest {
	
	private final String TEST_USER = "mjøsa";
	
	private ProjectUserDataStore dataStore;
	
	@Before
	public void setUp() throws IOException{
		ProjectUserDataStoreFactory factory = new ProjectUserDataStoreFactory();
		Map<String, Serializable> params = new HashMap<>();
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
		Assert.assertNotNull(bounds);
	}
	
	
	@Test
	public void testReaderWithBBox() throws Exception {
		final Query query = new Query();
		query.setFilter(CQL.toFilter("BBOX(the_geom, 10.980, 60.818, 10.981, 60.819)"));

		try (FeatureReader<SimpleFeatureType, SimpleFeature> reader = getStationPoints().getReader(query)) {
    		int i = 0;
    		while (reader.hasNext()) {
    			reader.next();
    			i++;
    		}
    		Assert.assertEquals(1, i);
		}
		
		try (FeatureReader<SimpleFeatureType, SimpleFeature> reader = getStationPoints()
		        .getReader(CQL.toFilter("BBOX(the_geom, 10.227497265625, 60.490836875, 10.227902734374998, 60.491243125)"))) {
		    Assert.assertFalse("Filteret skal ikke finne noen lokaliteter.", reader.hasNext());
		}
	}
	
	@Test
	public void testCountWithFilter() throws Exception  {
		Query query = new Query();
		query.setFilter(CQL.toFilter("STATION_CODE='M072'"));
		int count = getStationPoints().getCount(query);
		
		Assert.assertEquals(1, count);
	}
	
	
	@Test
	public void testBoundsWithFilter() throws Exception  {
		StationPointSource source = (StationPointSource) getStationPoints();
		Query query = new Query();
		query.setFilter(CQL.toFilter("STATION_CODE='M072' AND BBOX(the_geom, 10.78, 60.76, 10.79, 60.77)"));
		ReferencedEnvelope env = source.getBounds(query);
		Assert.assertNotNull(env);
	}

	
	@After
	public void stop() throws IOException {
		if (this.dataStore != null ) {
			this.dataStore.dispose();
		}
	}
}
