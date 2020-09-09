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
	
	
	private String HOST = "https://test-aquamonitor.niva.no/";
	private String SITE = "Intern";
	

	@Test
	public void getAllStationsEasy() throws Exception {
		GeographyWebService ws = GeographyWebService.createService(HOST, SITE);
		StationPointReader reader = ws.getProjectUserStationReader("Mjøsa");
		Iterator<StationPointCargo> iter = reader.iterator();
		
		assertTrue(iter.hasNext());
		
		final String typ = iter.next().stationType;
		
		assertTrue("Innsjø".equals(typ) || "Elv".equals(typ));		
	}
	
	@Test
	public void getAllStationsWrong() throws Exception {
		GeographyWebService ws = GeographyWebService.createService(HOST, SITE);
		
		try {
			StationPointReader reader = ws.getProjectUserStationReader("RBR");
			reader.getCount();
			Assert.fail();
		}
		catch (IOException ie) {
			assertTrue("Feilmelding er endret.", ie.toString().contains("Kun gyldig for brukere av typen Project."));
		}
	}
	
	@Test
	public void getAllStationsBig() throws Exception {
		GeographyWebService ws = GeographyWebService.createService(HOST, SITE);
		StationPointReader reader = ws.getProjectUserStationReader("Østfold");
		int c = reader.getCount();
		
		assertTrue(c > 1);
	}
	
	@Test
	public void getCurrentStationsWrongKey() throws Exception  {
		GeographyWebService ws = GeographyWebService.createService(HOST, SITE);
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
		
		LoginController lws = LoginController.createService(HOST, SITE);
		UserCargo user = lws.authenticateUser("RBR", "10. august 2020"); // Change to appropriate password
		
		assertNotNull(user);
		assertFalse(user.key == null || user.key == "");
		
		GeographyWebService ws = GeographyWebService.createService(HOST, SITE);
		StationPointReader reader = ws.getCurrentStationReader(user.key);
		assertEquals(0, reader.getCount());
		
	}
	
	@Test
	public void getAllDatatypesFindWater() throws Exception {

		GeographyWebService ws = GeographyWebService.createService(HOST, SITE);
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
		GeographyWebService ws = GeographyWebService.createService(HOST, SITE);

		DatatypeReader reader = ws.getAllDatatypePointsReader();		
		int l = reader.getCount();
		System.out.println("Antall stasjoner:" + l);
		assertTrue(l > 0);
			
	}
}
