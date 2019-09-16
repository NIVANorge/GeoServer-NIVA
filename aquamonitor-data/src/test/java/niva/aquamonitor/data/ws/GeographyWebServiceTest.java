package niva.aquamonitor.data.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

public class GeographyWebServiceTest {
	
	
	private String host = "https://test-aquamonitor.niva.no/";
	private String site = "Intern";
	

	@Test
	public void getAllStationsEasy() throws Exception {
		GeographyWebService ws = GeographyWebService.createService(host, site);
		StationPointReader reader = ws.getProjectUserStationReader("Mjøsa");
		Iterator<StationPointCargo> iter = reader.iterator();
		
		assertTrue(iter.hasNext());
		assertEquals("Innsjø", iter.next().stationType);
		
	}
	
	@Test
	public void getAllStationsWrong() throws Exception {
		GeographyWebService ws = GeographyWebService.createService(host, site);
		
		try {
			StationPointReader reader = ws.getProjectUserStationReader("RBR");
			reader.getCount();
			Assert.fail();
		}
		catch (IOException ie) {
			assertEquals("java.io.IOException: Kun gyldig for brukere av typen Project.", ie.toString());
		}
	}
	
	@Test
	public void getAllStationsBig() throws Exception {
		GeographyWebService ws = GeographyWebService.createService(host, site);
		StationPointReader reader = ws.getProjectUserStationReader("Østfold");
		int c = reader.getCount();
		
		assertTrue(c > 1);
	}
	
	@Test
	public void getCurrentStationsWrongKey() throws Exception  {
		GeographyWebService ws = GeographyWebService.createService(host, site);
		try  {
			StationPointReader reader = ws.getCurrentStationReader("tull");
			reader.getEnvelope();
			Assert.fail();
		}
		catch (IOException ie) {
		}
	}
	
	@Test
	public void getCurrentStationsEmptyKey() throws Exception  {
		
		LoginWebService lws = LoginWebService.createService(host, site);
		UserCargo user = lws.authenticateUser("RBR", "RBR");
		
		assertNotNull(user);
		assertFalse(user.key == null || user.key == "");
		
		GeographyWebService ws = GeographyWebService.createService(host, site);
		StationPointReader reader = ws.getCurrentStationReader(user.key);
		assertEquals(0, reader.getCount());
		
	}
	
	@Test
	public void getAllDatatypesFindWater() throws Exception {

		GeographyWebService ws = GeographyWebService.createService(host, site);
		StringReader reader = ws.getAllDatatypesReader();
		Iterator<String> iter = reader.iterator();
		boolean foundWater = false;
		
		while (iter.hasNext()) {
			if (iter.next().equals("Water")) {
				foundWater = true;
				break;
			}
		}
		
		assertTrue(foundWater);
	}
	

	@Test
	public void getAllDatatypePoints() throws Exception  {
		GeographyWebService ws = GeographyWebService.createService(host, site);

		DatatypeReader reader = ws.getAllDatatypePointsReader();		
		int l = reader.getCount();
		System.out.println("Antall stasjoner:" + l);
		assertTrue(l > 0);
			
	}
}
