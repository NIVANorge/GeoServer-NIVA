package niva.geoserver.wfs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.junit.Test;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import niva.geoserver.data.NivaTestSupport;

/**
 * Test to control if the NIVA specific download format shapefile works.
 * 
 * 
 * @author Roar Brænden, NIVA
 *
 */
public class ShapefileDownloadTest extends NivaTestSupport {
    
 
    private final static String EXPECTED_ARCMAP_PROJ = "GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\","
            + "SPHEROID[\"WGS_1984\",6378137.0,298.257223563]]," + "PRIMEM[\"Greenwich\",0.0],"
            + "UNIT[\"Degree\",0.0174532925199433]]";

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testDownloadShapefile() throws Exception {
        final Catalog catalog = getCatalog();
        
        final StoreInfo store = new DataStoreInfoImpl(catalog);
        store.setName("Intern_download");
        store.setWorkspace(catalog.getWorkspaceByName("no.niva.aquamonitor"));
        
        final Map<String, Serializable> params = store.getConnectionParameters();
        params.put("namespace", new URI("http://www.aquamonitor.no/"));
        params.put("dbtype", "aquamonitor-site");
        params.put("host", getAquaMonitorHost());
        params.put("site", "Intern");
        
        addAquaMonitorStore("Intern_download", params);
        addStationLayer(catalog.getDataStoreByName("no.niva.aquamonitor", "Intern_download"), "Intern_download_stations");
        
        final String xml = "<wfs:GetFeature service=\"WFS\" version=\"1.1.0\" "
                + "xmlns:ogc=\"http://www.opengis.net/ogc\" "
                + "xmlns:wfs=\"http://www.opengis.net/wfs\" "
                + "outputFormat=\"shape-zip\">"
                + "<wfs:Query typeName=\"no.niva.aquamonitor:Intern_download_stations\" srsName=\"EPSG:4326\">"
                + "<ogc:Filter>"
                + "<ogc:PropertyIsEqualTo><ogc:PropertyName>no.niva.aquamonitor:STATION_ID</ogc:PropertyName>"
                + "<ogc:Literal>3570</ogc:Literal></ogc:PropertyIsEqualTo>"
                + "</ogc:Filter></wfs:Query></wfs:GetFeature>";
        
        final String path = "wfs?typeName=no.niva.aquamonitor:Intern_download_stations&format_options=SHAPEFILE:download.shp;PRJFILEFORMAT:ESRI";
        
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
                                                .getFeatures()
                                                .features()){
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
}
