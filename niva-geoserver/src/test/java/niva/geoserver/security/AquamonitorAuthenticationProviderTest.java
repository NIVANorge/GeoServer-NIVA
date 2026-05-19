package niva.geoserver.security;

import java.io.IOException;

import jakarta.servlet.http.Cookie;

import niva.aquamonitor.data.ws.LoginController;
import niva.aquamonitor.data.ws.UserCargo;

import org.geoserver.test.GeoServerTestSupport;
import org.junit.Assert;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.mock.web.MockHttpServletRequest;



public class AquamonitorAuthenticationProviderTest extends GeoServerTestSupport {

	private static AquamonitorAuthenticationProvider authProvider;
	
	private static final String TEST_USERNAME = "Ostfold";
	private static final String TEST_PASSWORD = "Ostfold";
	
	
	@Override
	protected void oneTimeSetUp() throws Exception {
		super.oneTimeSetUp();
		
		authProvider = new AquamonitorAuthenticationProvider();
		authProvider.setSecurityManager(this.getSecurityManager());
	}
	
	public void testPlainAuthentication() {
		
		TestingAuthenticationToken token = new TestingAuthenticationToken(TEST_USERNAME, TEST_PASSWORD);
		Authentication res = authProvider.authenticate(token);
		
		Assert.assertNotNull(res);
		Assert.assertTrue(res.getAuthorities()
		        .stream()
		        .anyMatch(predicate -> predicate instanceof AquamonitorAuthenticationProvider.AquamonitorUserRole));
	}
	
	public void testCookieAuthentication() throws IOException {
		
		LoginController loginServ = LoginController.createService();
		UserCargo uc = loginServ.authenticateUser(TEST_USERNAME, TEST_PASSWORD);
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setCookies(new Cookie("aqua_key", uc.token));
		
		Authentication res = authProvider.authenticate(new TestingAuthenticationToken("dummy", "yyy"), request);
		
		Assert.assertNotNull(res);
		Assert.assertTrue(TEST_USERNAME.equals(res.getName()));
	}

}
