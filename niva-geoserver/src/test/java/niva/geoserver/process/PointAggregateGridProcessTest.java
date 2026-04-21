package niva.geoserver.process;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.geoserver.wps.WPSTestSupport;
import org.geotools.TestData;
import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.data.memory.MemoryFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.store.ReprojectingFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.URLs;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.filter.Filter;
import org.geotools.api.filter.FilterFactory;
import niva.geoserver.cache.CacheDataStoreFactory;
import niva.geotools.referencing.CRS;


public class PointAggregateGridProcessTest extends WPSTestSupport{

	
	@Test
	public void testPlain() throws Exception {
		final SimpleFeatureCollection points = AquamonitorTestData.getPlain();
		
		final Set<String> aggregateAttributes = AquamonitorTestData.getAttributes();
		final Integer cellSize = 50;
		final Integer outputHeight = 100;
		final Integer outputWidth = 100;
		final ReferencedEnvelope outputBbox = new ReferencedEnvelope(10.0, 11.0, 60.0, 61.0, CRS.getLengdeBreddegrad());
		
		final SimpleFeatureCollection result = new PointAggregateGridProcess().execute(points, 
		                                        outputBbox, 
		                                        outputWidth, 
		                                        outputHeight, 
		                                        cellSize, 
		                                        aggregateAttributes);
		
		assertEquals(1, result.size());
		SimpleFeature feat = (SimpleFeature)result.toArray()[0];
		Point geom = (Point)feat.getDefaultGeometry();
		
		assertEquals(1025, Math.round(geom.getCoordinate().x * 100.0));
		assertEquals(6035, Math.round(geom.getCoordinate().y * 100.0));
		assertEquals(1, feat.getAttribute(AquamonitorTestData.WATER));
		assertEquals(0, feat.getAttribute(AquamonitorTestData.PLANKTON));
		assertEquals(1, feat.getAttribute(AquamonitorTestData.BIOTA));
		assertEquals(2, feat.getAttribute("COUNT"));
		assertNull(feat.getAttribute("STATION_TYPE"));
		
		final Integer cellSize2 = 5;
		
		final SimpleFeatureCollection result2 = new PointAggregateGridProcess().execute(points, 
		                    outputBbox, 
		                    outputWidth, 
		                    outputHeight, 
		                    cellSize2, 
		                    aggregateAttributes);
		assertEquals(2, result2.size());
		final SimpleFeature first = (SimpleFeature)result2.toArray()[0];
		
		assertEquals(1, first.getAttribute("COUNT"));
		assertNotNull(first.getAttribute("STATION_TYPE"));
	}
	

	/** Special case where we got exception on cached points. More specific when using a Shapefile. */
	@Test
	public void testAggregateCachedPoints() throws Exception {
		File cacheDir = TestData.file(this, "cache_vannmiljo");
		
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(CacheDataStoreFactory.NAMESPACE_PARAM.key, "http://www.aquamonitor.no/");
		parameters.put(CacheDataStoreFactory.DBTYPE_PARAM.key, CacheDataStoreFactory.DBTYPE_PARAM.sample);
		parameters.put(CacheDataStoreFactory.BACKEND_PARAM.key, "dbtype=aquamonitor-site;host=https://test-aquamonitor.niva.no/;site=Vannmiljo");
		parameters.put(CacheDataStoreFactory.CACHE_PARAM.key, "dbtype=shapefile;url=" + URLs.fileToUrl(cacheDir).toExternalForm());
		parameters.put(CacheDataStoreFactory.INTERVAL_PARAM.key, 1440);
		ReferencedEnvelope outputBbox = new ReferencedEnvelope(221288, 283749, 6661953, 6769393, CRS.getUtm33());
		FilterFactory ff = CommonFactoryFinder.getFilterFactory();
		Filter bboxFilter = ff.bbox(ff.property("the_geom"),
									ff.literal(JTS.transform(outputBbox,
											org.geotools.referencing.CRS.findMathTransform(CRS.getUtm33(),
															CRS.getLengdeBreddegrad()))));
		
		DataStore store = DataStoreFinder.getDataStore(parameters);
		assertNotNull(store);
		try {
			SimpleFeatureCollection features = new ReprojectingFeatureCollection(store.getFeatureSource("STATION_DATATYPE_POINTS").getFeatures(bboxFilter), 
									outputBbox.getCoordinateReferenceSystem());
			
			final SimpleFeatureCollection result = new PointAggregateGridProcess().execute(features,
																						outputBbox, 100, 100, 60, 
																						AquamonitorTestData.getAttributes());

			assertTrue("Should at least contain one point", result.size() > 0);
		} finally {
			store.dispose();
			Arrays.stream(cacheDir.listFiles())
				  .filter(file -> !"dummy".equals(file.getName()))
				  .forEach(file -> file.delete());
		}
	}
	
	
	
	
	
	
	/** Test data representing usual from AquaMonitor. */
	private static class AquamonitorTestData {
		
		static final String WATER = "Water";
		static final String PLANKTON = "Plankton";
		static final String BIOTA = "Biota";
		
		static final String[] ATTRIBUTES = new String[] { WATER, PLANKTON, BIOTA};
		
		static SimpleFeatureCollection getPlain() {
			SimpleFeatureType featureType = getFeatureType();
			SimpleFeatureBuilder builder = new SimpleFeatureBuilder(featureType);
			
			MemoryFeatureCollection points = new MemoryFeatureCollection(featureType);
			GeometryFactory fact = new GeometryFactory();
			
			builder.addAll(new Object[]{fact.createPoint(new Coordinate(10.2, 60.3)), "Innsjø", Integer.valueOf(1), Integer.valueOf(0), Integer.valueOf(0)});
			points.add(builder.buildFeature("1"));
			
			builder.addAll(new Object[]{fact.createPoint(new Coordinate(10.3, 60.4)), "Elv", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(1)});
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
	        builder.setName("Testdata");
			builder.setCRS(CRS.getLengdeBreddegrad());
			builder.add("theGeom", Point.class);
			builder.add("STATION_TYPE", String.class);
			builder.add(WATER, Integer.class);
			builder.add(PLANKTON, Integer.class);
			builder.add(BIOTA, Integer.class);
			
			return builder.buildFeatureType();
		}
	}

}
