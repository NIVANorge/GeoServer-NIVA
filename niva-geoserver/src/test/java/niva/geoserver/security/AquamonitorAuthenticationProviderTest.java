package niva.geoserver.security;

import java.io.IOException;

import javax.servlet.http.Cookie;

import niva.aquamonitor.data.ws.LoginWebService;
import niva.aquamonitor.data.ws.UserCargo;

import org.geoserver.test.GeoServerTestSupport;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.mockrunner.mock.web.MockHttpServletRequest;



public class AquamonitorAuthenticationProviderTest extends GeoServerTestSupport {

	private static AquamonitorAuthenticationProvider authProvider;
	
	@Override
	protected void oneTimeSetUp() throws Exception {
		super.oneTimeSetUp();
		
		authProvider = new AquamonitorAuthenticationProvider();
		authProvider.setSecurityManager(this.getSecurityManager());
	}
	
	public void testPlainAuthentication() {
		
		TestingAuthenticationToken token = new TestingAuthenticationToken("RESA", "RESA");
		Authentication res = authProvider.authenticate(token);
		
		assertNotNull(res);
		assertTrue(res.getAuthorities().contains(AquamonitorAuthenticationProvider.AQUAMONITOR_USER));
	}
	
	public void testCookieAuthentication() throws IOException {
		
		LoginWebService loginServ = LoginWebService.createService();
		UserCargo uc = loginServ.authenticateUser("RESA", "RESA");
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addCookie(new Cookie("aqua_key", uc.token));
		
		Authentication res = authProvider.authenticate(new TestingAuthenticationToken("dummy", "yyy"), request);
		
		assertNotNull(res);
		assertTrue("RESA".equals(res.getName()));
	}

}
