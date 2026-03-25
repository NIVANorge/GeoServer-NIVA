package niva.geoserver.wfs;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URI;
import java.util.Map;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.catalog.impl.DataStoreInfoImpl;
import niva.geoserver.data.NivaTestSupport;
import org.junit.Test;
import static org.junit.Assert.assertTrue;


/**
 * Tests for WFS queries on AquaMonitor stores.
 * These tests uses the end-point of the AquaMonitor Test server.
 */
public class AquaMonitorWFSTest extends NivaTestSupport {
	
	private final static String TEST_HOST = "https://test-aquamonitor.niva.no/";

	@Test
	public void testQueryStationOrList() throws Exception {
		Catalog catalog = getCatalog();
		
		StoreInfo store = new DataStoreInfoImpl(catalog);
		store.setName("Intern_query");
		store.setWorkspace(catalog.getWorkspaceByName("no.niva.aquamonitor"));
		
		Map<String, Serializable> params = store.getConnectionParameters();
		params.put("namespace", new URI("http://www.aquamonitor.no/"));
		params.put("dbtype", "aquamonitor-site");
		params.put("host", TEST_HOST);
		params.put("site", "Intern");
		
		addAquaMonitorStore("Intern_query", params);
		addStationLayer(catalog.getDataStoreByName("no.niva.aquamonitor", "Intern_query"), "Intern_query_stations");
		
		String xml =  "<wfs:GetFeature service=\"WFS\" version=\"1.1.0\" "
				+ "xmlns:ogc=\"http://www.opengis.net/ogc\" "
				+ "xmlns:wfs=\"http://www.opengis.net/wfs\">"
				+ "<wfs:Query typeName=\"no.niva.aquamonitor:Intern_query_stations\">"
				+ "<wfs:PropertyName>no.niva.aquamonitor:STATION_ID</wfs:PropertyName>"
				+ "<wfs:PropertyName>no.niva.aquamonitor:STATION_CODE</wfs:PropertyName>"
				+ "<wfs:PropertyName>no.niva.aquamonitor:STATION_NAME</wfs:PropertyName>"
				+ "<ogc:Filter><ogc:Or>"
				+ "<ogc:Or><ogc:PropertyIsEqualTo><ogc:PropertyName>no.niva.aquamonitor:STATION_ID</ogc:PropertyName>"
				+ "<ogc:Literal>3570</ogc:Literal></ogc:PropertyIsEqualTo>"
				+ "<ogc:PropertyIsEqualTo><ogc:PropertyName>no.niva.aquamonitor:STATION_ID</ogc:PropertyName>"
				+ "<ogc:Literal>3571</ogc:Literal></ogc:PropertyIsEqualTo></ogc:Or>"
				+ "<ogc:Or><ogc:PropertyIsEqualTo><ogc:PropertyName>no.niva.aquamonitor:STATION_ID</ogc:PropertyName>"
				+ "<ogc:Literal>3572</ogc:Literal></ogc:PropertyIsEqualTo>"
				+ "<ogc:PropertyIsEqualTo><ogc:PropertyName>no.niva.aquamonitor:STATION_ID</ogc:PropertyName>"
				+ "<ogc:Literal>3573</ogc:Literal></ogc:PropertyIsEqualTo></ogc:Or></ogc:Or>"
				+ "</ogc:Filter></wfs:Query></wfs:GetFeature>";
		

		try (InputStream is = this.post("wfs", xml);
		     BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
			String resp = "";
			String next = reader.readLine();
			while (next != null) {
				resp += next;
				next = reader.readLine();
			}
			assertTrue("Result didn't contain station id:3570", resp.contains("<no.niva.aquamonitor:STATION_ID>3570</no.niva.aquamonitor:STATION_ID>"));
			assertTrue("Result had an unknown geographic representation", resp.contains("<gml:boundedBy>"
					+ "<gml:Envelope srsName=\"urn:x-ogc:def:crs:EPSG:4326\" srsDimension=\"2\">"
					+ "<gml:lowerCorner>60.64815413 6.00033668</gml:lowerCorner>"
					+ "<gml:upperCorner>60.64815413 6.00033668</gml:upperCorner>"
					+ "</gml:Envelope>"
					+ "</gml:boundedBy>"));
			assertTrue("Result doesn't contain project id.", !resp.contains("PROJECT_ID"));
		}
	}
	
	@Test
	public void testProjectSite() throws Exception {
		Catalog catalog = getCatalog();
		
		StoreInfo store = new DataStoreInfoImpl(catalog);
		store.setName("Mjosovervak");
		store.setWorkspace(catalog.getWorkspaceByName("no.niva.aquamonitor"));
		
		Map<String, Serializable> params = store.getConnectionParameters();
		params.put("namespace", new URI("http://www.aquamonitor.no/"));
		params.put("dbtype", "aquamonitor");
		params.put("user", "Mjøsa");
		
		addAquaMonitorStore(store.getName(), params);
		
		addStationLayer(catalog.getDataStoreByName("no.niva.aquamonitor", "Mjosovervak"), "Mjosovervak_stations");
		
		
		String xml;
		
		xml = "<wfs:GetFeature xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
				+ " xsi:schemaLocation=\"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd\" service=\"WFS\""
				+ " version=\"1.1.0\"><wfs:Query srsName=\"EPSG:32632\" typeName=\"feature:Mjosovervak_stations\" /></wfs:GetFeature>";
		

		try (InputStream is = this.post("wfs", xml);
		     BufferedReader reader = new BufferedReader(new InputStreamReader(is))){
			String resp = "";
			String next = reader.readLine();
			while (next != null) {
				resp += next;
				next = reader.readLine();
			}
			assertTrue(resp.contains("<no.niva.aquamonitor:PROJECT_ID>1098</no.niva.aquamonitor:PROJECT_ID>"));
		}
	}
}
