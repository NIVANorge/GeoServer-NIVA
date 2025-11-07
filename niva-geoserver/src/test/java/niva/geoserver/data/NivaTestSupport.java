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
import org.geotools.api.referencing.FactoryException;

/**
 * Base class for NIVA related Geoserver setup.
 * 
 * @author Roar Brænden, NIVA
 *
 */
public class NivaTestSupport extends GeoServerSystemTestSupport {
    
    private static final String HOST_ADDRESS_KEY = "AQUAMONITOR_HOST_ADDRESS";
    
    public static String getAquaMonitorHost() {
        return System.getProperty(HOST_ADDRESS_KEY) != null 
                ? System.getProperty(HOST_ADDRESS_KEY) 
                : System.getenv(HOST_ADDRESS_KEY) != null 
                    ? System.getenv(HOST_ADDRESS_KEY) 
                            : "https://test-aquamonitor.niva.no/";
    }
	
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
	
    public static StoreInfo addAquaMonitorStore(Catalog catalog, String name, Map<String, Serializable> params) {
		StoreInfo store = new DataStoreInfoImpl(catalog);
		store.setName(name);
		store.setWorkspace(catalog.getWorkspaceByName("no.niva.aquamonitor"));
		store.setEnabled(true);
		store.getConnectionParameters().putAll(params);

		catalog.add(store);
		return store;
    }
	
	protected StoreInfo addAquaMonitorStore(String name, Map<String, Serializable> params) {
		return addAquaMonitorStore(getCatalog(), name, params);
	}
	
	protected LayerInfo addStationLayer(StoreInfo store, String name) throws FactoryException {
			return addLayer(addFeatureLayer(store, "no.niva.aquamonitor", name, "STATION_POINTS", "EPSG:4326"), null);
	}
	
	public static LayerInfo addStationDatatypesLayer(Catalog catalog, StoreInfo store, StyleInfo style, String name) throws FactoryException {
		return addLayer(catalog,addFeatureLayer(catalog, store, "no.niva.aquamonitor", name, "STATION_DATATYPE_POINTS", "EPSG:4326"), style);
	}
	
	protected LayerInfo addStationDatatypesLayer(StoreInfo store, String name) throws FactoryException {
		return addStationDatatypesLayer(getCatalog(), store, null, name);
	}
	
	protected LayerInfo addStationDatatypesLayer(StoreInfo store, StyleInfo style, String name) throws FactoryException {
		return addStationDatatypesLayer(getCatalog(), store, style, name);
	}
	
	public static FeatureTypeInfo addFeatureLayer(Catalog catalog, StoreInfo store, String prefix, String name, String nativeName, String srs) throws FactoryException {
		NamespaceInfo namespace = catalog.getNamespaceByPrefix(prefix);
		
		FeatureTypeInfo resourceCat = new FeatureTypeInfoImpl(catalog);
		resourceCat.setStore(store);
		resourceCat.setNamespace(namespace);
		resourceCat.setName(name);
		resourceCat.setNativeName(nativeName);
		resourceCat.setNativeCRS(org.geotools.referencing.CRS.decode(srs));
		resourceCat.setSRS(srs);
		resourceCat.setProjectionPolicy(ProjectionPolicy.REPROJECT_TO_DECLARED);
		resourceCat.setEnabled(true);
		
		catalog.add(resourceCat);
		return resourceCat;
	}
	
	protected FeatureTypeInfo addFeatureLayer(StoreInfo store, String prefix, String name, String nativeName, String srs) throws FactoryException {
		return addFeatureLayer(getCatalog(), store, prefix, name, nativeName, srs);
	}
	
	public static LayerInfo addLayer(Catalog catalog, FeatureTypeInfo resource, StyleInfo style) {
		final LayerInfo layerCat = new LayerInfoImpl();
		layerCat.setResource(resource);
		layerCat.setName(resource.getName());
		layerCat.setDefaultStyle(style);
		layerCat.setEnabled(true);
		
		catalog.add(layerCat);
		return layerCat;
	}
	
	protected LayerInfo addLayer(FeatureTypeInfo resource, StyleInfo style) {
		return addLayer(getCatalog(), resource, style);
	}
}
