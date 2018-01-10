package niva.aquamonitor.data;


import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Point;

import niva.aquamonitor.data.ws.GeographyWebService;
import niva.geotools.referencing.CRS;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DatatypePointReaderTest {
	
	@Test
	public void datatypePointReaderConstructorTest() throws Exception {
		GeographyWebService service = GeographyWebService.createService("http://www.aquamonitor.no/", "Vannplanter");
		
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

		SimpleFeatureType schema = builder.buildFeatureType();
		
		DatatypePointReader reader = new DatatypePointReader(schema, service.getAllDatatypePointsReader());
		
		assertNotNull(reader);
	}
	
	@Test
	public void datatypePointReaderReadTest() throws Exception {
		GeographyWebService service = GeographyWebService.createService("http://www.aquamonitor.no/", "Vannplanter");
		
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName("STATION_DATA_TYPE");
		
		builder.setCRS(CRS.getBreddeLengdegrad());
		builder.add("the_geom", Point.class);
		
		builder.add("STATION_ID", Integer.class);
		builder.add("STATION_TYPE_ID", Integer.class);
		builder.add("STATION_TYPE", String.class);
		
		builder.add("Vannplanter", Integer.class);

		SimpleFeatureType schema = builder.buildFeatureType();
		
		DatatypePointReader reader = new DatatypePointReader(schema, service.getAllDatatypePointsReader());
		try {
			assertTrue(reader.hasNext());
			
			SimpleFeature feature = reader.next();
			assertNotNull(feature);
			assertEquals(1, feature.getAttribute("Vannplanter"));
		}
		finally {
			reader.close();
		}
	}

}
