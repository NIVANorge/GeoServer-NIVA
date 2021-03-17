package niva.geoserver.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.impl.DataStoreInfoImpl;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.util.URLs;
import org.opengis.feature.simple.SimpleFeature;

import org.locationtech.jts.geom.Point;


import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class WFSTest extends NivaTestSupport {
	
	private final static String TEST_HOST = "https://test-aquamonitor.niva.no/";
	
	private final static String EXPECTED_ARCMAP_PROJ = "GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\","
            + "SPHEROID[\"WGS_1984\",6378137.0,298.257223563]]," + "PRIMEM[\"Greenwich\",0.0],"
            + "UNIT[\"Degree\",0.0174532925199433]]";
	
	
	@Test
	public void testQueryStationOrList() throws Exception {
		Catalog catalog = getCatalog();
		
		StoreInfo store = new DataStoreInfoImpl(catalog);
		store.setName("Intern_query");
		store.setWorkspace(catalog.getWorkspaceByName("no.niva.aquamonitor"));
		
		Map<String, Serializable> params = store.getConnectionParameters();
		params.put("namespace", new URI("http://www.aquamonitor.no/"));
		params.put("dbtype", "aquamonitor-site");
		params.put("host", TEST_HOST);
		params.put("site", "Intern");
		
		addAquaMonitorStore("Intern_query", params);
		addStationLayer(catalog.getDataStoreByName("no.niva.aquamonitor", "Intern_query"), "Intern_query_stations");
		
		String xml =  "<wfs:GetFeature service=\"WFS\" version=\"1.1.0\" "
				+ "xmlns:ogc=\"http://www.opengis.net/ogc\" "
				+ "xmlns:wfs=\"http://www.opengis.net/wfs\">"
				+ "<wfs:Query typeName=\"no.niva.aquamonitor:Intern_query_stations\">"
				+ "<wfs:PropertyName>no.niva.aquamonitor:STATION_ID</wfs:PropertyName>"
				+ "<wfs:PropertyName>no.niva.aquamonitor:STATION_CODE</wfs:PropertyName>"
				+ "<wfs:PropertyName>no.niva.aquamonitor:STATION_NAME</wfs:PropertyName>"
				+ "<ogc:Filter><ogc:Or>"
				+ "<ogc:Or><ogc:PropertyIsEqualTo><ogc:PropertyName>no.niva.aquamonitor:STATION_ID</ogc:PropertyName>"
				+ "<ogc:Literal>3570</ogc:Literal></ogc:PropertyIsEqualTo>"
				+ "<ogc:PropertyIsEqualTo><ogc:PropertyName>no.niva.aquamonitor:STATION_ID</ogc:PropertyName>"
				+ "<ogc:Literal>3571</ogc:Literal></ogc:PropertyIsEqualTo></ogc:Or>"
				+ "<ogc:Or><ogc:PropertyIsEqualTo><ogc:PropertyName>no.niva.aquamonitor:STATION_ID</ogc:PropertyName>"
				+ "<ogc:Literal>3572</ogc:Literal></ogc:PropertyIsEqualTo>"
				+ "<ogc:PropertyIsEqualTo><ogc:PropertyName>no.niva.aquamonitor:STATION_ID</ogc:PropertyName>"
				+ "<ogc:Literal>3573</ogc:Literal></ogc:PropertyIsEqualTo></ogc:Or></ogc:Or>"
				+ "</ogc:Filter></wfs:Query></wfs:GetFeature>";
		

		try (InputStream is = this.post("wfs", xml);
		     BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
			String resp = "";
			String next = reader.readLine();
			while (next != null) {
				resp += next;
				next = reader.readLine();
			}
			
			assertTrue("Result didn't contain station id:3570", resp.contains("<no.niva.aquamonitor:STATION_ID>3570</no.niva.aquamonitor:STATION_ID>"));
			assertTrue("Result doesn't contain project id.", !resp.contains("PROJECT_ID"));
		}
	}
	
	@Test
	public void testProjectSite() throws Exception {
		Catalog catalog = getCatalog();
		
		StoreInfo store = new DataStoreInfoImpl(catalog);
		store.setName("Mjosovervak");
		store.setWorkspace(catalog.getWorkspaceByName("no.niva.aquamonitor"));
		
		Map<String, Serializable> params = store.getConnectionParameters();
		params.put("namespace", new URI("http://www.aquamonitor.no/"));
		params.put("dbtype", "aquamonitor");
		params.put("user", "Mjøsa");
		
		addAquaMonitorStore(store.getName(), params);
		
		addStationLayer(catalog.getDataStoreByName("no.niva.aquamonitor", "Mjosovervak"), "Mjosovervak_stations");
		
		
		String xml;
		
		xml = "<wfs:GetFeature xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
				+ " xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd\" service=\"WFS\""
				+ " version=\"1.1.0\"><wfs:Query srsName=\"EPSG:32632\" typeName=\"feature:Mjosovervak_stations\" /></wfs:GetFeature>";
		

		try (InputStream is = this.post("wfs", xml);
		     BufferedReader reader = new BufferedReader(new InputStreamReader(is))){
			String resp = "";
			String next = reader.readLine();
			while (next != null) {
				resp += next;
				next = reader.readLine();
			}
			assertTrue(resp.contains("<no.niva.aquamonitor:PROJECT_ID>1098</no.niva.aquamonitor:PROJECT_ID>"));
		}
	}
	

	@Test
	public void testDownloadShapefile() throws Exception {
		Catalog catalog = getCatalog();
		
		StoreInfo store = new DataStoreInfoImpl(catalog);
		store.setName("Intern_download");
		store.setWorkspace(catalog.getWorkspaceByName("no.niva.aquamonitor"));
		
		Map<String, Serializable> params = store.getConnectionParameters();
		params.put("namespace", new URI("http://www.aquamonitor.no/"));
		params.put("dbtype", "aquamonitor-site");
		params.put("host", TEST_HOST);
		params.put("site", "Intern");
		
		addAquaMonitorStore("Intern_download", params);
		addStationLayer(catalog.getDataStoreByName("no.niva.aquamonitor", "Intern_download"), "Intern_download_stations");
		
		String xml =  "<wfs:GetFeature service=\"WFS\" version=\"1.1.0\" "
				+ "xmlns:ogc=\"http://www.opengis.net/ogc\" "
				+ "xmlns:wfs=\"http://www.opengis.net/wfs\" "
				+ "outputFormat=\"shape-zip\">"
				+ "<wfs:Query typeName=\"no.niva.aquamonitor:Intern_download_stations\" srsName=\"EPSG:4326\">"
				+ "<ogc:Filter>"
				+ "<ogc:PropertyIsEqualTo><ogc:PropertyName>no.niva.aquamonitor:STATION_ID</ogc:PropertyName>"
				+ "<ogc:Literal>3570</ogc:Literal></ogc:PropertyIsEqualTo>"
				+ "</ogc:Filter></wfs:Query></wfs:GetFeature>";
		
		String path = "wfs?typeName=no.niva.aquamonitor:Intern_download_stations&format_options=SHAPEFILE:download.shp;PRJFILEFORMAT:ESRI";
		
		File temp = null;
		try (InputStream is = getBinaryInputStream(postAsServletResponse(path, xml))){

		    temp = saveUnwrapZip("downloaded", is);
			checkFileExists("download.shp", temp);
			
			byte[] bytes = Files.readAllBytes(new File(temp, "download.prj").toPath());
			String prjC = new String(bytes, Charset.defaultCharset());
			assertEquals("download.prj wasn't as expected.", EXPECTED_ARCMAP_PROJ, prjC);

			final URL downloadFileURL = URLs.extendUrl(URLs.fileToUrl(temp), "download.shp");

	        try (SimpleFeatureIterator iter = new ShapefileDataStore(downloadFileURL)
                                                .getFeatureSource()
                                                .getFeatures().features()){
	            // check that every field has a not null or "empty" value
	            if (iter.hasNext()) {
	                final SimpleFeature f = iter.next();
	                final Object geomObj = f.getDefaultGeometry();
	                assertNotNull(geomObj);
	                
	                assertTrue(Point.class.isAssignableFrom(geomObj.getClass()));
	                
	                final Point pnt = (Point)geomObj;
	                assertTrue("x-coordinate wasn't in expected interval: " + pnt.getX(), pnt.getX() > 10.0 && pnt.getX() < 20.0);
	                assertTrue("y-coordinate wasn't in expected interval: " + pnt.getY(), pnt.getY() > 50.0 && pnt.getY() < 70.0);
	                
	            }
	        }
		}
		finally {
			if (temp != null) {
			    temp.delete();
			}
		}
	}
	
    private File createTempFolder(String prefix) throws IOException {
        File temp = File.createTempFile(prefix, null);

        temp.delete();
        temp.mkdir();
        return temp;
    }

    private void copyStream(InputStream inStream, OutputStream outStream) throws IOException {
        int count = 0;
        byte[] buf = new byte[8192];
        while ((count = inStream.read(buf, 0, 8192)) != -1)
            outStream.write(buf, 0, count);
    }
	
    private File saveUnwrapZip(String temp, InputStream in) throws IOException {
        ZipInputStream zis = new ZipInputStream(in);
        ZipEntry entry = null;

        File tempFolder = createTempFolder(temp);

        while ((entry = zis.getNextEntry()) != null) {
            final String name = entry.getName();
            String outName = tempFolder.getAbsolutePath() + File.separatorChar + name;
            
            FileOutputStream outFile = new FileOutputStream(outName);
            copyStream(zis, outFile);
            outFile.close();
            zis.closeEntry();
        }
        zis.close();

        return tempFolder;
    }
	
	private void checkFileExists(final String fileName, final File zippedFolder) throws IOException {

        File[] files = zippedFolder.listFiles();
        
        for (File f : files) {
            final String name = f.getName();
            if (name.toLowerCase().equals(fileName.toLowerCase())) {
                return;
            }
        }
        fail(fileName + " was not found in the zip-file.");
    }
}
