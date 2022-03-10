package niva.aquamonitor.data.ws;

import java.util.Date;

/**
 * Represents the json structure sent for end-points /valuepoints
 * 
 * @author Roar Brænden
 *
 */
public class ValuePointCargo extends PointCargo {
    
    public int samplePointId;
    
    public Date sampleDate;
    
    public String specifics;
    
    public String taxonomyName;
    
    public String datatype;
    
    public int parameterId;
    
    public String parameterName;
    
    public String unit;
    
    public double value;
    
    public String flag;
}
