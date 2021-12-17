package niva.aquamonitor.data.ws;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLHandshakeException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.HttpEntity;
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

/**
 * Stream from a AquaMonitor WebService call returning JSON. 
 *
 * @author Roar Brænden, NIVA
 *
 */
class JsonStreamIterator implements Iterator<Object>, ContentHandler, AutoCloseable {
	
	private final static Logger LOGGER = Logging.getLogger(JsonStreamIterator.class);

    private final String url;
	private final CloseableHttpClient client;
	private final String token;
	private final Integer timeoutMins;
	private CloseableHttpResponse response;
	private InputStreamReader reader;
	private JSONParser parser;
	private boolean hasReadHeader = false;
	private boolean hasMessage = false;
	private String actKey = null;
	private List<Object> actArray;
	private Object nextObject = null;
	private boolean isDone = false;

	
	/**
	 * Constructor with a url and no token.
	 * Used for situations without Authorization.
	 */
	JsonStreamIterator(String url) throws IOException {
		this.url = url;
		this.token = null;
		this.timeoutMins = null;
		this.client = initHttp();
		read();
	}
	
	/**
	 * Constructor with a url and token.
	 */
    JsonStreamIterator(String url, String token) throws IOException {
        this.url = url;
        this.token = token;
        this.timeoutMins = null;
        this.client = initHttp();
        read();
    }
	
    /**
     * Constructor with a timeout, but without token
     */
    JsonStreamIterator(String url, Integer timeoutMins) throws IOException {
        this.url = url;
        this.token = null;
        this.client = initHttp();
        this.timeoutMins = timeoutMins;
        read();
    }
    
	/**
	 * Constructor with both token and timeout
	 */
   JsonStreamIterator(String url, String token, Integer timeoutMins) throws IOException {
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
                .setConnectTimeout(1000);
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
	public boolean hasNext() {
	    if (nextObject != null) {
	        return true;
	    }
	    if (isDone) {
	        return false;
	    }
	    
		try {
			parser.parse(reader, this, true);
		} catch (IOException ie) {
			LOGGER.warning(ie.toString());
		} catch (ParseException pe) {
			LOGGER.warning(pe.toString());
		}
		if (nextObject == null) {
		    isDone = true;
		    return false;
		}
		else {
		    return true;
		}
	}

	@Override
	public Object next() {
	    if (!hasNext()) {
	        throw new NoSuchElementException();
	    }

	    synchronized(nextObject) {
	        if (LOGGER.isLoggable(Level.FINE)) {
	            LOGGER.fine(nextObjectAsString()); 
	        }
	  
    		final Object ret = nextObject;
    		nextObject = null;
    		return ret;
	    }
	}

	@Override
	public void remove() {
	    throw new UnsupportedOperationException("We don't support this.");
	}

	@Override
	public void startJSON() throws ParseException, IOException {
	}


	/**
	 * We close all streams when we're done with reading the json.
	 * An explicit close call is harder to implement.
	 */
	@Override
	public void endJSON() throws ParseException, IOException {

	}


	@Override
	public boolean startObject() throws ParseException, IOException {
		if (hasReadHeader) {
			nextObject = new HashMap<String, Object>();
		}
		return true;
	}


	@Override
	public boolean endObject() throws ParseException, IOException {
		return false; // Stop parsing at the end of every object
	}


	/**
	 * If a json object has a property named Message, we will set the hasMessage to true.
	 * This will be an Exception.
	 */
	@Override
	public boolean startObjectEntry(String key) throws ParseException, IOException {
		if (hasReadHeader) {
			actKey = key;
		}
		else if ("Message".equals(key)) {
			hasMessage = true;
		}
		
		return true;
	}


	@Override
	public boolean endObjectEntry() throws ParseException, IOException {
		if (hasReadHeader) {
			actKey = null;
		}
		return true;
	}


	@Override
	public boolean startArray() throws ParseException, IOException {
		if (hasReadHeader) {
			actArray = new ArrayList<Object>();
			return true;
		}
		else {
			return false; // Stop initial parsing at the start of the array.
		}
	}


	@SuppressWarnings("unchecked")
	@Override
	public boolean endArray() throws ParseException, IOException {
		if (actArray != null) {
			((Map<String, Object>)nextObject).put(actKey, actArray);
			actArray = null;
			return true;
		}
		else {
			nextObject = null;
			return true;
		}
	}


	@SuppressWarnings("unchecked")
	@Override
	public boolean primitive(Object value) throws ParseException, IOException {
		if (hasReadHeader) {
			if (actArray != null) {
				actArray.add(value);
				return true;
			}
			else if (actKey != null) {
			    if (nextObject == null) {
			        throw new IOException(
			                String.format("Json couldn't be parsed. actKey:(%s) value:(%s)", actKey, value));
			    }
				((Map<String, Object>)nextObject).put(actKey, value);
				return true;
			}
			else {
				nextObject = value;
				return false;
			}
		}
		else if (hasMessage) {
		    if ("Bruker er ikke satt.".equals(value)) {
		        throw new IOException("Given token wasn't accepted.");
		    }
			throw new IOException((String)value);
		}
		else {
			return true;
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
    
    @SuppressWarnings("unchecked")
    private String nextObjectAsString() {
        if (nextObject == null) {
            return "{}";
        }
        if (!(nextObject instanceof Map)) {
            return nextObject.toString();
        }

        StringBuilder builder = new StringBuilder();
        builder.append("{");
        Set<String> keys = ((Map<String, Object>)nextObject).keySet();
        for(String key : keys) {
            builder.append(key);
            Object value = ((Map<String, Object>)nextObject).get(key);
            if (value != null) {
                builder.append(":").append(value.toString());
            }
            builder.append(",");
        }
        builder.deleteCharAt(builder.lastIndexOf(","));
        builder.append("}");
        return builder.toString();
    }
}
