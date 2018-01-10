package niva.aquamonitor.data.ws;

import java.io.IOException;
import java.util.Iterator;

public class StringReader extends AquaReader<String> {

	StringReader(AquaWebService webservice, String function) {
		super(webservice, function);
	}

	@Override
	public Iterator<String> iterator() throws IOException {
		return new StringIterator(callJsonService());
	}
	
	class StringIterator implements Iterator<String> {
		
		Iterator<Object> iter;
		
		StringIterator(Iterator<Object> iter) {
			this.iter = iter;
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