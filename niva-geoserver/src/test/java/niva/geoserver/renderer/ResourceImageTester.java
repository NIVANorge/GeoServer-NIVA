package niva.geoserver.renderer;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.geotools.TestData;

public class ResourceImageTester {
	
	static void assertImage(String filenameActual, BufferedImage imageTest) throws Exception {
		final File actualFile = TestData.file(ResourceImageTester.class, filenameActual);
		
		final int width = imageTest.getWidth();
		final int height = imageTest.getHeight();
		
		final BufferedImage actualImage = ImageIO.read(actualFile);
		assertTrue("The two pictures doesn't have the same dimension.", width == actualImage.getWidth() 
																		&& height == actualImage.getHeight());
		
		final long size = width * height;
		long missed = 0;

		for (int y = 0; y < height; y++){
			for (int x = 0; x < width; x++){
				if (imageTest.getRGB(x, y) != actualImage.getRGB(x, y)) {
					missed += 1;
					if (missed*100 / size > 10) {
						fail("The two images have more than 10% mismatch.");
					}
				}
			}
		}
	}
}
