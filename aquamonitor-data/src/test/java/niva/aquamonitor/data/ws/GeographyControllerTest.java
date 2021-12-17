package niva.aquamonitor.data.ws;

import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Test;

public class GeographyControllerTest {
    

    @Test
    public void testGetAllDatatypes() throws Exception {
        try (Stream<String> datatypesStream = GeographyController.createService()
                    .getAllDatatypesReader()
                    .stream()) {
            List<String> datatypes = datatypesStream.collect(Collectors.toList());
            Assert.assertTrue(datatypes.contains("Water"));
        }
    }
    
    @Test
    public void getAllStationsEasy() throws Exception {
        
        try (CloseableIterator<StationPointCargo> iter = GeographyController.createService()
                                                                            .getProjectUserStationReader("Mjøsa")
                                                                            .iterator()) {
            assertTrue(iter.hasNext());
            final String typ = iter.next().stationType;   
            assertTrue("Innsjø".equals(typ) || "Elv".equals(typ));    
        }
    }
    
    @Test
    public void getAllStationsWrong() throws Exception {
        try {
            StationPointReader reader = GeographyController.createService().getProjectUserStationReader("RBR");
            reader.getCount();
            Assert.fail();
        }
        catch (IOException ie) {
            assertTrue("Feilmelding er endret.", ie.toString().contains("Kun gyldig for brukere av typen Project."));
        }
    }
    

}
