package niva.geotools.data.naturbasen;

import java.io.IOException;

import niva.geotools.GeoToolsException;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.ReferencedEnvelope;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;


public class NaturbasenSource extends ContentFeatureSource {

	public NaturbasenSource(ContentEntry entry, Query query) {
		super(entry, query);
	}
	
	private String getLayerName() {
		String name = entry.getName().getLocalPart();
		String layer;
		
		if (name.startsWith("flate"))
			layer = "NIVA_GEOMETRY.BIOMANGFOLD_F";
		else if (name.startsWith("punkt"))
			layer = "NIVA_GEOMETRY.BIOMANGFOLD_P";
		else
			throw new GeoToolsException("Oppgitt feil layer name (layer) for NaturbasenSource :" + name);
		return layer;
	}
	
	protected SimpleFeatureSource getFeatureSource() throws IOException {
		String layer = getLayerName();
		NaturbasenStore store = (NaturbasenStore)this.entry.getDataStore();
		return store.getArcStore().getFeatureSource(layer);
	}
	
	protected Filter getFilter() {
		String name = entry.getName().getLocalPart();
		String[] parts = name.split("_");
		
		Filter filter;
		try {
			
			String where;
			
			where = (parts.length == 3 ? "NATURTYPE_ID='" + parts[1] + "' AND " : "");
			
			String status;
			status = parts[parts.length-1];
			where = where + (status.equals("aktiv")?"STATUS_ID=3":(status.equals("vurdering")?"STATUS_ID=1":"STATUS_ID=0"));
			
			filter = CQL.toFilter(where);
		}
		catch (CQLException ce) {
			throw new GeoToolsException(ce);
		}
		return filter;
	}

	@Override
	protected ReferencedEnvelope getBoundsInternal(Query query)	throws IOException {
		SimpleFeatureSource source = getFeatureSource();
		
		return source.getBounds(retypeQuery(query));
	}

	@Override
	protected int getCountInternal(Query query) throws IOException {
		SimpleFeatureSource source = getFeatureSource();
		
		return source.getCount(retypeQuery(query));
	}

	@Override
	protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(Query query) throws IOException {
		NaturbasenStore store = (NaturbasenStore)this.entry.getDataStore();
		
		return store.getArcStore().getFeatureReader(retypeQuery(query), Transaction.AUTO_COMMIT);
	}

	@Override
	protected SimpleFeatureType buildFeatureType() throws IOException {
		NaturbasenStore store = (NaturbasenStore)this.entry.getDataStore();
		SimpleFeatureType arcSchema = store.getArcStore().getSchema(getLayerName());
		
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName(entry.getName());
		builder.setCRS(arcSchema.getCoordinateReferenceSystem());
		builder.setAttributes(arcSchema.getAttributeDescriptors());
		
		return builder.buildFeatureType();
	}
	
	@Override
	protected boolean canFilter() {
		return true;
	}
	
	@Override
	protected boolean canSort() {
		return true;
	}
	
	@Override
	protected boolean canOffset() {
		return true;
	}
	
	@Override
	protected boolean canLimit() {
		return true;
	}
	
	@Override
	protected boolean canReproject() {
		return true;
	}
	
	private Query retypeQuery(Query query) {
		FilterFactory fact = CommonFactoryFinder.getFilterFactory(GeoTools.getDefaultHints());
		Filter status = getFilter();
		
		Query newQ = new Query(query);
		newQ.setTypeName(getLayerName());
		newQ.setFilter(fact.and(query.getFilter(), status));
		
		return newQ;
	}
	
	
}
