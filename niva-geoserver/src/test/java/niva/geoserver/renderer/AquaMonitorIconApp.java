package niva.geoserver.renderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.geotools.factory.CommonFactoryFinder;

import org.geotools.api.filter.FilterFactory;
import org.geotools.api.filter.expression.Literal;


public class AquaMonitorIconApp {
	
    public static final void main(String[] args) {
    	AquamonitorSymbolFactory fact = new AquamonitorSymbolFactory();
    	FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);

    	//Literal url = ff.literal("http://aquamonitor?keys=Vannkjemi,Plankton&vals=130,90&cols=55ff55,5555ff&w=220&h=90");
    	Literal url = ff.literal("http://aquamonitor?vals=130,90&cols=55ff55,5555ff&cnt=24");
    	//Literal url = ff.literal("http://aquamonitor?typ=A");
    	int size = 60;
    	try {
    	
    		Icon icn = fact.getIcon(null, url, AquamonitorSymbolFactory.FORMAT, size);
    		showChart("test", icn);
    		System.exit(0);
    		
    	}
    	catch (Exception ex) {
    		ex.printStackTrace(System.err);
    		System.exit(1);
    	}
    }
    
    private static void showChart(String name, Icon icn) throws Exception {
    	
        JFrame frame = new JFrame(name);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                e.getWindow().dispose();
            }
        });
        
        Container c = frame.getContentPane();
        c.setLayout(new BorderLayout());
        c.add(new JLabel(icn), BorderLayout.CENTER);
        c.setBackground(Color.WHITE);
        
        frame.setSize(new Dimension(500,500));
        frame.setVisible(true);  
        Thread.sleep(10000);
        frame.dispose();
    }
}
