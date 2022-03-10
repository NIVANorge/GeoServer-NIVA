package niva.aquamonitor.data;

import java.io.IOException;
import java.util.NoSuchElementException;
import org.geotools.data.FeatureReader;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import niva.aquamonitor.data.ws.CloseableIterator;
import niva.aquamonitor.data.ws.PointCargo;

/**
 *
 * Creates SimpleFeature's of cargo objects.
 * 
 * @author Roar Brænden, NIVA
 *
 */
abstract class AbstractFeatureReader<T extends PointCargo> implements FeatureReader<SimpleFeatureType, SimpleFeature> {

    private final CloseableIterator<T> iter;
    
    private final GeometryFactory fact = JTSFactoryFinder.getGeometryFactory();
    private final SimpleFeatureBuilder builder;
    private int actual = 0;
    
    public AbstractFeatureReader(SimpleFeatureType schema, CloseableIterator<T> iter) throws IOException {
        this.builder = new SimpleFeatureBuilder(schema);
        this.iter = iter;
    }

    @Override
    public SimpleFeatureType getFeatureType() {
        return builder.getFeatureType();
    }

    @Override
    public SimpleFeature next() throws IOException, NoSuchElementException {
        
        final T spc = iter.next();
        builder.add(this.fact.createPoint(new Coordinate(spc.longitude, spc.latitude)));
        addNextFeature(builder, spc);
        return builder.buildFeature(String.valueOf(++actual));
    }
    
    /**
     * Sub-class should handle all other attributes than the geometry.
     * <p>NB! Add attributes in same order as in AbstractPointSource.addAttributes() 
     */
    protected abstract void addNextFeature(SimpleFeatureBuilder builder, T next);

    @Override
    public boolean hasNext() throws IOException {
        return iter.hasNext();
    }

    @Override
    public void close() throws IOException {
        this.iter.close();
    }
}
