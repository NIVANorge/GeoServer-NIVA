package niva.aquamonitor.data.ws;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

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
	private CloseableHttpResponse response;
	private InputStreamReader reader;
	private JSONParser parser;
	
	private boolean hasReadHeader = false;
	private boolean hasMessage = false;
	
	private String actKey = null;
	private List<Object> actArray;
	
	private Object nextObject = null;
	
	private Integer timeoutMins = null;
	
	JsonStreamIterator(String url) throws IOException {
		this.url = url;
		this.client = initHttp();
		read();
	}
	
	JsonStreamIterator(String url, Integer timeoutMins) throws IOException {
	    this.url = url;
        this.client = initHttp();
        this.timeoutMins = timeoutMins;
        read();
	}
	
	private CloseableHttpClient initHttp() {
	    final Header accept = new BasicHeader(HttpHeaders.ACCEPT, "application/json");
        return HttpClients.custom().setDefaultHeaders(Collections.singletonList(accept)).build();
	}
	
	/**
	 * Resources are closed within endJson.
	 * 
	 * @throws IOException
	 */
	private void read() throws IOException {
		LOGGER.fine("Calling webservice: " + url);
			
		HttpGet get = new HttpGet(url);
		
		if (timeoutMins != null) {
			RequestConfig config = RequestConfig.custom()
												.setConnectTimeout(1000)
												.setSocketTimeout(timeoutMins * 60 * 1000)
												.build();
			get.setConfig(config);
		}

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

		try {
			parser.parse(reader, this, true);
		} catch (IOException ie) {
			LOGGER.warning(ie.toString());
		} catch (ParseException pe) {
			LOGGER.warning(pe.toString());
		}
		
		return (nextObject != null);
	}

	@Override
	public Object next() {
	    if (!hasNext()) {
	        throw new NoSuchElementException();
	    }
    
		Object ret = nextObject;
		nextObject = null;
		return ret;	
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
				((Map<String, Object>)nextObject).put(actKey, value);
				return true;
			}
			else {
				nextObject = value;
				return false;
			}
		}
		else if (hasMessage) {
			throw new IOException(String.format("Call for %s got the error response:\n%s",  this.url, value));
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
}
