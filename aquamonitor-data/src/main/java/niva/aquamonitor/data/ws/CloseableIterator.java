package niva.aquamonitor.data.ws;

import java.util.Iterator;

/**
 * Wrapper iterator class above a JsonStreamIterator implementing the closing.
 * 
 * @author Roar Brænden
 *
 * @param <T>
 */
public abstract class CloseableIterator<T> implements Iterator<T>, AutoCloseable {
    
    protected final JsonStreamIterator iter;
    
    CloseableIterator(JsonStreamIterator iter) {
        this.iter = iter;
    }

    @Override
    public void close() {
        this.iter.close();
    }

}
