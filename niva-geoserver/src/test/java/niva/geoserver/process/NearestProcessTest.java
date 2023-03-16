package niva.geoserver.process;


import java.util.Iterator;
import java.util.LinkedList;

import org.geoserver.wps.WPSTestSupport;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.collection.AbstractFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.process.vector.NearestProcess;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import niva.geotools.referencing.CRS;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * 
 * @author Roar Brænden, NIVA
 *
 */
public class NearestProcessTest extends WPSTestSupport {
	
	private static final GeometryFactory geomFact = new GeometryFactory();
	private final Coordinate[] coordinates = new Coordinate[] {
			new Coordinate(11.76, 60.67),
			new Coordinate(11.77, 61.32),
			new Coordinate(11.45, 61.88),
			new Coordinate(11.87, 60.78),
			new Coordinate(11.23, 62.23),
			new Coordinate(11.54, 61.90),
			new Coordinate(11.65, 61.67),
			new Coordinate(11.29, 60.34)
	}; 
	
	/** We've tampered with the NearestProcess to include a num argument. */
	@Test
	public void testNearestFeature() throws Exception {
		
		SimpleFeatureCollection features = testFeatures();
		Point point = geomFact.createPoint(new Coordinate(323000, 6730000));
		CoordinateReferenceSystem crs = CRS.getUtm33();
		int num = 5;
		
		/*
		FeatureCollection result = new NearestProcess().execute(features, point, crs, num);
		assertEquals(num, result.size());
		*/
	}
	
	private final AbstractFeatureCollection testFeatures() {
		return new AbstractFeatureCollection(createTestSchema()) {
	
			@Override
			protected Iterator<SimpleFeature> openIterator() {
				SimpleFeatureBuilder builder = new SimpleFeatureBuilder(this.getSchema());
				LinkedList<SimpleFeature> list = new LinkedList<SimpleFeature>();
				for (int i = 0; i < coordinates.length; i++) {
					builder.set(0, geomFact.createPoint(coordinates[i]));
					list.add(builder.buildFeature(String.valueOf(i)));
				}
				
				return list.iterator();
			}
	
			@Override
			public int size() {
				return coordinates.length;
			}
	
			@Override
			public ReferencedEnvelope getBounds() {
				double minx = coordinates[0].x;
				double maxx = minx;
				double miny = coordinates[0].y;
				double maxy = miny;
				for (int i = 1; i < coordinates.length; i++) {
					if (coordinates[i].x < minx) {
						minx = coordinates[i].x;
					} else if (coordinates[i].x > maxx) {
						maxx = coordinates[i].x;
					}
					
					if (coordinates[i].y < miny) {
						miny = coordinates[i].y;
					} else if (coordinates[i].y > maxy) {
						maxy = coordinates[i].y;
					}
				}
				return new ReferencedEnvelope(minx, maxx, miny, maxy, CRS.getLengdeBreddegrad());
			}
		};
	}
	
	private static final SimpleFeatureType createTestSchema() {
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName("Testdata");
		builder.crs(CRS.getLengdeBreddegrad())
		       .add("theGeom", Point.class);
		builder.setDefaultGeometry("theGeom");
		
		return builder.buildFeatureType();
	};
}
