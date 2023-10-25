package niva.aquamonitor.data.ws;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.geotools.util.logging.Logging;
import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public abstract class JsonStream implements ContentHandler, AutoCloseable  {
	
	private final static Logger LOGGER = Logging.getLogger(JsonStream.class);

    private final String url;
	private final CloseableHttpClient client;
	private final String token;
	private final Integer timeoutMins;
	
	private CloseableHttpResponse response;
	
	protected JSONParser parser;
	
	protected InputStreamReader reader;

	protected boolean hasReadHeader = false;
	
	/**
	 * Constructor with a url and no token.
	 * Used for situations without Authorization.
	 */
	JsonStream(String url) throws IOException {
		this.url = url;
		this.token = null;
		this.timeoutMins = null;
		this.client = initHttp();
		read();
	}
	
	/**
	 * Constructor with a url and token.
	 */
    JsonStream(String url, String token) throws IOException {
        this.url = url;
        this.token = token;
        this.timeoutMins = null;
        this.client = initHttp();
        read();
    }
	
    /**
     * Constructor with a timeout, but without token
     */
    JsonStream(String url, Integer timeoutMins) throws IOException {
        this.url = url;
        this.token = null;
        this.client = initHttp();
        this.timeoutMins = timeoutMins;
        read();
    }
    
	/**
	 * Constructor with both token and timeout
	 */
   JsonStream(String url, String token, Integer timeoutMins) throws IOException {
        this.url = url;
        this.token = token;
        this.client = initHttp();
        this.timeoutMins = timeoutMins;
        read();
    }
	
	private CloseableHttpClient initHttp() throws IOException {
	    if (token == null) {
	        return HttpClients.createDefault();
	    }
        
	    try {
	        BasicClientCookie cookie = new BasicClientCookie("aqua_key", token);
            cookie.setDomain(new URL(url).getHost());
            cookie.setPath("/");
            
            BasicCookieStore cookieStore = new BasicCookieStore();
            cookieStore.addCookie(cookie);
            return HttpClients.custom()
                              .setDefaultCookieStore(cookieStore)
                              .build();
        } catch (MalformedURLException e) {
            throw new IOException("Wrong url:" + url);
        }
	}
	
	
	/**
	 * Resources are closed within endJson.
	 * 
	 * @throws IOException
	 */
	private void read() throws IOException {
		LOGGER.info("Calling webservice: " + url);
			
		final HttpGet get = new HttpGet(url);
		get.setHeader(HttpHeaders.ACCEPT, "application/json");
		Builder configBuilder = RequestConfig.custom()
		        .setCookieSpec(CookieSpecs.STANDARD)
                .setConnectTimeout(5 * 1000);
		if (timeoutMins != null) {
            configBuilder.setSocketTimeout(timeoutMins * 60 * 1000);
            LOGGER.fine(String.format("Timeout set to %d minutes", timeoutMins));
		}
        get.setConfig(configBuilder.build());
		try {
		    response = client.execute(get);
			int respStatusCode = response.getStatusLine().getStatusCode();
			if (respStatusCode == HttpStatus.SC_NOT_FOUND) {
				throw new IOException("Url: " + this.url + " responds with http status code 404.");
			}
            HttpEntity entity = response.getEntity();
			if (entity != null) {
				parser = new JSONParser();
				reader = new InputStreamReader(entity.getContent(), "utf-8");
				try  {
					parser.parse(reader, this);
					hasReadHeader = true;
				}
				catch (ParseException pe) {
					
					if ("Unexpected character (<) at position 0.".equals(pe.toString())) {
						throw new IOException("WebService seems to return HTML instead of Json.");
					}
					else {
						LOGGER.warning(pe.toString());
						throw new IOException("Json parser exception.");
					}
				}
			}
		}
		catch (SSLHandshakeException ex) {
			if (ex.getMessage().startsWith("Unsupported curveId")) {
				throw new IOException("The host " + get.getURI().getHost() + " doesn't respond correct on a HTTPS request.", ex);
			}
			else {
				LOGGER.warning(ex.toString());
				throw ex;
			}
		}
	}

	

    @Override
    public void close() {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException ex) {
                LOGGER.warning("Exception while closing reader: " + ex.getMessage());
            }
        }
        
        if (response != null) {
            try {
                response.close();
            }
            catch (IOException e) {
                LOGGER.warning("Exception while closing response: " + e.getMessage());
            }
        }
        
        if (client != null) {
            try {
                client.close();
            } catch (IOException ex) {
                LOGGER.warning("Exception while closing client: " + ex.getMessage());
            }
        }
    }
}
