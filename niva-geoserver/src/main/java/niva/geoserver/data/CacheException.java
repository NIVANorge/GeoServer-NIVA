package niva.geoserver.data;

/**
 * Exception for cache problems
 * 
 * Used to start a transaction rollback when cache problems occur
 *
 */
public class CacheException extends RuntimeException {

    /** serialVersionUID */
    private static final long serialVersionUID = 5725618984701350769L;

    public CacheException(String text, Throwable reason) {
        super(text, reason);
    }
}
