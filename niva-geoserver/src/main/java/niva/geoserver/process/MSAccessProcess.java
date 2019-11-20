package niva.geoserver.process;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.geoserver.config.GeoServer;
import org.geoserver.platform.GeoServerExtensions;

import org.geotools.jdbc.JDBCDataStore;
import org.geotools.process.ProcessException;
import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.geotools.util.logging.Logging;

import niva.geotools.data.msaccess.BegroingDataAccess;
import niva.geotools.data.msaccess.BunndyrDataAccess;

@DescribeProcess(title = "MS Access XY-table", description = "Update featureStore based on some prefined tables within MS Access database.")
public class MSAccessProcess implements NivaProcess {
	
	private static final Logger LOGGER = Logging.getLogger(MSAccessProcess.class);
	
	private GeoServer geoserver;
	
	public MSAccessProcess() {
		this.geoserver = GeoServerExtensions.bean(GeoServer.class);
	}
			
	
	@DescribeResult(description = "Returns true or exception")
	public boolean execute(@DescribeParameter(name= "workspace")String workspace,
			@DescribeParameter(name="storeName")String storeName,
			@DescribeParameter(name="accessPath")String accessPath,
			@DescribeParameter(name="database", description="Begroing;Bunndyr")String database) 
	throws ProcessException {
		
		
		try {
			
			final JDBCDataStore dataStore = (JDBCDataStore) geoserver.getCatalog()
																	 .getDataStoreByName(workspace, storeName)
																	 .getDataStore( null );
			
			switch (database.toLowerCase()) {
			case "begroing":
					new BegroingDataAccess(dataStore, accessPath).updateAll();
					break;
			case "bunndyr":
				new BunndyrDataAccess(dataStore, accessPath).updateAll();
				break;
			}
			return true;
		} catch (IOException | SQLException ex) {
			LOGGER.severe(ex.getMessage());
			throw new ProcessException("MS Access processing failed.", ex);
		}
	}
}
