package niva.geoserver.cache;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;


import org.geotools.TestData;
import org.geotools.api.data.DataStore;
import org.geotools.api.data.FileStoreFactory;
import org.geotools.data.directory.DirectoryDataStore;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.shapefile.ShapefileDirectoryFactory;
import org.geotools.data.shapefile.files.ShpFiles;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.Name;
import org.geotools.api.filter.FilterFactory;



import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import niva.geotools.referencing.CRS;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
/**
 * 
 * @author Roar Brænden, NIVA
 *
 */
public class CacheDataStoreTest {

	@Test
	public void simpleGenerateCacheTest() throws Exception {
		
		File cacheFolder = TestData.file(this, "cache");
		URL backendShp = TestData.url(this, "211_Arealdekke_pnt.shp");
		CacheDataStore ds = null;
		SimpleFeatureIterator iter = null;
		try {
			
			ShapefileDataStore backend = new ShapefileDataStore(backendShp);
			DirectoryDataStore cacheDir = getCacheDataStore(cacheFolder);
			
			ds = new CacheDataStore(backend, cacheDir);
			
			Name first = ds.getNames().get(0);
			CacheFeatureStore source = ds.getFeatureSource(first);
			source.generate();
			
			assertTrue(source.isCached());
			
			iter = source.getFeatures().features();
			
			assertTrue(iter.hasNext());
			SimpleFeature single = iter.next();
			assertEquals("POINT (254762.44999735337 6615016.41999901)", single.getDefaultGeometry().toString());

		}
		finally {
			
			if (iter != null)
				iter.close();
			
			if (ds != null)
				ds.dispose();
			
			ShpFiles shp = new ShpFiles(new File(cacheFolder, "211_Arealdekke_pnt.shp"));
			shp.delete();
		}
	}
	
	@Test
	public void refreshCacheTest() throws Exception {
		
		File cacheFolder = TestData.file(this, "cache");
		
		FilterFactory ff = CommonFactoryFinder.getFilterFactory();
		
		try {
			SimpleFeature[] data = getSimpleData();
			
			MemoryDataStore backend = new MemoryDataStore(data);
			DirectoryDataStore cacheDir = getCacheDataStore(cacheFolder);
			
			CacheDataStore ds = new CacheDataStore(backend, cacheDir);
			CacheFeatureStore source = ds.getFeatureSource(new NameImpl("test"));
			source.generate();
			
			assertTrue(source.isCached());
			
			data[0].setAttribute(2, "Navn-1-b");
			source.refresh();
			
			SimpleFeatureCollection coll = source.getFeatures(ff.equals(ff.property("navn"), ff.literal("Navn-1-b")));
			
			assertEquals(1, coll.size());
			
			ds.dispose();
		}
		finally {
			ShpFiles shp = new ShpFiles(new File(cacheFolder, "test.shp"));
			shp.delete();
		}
	}
	
	
	@Test
	public void disposeCacheTest() throws Exception {

		URL backendShp = TestData.url(this, "211_Arealdekke_pnt.shp");
		CacheDataStore ds = null;
		
		ShapefileDataStore backend = new ShapefileDataStore(backendShp);
		MemoryDataStore cache = new MemoryDataStore();
		
		ds = new CacheDataStore(backend, cache);
		
		Name first = ds.getNames().get(0);
		CacheFeatureStore source = ds.getFeatureSource(first);
		source.generate();
		
		assertTrue(source.isCached());
		
		ds.dispose();
		
	}

	
	private static SimpleFeature[] getSimpleData() {
		SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
		typeBuilder.setName("test");
		typeBuilder.setCRS(CRS.getBreddeLengdegrad());
		typeBuilder.add("shape", Point.class);
		typeBuilder.add("fid", Integer.class);
		typeBuilder.add("navn", String.class);
		
		SimpleFeatureType type = typeBuilder.buildFeatureType();
		
		GeometryFactory fact = new GeometryFactory();
		
		SimpleFeatureBuilder featBuilder = new SimpleFeatureBuilder(type);
		SimpleFeature feat1 = featBuilder.buildFeature("1", new Object[] {fact.createPoint(new Coordinate(10, 10)), 1, "Navn-1"} );
		SimpleFeature feat2 = featBuilder.buildFeature("2", new Object[] {fact.createPoint(new Coordinate(10.5, 25.0)), 2, "Navn-2"} );
		
		return new SimpleFeature[] { feat1, feat2};
	}

	private DirectoryDataStore getCacheDataStore(File dir) throws IOException{
		FileStoreFactory fact = new ExtendedShapefileDirectoryFactory();

		return new DirectoryDataStore(dir, fact);
	}
	
	class ExtendedShapefileDirectoryFactory extends ShapefileDirectoryFactory 
														implements FileStoreFactory {
		
		FileStoreFactory shpFactory;
		
		public ExtendedShapefileDirectoryFactory() {
			shpFactory = new ShapefileDataStoreFactory.ShpFileStoreFactory(this, Collections.emptyMap());
		}

		@Override
		public DataStore getDataStore(File file) throws IOException {
			return shpFactory.getDataStore(file);
		}	

	}
}
