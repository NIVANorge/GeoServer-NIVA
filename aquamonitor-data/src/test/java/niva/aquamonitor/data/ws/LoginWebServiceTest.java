package niva.aquamonitor.data.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class LoginWebServiceTest {
	
	

	@Test
	public void checkLogin() throws Exception {
		LoginWebService serv = LoginWebService.createService();
		UserCargo ret = serv.authenticateUser("RBR", "RBR");
		
		assertEquals(168, ret.userid);
	}
	
	@Test
	public void checkAuthenticateToken() throws Exception {
		LoginWebService serv = LoginWebService.createService();
		UserCargo ret = serv.authenticateUser("RBR", "RBR");
		assertNotNull(ret.token);
		
		UserCargo ret2 = serv.authenticateToken(ret.token);
		assertEquals("RBR", ret2.username);
	}

	
	@Test
	public void chechWrongToken() throws Exception {
		LoginWebService serv = LoginWebService.createService();
		UserCargo ret = serv.authenticateToken("dummy");
		
		assertEquals("NoUser", ret.usertype);
	}

}
