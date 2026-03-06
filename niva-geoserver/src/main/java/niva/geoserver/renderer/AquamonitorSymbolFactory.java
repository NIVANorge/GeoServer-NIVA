package niva.geoserver.renderer;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.general.DefaultPieDataset;
import org.geotools.renderer.style.ExternalGraphicFactory;
import org.geotools.api.feature.Feature;
import org.geotools.api.filter.expression.Expression;

/**
 * 
 * Generating the AquaMonitor map symbol.
 * 
 * @author Roar Brænden, NIVA
 *
 */
public class AquamonitorSymbolFactory implements ExternalGraphicFactory {
	
	final static String FORMAT = "application/chart";
	final static String HTTP_CHART = "http://aquamonitor?";
	
	private final static int MIN_PIE_BORDER = 4;
	

	@Override
	public Icon getIcon(Feature feature, Expression urlExpr, String format, int size) throws Exception {
	       // evaluate the expression as a string, get the query params
        String url = urlExpr.evaluate(feature, String.class);
        if (!validRequest(url, format)) {
            return null;
        }
        
        Map<String, String> params = parseQueryString(url.substring(HTTP_CHART.length()));
        
        String typ = params.get("typ");
        String cnt = params.get("cnt");
        String sw = params.get("w");
        String sh = params.get("h");
        int w = (sw != null ? Integer.parseInt(sw) : size);
        int h = (sh != null ? Integer.parseInt(sh) : size);
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D gr = bi.createGraphics();
        try {
	        if ( params.get("cols") != null ) {
	    		JFreeChart pie = createPieSymbolChart(params);
	            int cs = Math.max((int)Math.round( 0.15*size), MIN_PIE_BORDER);
	            pie.draw(gr, new Rectangle2D.Double( 0, 0, w, h));
	            if (typ != null || cnt != null) {
		            drawOval(gr, (int)Math.round( 0.5*(w-size) ) + cs,
		            		(int)Math.round( 0.5*(h-size) ) + cs,
		            		size - (2*cs),
		            		size - (2*cs));
		            
		            drawText(gr, w, h, typ, cnt);
	            }
	        }
	        else {
	            drawOval(gr, (int)Math.round(0.5*(w-size)),
	            		(int)Math.round(0.5*(h-size)),
	            		size, size);
	            
	            drawText(gr, w, h, typ, cnt);
	        }
	        return new ImageIcon(bi);
        } finally {
            gr.dispose();
        }
	}
	
	private void drawText(Graphics2D gr, int width, int height, String typ, String cnt) {
        String text;
        if (typ != null) {
            gr.setFont(new Font("Arial", Font.BOLD, 14));
            text = typ;
        }
        else if (cnt != null) {
        	gr.setFont(new Font("Arial", Font.PLAIN, 12));	
        	text = cnt;
        }
        else {
            return;
        }
    	final FontMetrics met = gr.getFontMetrics();
    	final int x = (int)Math.round(0.5 * (width - met.stringWidth(text)));
    	final int y = (int)Math.round(0.5 * height) + (int)Math.round(0.5 * met.getAscent()) - 2;
    	
    	gr.setColor(new Color(9, 9, 9));
    	gr.drawString(text, x, y);
	}
	
	private void drawOval(Graphics2D gr, int x, int y, int w, int h) {
		
		String col = "e46809";
		
        gr.setColor(new Color(Integer.parseInt(col.substring(0,2), 16),
        		Integer.parseInt(col.substring(2,4), 16),
        		Integer.parseInt(col.substring(4), 16)));
        
        gr.fillOval(x, y, w, h);
	}

	private boolean validRequest(String url, String format) {
        return FORMAT.equals(format) && url.startsWith(HTTP_CHART);
    }


	private HashMap<String, String> parseQueryString(String substring) {
	    final HashMap<String, String> res = new HashMap<>();
		Arrays.stream(substring.split("&"))
		        .filter(s -> s.contains("="))
		        .map(s -> s.split("="))
		        .forEach(p -> res.put(p[0], p[1]));
		return res;
	}
	
	@SuppressWarnings("unchecked")
	private JFreeChart createPieSymbolChart(Map<String, String> params) {

        DefaultPieDataset<String> data = new DefaultPieDataset<>();
        if (params.get("cols") ==  null) {
            throw new IllegalArgumentException("At least give me some colors");
        }

        String[] cols = params.get("cols").toString().split(",");
        String[] keys = (params.get("keys") != null ? params.get("keys").toString().split(",") : null);
        
        if (keys != null && keys.length != cols.length) {
            throw new IllegalArgumentException("Keys should be same length as colors.");
        }
        
        boolean hasKeys = (keys != null);
        
        String[] values = (params.get("vals") != null ? params.get("vals").toString().split(",") : null);
        
        if (values != null && values.length != cols.length) {
            throw new IllegalArgumentException("Values should be same length as colors.");
        }
        
        for (int i = 0; i < cols.length; i++) {
            String k = ( hasKeys ? keys[i] : Integer.toString(i));
            if (values != null) {
                data.setValue(k, (values[i].equals("") ? 0 : Integer.parseInt(values[i])));
            }
            else {
                data.setValue(k, 1);
            }
        }
        
        final JFreeChart chart = ChartFactory.createPieChart(null, data, false, false, false);
        chart.setBackgroundPaint(new Color(255,255,255,0));
        
        PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
        plot.setShadowXOffset(0.0);
        plot.setShadowYOffset(0.0);
        
        if ( !hasKeys ) {
            plot.setLabelGenerator(null);
            plot.setInteriorGap(0.0);
            plot.setInsets(RectangleInsets.ZERO_INSETS, false);
        }
        
        plot.setOutlineVisible(false);
        plot.setSectionOutlinesVisible(false);      
        plot.setBackgroundAlpha(0);
        
        for (int i = 0; i < cols.length; i++) {
            int r, g, b;
            r = Integer.parseInt(cols[i].substring(0, 2), 16);
            g = Integer.parseInt(cols[i].substring(2, 4), 16);
            b = Integer.parseInt(cols[i].substring(4), 16);
            Color col = new Color(r, g, b);
            String k = (keys != null ? keys[i] : Integer.toString(i));
            plot.setSectionPaint(k, col);
        }
        return chart;
	}

}
