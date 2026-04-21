package niva.geoserver.cache;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geoserver.platform.GeoServerExtensions;
import org.geotools.api.data.DataStore;
import org.geotools.data.DefaultTransaction;
import org.geotools.api.data.FeatureReader;
import org.geotools.api.data.FeatureWriter;
import org.geotools.api.data.LockingManager;
import org.geotools.api.data.Query;
import org.geotools.api.data.ServiceInfo;
import org.geotools.api.data.Transaction;
import org.geotools.feature.NameImpl;
import org.geotools.util.logging.Logging;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.Name;
import org.geotools.api.filter.Filter;
import org.springframework.scheduling.TaskScheduler;


/**
 * DataStore that handles a set of Caches.
 * @author Roar Brænden, NIVA
 *
 */
public class CacheDataStore implements DataStore {
	
	
	private final DataStore backendDataStore;
	
	private final DataStore cacheStore;
	
	private static Logger LOGGER = Logging.getLogger(CacheDataStore.class);
	
	private final String cacheNameFormat;
	
	private final int intervalMinutes;
	
	private ScheduledFuture<?> scheduledRefresh = null;
	
	/**
	 * Constructor for a single CacheDataStore without automatic refresh using type name from backend.
	 * 
	 * @param backend
	 * @param cache DataStore used as cache. Should not be null.
	 */
	public CacheDataStore(DataStore backend, DataStore cache) {
		this(backend, cache, null, 0);
	}
	
	/**
	 * Constructor for a single CacheDataStore without automatic refresh.
	 * 
	 * @param backend
	 * @param cache DataStore used as cache. Should not be null.
	 * @param cacheNameFormat Format for cache type names. As used in String.format with backend type name as input. %1s used as default.
	 */
	public CacheDataStore(DataStore backend, DataStore cache, String cacheNameFormat) {
		this(backend, cache, cacheNameFormat, 0);
	}
	
	/**
	 * Constructor for a single CacheDataStore.
	 * 
	 * @param backend
	 * @param cache DataStore used as cache. Should not be null.
	 * @param cacheNameFormat Format for cache type names. As used in String.format with backend type name as input. %1s used as default.
	 * @param intervalMinutes Interval in minutes for automatic refresh of cache. 0 = no automatic refresh.
	 */
	public CacheDataStore(DataStore backend, DataStore cache, String cacheNameFormat, int intervalMinutes) {
		if (backend == null) {
			throw new IllegalArgumentException("Backend dataStore is null.");
		}
		if (cache == null) {
			throw new IllegalArgumentException("Cache dataStore is null.");
		}
		LOGGER.info("Creating CacheDataStore for backend: " + backend.getInfo().getTitle());
		this.backendDataStore = backend;
		this.cacheStore = cache;
		this.cacheNameFormat = (cacheNameFormat == null ? "%1s" : cacheNameFormat);
		this.intervalMinutes = intervalMinutes;
		if (intervalMinutes > 0) {
			GeoServerExtensions.extensions(CacheScheduler.class).get(0).schedule( this );
		}
	}
	
	public void initScheduledRefresh(TaskScheduler taskScheduler) {
		ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(new RefreshCacheDataStore(), Duration.ofMinutes(intervalMinutes).toMillis());
		LOGGER.info("Init CacheDataStore(" + getInfo().getTitle() + ") will start refresh each " + intervalMinutes + " minutes.");
		scheduledRefresh = future;
	}
	
	/**
	 * Calling refresh for each featureSource in the cache datastore representing a backend featureSource.
	 * Using a map of local names based on cacheNameFormat with it's counterpart in backend.
	 * Avoiding those featureSource's in cache datastore that doesn't represent a backend featuresource.
	 *  <p>Called at a given interval by a scheduler.</p>
	 */
	private class RefreshCacheDataStore implements Runnable {
	    
		/**
		 * Refresh data store. Iterate each feature type with function call callRefresh.
		 */
		@Override
	    public void run() {
            LOGGER.info("Refresh CacheDataStore(" + getInfo().getTitle() + ") startet.");
            try {
                final Map<String, Name> lookupCacheNames = new HashMap<>();
                CacheDataStore.this.getNames()
                        .forEach(name -> lookupCacheNames.put(getCacheTypeName(name), name));
                
                Arrays.stream(CacheDataStore.this.cacheStore.getTypeNames())
                        .map(lookupCacheNames::get)
                        .filter(Objects::nonNull)
                        .forEach(this::callRefresh);
            }
            catch (Exception ex) {
                LOGGER.severe("Refresh CacheDataStore(" 
                        + getInfo().getTitle() + ") ended with unknown exception:" + ex.toString());
            }
            LOGGER.info("Refresh CacheDataStore(" + getInfo().getTitle() + ") ended.");
        }
	    
