package niva.aquamonitor.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import org.geotools.console.CommandLine;
import org.geotools.console.Option;
import org.geotools.http.HTTPClient;
import org.geotools.http.HTTPClientFinder;
import org.geotools.http.HTTPResponse;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;


/**
 * Create AquaMonitor_Aggragation.sld and accompanying icons.
 * 
 * Should have username and password for Aquamonitor user, and a folder for output.
 * Given as options.
 * 
 * Could also have host as which AquaMonitor server to use.
 * 
 * @author Roar Brænden, NIVA
 *
 */
public class CreateAquaMonitorSLD extends CommandLine {
    
    private static final String STYLES_PATH = "/AquaServices/api/geography/styles";
    
    private static final String DEFAULT_HOST = "aquamonitor.niva.no";

    private static final String ICONS_FOLDER = "aqm_icons";
    
    @Option
    String username;
    
    @Option
    String password;
    
    @Option
    String output;
    
    @Option
    String host;
    
    private ObjectMapper mapper;
    
    private HTTPClient client;
    
    private File outputFolder;
    
    private File iconsFolder;
    
    private Styles styles;
    
    private URL stylesUrl;
    
    CreateAquaMonitorSLD(String...args) throws IOException{
        super(args, 0);
        
        mapper = JsonMapper.builder()
                           .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                           .build();
        
        client = HTTPClientFinder.createClient();
        client.setUser(username);
        client.setPassword(password);
        
        outputFolder = output == null ? new File(".") : new File(output);
        if (!outputFolder.exists()) {
            throw new IllegalArgumentException("Output folder not found.");
        }
        
        iconsFolder = new File(outputFolder, ICONS_FOLDER);
        if (!iconsFolder.exists()) {
            iconsFolder.mkdir();
        }
        
        stylesUrl = new URL("https://" + (host != null ? host : DEFAULT_HOST) + STYLES_PATH);
        
        styles = readStyles();
    }
    
    public static void main(String...args) {
        try {
            CreateAquaMonitorSLD app = new CreateAquaMonitorSLD(args);
            app.execute();
            System.exit(0);
        } catch (Exception e) {
            System.err.println("Error:" + e.getMessage());
            System.exit(-1);
        }
    }

    
    void execute() throws IOException {

        XmlBuilder builder = new XmlBuilder();
        generateHeader(builder);
        generateTransformation(builder);
        generateStationTypeRules(builder);
        generateNumStationRules(builder, "x", 2, 10, 20);
        generateNumStationRules(builder, "xx", 10, 100, 30);
        generateNumStationRules(builder, "xxx", 100, 1000, 36);
        generateNumStationRules(builder, "xxxx", 1000, null, 46);
        generateFullRule(builder);
        generateFooter(builder);
        try (FileWriter writer = new FileWriter(new File(outputFolder, "AquaMonitor_Aggragation.sld"))) {
           writer.write(builder.toString());
        }
    }
    
