package niva.vannmiljo.data;
import static org.geotools.data.arcgisrest.ArcGISRestDataStoreFactory.NAMESPACE_PARAM;

import java.io.Serializable;
import java.util.Collections;

import org.geotools.data.DataStore;
import org.junit.Test;

public class VannmiljoDataStoreFactoryTest {
	
	@Test
	public void createDataStoreDirectly() throws Exception {
		DataStore store = new VannmiljoDataStoreFactory().createDataStore(Collections.singletonMap(NAMESPACE_PARAM.key, (Serializable)"http://www.aquamonitor.no/"));
		System.out.println(store.getInfo().getTitle());
		store.getNames().forEach((n) -> System.out.println(n.getLocalPart()));
	}

}
