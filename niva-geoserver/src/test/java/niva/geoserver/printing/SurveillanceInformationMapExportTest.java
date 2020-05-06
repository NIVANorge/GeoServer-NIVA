package niva.geoserver.printing;


import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.geoserver.data.test.SystemTestData;

import org.springframework.mock.web.MockHttpServletResponse;

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
	
	/**
	 * Call this method before org.geoserver.printing.PrintingServletWrappingController.setInitParameters() is called.
	 * Otherwise this method will throw an exception that file config.yaml exists.
	 */
	@Override
	protected void setUpTestData(SystemTestData testData) throws Exception {
		super.setUpTestData(testData);
		File printDir = new File(testData.getDataDirectoryRoot(), "printing");
        if (!printDir.exists()) {
        	printDir.mkdir();
        }
        
        File printConfig = new File(printDir, "config.yaml");
    	try (InputStream is = getClass().getClassLoader().getResourceAsStream("niva/geoserver/printing/config.yaml")) {
    		Files.copy(is, Paths.get(printConfig.getAbsolutePath()));
    	}
	}

	/**
	 * Test for an anomalie with the color returned from open street map.
	 * @throws Exception
	 */
	@Test
	public void testSurveillanceInformationMapExport() throws Exception {
		String spec = null;
		try (InputStream is = getClass().getClassLoader().getResourceAsStream("niva/geoserver/printing/Spec.txt")) {
			StringBuilder builder = new StringBuilder();
			try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
				String line;
				while ((line = br.readLine()) != null) {
					builder.append(line);
				}
			}
			spec = URLEncoder.encode(builder.toString(), StandardCharsets.UTF_8.name());
		}
		assertNotNull(spec);
        MockHttpServletResponse resp = getAsServletResponse("pdf/print.pdf?spec=" + spec);
        
        InputStream is = getBinaryInputStream(resp);
        BufferedImage image = ImageIO.read(is);
		assertNotNull("Didn't get an image.", image);

		CountColor typeColorOcean = computeTypeColor(image.getData(new Rectangle(375, 2000, 10, 10)));
		CountColor typeColorLowerLeft = computeTypeColor(image.getData(new Rectangle(125, 2000, 10, 10)));
		CountColor typeColorLowerRight = computeTypeColor(image.getData(new Rectangle(500, 2350, 10, 10)));
		
		assertEquals("Color in lower left corner", typeColorOcean, typeColorLowerLeft);
		assertEquals("Color a little bit longer to right", typeColorOcean, typeColorLowerRight);
	}
	
	/**
	 * We grab a little clip from the image, and finds the most frequent color
	 * @param clip
	 * @return
	 */
	private CountColor computeTypeColor(Raster clip) {
		int height = clip.getHeight();
		int width = clip.getWidth();
		HashSet<CountColor> colors = new HashSet<CountColor>();
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int[] color = new int[4];
				clip.getPixel(clip.getMinX() + x,  clip.getMinY() + y, color);
				Optional<CountColor> optional = colors.stream()
												.filter((element) -> element.sameColor(color))
												.findFirst();
				if (optional.isPresent()) {
					optional.get().incrementCount();
				}
				else {
					colors.add(new CountColor(color));
				}
			}
		}
		
		return colors.stream().max(new Comparator<CountColor>() {

			@Override
			public int compare(CountColor arg0, CountColor arg1) {
				return Integer.compare(arg0.count, arg1.count);
			}
		}).get();
	}

	/**
	 * Helper class to find the most frequent color.
	 * 
	 * @author Roar Brænden
	 *
	 */
	static class CountColor {
		int count;
		int[] color;
		
		CountColor(int[] color) {
			this.color = color;
			this.count = 1;
		}
		
		void incrementCount() {
			count += 1;
		}

		boolean sameColor(int[] other) {

			if (other.length == this.color.length) {
				for (int i = 0; i < this.color.length; i++) {
					if (other[i] != this.color[i]) {
						return false;
					}
				}
				return true;
			}
			return false;
		}
		
		@Override
		public boolean equals(Object other) {
			if (other instanceof CountColor) {
				return sameColor(((CountColor)other).color);
			}
			return false;
		}
		
		@Override
		public String toString() {
			return String.format("[%d %d %d]", color[0], color[1], color[2]);
		}
	}
}
