package niva.geoserver.data;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;


import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.ProjectionPolicy;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.impl.DataStoreInfoImpl;
import org.geoserver.catalog.impl.FeatureTypeInfoImpl;
import org.geoserver.catalog.impl.LayerInfoImpl;
import org.geoserver.data.test.SystemTestData;
import org.geoserver.test.GeoServerSystemTestSupport;
import org.opengis.referencing.FactoryException;

/**
 * Base class for NIVA related Geoserver setup.
 * 
 * 
 * 
 * @author Roar Brænden, NIVA
 *
 */
public class NivaTestSupport extends GeoServerSystemTestSupport {
	
	@Override
	protected void onSetUp(SystemTestData testData) throws Exception {
		Catalog catalog = getCatalog();
		testData.addWorkspace("no.niva.aquamonitor", "http://www.aquamonitor.no/", catalog);
		testData.addWorkspace("no.norgedigitalt", "http://www.norgedigitalt.no", catalog);
		
		File dataDir = new File(testData.getDataDirectoryRoot(), "data");
        if (!dataDir.exists()) {
        	dataDir.mkdir();
        }
        
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
        org.geoserver.util.IOUtils.copy(input, file);
    }
    
    private String get4326_ESRI_WKTContent() {
        return "GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\","
                + "SPHEROID[\"WGS_1984\",6378137.0,298.257223563]]," + "PRIMEM[\"Greenwich\",0.0],"
                + "UNIT[\"Degree\",0.0174532925199433]]";
    }
	
	
	protected StoreInfo addAquaMonitorStore(String name, Map<String, Serializable> params) {
		Catalog catalog = getCatalog();
		
		StoreInfo store = new DataStoreInfoImpl(catalog);
		store.setName(name);
		store.setWorkspace(catalog.getWorkspaceByName("no.niva.aquamonitor"));
		store.setEnabled(true);
		store.getConnectionParameters().putAll(params);

		catalog.add(store);
		return store;
	}
	
	protected LayerInfo addStationLayer(StoreInfo store, String name) {
		try {
			return addLayer(addFeatureLayer(store, name, "STATION_POINTS", "EPSG:4326"), null);
		} catch (FactoryException e) {
			throw new RuntimeException("Something is missing in your setup.");
		}
	}
	
	protected FeatureTypeInfo addFeatureLayer(StoreInfo store, String name, String nativeName, String srs) throws FactoryException {
		final Catalog catalog = getCatalog();
		
		final NamespaceInfo namespace = catalog.getNamespaceByPrefix("no.niva.aquamonitor");
		
		FeatureTypeInfo resourceCat = new FeatureTypeInfoImpl(catalog);
		resourceCat.setStore(store);
		resourceCat.setNamespace(namespace);
		resourceCat.setName(name);
		resourceCat.setNativeName(nativeName);
		resourceCat.setNativeCRS(org.geotools.referencing.CRS.decode(srs));
		resourceCat.setSRS(srs);
		resourceCat.setProjectionPolicy(ProjectionPolicy.FORCE_DECLARED);
		resourceCat.setEnabled(true);
		
		catalog.add(resourceCat);
		return resourceCat;
		
	}
	
	protected LayerInfo addLayer(FeatureTypeInfo resource, StyleInfo style) {
		final Catalog catalog = getCatalog();
		
		LayerInfo layerCat = new LayerInfoImpl();
		layerCat.setResource(resource);
		layerCat.setName(resource.getName());
		layerCat.setDefaultStyle(style);
		layerCat.setEnabled(true);
		
		catalog.add(layerCat);
		return layerCat;
	}

}
