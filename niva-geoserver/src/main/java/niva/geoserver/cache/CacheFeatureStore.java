package niva.geoserver.cache;

import java.awt.RenderingHints.Key;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.geotools.api.data.DataStore;
import org.geotools.api.data.FeatureListener;
import org.geotools.api.data.FeatureReader;
import org.geotools.api.data.FeatureWriter;
import org.geotools.api.data.Query;
import org.geotools.api.data.QueryCapabilities;
import org.geotools.api.data.ResourceInfo;
import org.geotools.api.data.Transaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.data.SimpleFeatureStore;
import org.geotools.data.transform.Definition;
import org.geotools.data.transform.TransformFactory;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.logging.Logging;
import org.geotools.api.feature.Property;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.api.feature.type.Name;
import org.geotools.api.filter.Filter;
import org.geotools.api.filter.FilterFactory;
import org.geotools.api.filter.identity.FeatureId;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

/**
 * A SimpleFeatureStore encapsulating two separate SimpleFeatureSources.
 * One being the original. The other being a copy of the first.
 * 
 * @author Roar Brænden, NIVA
 *
 */
public class CacheFeatureStore implements SimpleFeatureStore {
	
    // Link to our parent
	private final CacheDataStore dataStore;
	
	// The original
	private final SimpleFeatureSource backendSource;
	
    // The name given at backend
    private final Name name;

    // The cache
    private SimpleFeatureStore cacheStore = null;
	
	// Transformation features from backend to cache
	private SimpleFeatureSource transBackend = null;
	
	// Transformation features from cache to backend
	private SimpleFeatureSource transCache = null;
	
	
	private static final FilterFactory ff = CommonFactoryFinder.getFilterFactory();	

	private static Logger LOGGER = Logging.getLogger(CacheFeatureStore.class);
	
	// Interface FeatureStore
	private Transaction transaction;
	
	
	public CacheFeatureStore(CacheDataStore store, SimpleFeatureSource backend, Name name) {
		LOGGER.fine("Creating CacheFeatureStore for " + name.toString());
		this.dataStore = store;
		this.backendSource = backend;
		this.name = name;
	}
	
	
	// ----  Functions working against backendSource. Without the need for a cacheSource.

	@Override
	public Name getName() {
		return name;
	}

	@Override
	public ResourceInfo getInfo() {
		return new ResourceInfo() {

			@Override
			public String getTitle() {
				return CacheFeatureStore.this.getName().getLocalPart();
			}

			@Override
			public Set<String> getKeywords() {
				return new HashSet<>();
			}

			@Override
			public String getDescription() {
				return null;
			}

			@Override
			public String getName() {
				return CacheFeatureStore.this.getSchema().getTypeName();
			}

			@Override
			public URI getSchema() {
				try {
					return new URI(CacheFeatureStore.this.getName().getNamespaceURI());
				}
				catch (URISyntaxException ue) {
					return null;
				}
			}

			@Override
			public ReferencedEnvelope getBounds() {
				try {
					return CacheFeatureStore.this.backendSource.getBounds();
				}
				catch (IOException ie) {
					return null;
				}
			}

			@Override
			public CoordinateReferenceSystem getCRS() {	
				return CacheFeatureStore.this.getSchema().getCoordinateReferenceSystem();
			}
		};
	}

	@Override
	public CacheDataStore getDataStore() {
		return dataStore;
	}
	
	@Override
	public SimpleFeatureType getSchema() {
		return backendSource.getSchema();
	}
	
	// ------- Function working against cacheStore if that's populated


    @Override
    public ReferencedEnvelope getBounds() throws IOException {
        if (isCached()) {
            if (transCache == null) {
                throw new IllegalStateException("transCache hasn't been initialised. Turn on Verbose logging and check logs.");
            }
            return transCache.getBounds();
        }
        else {
            return backendSource.getBounds();
        }
    }

    @Override
    public ReferencedEnvelope getBounds(Query query) throws IOException {
        if (isCached()) {
            if (transCache == null) {
                throw new IllegalStateException("transCache hasn't been initialised. Turn on Verbose logging and check logs.");
            }
            return transCache.getBounds(query);
        }
        else {
            return backendSource.getBounds(query);
        }
    }

    @Override
    public int getCount(Query query) throws IOException {
        if (isCached()) {
            if (transCache == null) {
                throw new IllegalStateException("transCache hasn't been initialised. Turn on Verbose logging and check logs.");
            }
            return transCache.getCount(query);
        }
        else {
            return backendSource.getCount(query);
        }
    }

