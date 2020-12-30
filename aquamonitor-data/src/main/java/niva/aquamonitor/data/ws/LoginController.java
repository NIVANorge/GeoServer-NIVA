package niva.aquamonitor.data.ws;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

import org.geotools.util.logging.Logging;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Calling /login at a AquaMonitor site
 * 
 * @author Roar Brænden, NIVA
 *
 */
public class LoginController extends AquaWebService {
	
	private static final Logger LOGGER = Logging.getLogger(LoginController.class);
	

	public LoginController(String url) {
		super(url);
	}

	public static LoginController createService() {
		return createService(getHostAddress(), DEFAULT_SITE);
	}
	

	public static LoginController createService(String host, String site) {
		return new LoginController(host + site + "/login");
	}
	
	public UserCargo authenticateUser(String username, String password) throws IOException {
		return callPost(getUrl(), "{'username':'" + username + "','password':'" + password + "'}");
	}
	
	public UserCargo authenticateToken(String token) throws IOException  {
		return callPost(getUrl(), "{'token':'" + token + "'}");
	}
	
	private UserCargo callPost(String url, String json) throws IOException {
		LOGGER.fine("call login at: " + url);
		
		List<Header> headers = new ArrayList<Header>(1);
		headers.add(new BasicHeader(HttpHeaders.ACCEPT, "application/json"));
		
		CloseableHttpClient client = HttpClients.custom().setDefaultHeaders(headers).build();
		HttpPost post = new HttpPost(url);
		post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

		try {
		
			CloseableHttpResponse resp = client.execute(post);
			JSONParser parser = new JSONParser();
			
			try {
				HttpEntity entity = resp.getEntity();
				if (entity != null) {
					InputStream inp = entity.getContent();
					try {
						Object res = parser.parse(new InputStreamReader(inp));
						return readUserJson((JSONObject)res);
					}
					catch (ParseException pe) {
						throw new IOException("AquaMonitor server responded with an illegal JSON.", pe);
					}
					finally {
						inp.close();
					}
				}
				else {
					throw new IOException("AquaMonitor server didn't provide any response.");
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
	}
	
	private UserCargo readUserJson(JSONObject json) throws IOException {
		if (json.containsKey("Message")) {
			throw new IOException("AquaMonitor responded with: " + (String)json.get("Message"));
		}
		
		UserCargo user = new UserCargo();
		user.key = (String)json.get("Key");
		user.token = (String)json.get("Token");
		user.userid = (json.get("Userid") == null ? 0 : ((Long)json.get("Userid")).intValue());
		user.username = (String)json.get("Username");
		user.usertype = (String)json.get("Usertype");
		
		return user;
	}
	

}
