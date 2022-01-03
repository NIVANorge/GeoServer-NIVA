package niva.aquamonitor.data;


import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.locationtech.jts.geom.Point;
import niva.aquamonitor.data.ws.GeographyController;
import niva.geotools.referencing.CRS;
import org.junit.Assert;
import org.junit.Test;

public class DatatypePointReaderTest {
	
	@Test
	public void datatypePointReaderConstructorTest() throws Exception {
		GeographyController service = GeographyController.createService("https://test-aquamonitor.niva.no/", 
		                                                                "Vannplanter");
		
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName("STATION_DATA_TYPE");
		builder.setCRS(CRS.getBreddeLengdegrad());
		builder.add("the_geom", Point.class);
		builder.add("STATION_ID", Integer.class);
		builder.add("STATION_TYPE_ID", Integer.class);
		builder.add("STATION_TYPE", String.class);
		builder.add("Vannplanter", Integer.class);
		builder.add("Water", Integer.class);
		builder.add("Begroing", Integer.class);
		builder.add("Bunndyr", Integer.class);
		builder.add("Plankton", Integer.class);

		final SimpleFeatureType schema = builder.buildFeatureType();
		
		try (DatatypePointReader reader = 
		        new DatatypePointReader(schema, service.getAllDatatypePointsReader().iterator())) {
	        Assert.assertNotNull(reader);
		}
	}
	
	@Test
	public void datatypePointReaderReadTest() throws Exception {
		GeographyController service = GeographyController.
		        createService("https://test-aquamonitor.niva.no/", "Vannplanter");
		
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName("STATION_DATA_TYPE");
		
		builder.setCRS(CRS.getBreddeLengdegrad());
		builder.add("the_geom", Point.class);
		builder.add("STATION_ID", Integer.class);
		builder.add("STATION_TYPE_ID", Integer.class);
		builder.add("STATION_TYPE", String.class);
		builder.add("Vannplanter", Integer.class);

		SimpleFeatureType schema = builder.buildFeatureType();
	    try (DatatypePointReader reader = new DatatypePointReader(schema, 
	                                            service.getAllDatatypePointsReader()
	                                                   .iterator())) {
			Assert.assertTrue(reader.hasNext());
			
			SimpleFeature feature = reader.next();
			Assert.assertNotNull(feature);
			Assert.assertEquals(1, feature.getAttribute("Vannplanter"));
		}
	}
}