	// -------   Functions working against cacheStore
	
	@Override
	public QueryCapabilities getQueryCapabilities() {
		try {
			checkPopulated();
			return cacheStore.getQueryCapabilities();
		} catch (IOException e) {
			throw new RuntimeException("Error occurred while populating cache.", e);
		}
	}

	@Override
	public void addFeatureListener(FeatureListener listener) {
		try {
			checkPopulated();
			cacheStore.addFeatureListener(listener);
		}
		catch (IOException ie) {
			LOGGER.warning("Populating failed because " + ie.getMessage());
		}
	}

	@Override
	public void removeFeatureListener(FeatureListener listener) {
		try {
			checkPopulated();
			cacheStore.removeFeatureListener(listener);
		}
		catch (IOException ie) {
			LOGGER.warning("Populating failed because " + ie.getMessage());
		}
	}
	

	@Override
	public Set<Key> getSupportedHints() {
		try {
			checkPopulated();
			return cacheStore.getSupportedHints();
		}
		catch (IOException ie) {
			LOGGER.warning(ie.getMessage());
			return Collections.emptySet();
		}
	}


	@Override
	public SimpleFeatureCollection getFeatures() throws IOException {
		checkPopulated();
	    if (transCache == null) {
            throw new IllegalStateException("transCache hasn't been initialised. Turn on Verbose logging and check logs.");
        }
		return transCache.getFeatures();
	}

	@Override
	public SimpleFeatureCollection getFeatures(Filter filter) throws IOException {
		checkPopulated();
	    if (transCache == null) {
            throw new IllegalStateException("transCache hasn't been initialised. Turn on Verbose logging and check logs.");
        }
		return transCache.getFeatures(filter);
	}

	@Override
	public SimpleFeatureCollection getFeatures(Query query) throws IOException {
		checkPopulated();
		if (transCache == null) {
		    throw new IllegalStateException("transCache hasn't been initialised. Turn on Verbose logging and check logs.");
		}
		return transCache.getFeatures(query);
	}
	
	
	/**
	 * Is there a cache for this specific layer
	 * @return true if there is a cache for this layer
	 * @throws IOException
	 */
	boolean isCached() throws IOException {
		
		if (cacheStore != null) {
			LOGGER.fine(this.name + " is cached.");
		    return true;
		}
		for (String typeName : dataStore.getCacheStore().getTypeNames()) {
			if (dataStore.getCacheTypeName(name.getLocalPart()).equals(typeName)) {
				LOGGER.fine(this.name + " is cached.");
				return checkSchema();
			}
		}
		LOGGER.fine(this.name + " isn't cached.");
		return false;
	}

	/**
	 * The cache is populated in lazy mode, at first access against features.
	 */
	private void checkPopulated() throws IOException {
		if (!isCached()) {
		    generate();
		}
	}
	
