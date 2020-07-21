package niva.geoserver.process;


import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;


import org.geoserver.wps.WPSTestSupport;
import org.geotools.data.memory.MemoryFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;


import niva.geotools.referencing.CRS;


import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


public class PointAggregateGridProcessTest extends WPSTestSupport{

	
	@Test
	public void testPlain() throws Exception {
		SimpleFeatureCollection points = TestData.getPlain();
		
		Set<String> aggregateAttributes = TestData.getAttributes();
		Integer cellSize = 50;
		Integer outputHeight = 100;
		Integer outputWidth = 100;
		ReferencedEnvelope outputBbox = ReferencedEnvelope.create(new Envelope(10.0, 11.0, 60.0, 61.0), CRS.getBreddeLengdegrad());
		
		SimpleFeatureCollection result = new PointAggregateGridProcess().execute(points, outputBbox, outputWidth, outputHeight, cellSize, aggregateAttributes);
		
		assertEquals(1, result.size());
		SimpleFeature feat = (SimpleFeature)result.toArray()[0];
		Point geom = (Point)feat.getDefaultGeometry();
		
		assertEquals(1025, Math.round(geom.getCoordinate().x * 100.0));
		assertEquals(6035, Math.round(geom.getCoordinate().y * 100.0));
		assertEquals(1, feat.getAttribute(TestData.WATER));
		assertEquals(0, feat.getAttribute(TestData.PLANKTON));
		assertEquals(1, feat.getAttribute(TestData.BIOTA));
		assertEquals(2, feat.getAttribute("COUNT"));
		assertNull(feat.getAttribute("STATION_TYPE"));
		
		cellSize = 5;
		
		SimpleFeatureCollection result2 = new PointAggregateGridProcess().execute(points, outputBbox, outputWidth, outputHeight, cellSize, aggregateAttributes);
		assertEquals(2, result2.size());
		SimpleFeature first = (SimpleFeature)result2.toArray()[0];
		
		assertEquals(1, first.getAttribute("COUNT"));
		assertNotNull(first.getAttribute("STATION_TYPE"));
	}
	
	@Test
	public void testOtherCRS() throws Exception {
		
		final ReferencedEnvelope outputBbox = ReferencedEnvelope.create(new Envelope(221288, 283749, 6661953, 6769393), CRS.getUtm33());
		
		final SimpleFeatureCollection result = new PointAggregateGridProcess().execute(TestData.getPlain(),
																					   outputBbox,
																					   100,
																					   100,
																					   50,
																					   TestData.getAttributes());
		
		assertEquals(1, result.size());
	}

	
	
	private static class TestData {
		
		static final String WATER = "Water";
		static final String PLANKTON = "Plankton";
		static final String BIOTA = "Biota";
		
		static final String[] ATTRIBUTES = new String[] { WATER, PLANKTON, BIOTA};
		
		static SimpleFeatureCollection getPlain() {
			SimpleFeatureType featureType = getFeatureType();
			SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
			
			MemoryFeatureCollection points = new MemoryFeatureCollection(featureType);
			GeometryFactory fact = new GeometryFactory();
			
			builder.addAll(new Object[]{fact.createPoint(new Coordinate(10.2, 60.3)), "Innsjø", new Integer(1), new Integer(0), new Integer(0)});
			points.add(builder.buildFeature("1"));
			
			builder.addAll(new Object[]{fact.createPoint(new Coordinate(10.3, 60.4)), "Elv", new Integer(0), new Integer(0), new Integer(1)});
			points.add(builder.buildFeature("2"));
			
			return points;
		}
		
		public static Set<String> getAttributes() {

			return new AbstractSet<String>() {

				@Override
				public Iterator<String> iterator() {
					return new Iterator<String>() {
						int inx = 0;

						@Override
						public boolean hasNext() {
							return inx < ATTRIBUTES.length;
						}

						@Override
						public String next() {
							return ATTRIBUTES[inx++];
						}

						@Override
						public void remove() {
							throw new UnsupportedOperationException();
						}
						
					};
				}

				@Override
				public int size() {
					return ATTRIBUTES.length;
				}
			};
		}

		static SimpleFeatureType getFeatureType() {
			final SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
			builder.setCRS(CRS.getBreddeLengdegrad());
			builder.setName("Testdata");
			builder.add("theGeom", Point.class);
			builder.add("STATION_TYPE", String.class);
			builder.add(WATER, Integer.class);
			builder.add(PLANKTON, Integer.class);
			builder.add(BIOTA, Integer.class);
			
			return builder.buildFeatureType();
		}
	}

}
