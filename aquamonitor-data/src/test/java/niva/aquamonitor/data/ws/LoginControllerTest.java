package niva.aquamonitor.data.ws;

import static org.junit.Assert.assertEquals;
import org.junit.Assume;
import org.junit.Test;

public class LoginControllerTest {
	
	
	@Test
	public void checkLogin() throws Exception {
	    Assume.assumeTrue("Test is based on RBR as test user.", 
	            "RBR".equalsIgnoreCase(TestAuthentication.getUsername()));
	    
		final LoginController serv = LoginController.createService();
		UserCargo ret = serv.authenticateUser("RBR",
		                                        TestAuthentication.getPassword());
																  
		
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
