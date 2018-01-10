package org.geoserver.wps.niva;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.geoserver.wps.WPSTestSupport;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class PlatformUpdateProcessTest extends WPSTestSupport {

	@Ignore
	@Test
	public void testIssueWPSRequest() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
		         + "<wps:Execute version=\"1.0.0\" service=\"WPS\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.opengis.net/wps/1.0.0\" xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:wcs=\"http://www.opengis.net/wcs/1.1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsAll.xsd\">"
		         +   "<ows:Identifier>niva:PlatformUpdateProcess</ows:Identifier>"
		         +     "<wps:DataInputs>"
		         +       "<wps:Input>"
		         +         "<ows:Identifier>Typename</ows:Identifier>"
		         +         "<wps:Data>"
		         +           "<wps:LiteralData>TEST_181</wps:LiteralData>"
		         +         "</wps:Data>"
		         +       "</wps:Input>"
		         +       "<wps:Input>"
		         +         "<ows:Identifier>Password</ows:Identifier>"
		         +         "<wps:Data>"
		         +           "<wps:LiteralData>RBR</wps:LiteralData>"
		         +         "</wps:Data>"
		         +       "</wps:Input>"
		         +       "<wps:Input>"
		         +         "<ows:Identifier>Username</ows:Identifier>"
		         +         "<wps:Data>"
		         +           "<wps:LiteralData>RBR</wps:LiteralData>"
		         +         "</wps:Data>"
		         +       "</wps:Input>"
		         +     "</wps:DataInputs>"
		         +   "<wps:ResponseForm/>"
		         + "</wps:Execute>";
		
		
		InputStream stream = this.post("wps", xml);
		
		BufferedReader r = new BufferedReader( new InputStreamReader( stream ) );
		StringBuilder sb = new StringBuilder();
		String next = r.readLine();
		while (next != null) {
			sb.append(next);
			next = r.readLine();
		}
		assertTrue("Process returnerte ikke ProcessSucceeded", sb.toString().indexOf("ProcessSucceeded") > -1);
	}
}
