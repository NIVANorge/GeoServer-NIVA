package niva.aquamonitor.data.ws;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.util.logging.Logging;

/**
 * Each implementing mapCargo to read a json into the given cargo-object.
 * The argument for constructor could come from a call to callJsonService.
 * 
 * @author Roar Brænden
 *
 */
abstract class JsonMapper<T> extends CloseableIterator<T>  {
    
    private final static Logger LOGGER = Logging.getLogger(JsonMapper.class);

	JsonMapper(JsonStreamIterator iter) {
		super(iter);
	}
	
	@Override
	public boolean hasNext() {
		return iter.hasNext();
	}

	@SuppressWarnings("unchecked")
	@Override
	public T next() {
		final Map<String, Object> curr = (Map<String, Object>)iter.next();
		return mapCargo(curr);
	}
	
	public abstract T mapCargo(Map<String, Object> currJson);
	

	@Override
	public void remove() {
	}
	
	
	protected Integer getInteger(Map<String, Object> current, String key) {
		Object val = current.get(key);
		if (val == null)
			return null;
		
		return (int)(long)current.get(key);
	}
	
	/**
	 * Return a parsed representation of the date.
	 * Prepared for the format AquaMonitor uses.
	 */
	protected Date getDate(Map<String, Object> current, String key) {
	    Object val = current.get(key);
	    if (val instanceof String) {
	        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ssZ");
	        try {
                return formatter.parse((String)val);
            } catch (ParseException e) {
                LOGGER.log(Level.SEVERE, "Error while parsing the value in key:" + key + " to a date. The string was:" + val, e);
                return null;
            }
	    }
	    else {
	        return null;
	    }
	}
	
	protected double getDouble(Map<String, Object> current, String key) {
		Object val = current.get(key);
		if (val instanceof Double)
			return (double)val;
		else if (val instanceof Long)
			return ((Long) val).doubleValue();
		else if (val == null)
			throw new RuntimeException("Missing value for key:" + key);
		else
			throw new RuntimeException("Unknown type:" + val.getClass() + " for getDouble.");
	}
	
	protected String getString(Map<String, Object> current, String key) {
		return (String)current.get(key);
	}
	
	/**
	 * Returns an array of Strings. If the key isn't present we'll return an empty array.
	 * @param current
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected String[] getStringArr(Map<String, Object> current, String key) {
		Object arr = current.get(key);
		if (arr != null) {
			if (arr instanceof List<?>) {
				List<String> l = (List<String>)arr;
				
				String[] res = new String[l.size()];
				l.toArray(res);
				return res;
			}
			else {
				throw new RuntimeException("Unknown type:" + arr.getClass() + " for getStringArr.");
			}
		}
		else {
			return new String[] {};
		}
	}
}
