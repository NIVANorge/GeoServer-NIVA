package niva.aquamonitor.data.ws;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Used as an inner class inside a Reader. Each Reader should extend with a mapping to their json-structure.
 * 
 * @author Roar Brænden
 *
 */
public abstract class JsonMapper<T> implements Iterator<T>  {
	
	Iterator<Object> iter;
	
	JsonMapper(Iterator<Object> iter) {
		this.iter = iter;
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
	
	
	protected int getInteger(Map<String, Object> current, String key) {
		return (int)(long)current.get(key);
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
