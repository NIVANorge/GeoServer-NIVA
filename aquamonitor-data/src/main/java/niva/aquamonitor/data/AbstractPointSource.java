package niva.aquamonitor.data;

import java.io.IOException;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import niva.aquamonitor.data.ws.AquaReader;
import niva.aquamonitor.data.ws.CloseableIterator;
import niva.aquamonitor.data.ws.PointCargo;
import niva.geotools.referencing.CRS;

/**
 * 
 * Abstract FeatureSource for all source's that wrap's the response of a PointCargo or super-classes.
 * 
 * @author Roar Brænden, NIVA
 * 
 */
abstract class AbstractPointSource<T extends PointCargo> extends ContentFeatureSource {

    private final AquaReader<T> reader;
    AbstractPointSource(ContentEntry entry, AquaReader<T> reader) {
        super(entry, Query.ALL);
       this.reader = reader;
    }
    
    @Override
    protected SimpleFeatureType buildFeatureType() throws IOException {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(entry.getName());
        CoordinateReferenceSystem crs = CRS.getLengdeBreddegrad();
        
        builder.setCRS(crs);
        builder.add("the_geom", Point.class);
        
        addAttributes(builder);
        
        return builder.buildFeatureType();
    }
    
    protected abstract void addAttributes(SimpleFeatureTypeBuilder builder);
    
    @Override
    protected ReferencedEnvelope getBoundsInternal(Query query) throws IOException {        
        
        if (query.equals(Query.ALL)) {
            return new ReferencedEnvelope(reader.getEnvelope(), CRS.getLengdeBreddegrad());
        }
        try (FeatureReader<SimpleFeatureType, SimpleFeature> reader = getReaderInternal(query)) {
            ReferencedEnvelope bounds = null;
            while (reader.hasNext()) {
                SimpleFeature feature = reader.next();
                BoundingBox fb = feature.getBounds();
                if(fb != null) {
                    if (bounds == null)
                        bounds = ReferencedEnvelope.reference(fb);
                    else 
                        bounds.expandToInclude(ReferencedEnvelope.reference(fb));
                }
            }
            return bounds;
        }
    }


    @Override
    protected int getCountInternal(Query query) throws IOException {
        if (query.equals(Query.ALL)) {
            return reader.getCount();
        }
        
        try (FeatureReader<SimpleFeatureType, SimpleFeature> reader = getReader(query)) {
            int i = 0;
            while (reader.hasNext()) {
                reader.next();
                i++;
            }
            return i;
        }
    }
    
    
    /**
     * Try to catch exception's and report a user-friendly exception.
     */
    @Override
    protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(Query query) throws IOException {
        try {
            return createFeatureReader(getSchema(), reader.iterator());
        }
        catch (IOException ie) {
            String message = ie.getMessage();
            
            if (message != null && message.startsWith("Given key") && message.endsWith("is missing.")) {
                throw new IOException("Store " + this.getEntry().getTypeName() + " represents a session that has expired.", ie);
            }
            else {
                throw ie;
            }
        }
    }

    /**
     * Return an AbstractFeatureReader that can wrap the iterator and convert them into a SimpleFeature. 
     */
    abstract protected AbstractFeatureReader<T> createFeatureReader(SimpleFeatureType schema, CloseableIterator<T> iter) throws IOException;
}
