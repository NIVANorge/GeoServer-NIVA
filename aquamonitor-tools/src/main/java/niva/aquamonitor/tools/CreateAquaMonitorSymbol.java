package niva.aquamonitor.tools;

import java.awt.image.RenderedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.geotools.console.CommandLine;
import org.geotools.console.Option;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.api.filter.FilterFactory;
import org.geotools.api.filter.expression.Literal;

import niva.geoserver.renderer.AquamonitorSymbolFactory;

/**
 * A command line tool to generate an image file with the symbol to use in a legend.
 * 
 * @author Roar Brænden, NIVA
 *
 */
public class CreateAquaMonitorSymbol extends CommandLine {
    
    /** Station type letter to use. */
    @Option
    String type;
    
    /** Count to use inside circle. */
    @Option
    String count;
    
    /** Text to display outside the pie. */
    @Option
    String keys;
    
    /** Relative size of pie slices. */
    @Option
    String values;
    
    /** Colors to use in pie. */
    @Option
    String colors;
    
    /** Size specified in pixels. */
    @Option
    Integer size;
    
    /** Format to use for image file. Default=png */
    @Option
    String format;
    
    /** Output file from first argument. Default=aquamonitor_symbol.png */
    File outFile;
    
    CreateAquaMonitorSymbol(String... args) {
        super(args, 1);
        if (type != null && count != null) {
            throw new IllegalArgumentException("Type and Count can't be specified at the same time.");
        }
        outFile = arguments.length == 0 ? new File("aquamonitor_symbol.png") : new File(arguments[0]);
        if (format == null) {
			format = "png";
		}
    }

    
    private static final FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
    
    void execute() throws Exception {
        final AquamonitorSymbolFactory fact = new AquamonitorSymbolFactory();
        Literal url = ff.literal(AquamonitorSymbolFactory.HTTP_CHART 
                + (type != null ? "typ=" + type : count != null ? "cnt=" + count : "") 
                + (values != null ? "&vals=" + values : "") 
                + (colors != null ? "&cols=" + colors : "")
                + (keys != null ? "&keys=" + keys : ""));
        ImageIcon icn = (ImageIcon)fact.getIcon(null, url, AquamonitorSymbolFactory.FORMAT, size);
        if (!ImageIO.write((RenderedImage)icn.getImage(), format, outFile)) {
            throw new IllegalArgumentException("No suitable writer found for format: " + format);
        }
    }

    /**
     * Program taking options type, values, colors and size.
     * 
     * <p>Creating a image file based on a call for application/chart with url: 
     * <i>http://aquamonitor?typ={type}&vals={valuess}&cols={colors}
     */
    public static void main(String... args) {
        
        try {
            CreateAquaMonitorSymbol tool = new CreateAquaMonitorSymbol(args);
            tool.execute();
            System.exit(0);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }
}
