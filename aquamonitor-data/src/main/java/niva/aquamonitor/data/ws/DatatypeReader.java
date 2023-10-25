package niva.aquamonitor.data.ws;

import java.io.IOException;
import java.util.Map;

import org.json.simple.parser.ParseException;
import org.locationtech.jts.geom.Envelope;

/** Read a url with datatypepoints in it. That delivers DatatypeCargo. */
public class DatatypeReader extends AquaReader<DatatypeCargo>{
	
	DatatypeReader(AquaWebService webservice, String path, String token) {
	    super(webservice, path, token);
	}
	
	@Override
	public CloseableIterator<DatatypeCargo> iterator() throws IOException {
		return new DatatypeIterator(callJsonService());
	}
	
	/** Call /extent to get envelope directly from server. */
	@Override
	public Envelope getEnvelope() {
		StringBuilder builder = initUrlStringBuilder();
		builder.append("/extent");
		appendQueryParameters(builder);
		final String url = builder.toString();
		try (EnvelopeStream stream = new EnvelopeStream(url, token)) {
			return stream.getEnvelope();
		} catch (IOException e) {
			throw new RuntimeException("Couldn't read envelope from " + url);
		}
	}
	
	/** Turn a Json stream into an Envelope */
	private static class EnvelopeStream extends JsonStream {
		
		private String nextEntry;

		private Double minLatitude;
		
		private Double maxLatitude;
		
		private Double minLongitude;
		
		private Double maxLongitude;
		
		EnvelopeStream(String url, String token) throws IOException {
			super(url, token);
		}
		
		public Envelope getEnvelope() {
			return new Envelope(minLongitude, maxLongitude, minLatitude, maxLatitude);
		}

		@Override
		public boolean startObjectEntry(String key) throws ParseException, IOException {
			nextEntry = key;
			return true;
		}

		@Override
		public boolean primitive(Object value) throws ParseException, IOException {
			switch (nextEntry) {
			case "MinLatitude" : minLatitude = (Double)value; break;
			case "MaxLatitude" : maxLatitude = (Double)value; break;
			case "MinLongitude" : minLongitude = (Double)value; break;
			case "MaxLongitude" : maxLongitude = (Double)value; break;
			}
			return true;
		}


		@Override
		public void startJSON() throws ParseException, IOException {
		}

		@Override
		public void endJSON() throws ParseException, IOException {
		}

		@Override
		public boolean startObject() throws ParseException, IOException {
			return true;
		}

		@Override
		public boolean endObject() throws ParseException, IOException {
			return true;
		}
		@Override
		public boolean endObjectEntry() throws ParseException, IOException {
			return true;
		}

		@Override
		public boolean startArray() throws ParseException, IOException {
			throw new UnsupportedOperationException("Shouldn't get an array in this JSON.");
		}

		@Override
		public boolean endArray() throws ParseException, IOException {
			throw new UnsupportedOperationException("Shouldn't get an array in this JSON.");
		}

	}
	
	/** Convert Json into plain Java Object of type DatatypeCargo */
	private static class DatatypeIterator extends JsonMapper<DatatypeCargo>{

		DatatypeIterator(JsonStreamIterator iter) {
			super(iter);
		}
		
		public DatatypeCargo mapCargo(Map<String, Object> curr) {
			final DatatypeCargo cargo = new DatatypeCargo();
			cargo.stationId = getInteger(curr, "StationId");
			cargo.latitude = getDouble(curr, "Latitude");
			cargo.longitude = getDouble(curr, "Longitude");
			cargo.stationTypeId = getInteger(curr, "StationTypeId");
			cargo.stationType = getString(curr, "StationType");
			cargo.datatypes = getStringArr(curr, "Datatypes");
			
			return cargo;
		}
	}
}
