package niva.aquamonitor.data.ws;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LoginControllerTest {
	
	
	@Test
	public void checkLogin() throws Exception {
		final LoginController serv = LoginController.createService();
		UserCargo ret = serv.authenticateUser("RBR", "xxxxxxxxxxxxxx"); // Change this to the appropriate one when failing.
																  // Don't commit
		
		assertEquals(168, ret.userid);
		
		UserCargo ret2 = serv.authenticateToken(ret.token);
		assertEquals("RBR", ret2.username);
	}

	
	@Test
	public void checkWrongToken() throws Exception {
		final LoginController serv = LoginController.createService();
		UserCargo ret = serv.authenticateToken("dummy");
		
		assertEquals("NoUser", ret.usertype);
	}

}
