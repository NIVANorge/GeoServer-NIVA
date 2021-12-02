package niva.aquamonitor.data.ws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.locationtech.jts.geom.Envelope;

/**
 * Base functionality for reading content from a webservice in AquaMonitor.
 * Content could be one of the cargo classes. hich also has a super Reader class.
 * 
 * @author Roar Brænden, NIVA
 *
 * @param <T>
 */
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
	
	/**
	 * Set a new value for parameter. Use addParameter for new parameters.
	 * 
	 * @param parameter A exsiting parameter
	 * @param value New value for parameter
	 * @throw IllegalArgumentException If parameter doesn't exist
	 */
	public void setArgument(String parameter, String value) {
	    this.arguments.stream()
	            .filter(param -> param.getParameter().equals(parameter))
	            .findFirst()
	            .orElseThrow(() -> new IllegalArgumentException("No argument with parametername:" + parameter))
	            .setValue(value);
	}
	

	/**
	 * Get the number of points
	 * 
	 * @return
	 * @throws IOException
	 */
	public int getCount() throws IOException {
	    return (int) stream().count();
	}


	/**
	 * Get envelope expressed as minLon,maxLon,minLat,maxLat
	 * 
	 * @return Empty Envelope in cases of no points
	 * @throws IOException
	 */
	public Envelope getEnvelope() throws IOException {
	    final Envelope env = new Envelope();
	    stream().map((next -> (PointCargo)next))
	            .forEach(point -> env.expandToInclude(point.longitude, point.latitude));
		return env;
	}
	

	/**
	 * Super classes should return an iterator on TCargo. Often by wrapping a call to callJsonService.
	 * @return
	 * @throws IOException
	 */
	public abstract CloseableIterator<T> iterator() throws IOException;
	
	public Stream<T> stream() throws IOException {
	    final CloseableIterator<T> iter = iterator();
	    
	    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iter,
	                    Spliterator.DISTINCT), false)
	            .onClose(() -> iter.close());
	}

	/**
	 * Creates the url, and returns a new JsonStreamIterator.
	 * timeoutMins could be set prior to this call.
	 */
	protected JsonStreamIterator callJsonService() throws IOException {

	    final StringBuilder builder = new StringBuilder();
		builder.append(webservice.getUrl()).append("/").append(this.function);
		String prefix = "?";
		for (Argument arg : arguments) {
		    builder.append(prefix)
		           .append(arg.getParameter())
		           .append("='")
		           .append(arg.getValue())
		           .append("'");
		    prefix = "&";
		}
		
		final String url = builder.toString();
		return (timeoutMins != null ? new JsonStreamIterator(url, timeoutMins) 
		                            : new JsonStreamIterator(url));
	}
	
	class Argument {
		private final String parameter;
		private String value;
		
		/**
		 * Initialise with a name and value.
		 * 
		 * @param parameter
		 * @param value
		 */
		Argument(final String parameter, final String value) {
			this.parameter = parameter;
			this.value = value;
		}
		
		/**
		 * Get the name of the parameter
		 * @return
		 */
		String getParameter() {
			return this.parameter;
		}
		
		/**
		 * Get the value
		 * @return
		 */
		String getValue() {
			return this.value;
		}
		
		/**
		 * Change the value from the original
		 * @param value
		 */
		void setValue(String value) {
			this.value = value;
		}
	}
}
