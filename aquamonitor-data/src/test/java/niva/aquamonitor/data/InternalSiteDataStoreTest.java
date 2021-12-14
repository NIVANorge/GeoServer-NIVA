package niva.aquamonitor.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.opengis.feature.type.AttributeDescriptor;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.store.ContentFeatureSource;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the functionality behind the Intern site.
 * 
 * @author Roar Brænden, NIVA
 *
 */
public class InternalSiteDataStoreTest {
	
	private SiteDataStore dataStore;
	
	@Before
	public void setUp() throws IOException{
		SiteDataStoreFactory factory = new SiteDataStoreFactory();
		Map<String, Serializable> params = new HashMap<>();
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
		
		try (SimpleFeatureIterator features = source.getFeatures().features()) {    
	        assertTrue(features.hasNext());
	        features.next();
		};
	}
	
	@Test
	public void getAllStationPoints() throws Exception {
	    ContentFeatureSource source = this.dataStore.getFeatureSource("STATION_POINTS");
	       
        try (SimpleFeatureIterator features = source.getFeatures().features()) {    
            assertTrue(features.hasNext());
            features.next();
        };
	}
}
