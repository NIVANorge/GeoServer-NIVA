package niva.geoserver.data;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;

import niva.geotools.referencing.CRS;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.ProjectionPolicy;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.impl.DataStoreInfoImpl;
import org.geoserver.catalog.impl.FeatureTypeInfoImpl;
import org.geoserver.catalog.impl.LayerInfoImpl;
import org.geoserver.data.test.SystemTestData;
import org.geoserver.test.GeoServerSystemTestSupport;


public class NivaTestSupport extends GeoServerSystemTestSupport {
	
	@Override
	protected void onSetUp(SystemTestData testData) throws Exception {
		Catalog catalog = getCatalog();
		testData.addWorkspace("no.niva.aquamonitor", "http://www.aquamonitor.no/", catalog);
		testData.addWorkspace("no.norgedigitalt", "http://www.norgedigitalt.no", catalog);
		
		File dataDir = new File(testData.getDataDirectoryRoot(), "data");
        if (!dataDir.exists())
        	dataDir.mkdir();
        
        setupESRIPropertyFile();
	}
	
	
	
    private void setupESRIPropertyFile() throws IOException {
        String esri_properties = "4326=" + get4326_ESRI_WKTContent();
        InputStream input = new ByteArrayInputStream(esri_properties.getBytes());
        File directory = getResourceLoader().findOrCreateDirectory("user_projections");
        File file = new File(directory, "esri.properties");
        if (file.exists()) {
            file.delete();
        }
        org.geoserver.data.util.IOUtils.copy(input, file);
    }
    
    private String get4326_ESRI_WKTContent() {
        return "GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\","
                + "SPHEROID[\"WGS_1984\",6378137.0,298.257223563]]," + "PRIMEM[\"Greenwich\",0.0],"
                + "UNIT[\"Degree\",0.0174532925199433]]";
    }
	
	
	protected void addAquaMonitorStore(String name, Map<String, Serializable> params) {
		Catalog catalog = getCatalog();
		
		StoreInfo store = new DataStoreInfoImpl(catalog);
		store.setName(name);
		store.setWorkspace(catalog.getWorkspaceByName("no.niva.aquamonitor"));
		store.setEnabled(true);
		store.getConnectionParameters().putAll(params);

		catalog.add(store);
		
	}
	
	protected void addStationLayer(StoreInfo storeCat, String name) {
		Catalog catalog = getCatalog();
		
		NamespaceInfo namespace = catalog.getNamespaceByPrefix("no.niva.aquamonitor");
		
		FeatureTypeInfo resourceCat = new FeatureTypeInfoImpl(catalog);
		resourceCat.setStore(storeCat);
		resourceCat.setNamespace(namespace);
		resourceCat.setName(name);
		resourceCat.setNativeName("STATION_POINTS");
		resourceCat.setNativeCRS(CRS.getBreddeLengdegrad());
		resourceCat.setSRS("EPSG:4326");
		resourceCat.setProjectionPolicy(ProjectionPolicy.FORCE_DECLARED);
		resourceCat.setEnabled(true);
		
		catalog.add(resourceCat);
		
		LayerInfo layerCat = new LayerInfoImpl();
		layerCat.setResource(resourceCat);
		layerCat.setName(name);
		layerCat.setEnabled(true);
		
		catalog.add(layerCat);
		
	}

}
