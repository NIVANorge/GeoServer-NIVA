package niva.aquamonitor.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import niva.aquamonitor.data.ws.AquaReader;
import niva.aquamonitor.data.ws.StationGeometryCargo;
import niva.geotools.referencing.CRS;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureReader;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.MultiPolygon;

/**
 * 
 * StationSectorSource combines the result of a call to webservice with the ArcSde layer SAMPLING_SECTORS.
 * 
 * @author Roar Brænden, Niva
 *
 */
public class StationSectorSource extends ContentFeatureSource {

	private AquaReader<StationGeometryCargo> reader;
	private DataStore store;
	
	public StationSectorSource(ContentEntry entry, DataStore store, AquaReader<StationGeometryCargo> reader) {
		super(entry, Query.ALL);
	
		this.store = store;
		this.reader = reader;
	}
	
	/**
	 * Bygger opp struktur for SamplingSector
	 */
	@Override
	protected SimpleFeatureType buildFeatureType() throws IOException {
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName(entry.getName());
		CoordinateReferenceSystem crs = CRS.getUtm33();
		
		builder.setCRS(crs);
		builder.add("the_geom", MultiPolygon.class);
		
		builder.nillable(false).add("SAMPLING_SECTOR_ID", Integer.class);

		builder.nillable(false).add("PROJECT_ID", Integer.class);
		builder.add("PROJECT_NAME", String.class);
		builder.nillable(false).add("STATION_ID", Integer.class);
		builder.add("STATION_TYPE_ID", Integer.class);
		builder.add("STATION_TYPE", String.class);
		
		builder.add("STATION_CODE", String.class);
		builder.add("STATION_NAME", String.class);
		builder.add("FULL_STATION_NAME", String.class);
		
		return builder.buildFeatureType();
	}

	@Override
	protected ReferencedEnvelope getBoundsInternal(Query query)
			throws IOException {
		
		FeatureReader<SimpleFeatureType, SimpleFeature> featReader = getReaderInternal(query);
		ReferencedEnvelope bounds = null;
		while (featReader.hasNext()) {
			SimpleFeature feature = featReader.next();
            BoundingBox fb = feature.getBounds();
            if(fb != null) {
            	if (bounds == null)
            		bounds = ReferencedEnvelope.reference(fb);
            	else 
            		bounds.expandToInclude(ReferencedEnvelope.reference(fb));
            }
		}
		featReader.close();
		return bounds;
	}

	@Override
	protected int getCountInternal(Query query) throws IOException {
		if (query.equals(Query.ALL)) {
			return reader.getCount();
		}
		else {
			FeatureReader<SimpleFeatureType, SimpleFeature> reader = getReaderInternal(query);
			int i = 0;
			while (reader.hasNext()) {
				reader.next();
				i++;
			}
			reader.close();
			return i;
		}
	}

	@Override
	protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(Query query) throws IOException {
		
		FilterFactory fact = CommonFactoryFinder.getFilterFactory();
		
		Iterator<StationGeometryCargo> iter = this.reader.iterator();
		final HashMap<Integer, StationGeometryCargo> cargos = new HashMap<Integer, StationGeometryCargo>();
		List<Filter> inList = new LinkedList<Filter>();
		
		while(iter.hasNext()) {
			StationGeometryCargo c = iter.next();
			cargos.put(c.geometryId, c);
			inList.add(fact.equals(fact.property("SAMPLING_SECTOR_ID"), fact.literal(c.geometryId)));
		}
		
		final Query arcQuery = new Query("NIVA_GEOMETRY.SAMPLING_SECTORS", fact.or(inList));
		final FeatureReader<SimpleFeatureType, SimpleFeature> arcReader = this.store.getFeatureReader(arcQuery, Transaction.AUTO_COMMIT);
		final SimpleFeatureType schema = buildFeatureType();
		
		final SimpleFeatureBuilder builder = new SimpleFeatureBuilder(schema);
		
		return new SimpleFeatureReader() {

			@Override
			public SimpleFeatureType getFeatureType() {
				return schema;
			}

			@Override
			public SimpleFeature next() throws IOException,
					IllegalArgumentException, NoSuchElementException {
				SimpleFeature arcFeat = arcReader.next();
				StationGeometryCargo cargo = cargos.get((Integer) arcFeat.getAttribute("SAMPLING_SECTOR_ID"));
				
				builder.add(arcFeat.getDefaultGeometry());
				builder.add(cargo.geometryId);
				builder.add(cargo.projectId);
				builder.add(cargo.projectName);
				builder.add(cargo.stationId);
				builder.add(cargo.stationTypeId);
				builder.add(cargo.stationType);
				builder.add(cargo.stationCode);
				builder.add(cargo.stationName);
				builder.add(cargo.fullStationName);
				
				return builder.buildFeature(String.valueOf(cargo.geometryId));
			}

			@Override
			public boolean hasNext() throws IOException {
				return arcReader.hasNext();
			}

			@Override
			public void close() throws IOException {
				arcReader.close();
			}};
	}

}
