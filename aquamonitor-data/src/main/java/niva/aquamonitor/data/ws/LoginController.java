package niva.aquamonitor.data.ws;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.StringEntity;
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

		try (CloseableHttpClient client = HttpClients.createDefault()) {
		    HttpPost post = new HttpPost(url);
		    post.setHeader(HttpHeaders.ACCEPT, "application/json");
	        post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

	        RequestConfig config = RequestConfig.custom()
                                                .setCookieSpec(null)
                                                .build();
	        post.setConfig(config);

	        try (CloseableHttpResponse resp = client.execute(post)) {
	            HttpEntity entity = resp.getEntity();
	            if (entity != null) {
	                try (InputStream inp = entity.getContent()) {
	                    Object res = new JSONParser().parse(new InputStreamReader(inp));
	                    return readUserJson((JSONObject)res);
	                }
	                catch (ParseException pe) {
	                    throw new IOException("AquaMonitor server responded with an illegal JSON.", pe);
	                }
	            }
	            else {
	                throw new IOException("AquaMonitor server didn't provide any response.");
	            }
	        }
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
