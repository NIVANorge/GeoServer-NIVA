package niva.geotools.data.naturbasen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.io.IOException;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.text.cql2.CQL;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

public class NaturbasenStoreTest {
	

	
	private DataStore getStore() throws IOException {
		return DataStoreFinder.getDataStore(NaturbasenStoreFactoryTest.PARAMS);
	}
	

	@Test
	public void getTypeNames() throws Exception {
		DataStore store = getStore();
		assertNotNull(store);
		
		String[] names = store.getTypeNames();
		assertEquals("flate_aktiv", names[0]);
		
		assertEquals("flate_I01_aktiv", names[6]);
		store.dispose();
	}
	
	@Test
	public void getSource() throws Exception {
		DataStore store = getStore();
		assertNotNull(store);
		
		SimpleFeatureSource source = store.getFeatureSource("flate_aktiv");
		assertNotNull(source);
		store.dispose();
	}
	
	@Test
	public void queryFeatureReader() throws Exception {
		DataStore store = getStore();
		assertNotNull(store);
		
		Query query = new Query();
		query.setTypeName("flate_aktiv");
		query.setFilter(CQL.toFilter("NATURTYPE_ID='I11'"));
		FeatureReader<SimpleFeatureType, SimpleFeature> reader = null;
		try {
			reader = store.getFeatureReader(query, Transaction.AUTO_COMMIT);
			assertTrue(reader.hasNext());
		}
		finally {
			if (reader != null)
				reader.close();
		}
		
		FeatureReader<SimpleFeatureType, SimpleFeature> reader2 = null;
		query.setTypeName("punkt_aktiv");
		try {
			reader2 = store.getFeatureReader(query, Transaction.AUTO_COMMIT);
			assertTrue(reader2.hasNext());
			
			SimpleFeature feature = reader2.next();
			assertEquals("GeometryTypeImpl SHAPE<Point>", feature.getFeatureType().getGeometryDescriptor().getType().toString());
		}
		finally {
			if (reader2 != null)
				reader2.close();
		}
		
		store.dispose();
	}
	
	
	@Test
	public void queryFeatureReaderI01() throws Exception {
		DataStore store = getStore();
		assertNotNull(store);
		
		Query query = new Query();
		query.setTypeName("flate_I01_aktiv");

		FeatureReader<SimpleFeatureType, SimpleFeature> reader = null;
		FeatureReader<SimpleFeatureType, SimpleFeature> reader2 = null;
		try {
			query.setFilter(Filter.INCLUDE);
			reader = store.getFeatureReader(query, Transaction.AUTO_COMMIT);
			assertTrue(reader.hasNext());
			
			query.setFilter(CQL.toFilter("NATURTYPE_ID='I11'"));
			reader2 = store.getFeatureReader(query, Transaction.AUTO_COMMIT);
			assertFalse(reader2.hasNext());
		}
		finally {
			if (reader != null)
				reader.close();
			if (reader2 != null)
				reader2.close();
		}
		
		store.dispose();
	}
	
	@Test
	public void getSchema() throws Exception {
		DataStore store = getStore();
		assertNotNull(store);
		
		try {
			store.getSchema("NIVA_GEOMETRY.BIOMANGFOLD_F");
			fail("Shouldn't allow this.");
		}
		catch (IOException ie) {}
		
		SimpleFeatureType schema = store.getSchema("flate_aktiv");
		assertEquals("flate_aktiv", schema.getName().getLocalPart());

		store.dispose();
	}
}
