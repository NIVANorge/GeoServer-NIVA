package niva.geoserver.printing;


import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.springframework.mock.web.MockHttpServletResponse;

import com.google.common.io.Files;

import org.geoserver.catalog.Catalog;
import org.geoserver.data.test.SystemTestData;

import org.geotools.TestData;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import niva.geoserver.data.NivaTestSupport;


/**
 * The images exported from AquaMonitor SI have some anomalies within the color tables.
 * 
 * @author Roar Brænden, NIVA
 *
 */
public class SurveillanceInformationMapExportTest extends NivaTestSupport {
	
	
	@Override
	protected void onSetUp(SystemTestData systemData) throws Exception {
		super.onSetUp(systemData);
		final Catalog catalog = getCatalog();
		
        systemData.addStyle("Skjematisk halvmork linje",
				  "Skjematisk halvmork linje.sld",
				  this.getClass(), catalog);
        
        systemData.addStyle("border",
				  "border.sld",
				  this.getClass(), catalog);
        
		File dataDir = new File(systemData.getDataDirectoryRoot(), "data");
		File testDir = TestData.file(this, null);
		Arrays.stream(testDir.listFiles()).forEach((file)->{
			try {
				Files.copy(file, new File(dataDir, file.getName()));
			} catch (IOException ex) {
				ex.printStackTrace();
			}});
		
		final HashMap<String, Serializable> shpParams = new HashMap<String, Serializable>();
		shpParams.put("dbtype", "shapefile");
		shpParams.put("url", new File(dataDir, "provinser.shp").toURI().toURL());

		addLayer(addFeatureLayer(addAquaMonitorStore("Myanmar_provins", shpParams),
								"myanmar_provins", 
								"provinser", 
								"EPSG:4326"), 
				catalog.getStyleByName("Skjematisk halvmork linje"));
		
		final HashMap<String, Serializable> propParams = new HashMap<String, Serializable>();
		propParams.put("directory", new File(dataDir, "extent.properties").getAbsolutePath());
		
		addLayer(addFeatureLayer(addAquaMonitorStore("Property files", propParams),
								"extent", "extent", "EPSG:900913"),
				catalog.getStyleByName("border"));
	}
	
	/**
	 * Test for an anomalie with the color returned from OpenStreetMap.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSurveillanceInformationMapExport() throws Exception {
		String spec = null;
		try (InputStream is = getClass().getClassLoader().getResourceAsStream("niva/geoserver/printing/SpecNew.txt")) {
			StringBuilder builder = new StringBuilder();
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
				String line;
				while ((line = br.readLine()) != null) {
					builder.append(line);
				}
			}
			spec = builder.toString();
		}
		assertNotNull(spec);
        MockHttpServletResponse resp = postAsServletResponse("rest/printing", spec, "application/json");
        assertEquals("We got a http status code of:" + resp.getStatus(), 200, resp.getStatus());
        
        InputStream is = getBinaryInputStream(resp);
        BufferedImage image = ImageIO.read(is);
		assertNotNull("Didn't get an image.", image);

		ImageIO.write(image, "png", new File("C:/temp/myanmar.png"));

	}

}
