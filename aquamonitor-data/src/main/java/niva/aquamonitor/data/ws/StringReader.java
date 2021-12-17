package niva.aquamonitor.data.ws;

import java.io.IOException;

public class StringReader extends AquaReader<String> {

	StringReader(AquaWebService webservice, String function) {
		super(webservice, function);
	}
	
	StringReader(AquaWebService webservice, String path, String token) {
	    super(webservice, path, token);
	}

	@Override
	public CloseableIterator<String> iterator() throws IOException {
		return new StringIterator(callJsonService());
	}
	
	class StringIterator extends CloseableIterator<String> {
		
		StringIterator(JsonStreamIterator iter) {
			super(iter);
		}
		
		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public String next() {
			return (String)iter.next();
		}

		@Override
		public void remove() {
			iter.remove();
		}
	}
}