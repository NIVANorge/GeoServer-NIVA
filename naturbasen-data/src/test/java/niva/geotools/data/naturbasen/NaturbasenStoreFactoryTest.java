package niva.geotools.data.naturbasen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.HashMap;


import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.junit.Test;

public class NaturbasenStoreFactoryTest {
	
	static final HashMap<String, Serializable> PARAMS;
	static {
		PARAMS = new HashMap<String, Serializable>();
		PARAMS.put(NaturbasenStoreFactory.PARAMETERS[0].key, (String)NaturbasenStoreFactory.PARAMETERS[0].sample);
		PARAMS.put(NaturbasenStoreFactory.PARAMETERS[1].key, "etna;nivabase");
		PARAMS.put(NaturbasenStoreFactory.PARAMETERS[2].key, "BM");
		PARAMS.put(NaturbasenStoreFactory.PARAMETERS[3].key, 3);
		PARAMS.put(NaturbasenStoreFactory.PARAMETERS[4].key, 1);	
	}
	
	@Test 
	public void notInterrupting() throws Exception  {
		NaturbasenStoreFactory factory = new NaturbasenStoreFactory();
		assertEquals(false, factory.canProcess(new HashMap<String, Serializable>()));
		
		try {
			factory.createDataStore(null);
			fail("Skal ikke godta denne");
		}
		catch (IllegalArgumentException ie) {}
	}
	
	
	
	@Test
	public void createDataStoreDirect() throws Exception {
		NaturbasenStoreFactory factory = new NaturbasenStoreFactory();
		DataStore store = factory.createDataStore(PARAMS);
		
		assertNotNull(store);
		
		store.dispose();
	}
	
	@Test
	public void createDataStoreExternal() throws Exception {
		DataStore store = DataStoreFinder.getDataStore(PARAMS);
		
		assertEquals("Features from NaturbasenStore", store.getInfo().getDescription());
		
		store.dispose();
	}
	
	@Test
	public void isAvailable() throws Exception {
		NaturbasenStoreFactory factory = new NaturbasenStoreFactory();
		
		assertEquals(true, factory.isAvailable());
	}
}
