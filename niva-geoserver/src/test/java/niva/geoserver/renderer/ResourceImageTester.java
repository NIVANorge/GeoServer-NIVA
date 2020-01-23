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

		for (int y = 0; y < height; y++){
			for (int x = 0; x < width; x++){
				if (imageTest.getRGB(x, y) != actualImage.getRGB(x, y)) {
					fail("The two images doesn't match.");
				}
			}
		}
	}
}
