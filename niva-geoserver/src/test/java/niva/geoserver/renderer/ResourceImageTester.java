package niva.geoserver.renderer;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.geotools.TestData;

/**
 * Helper class to compare two images, and to find the most frequent color in a clip.
 */
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
	
	/**
	 * We grab a little clip from the image, and finds the most frequent color
	 * @param clip
	 * @return
	 */
	static CountColor computeTypeColor(Raster clip) {
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
