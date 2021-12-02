package niva.geoserver.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.net.URI;
import java.util.Map;

import niva.aquamonitor.data.SiteDataStore;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.impl.DataStoreInfoImpl;
import org.geotools.data.DataAccess;
import org.junit.Test;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;

public class SiteStoreTest extends NivaTestSupport {

	
	@Test
	public void testCreateStore() throws Exception {
		Catalog catalog = getCatalog();
		
		StoreInfo store = new DataStoreInfoImpl(catalog);
		store.setName("Intern_direct");
		store.setWorkspace(catalog.getWorkspaceByName("no.niva.aquamonitor"));
		
		Map<String, Serializable> params = store.getConnectionParameters();
		params.put("namespace", new URI("http://www.aquamonitor.no/"));
		params.put("dbtype", "aquamonitor-site");
		params.put("host", "https://test-aquamonitor.niva.no/");
		params.put("site", "Intern");
		
		catalog.add(store);
		
		DataStoreInfo storeCat = catalog.getDataStoreByName("no.niva.aquamonitor", "Intern_direct");
		
		assertNotNull(storeCat);

		DataAccess<? extends FeatureType, ? extends Feature> dataStoreObj = storeCat.getDataStore(null);
		
		assertNotNull(dataStoreObj);
		
		if (dataStoreObj instanceof SiteDataStore) {
			SiteDataStore site = (SiteDataStore)dataStoreObj;
			String[] names = site.getTypeNames();
			assertEquals("STATION_POINTS", names[0]);
		}
		else {
			fail("Ikke SiteDataStore");
		}	
	}
}
