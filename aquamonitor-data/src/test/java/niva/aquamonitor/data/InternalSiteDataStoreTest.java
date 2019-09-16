package niva.aquamonitor.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.store.ContentFeatureSource;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeDescriptor;

public class InternalSiteDataStoreTest {
	
	private SiteDataStore dataStore;
	
	@Before
	public void setUp() throws IOException{
		SiteDataStoreFactory factory = new SiteDataStoreFactory();
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("dbtype", "aquamonitor-site");
		params.put("host", "https://test-aquamonitor.niva.no/");
		params.put("site", "Intern");
		this.dataStore = (SiteDataStore)factory.createDataStore(params);
	}
	
	
	@Test
	public void getAllDatatypePointsSchema() throws Exception {
		ContentFeatureSource source = this.dataStore.getFeatureSource("STATION_DATATYPE_POINTS");
		
		AttributeDescriptor desc = source.getSchema().getDescriptor("Water");
		assertNotNull(desc);
		assertEquals("class java.lang.Integer", desc.getType().getBinding().toString());
	}
	
	@Test
	public void getAllDatatypePoints() throws Exception {
		ContentFeatureSource source = this.dataStore.getFeatureSource("STATION_DATATYPE_POINTS");
		
		SimpleFeatureIterator features = source.getFeatures().features();
		
		assertTrue(features.hasNext());
		SimpleFeature feature = features.next();
		System.out.println(feature.toString());
		
	}
}
