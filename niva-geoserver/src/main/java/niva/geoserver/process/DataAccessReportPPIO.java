package niva.geoserver.process;

import java.io.InputStream;

import javax.xml.namespace.QName;

import org.geoserver.wps.ppio.XMLPPIO;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import niva.geotools.data.msaccess.DataAccessReport;

/**
 * 
 * @author Roar Brænden, NIVA
 *
 */
public class DataAccessReportPPIO extends XMLPPIO {

	protected DataAccessReportPPIO() {
		super(DataAccessReport.class, DataAccessReport.class, new QName("Report"));
	}

	@Override
	public void encode(Object object, ContentHandler handler) throws Exception {
		DataAccessReport report = (DataAccessReport) object;

        handler.startDocument();
        handler.startElement("", "", getElement().getLocalPart(), null);
        
        handler.startElement("", "", "Inserted", null);
        innerText(handler, report.getInserted());
        handler.endElement("", "", "Inserted");

        handler.startElement("", "", "Updated", null);
        innerText(handler, report.getUpdated());
        handler.endElement("", "", "Updated");
        
        handler.startElement("", "", "Deleted", null);
        innerText(handler, report.getDeleted());
        handler.endElement("", "", "Deleted");
        
        handler.startElement("", "", "Elapsed", null);
        innerText(handler, report.getElapsed());
        handler.endElement("", "", "Elapsed");
        
        handler.endElement("", "", getElement().getLocalPart());
        handler.endDocument();
	}

	/**
	 * 
	 */
	@Override
	public Object decode(InputStream input) throws Exception {
		throw new UnsupportedOperationException("This parameter is only meant for output.");
	}

	
	private static void innerText(ContentHandler handler, long value) throws SAXException {
		final String text = String.valueOf(value);
		handler.characters(text.toCharArray(), 0, text.length());
	}
}