	/**
	 * First time establishment of the cache, or a rebuild if checkSchema returns false.
	 * Schema at cache is generated. We populate it with all features from backend.
	 */
	public void generate() throws IOException {
	    for (String cacheName : dataStore.getCacheStore().getTypeNames()) {
            if (cacheName.equals(dataStore.getCacheTypeName(name.getLocalPart()))) {
                LOGGER.info(this.name.toString() + " must be regenerated.");
                try {
                    dataStore.getCacheStore().removeSchema(cacheName);
                }
                catch (Exception e) {
                    throw new CacheException("Couldn't remove schema from cache store.", e);
                }
            }
        }
       
		try
		{	
			DataStore cacheDataStore = getDataStore().getCacheStore();
			
			List<Definition> mappings = new ArrayList<>();
			String cacheTypeName = dataStore.getCacheTypeName(name.getLocalPart());
			SimpleFeatureType featureType = backendSource.getSchema();
			
			SimpleFeatureTypeBuilder cacheBuilder = new SimpleFeatureTypeBuilder();
			cacheBuilder.setName(cacheTypeName);
			for (AttributeDescriptor attr : featureType.getAttributeDescriptors())  {
				cacheBuilder.add(attr);
			}
			try {
				cacheDataStore.createSchema(cacheBuilder.buildFeatureType());
			} catch (UnsupportedOperationException e) {
				throw new CacheException("The cacheDataStore of type " + cacheDataStore.getClass().getName() + " couldn't create schema.", e);
			}
			SimpleFeatureStore newCacheStore = (SimpleFeatureStore)cacheDataStore.getFeatureSource(cacheTypeName);
			try {
				SimpleFeatureType cType = newCacheStore.getSchema();
				
				for (int m = 0; m < featureType.getAttributeCount(); m++) {
					String name = cType.getDescriptor(m).getLocalName();
					String fName = featureType.getDescriptor(m).getLocalName();
					if (name.equals(fName)) {
						mappings.add(new Definition(name));
					}
					else {
						LOGGER.fine("Cache mapping for table " + featureType.getTypeName() + " field " + name + " is " + fName);
						mappings.add(new Definition(name, ff.property(fName)));
					}
				}
				transBackend = TransformFactory.transform(backendSource, cacheTypeName, mappings);
				newCacheStore.addFeatures(transBackend.getFeatures());
				
				List<Definition> inverted = new ArrayList<>();
		        for (Definition definition : mappings) {
		            List<Definition> inverses = definition.inverse();
		            if (inverses != null) {
		                inverted.addAll(inverses);
		            }
		        }
				transCache = TransformFactory.transform(newCacheStore, name.getLocalPart(), inverted);
			}
			finally {
				cacheStore = newCacheStore;
			}
		}
		catch (Exception e) {
			throw new CacheException("Something went wrong when generating CacheStore.", e);
		}
	}
	
	
	/**
	 * Updates features. Based on two iterations against the two datastores.
	 */
	public void refresh() throws IOException {
		
		if (!isCached()) {
		    throw new IOException("Calling refresh on a store that isn't cached:" + getName().toString());
		}
		
		try (FeatureWriter<SimpleFeatureType, SimpleFeature> cacheWriter = this.dataStore
		                .getCacheStore()
		                .getFeatureWriter(dataStore.getCacheTypeName(name.getLocalPart()), getTransaction());
		     SimpleFeatureIterator backIter = this.transBackend
		                .getFeatures()
		                .features()) {
			
			iterateModifications(backIter, cacheWriter);
		}
		catch (Exception e) {
			throw new CacheException("Something went wrong when refreshing CacheStore:" 
			                                    + this.cacheStore.getName().toString(), e);
		}
	}
	

	/**
	 * The cache exists and we create a transformation.
	 * We simply map by index.
	 */
	private boolean checkSchema() throws IOException {
		final SimpleFeatureType featureType = backendSource.getSchema();
		final String cacheTypeName = dataStore.getCacheTypeName(name.getLocalPart());
		final List<Definition> mappings = new ArrayList<>();

		SimpleFeatureStore cache = (SimpleFeatureStore)getDataStore().getCacheStore().getFeatureSource(cacheTypeName);
		
		SimpleFeatureType cType = cache.getSchema();
		boolean makeNew = false;
		
		if (featureType.getAttributeCount() == cType.getAttributeCount()) {
			
			for (int m = 0; !makeNew && m < featureType.getAttributeCount(); m++) {
				String name = cType.getDescriptor(m).getLocalName();
				String fName = featureType.getDescriptor(m).getLocalName();
				if (name.equals(fName)) {
					mappings.add(new Definition(name));
				}
				else {
					boolean found = false;
					int i = name.length();
					while (!found && --i > 0) {
						found = name.substring(0, i).equals(fName.substring(0, i));
					}
					if (found) {
						mappings.add(new Definition(name, ff.property(fName)));
					}
					else {
						makeNew = true;
					}
				}		
			}
		}
		else {
			makeNew = true;
		}
		
		if (makeNew) {
			LOGGER.warning("Backend feature class has changed, and we should create a new cache feature source for:" + featureType.getTypeName());
			return false;
		}

		transBackend = TransformFactory.transform(backendSource, cacheTypeName, mappings);
		cacheStore = cache;
	
		List<Definition> inverted = new ArrayList<>();
        for (Definition definition : mappings) {
            List<Definition> inverses = definition.inverse();
            if (inverses != null) {
                inverted.addAll(inverses);
            }
        }
		
		transCache = TransformFactory.transform(cacheStore, name.getLocalPart(), inverted);
		return true;
	}


	@Override
	public void setTransaction(Transaction transaction) {
		if (transaction == null) {
			throw new IllegalArgumentException("Transaction shouldn't be null. Consider using Transaction.AUTO_COMMIT.");
		}
		this.transaction = transaction;
	}


