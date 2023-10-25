package niva.aquamonitor.data.ws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.util.logging.Logging;
import org.json.simple.parser.ParseException;

/**
 * Stream from a AquaMonitor WebService call returning JSON. 
 *
 * @author Roar Brænden, NIVA
 *
 */
class JsonStreamIterator extends JsonStream implements Iterator<Object> {
	
	private final static Logger LOGGER = Logging.getLogger(JsonStreamIterator.class);

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
		super(url);
	}
	
	/**
	 * Constructor with a url and token.
	 */
    JsonStreamIterator(String url, String token) throws IOException {
        super(url, token);
    }
	
    /**
     * Constructor with a timeout, but without token
     */
    JsonStreamIterator(String url, Integer timeoutMins) throws IOException {
        super(url, timeoutMins);
    }
    
	/**
	 * Constructor with both token and timeout
	 */
   JsonStreamIterator(String url, String token, Integer timeoutMins) throws IOException {
        super(url, token, timeoutMins);
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
