package niva.aquamonitor.data.ws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;

public abstract class AquaReader<T> {
	
	private final AquaWebService webservice;
	
	private final String function;
	
	private final List<Argument> arguments;
	
	private Integer timeoutMins  = null;
	
	
	AquaReader(AquaWebService webservice, String function) {
		this.webservice = webservice;
		this.function = function;
		this.arguments = new ArrayList<Argument>();
	}

	void setTimeout(int minutes) {
		this.timeoutMins = minutes;
	}
	
	void addArgument(String parameter) {
		this.arguments.add(new Argument(parameter, null));
	}
	
	void addArgument(String parameter, String value) {
		this.arguments.add(new Argument(parameter, value));
	}
	
	public void setArgument(String parameter, String value) {
		boolean funnet = false;
		int i = 0;
		while (i < this.arguments.size()) {
			if (this.arguments.get(i).getParameter().equals(parameter)) {
				funnet = true;
				break;
			}
			else {
				i++;
			}
		}
		
		if (funnet) {
			this.arguments.get(i).setValue(value);
		}
		else {
			throw new RuntimeException("No argument with parametername:" + parameter);
		}
	}
	

	public int getCount() throws IOException {
		Iterator<T> iter = iterator();
		
		int ret = 0;
		
		while (iter.hasNext()) {
			iter.next();
			ret += 1;
		}
				
		return ret;
	}


	public Envelope getEnvelope() throws IOException {
		Iterator<T> iter = iterator();
		
		double minLat, minLon, maxLat, maxLon;
		
		if (iter.hasNext()) {
			
			PointCargo curr;
			curr = (PointCargo)iter.next();
			
			minLat = curr.latitude;
			maxLat = minLat;
			minLon = curr.longitude;
			maxLon = minLon;
			
			while (iter.hasNext()) {
				
				curr = (PointCargo)iter.next();
				
				final double latitude = curr.latitude;
				final double longitude = curr.longitude;
				
				if (latitude < minLat)
					minLat = latitude;
				else if (latitude > maxLat)
					maxLat = latitude;
				
				if (longitude < minLon)
					minLon = longitude;
				else if (longitude > maxLon)
					maxLon = longitude;
			}
			
			return new Envelope(minLon, maxLon, minLat, maxLat);
		}
		else 
			return new Envelope();
	}
	

	abstract public Iterator<T> iterator() throws IOException;

	
	protected Iterator<Object> callJsonService() throws IOException {

		String url = webservice.getUrl() + "/" + this.function;
		
		if (this.arguments.size() > 0) {
			String params = "";
			
			for (Argument arg : this.arguments) {
				params +=  "&" + arg.parameter + "='" + arg.value + "'";
			}
			url += "?" + params.substring(1);
		}
		
		JsonStreamIterator iter = new JsonStreamIterator(url);
		if (timeoutMins != null) iter.setTimeoutMins(timeoutMins);
		iter.read();
		
		return iter;
	}
	
	class Argument {
		private final String parameter;
		private String value;
		
		public Argument(String parameter,String value) {
			this.parameter = parameter;
			this.value = value;
		}
		
		public String getParameter() {
			return this.parameter;
		}
		
		public String getValue() {
			return this.value;
		}
		
		public void setValue(String value) {
			this.value = value;
		}
	}
}