	@Override
	public Transaction getTransaction() {
	    if (transaction == null) {
	        return Transaction.AUTO_COMMIT;
	    }
		return this.transaction;
	}


	@Override
	public List<FeatureId> addFeatures(FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection) throws IOException {
		throw new UnsupportedOperationException("Features are taken from backend. Use modifyFeatures to fetch new one from the backend.");
	}


	/**
	 * Removes features from cache. Used in cases were the cache should reflect a user-action.
	 */
	@Override
	public void removeFeatures(Filter filter) throws IOException {
		if (isCached()) {			
			try (FeatureWriter<SimpleFeatureType, SimpleFeature> cacheWriter = 
			        dataStore.getCacheStore().getFeatureWriter(name.getLocalPart(), filter, getTransaction())) {
				while (cacheWriter.hasNext()) {
					cacheWriter.next();
					cacheWriter.remove();
				}
			}
			catch (Exception e) {
				throw new IOException("Exception when removing features from cache.", e);
			}
		}
		else {
			throw new IOException("CacheFeatureStore doesn't have a cache.");
		}
	}


	/**
	 * Modifies features based on filter and backend-source. Ignores attributeName, attributeValues.
	 */
	@Override
	public void modifyFeatures(Name[] attributeNames, Object[] attributeValues, Filter filter) throws IOException {
		modifyFeatures(filter);
	}

	/**
	 * Modifies features based on filter and backend-source. Ignores attributeName and attributeValue.
	 */
	@Override
	public void modifyFeatures(Name attributeName, Object attributeValue, Filter filter) throws IOException {
		modifyFeatures(filter);
	}

	/**
	 * Modifies features based on filter and backend-source. Ignores name and attributeValue.
	 */
	@Override
	public void modifyFeatures(String name, Object attributeValue, Filter filter) throws IOException {		
		modifyFeatures(filter);
	}

	/**
	 * Modifies features based on filter and backend-source. Ignores names and attributeValues.
	 */
	@Override
	public void modifyFeatures(String[] names, Object[] attributeValues, Filter filter) throws IOException {
		modifyFeatures(filter);
	}
	
	/**
	 * Do the real modification by calling backend-source by the filter, and update the cache accordingly.
	 */
	public void modifyFeatures(Filter filter) throws IOException {
		if (isCached()) {

			try (SimpleFeatureIterator backIter = this.transBackend
			                                          .getFeatures(filter)
			                                          .features();
		         FeatureWriter<SimpleFeatureType, SimpleFeature> cacheWriter = this.dataStore
		                                              .getCacheStore()
		                                              .getFeatureWriter(dataStore.getCacheTypeName(name.getLocalPart()), filter, getTransaction())) {

				iterateModifications(backIter, cacheWriter);
			}
			catch (IOException ie) {
				LOGGER.severe("Exception when modifying features. (" + ie.getMessage() + ")");
			}
		}
		else {
			throw new IOException("CacheFeatureStore doesn't have a cache.");
		}
	}
	
	/**
	 * Iterating two streams. Supposing they are sorted in the same manner?
	 */
	private void iterateModifications(SimpleFeatureIterator backIter, 
	        						  FeatureWriter<SimpleFeatureType, SimpleFeature> cacheWriter) throws IOException{

		while (backIter.hasNext()) {
			final SimpleFeature back = backIter.next();
			final boolean hasCache = cacheWriter.hasNext();
			final SimpleFeature cache = cacheWriter.next();
			
			boolean changed = false;
			Iterator<Property> iter = back.getProperties().iterator();
			while (iter.hasNext()) {
				Property backParam = iter.next();
				String cName = backParam.getName().getLocalPart();
				Object backObj = backParam.getValue();
				
				if (hasCache) {
					Object cacheObj = cache.getAttribute(cName);
					
					if ((backObj == null && cacheObj != null) 
							|| (backObj != null && !backObj.equals(cacheObj))) {
						cache.setAttribute(cName, backObj);
						changed = true;
					}
				} else {
					cache.setAttribute(cName, backObj);
					changed = true;
				}
			}
			
			if (changed) {
				cacheWriter.write();
			}
		}
		
		while (cacheWriter.hasNext()) {
			cacheWriter.next();
			cacheWriter.remove();
		}
	}

	
	/***
	 * Overwrites content of cacheStore
	 */
	@Override
	public void setFeatures( FeatureReader<SimpleFeatureType, SimpleFeature> reader) throws IOException {
		this.cacheStore.setFeatures(reader);
	}
}
