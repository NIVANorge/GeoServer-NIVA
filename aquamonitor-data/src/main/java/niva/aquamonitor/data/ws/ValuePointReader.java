package niva.aquamonitor.data.ws;

import java.io.IOException;
import java.util.Map;

/**
 * Used for handling end-point ending in /valuepoints
 * 
 * @author Roar Brænden, NIVA
 *
 */
public class ValuePointReader extends AquaReader<ValuePointCargo>{

    /**
     * At the moment we will require that a token is specified
     */
    ValuePointReader(AquaWebService webservice, String path, String token) {
        super(webservice, path, token);
    }

    @Override
    public CloseableIterator<ValuePointCargo> iterator() throws IOException {
        return new ValuePointIterator(callJsonService());
    }
    
    
    private static class ValuePointIterator extends JsonMapper<ValuePointCargo> {

        ValuePointIterator(JsonStreamIterator iter) {
            super(iter);
        }

        @Override
        public ValuePointCargo mapCargo(Map<String, Object> currJson) {
            final ValuePointCargo ret = new ValuePointCargo();
            ret.samplePointId = this.getInteger(currJson, "SamplePointId");
            ret.longitude = this.getDouble(currJson, "Longitude");
            ret.latitude = this.getDouble(currJson, "Latitude");
            ret.sampleDate = this.getDate(currJson, "SampleDate");
            ret.specifics = this.getString(currJson,  "Specifics");
            ret.taxonomyName = this.getString(currJson, "TaxonomyName");
            ret.datatype = this.getString(currJson, "Datatype");
            ret.parameterId = this.getInteger(currJson, "ParameterId");
            ret.parameterName = this.getString(currJson,  "ParameterName");
            ret.unit = this.getString(currJson, "Unit");
            ret.value = this.getDouble(currJson, "Value");
            ret.flag = this.getString(currJson, "Flag");
            return ret;
        }
        
    }

}
