package niva.geotools.data.naturbasen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NaturbasenSourceTest {
	
	private DataStore store = null;
	private SimpleFeatureSource source;
	
	@Before
	public void setup() throws Exception {
		store = DataStoreFinder.getDataStore(NaturbasenStoreFactoryTest.PARAMS);
		source = store.getFeatureSource("flate_aktiv");
	}
	
	@After
	public void close() {
		if (store != null)
			store.dispose();
	}

	@Test
	public void getCount() throws Exception {
		
		int i = source.getCount(Query.ALL);
		assertEquals(false, i == 0);
		
		Query query = new Query();
		query.setTypeName("flate_aktiv");
		query.setFilter(CQL.toFilter("NATURTYPE_ID='I11'"));
		
		int j = source.getCount(query);
		assertEquals(true, j < i);
		
	}
	
	@Test
	public void getBounds() throws Exception {
		
		ReferencedEnvelope env = source.getBounds();
		assertNotNull(env);
	}
	
}
