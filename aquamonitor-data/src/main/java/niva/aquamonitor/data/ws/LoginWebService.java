package niva.aquamonitor.data.ws;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.http.HttpHeaders;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.geotools.util.logging.Logging;



/**
 * Uses the WebService LoginService.asmx with POST method and JSON format.
 * Deprecated - we use a Login controller instead
 * 
 * @author Roar Brænden, NIVA
 *
 */
@Deprecated
public class LoginWebService extends AquaWebService {
	

	
	private static final String SERVICE_ADDRESS = "/WebServices/LoginService.asmx";
	

	/** Logger. */
	private final static Logger LOGGER = Logging.getLogger(LoginWebService.class);
	
	public static LoginWebService createService(String host, String site)    {
		return new LoginWebService(host + site + SERVICE_ADDRESS);
	}
	
	public static LoginWebService createService(String host)    {
		return createService(host, AquaWebService.DEFAULT_SITE);
	}
	
	public static LoginWebService createService() {
		return createService(getHostAddress(), AquaWebService.DEFAULT_SITE);
	}

	
	public LoginWebService(String url) {
		super(url);
	}
	
	public UserCargo authenticateUser(String username, String password) throws IOException {
		return callWebService(getUrl() + "/AuthenticateUser", "{'username':'" + username + "','password':'" + password + "'}");
	}
	
	public UserCargo authenticateToken(String token) throws IOException  {
		return callWebService(getUrl() + "/AuthenticateToken", "{'token':'" + token + "'}");
	}
	
	
	
	private UserCargo callWebService(String url, String json) throws IOException {

		LOGGER.fine("call login at: " + url);
		
		Header accept = new BasicHeader(HttpHeaders.ACCEPT, "application/json");
		
		List<Header> headers = new ArrayList<Header>(1);
		headers.add(accept);
		
		CloseableHttpClient client = HttpClients.custom().setDefaultHeaders(headers).build();
		HttpPost post = new HttpPost(url);
		post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
		
		UserCargo user = null;
		
		try {
		
			CloseableHttpResponse resp = client.execute(post);
			JSONParser parser = new JSONParser();
			
			try {
				HttpEntity entity = resp.getEntity();
				
				if (entity != null) {
					InputStream inp = entity.getContent();
					try {
						Object res = parser.parse(new InputStreamReader(inp));
						Object userJson = ((JSONObject)res).get("d");
						user = readUserJson((JSONObject)userJson);
					}
					catch (ParseException pe) {
						throw new IOException(pe);
					}
					finally {
						inp.close();
					}
				}
			}
			finally {
				resp.close();
			}
		}
		finally {
			post.releaseConnection();
			client.close();
		}

		return user;
	}
	
	private UserCargo readUserJson(JSONObject json) {
		UserCargo user = new UserCargo();
		user.key = (String)json.get("Key");
		user.token = (String)json.get("Token");
		user.userid = (int)(long)json.get("Userid");
		user.username = (String)json.get("Username");
		user.usertype = (String)json.get("Usertype");
		
		return user;
	}
	

}
