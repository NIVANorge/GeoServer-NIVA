package niva.aquamonitor.data;

import java.io.IOException;
import java.util.Date;
import org.geotools.data.store.ContentEntry;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeatureType;
import niva.aquamonitor.data.ws.AquaReader;
import niva.aquamonitor.data.ws.CloseableIterator;
import niva.aquamonitor.data.ws.ValuePointCargo;

/**
 * Content feature source specific for ValuePointCargo, coming from Aquamonitor.
 *
 * @author Roar Brænden, NIVA
 *
 */
public class ValuePointSource extends AbstractPointSource<ValuePointCargo> {

    ValuePointSource(ContentEntry entry, AquaReader<ValuePointCargo> reader) {
        super(entry, reader);
    }

    @Override
    protected void addAttributes(SimpleFeatureTypeBuilder builder) {
        builder.nillable(false).add("SAMPLE_POINT_ID", Integer.class);
        builder.add("LATITUDE", Double.class);
        builder.add("LONGITUDE", Double.class);
        builder.add("SAMPLE_DATE", Date.class);
        builder.add("SPECIFICS", String.class);
        builder.add("TAXONOMY_NAME", String.class);
        builder.add("DATATYPE", String.class);
        builder.add("PARAMETER_ID", Integer.class);
        builder.add("PARAMETER_NAME", String.class);
        builder.add("UNIT", String.class);
        builder.add("VALUE", Double.class);
        builder.add("FLAG", String.class);
    }    

    @Override
    protected AbstractFeatureReader<ValuePointCargo> createFeatureReader(SimpleFeatureType schema,
            CloseableIterator<ValuePointCargo> iter) throws IOException {
        
        return new AbstractFeatureReader<ValuePointCargo>(schema, iter) {
            @Override
            protected void addNextFeature(SimpleFeatureBuilder builder, ValuePointCargo next) {
                builder.add(next.samplePointId);
                builder.add(next.latitude);
                builder.add(next.longitude);
                builder.add(next.sampleDate);
                builder.add(next.specifics);
                builder.add(next.taxonomyName);
                builder.add(next.datatype);
                builder.add(next.parameterId);
                builder.add(next.parameterName);
                builder.add(next.unit);
                builder.add(next.value);
                builder.add(next.flag);
            }
        };
    }
}