	    /**
	     * Calling refresh on a singular featureSource.
	     * Catching exception's within this method avoids disturbing other featuresources.
	     */
	    private void callRefresh(Name name) {
	        LOGGER.info("Refresh CacheFeatureSource(" + getInfo().getTitle() + "." 
	                                                  + name.getLocalPart() +")");
	        
	        Transaction transaction = new DefaultTransaction();
	        try {
                final CacheFeatureStore source = getFeatureSource(name);
                source.setTransaction(transaction);
                source.refresh();
                transaction.commit();
            }
	        catch (CacheException e) {
	            try {
                    transaction.rollback();
                } catch (IOException e1) {
                    LOGGER.log(Level.SEVERE, "Error rolling back transaction.", e);
                }
	            LOGGER.log(Level.SEVERE, "Refresh CacheDataStore(" + getInfo().getTitle() 
	                    + "." + name.getLocalPart() + ") ended with a CacheException.", e);
	        }
            catch (IOException ie) {
                LOGGER.warning("Refresh CacheDataStore(" 
                        + getInfo().getTitle() + "." + name.getLocalPart() 
                        + ") got an IOException:" + ie.toString());
            }
	        finally {
	            try {
                    transaction.close();
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error closing transaction.", e);
                }
	        }
	    }
	}
	

	@Override
	public ServiceInfo getInfo() {
		return backendDataStore.getInfo();
	}
	

	@Override
	public SimpleFeatureType getSchema(String typeName) throws IOException {
		return backendDataStore.getSchema(typeName);
	}
	

	@Override
	public SimpleFeatureType getSchema(Name name) throws IOException {
		return backendDataStore.getSchema(name);
	}

	@Override
	public CacheFeatureStore getFeatureSource(Name name) throws IOException {
		return new CacheFeatureStore(this, backendDataStore.getFeatureSource(name), name);
	}
	
	@Override
	public CacheFeatureStore getFeatureSource(String typeName) throws IOException {
		return getFeatureSource(new NameImpl(typeName));
	}

	@Override
	public List<Name> getNames() throws IOException {
		return backendDataStore.getNames();
	}


	@Override
	public String[] getTypeNames() throws IOException {
		return backendDataStore.getTypeNames();
	}

	@Override
	public void dispose() {
		if (scheduledRefresh != null) {
			scheduledRefresh.cancel(false);
			scheduledRefresh = null;
		}
		this.backendDataStore.dispose();
		this.cacheStore.dispose();
		
		LOGGER.fine("CacheDataStore is disposed.");
	}

	protected List<Name> availableNames() throws IOException {
		List<Name> names = backendDataStore.getNames();
		List<Name> cached = cacheStore.getNames();
		
		for (Name bn : names) {
			Iterator<Name> iter = cached.iterator();
			
			String back = bn.getLocalPart();
			String cache = getCacheTypeName(back);
			
			while (iter.hasNext())  {
				if (iter.next().getLocalPart().equals(cache))  {
					names.remove(bn);
					break;
				}
			}
		}

		return names;
	}
	public String getCacheTypeName(Name name) {
	    return getCacheTypeName(name.getLocalPart());
	}
	
	public String getCacheTypeName(String typeName)  {
		return String.format(this.cacheNameFormat, typeName);
	}

	
	DataStore getCacheStore() {
		return cacheStore;
	}


	@Override
	public void createSchema(SimpleFeatureType featureType) throws IOException {
		throw new UnsupportedOperationException("We don't handle this right now.");
	}
	
	@Override
	public void updateSchema(String typeName, SimpleFeatureType featureType)
			throws IOException {
		throw new UnsupportedOperationException("We don't handle this right now.");
	}

	@Override
	public void updateSchema(Name typeName, SimpleFeatureType featureType) throws IOException {
		throw new UnsupportedOperationException("We don't handle this right now.");
	}
	
	@Override
	public void removeSchema(String typeName) throws IOException {
		throw new UnsupportedOperationException("We don't handle this right now.");
	}
	
	@Override
	public void removeSchema(Name typeName) throws IOException {
		throw new UnsupportedOperationException("We don't handle this right now.");
	}


	@Override
	public FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader(
			Query query, Transaction transaction) throws IOException {
		throw new UnsupportedOperationException("We don't handle this right now.");
	}


	@Override
	public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriter(
			String typeName, Filter filter, Transaction transaction)
			throws IOException {
		throw new UnsupportedOperationException("We don't handle this right now.");
	}


	@Override
	public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriter(
			String typeName, Transaction transaction) throws IOException {
		throw new UnsupportedOperationException("We don't handle this right now.");
	}


	@Override
	public FeatureWriter<SimpleFeatureType, SimpleFeature> getFeatureWriterAppend(
			String typeName, Transaction transaction) throws IOException {
		throw new UnsupportedOperationException("We don't handle this right now.");
	}


	@Override
	public LockingManager getLockingManager() {
		throw new UnsupportedOperationException("We don't handle this right now.");
	}

}