    private Styles readStyles() throws IOException {
        HTTPResponse response = client.get(stylesUrl);
        try {
            JsonParser parser = mapper.getFactory().createParser(response.getResponseStream());
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                throw new IllegalStateException("Response from dataTypes should be an array of strings.");
            }
            return mapper.readValue(parser, Styles.class);
        } finally {
            response.dispose();
        }
    }
    
    private void generateHeader(XmlBuilder builder) {
        builder.l("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>")
               .l("<StyledLayerDescriptor version=\"1.0.0\" xmlns=\"http://www.opengis.net/sld\" xmlns:ogc=\"http://www.opengis.net/ogc\""
                  + "  xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                  + "  xsi:schemaLocation=\"http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd\">")
               .t(1).l("<NamedLayer>")
               .t(2).l("<Name>AquaMonitor_aggregation</Name>")
               .t(2).l("<UserStyle>")
               .t(3).l("<Title>Stasjoner</Title>")
               .t(3).l("<Abstract>Visning av stasjoner symbolisert etter stasjonstype, aggregrert etter antall i rutenett</Abstract>")
               .t(3).l("<FeatureTypeStyle>");
    }
    
    private void generateTransformation(XmlBuilder builder) {
        builder.t(4).l("<Transformation>")
               .t(5).l("<ogc:Function name=\"niva:PointAggregateGrid\">")
               .t(6).l("<ogc:Function name=\"parameter\">")
               .t(7).l("<ogc:Literal>points</ogc:Literal>")
               .t(6).l("</ogc:Function>")
               .t(6).l("<ogc:Function name=\"parameter\">")
               .t(7).l("<ogc:Literal>outputBBOX</ogc:Literal>")
               .t(7).l("<ogc:Function name=\"env\">")
               .t(8).l("<ogc:Literal>wms_bbox</ogc:Literal>")
               .t(7).l("</ogc:Function>")
               .t(6).l("</ogc:Function>")
               .t(6).l("<ogc:Function name=\"parameter\">")
               .t(7).l("<ogc:Literal>outputWidth</ogc:Literal>")
               .t(7).l("<ogc:Function name=\"env\">")
               .t(8).l("<ogc:Literal>wms_width</ogc:Literal>")
               .t(7).l("</ogc:Function>")
               .t(6).l("</ogc:Function>")
               .t(6).l("<ogc:Function name=\"parameter\">")
               .t(7).l("<ogc:Literal>outputHeight</ogc:Literal>")
               .t(7).l("<ogc:Function name=\"env\">")
               .t(8).l("<ogc:Literal>wms_height</ogc:Literal>")
               .t(7).l("</ogc:Function>")
               .t(6).l("</ogc:Function>")
               .t(6).l("<ogc:Function name=\"parameter\">")
               .t(7).l("<ogc:Literal>cellSize</ogc:Literal>")
               .t(7).l("<ogc:Literal>60</ogc:Literal>")           
               .t(6).l("</ogc:Function>")
               .t(6).l("<ogc:Function name=\"parameter\">")
               .t(7).l("<ogc:Literal>aggregateAttributes</ogc:Literal>");
        styles.getDatatypes()
              .stream()
              .map(d -> d.getName())
              .forEach(s -> builder.t(7).l("<ogc:Literal>", s, "</ogc:Literal>"));
        builder.t(6).l("</ogc:Function>")
          .t(5).l("</ogc:Function>")
          .t(4).l("</Transformation>");
    }
    
    private String iconSize(int size) {
        return Integer.toString(size);
    }
    
    private void createIcon(String...options) {
        try {
            new CreateAquaMonitorSymbol(options).execute();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
    
    private void generateStationTypeRules(XmlBuilder builder) {
        String names = styles.getDatatypes().stream().map(d -> "${" + d.getName() + "}").collect(Collectors.joining(",")) + ",${EMPTY}";
        String colors = styles.getDatatypes().stream().map(d -> d.getColor()).collect(Collectors.joining(",")) + ",ffffff";
        for (StationType st : styles.getStationtypes()) {
            String filename = "aqm-a-" + st.getId() + ".png";
            String stationtype = escapeÆØÅ(st.getText()); 
            createIcon(new File(iconsFolder, filename).getAbsolutePath(), " --format", "png", "--size", iconSize(20), "--type", st.getLetter());
            builder.t(4).l("<Rule>")
                   .t(5).l("<Title>", stationtype, "</Title>")
                   .t(5).l("<LegendGraphic>")
                   .t(6).l("<Graphic>")
                   .t(7).l("<ExternalGraphic>")
                   .t(8).l("<OnlineResource xlink:href=\"", ICONS_FOLDER, "/", filename, "\" />")
                   .t(8).l("<Format>image/png</Format>")
                   .t(7).l("</ExternalGraphic>")
                   .t(7).l("<Size>20</Size>")
                   .t(6).l("</Graphic>")
                   .t(5).l("</LegendGraphic>")
                   .t(5).l("<ogc:Filter>")
                   .t(6).l("<ogc:PropertyIsEqualTo>")
                   .t(7).l("<ogc:PropertyName>STATION_TYPE</ogc:PropertyName>")
                   .t(7).l("<ogc:Literal>", stationtype, "</ogc:Literal>")
                   .t(6).l("</ogc:PropertyIsEqualTo>")
                   .t(5).l("</ogc:Filter>")
                   .t(5).l("<PointSymbolizer>")
                   .t(6).l("<Graphic>")
                   .t(7).l("<ExternalGraphic>")
                   .t(8).l("<OnlineResource xlink:href=\"http://aquamonitor?typ=", st.getLetter(), "&amp;vals=", names, "&amp;cols=", colors, "\" />")
                   .t(8).l("<Format>application/chart</Format>")
                   .t(7).l("</ExternalGraphic>")
                   .t(7).l("<Size>20</Size>")
                   .t(6).l("</Graphic>")
                   .t(5).l("</PointSymbolizer>")
                   .t(4).l("</Rule>");
        }
    }
    
    private String escapeÆØÅ(String text) {
		return text.replace("æ", "&#230;")
				.replace("ø", "&#248;")
				.replace("å", "&#229;")
				.replace("Æ", "&#198;")
				.replace("Ø", "&#216;")
				.replace("Å", "&#197;");
	}

	private void generateNumStationRules(XmlBuilder builder, String cnt, Integer minNum, Integer maxNum, Integer size) {
        String filename = "aqm-b-" + cnt + ".png";
        String names = styles.getDatatypes().stream().map(d -> "${" + d.getName() + "}").collect(Collectors.joining(",")) + ",${EMPTY}";
        String colors = styles.getDatatypes().stream().map(d -> d.getColor()).collect(Collectors.joining(",")) + ",ffffff";
        createIcon(new File(iconsFolder, filename).getAbsolutePath(), " --format", "png", "--size", 
                iconSize(size), "--count", cnt, "--colors", colors);
        builder.t(4).l("<Rule>");
        if (maxNum == null) {
            builder.t(5).l("<Title> &gt; ", minNum.toString(), " stasjoner</Title>");
        } else {
            builder.t(5).l("<Title> &gt;= ", minNum.toString(), " og &lt;", maxNum.toString(), " stasjoner</Title>");
        }
        builder.t(5).l("<LegendGraphic>")
               .t(6).l("<Graphic>")
               .t(7).l("<ExternalGraphic>")
               .t(8).l("<OnlineResource xlink:href=\"", ICONS_FOLDER, "/", filename, "\" />")
               .t(8).l("<Format>image/png</Format>")
               .t(7).l("</ExternalGraphic>")
               .t(7).l("<Size>", size.toString(), "</Size>")
               .t(6).l("</Graphic>")
               .t(5).l("</LegendGraphic>");
        if (maxNum == null) {
            builder.t(5).l("<ogc:Filter>")
                    .t(6).l("<ogc:PropertyIsGreaterThanOrEqualTo>")
                    .t(7).l("<ogc:PropertyName>COUNT</ogc:PropertyName>")  
                    .t(7).l("<ogc:Literal>", minNum.toString(), "</ogc:Literal>") 
                    .t(6).l("</ogc:PropertyIsGreaterThanOrEqualTo>")
                    .t(5).l("</ogc:Filter>");
        } else {
           builder.t(5).l("<ogc:Filter>")
                   .t(6).l("<ogc:And>")
                   .t(7).l("<ogc:PropertyIsGreaterThanOrEqualTo>")
                   .t(8).l("<ogc:PropertyName>COUNT</ogc:PropertyName>")
                   .t(8).l("<ogc:Literal>", minNum.toString(), "</ogc:Literal>") 
                   .t(7).l("</ogc:PropertyIsGreaterThanOrEqualTo>")
                   .t(7).l("<ogc:PropertyIsLessThan>")
                   .t(8).l("<ogc:PropertyName>COUNT</ogc:PropertyName>")
                   .t(8).l("<ogc:Literal>", maxNum.toString(), "</ogc:Literal>")
                   .t(7).l("</ogc:PropertyIsLessThan>")
                   .t(6).l("</ogc:And>")
                   .t(5).l("</ogc:Filter>");
        }
        builder.t(5).l("<PointSymbolizer>")
               .t(6).l("<Graphic>")
               .t(7).l("<ExternalGraphic>")
               .t(8).l("<OnlineResource xlink:href=\"http://aquamonitor?cnt=${COUNT}&amp;vals=", names, "&amp;cols=", colors, "\" />") 
               .t(8).l("<Format>application/chart</Format>")
               .t(7).l("</ExternalGraphic>")
               .t(7).l("<Size>", size.toString(), "</Size>")
               .t(6).l("</Graphic>")
               .t(5).l("</PointSymbolizer>")
               .t(4).l("</Rule>");
    }
    
    private void generateFullRule(XmlBuilder builder) {
        String filename = "aqm-c.png";
        String texts = styles.getDatatypes().stream().map(d -> d.getText()).collect(Collectors.joining(",")) + ",Ingen";
        String colors = styles.getDatatypes().stream().map(d -> d.getColor()).collect(Collectors.joining(",")) + ",ffffff";
        createIcon(new File(iconsFolder, filename).getAbsolutePath(), "--format", "png", "--size", iconSize(340), "--keys",
                texts, 
                "--colors", colors);
        builder.t(4).l("<Rule>")
               .t(5).l("<LegendGraphic>")
               .t(6).l("<Graphic>")  
               .t(7).l("<ExternalGraphic>")    
               .t(8).l("<OnlineResource xlink:href=\"", ICONS_FOLDER, "/" + filename + "\" />")    
               .t(8).l("<Format>image/png</Format>")  
               .t(7).l("</ExternalGraphic>")   
               .t(7).l("<Size>340</Size>")
               .t(6).l("</Graphic>")
               .t(5).l("</LegendGraphic>")
               .t(5).l("<PointSymbolizer>")
               .t(6).l("<Graphic>") 
               .t(7).l("<Mark>")   
               .t(8).l("<WellKnownName>circle</WellKnownName>")
               .t(7).l("</Mark>")   
               .t(7).l("<Size>0</Size>")  
               .t(6).l("</Graphic>") 
               .t(5).l("</PointSymbolizer>")
               .t(4).l("</Rule>");
    }
    private void generateFooter(XmlBuilder builder) {
        builder.t(3).l("</FeatureTypeStyle>")
            .t(2).l("</UserStyle>")
            .t(1).l("</NamedLayer>")
            .l("</StyledLayerDescriptor>");
    }
    
    static class XmlBuilder {
        private StringBuilder sb;
        
        XmlBuilder() {
            sb = new StringBuilder();
        }
        
        XmlBuilder t(int n) {
            for (int i = 0; i < n; i++) {
                sb.append("  ");
            }
            return this;
        }
        
        XmlBuilder l(String...parts) {
            for (String s : parts) {
                sb.append(s);
            }
            sb.append("\n");
            return this;
        }
        
        public String toString() {
            return sb.toString();
        }
    }
    

    
    static class Styles {
        
        private List<DataType> datatypes;
        
        private List<StationType> stationtypes;
        
        public Styles() {
            datatypes = new ArrayList<DataType>();
            stationtypes = new ArrayList<StationType>();
        }
        
        public List<DataType> getDatatypes() {
            return datatypes;
        }
        
        public List<StationType> getStationtypes() {
            return stationtypes;
        }
    }
    
    static class DataType {
        private String name;
        private String text;
        private String color;
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getText() {
            return text;
        }
        public void setText(String text) {
            this.text = text;
        }
        public String getColor() {
            return color;
        }
        public void setColor(String color) {
            this.color = color;
        }
        
    }
    
    static class StationType {
        
        private int id;
        private String letter;
        private String text;
        
        public int getId() {
            return id;
        }
        public void setId(int id) {
            this.id = id;
        }
        
        public void setLetter(String letter) {
            this.letter = letter;
        }
        
        public String getLetter() {
            return letter;
        }
        
        public String getText() {
            return text;
        }
        public void setText(String text) {
            this.text = text;
        }
    }
}
