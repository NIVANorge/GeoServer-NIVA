package niva.geoserver.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.logging.Logger;

import org.geoserver.catalog.Catalog;
import org.geotools.util.logging.Logging;
import org.springframework.scheduling.TaskScheduler;

/**
 * Scheduler for cache datastore updates
 * Initialized by applicationContext.xml
 * 
 * @author Roar Brænden, NIVA
 *
 */
public class CacheScheduler {
	
	private static final Logger LOGGER = Logging.getLogger(CacheScheduler.class);
	
	private final Catalog catalog;
	
	private final TaskScheduler scheduler;

	private static final int INITIAL_DELAY_REFRESH_MINUTES = 2; 
	
	/**
	 * Constructor use by applicationContext.xml
	 * @param catalog
	 * @param scheduler
	 */
	public CacheScheduler(Catalog catalog, TaskScheduler scheduler) {
		LOGGER.info("Initializing CacheScheduler");
		this.catalog = catalog;
		this.scheduler = scheduler;
		Date startTime = Date.from(Instant.now().plus(Duration.ofMinutes(INITIAL_DELAY_REFRESH_MINUTES)));
		this.scheduler.schedule(this::startScheduler, startTime);
	}
	
	/**
	 * Start scheduler for all cache datastores by calling CacheDataStoreFactory
	 */
	public void startScheduler() {
		catalog.getDataStores().stream()
			.filter(ds -> {
				if (ds.getConnectionParameters().containsKey(CacheDataStoreFactory.DBTYPE_PARAM.key)) {
					String dbtype = (String) ds.getConnectionParameters().get(CacheDataStoreFactory.DBTYPE_PARAM.key);
					return dbtype.equals(CacheDataStoreFactory.DBTYPE_PARAM.sample);
				} else {
					return false;
				}
			})
			.forEach(ds -> {
				try {
					ds.getDataStore(null);
				} catch (Exception e) {
					LOGGER.warning("Failed to get CacheDataStore for datastore: " + ds.getName() + ". " + e.getMessage());
				}
			});
	}
	
	/**
	 * Calling initScheduler on the cache datastore.
	 * @param ds
	 */
	public void schedule(CacheDataStore ds) {
		ds.initScheduledRefresh(scheduler);
	}
	
}
