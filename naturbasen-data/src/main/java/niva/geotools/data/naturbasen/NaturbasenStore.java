package niva.geotools.data.naturbasen;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.geotools.arcsde.data.ArcSDEDataStore;
import org.geotools.data.Query;
import org.geotools.data.Transaction;

import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.NameImpl;
import org.geotools.jdbc.JDBCDataStore;
import org.opengis.feature.type.Name;

public class NaturbasenStore extends ContentDataStore {
	
	private ArcSDEDataStore arcStore;
	private JDBCDataStore dbStore;
	
	public NaturbasenStore(ArcSDEDataStore arcStore, JDBCDataStore dbStore) {
		if (arcStore == null || dbStore == null) {
			throw new IllegalArgumentException("Arcsde and database-connections must be specified");
		}
		this.arcStore = arcStore;
		this.dbStore = dbStore;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		arcStore.dispose();
		dbStore.dispose();
	}
	
	
	@Override
	protected ContentFeatureSource createFeatureSource(ContentEntry entry) throws IOException {
		return new NaturbasenSource(entry, Query.ALL);
	}

	@Override
	protected List<Name> createTypeNames() throws IOException {
		
		ArrayList<Name> result = new ArrayList<Name>();
		
		addLayers(result, "flate");
		addLayers(result, "punkt");
		
		try {
			Connection conn = dbStore.getConnection(Transaction.AUTO_COMMIT);
			
			String sql = "select distinct naturtype_id from NIVA_GEOMETRY.BIOMANGFOLD_F order by naturtype_id";
	
			try
			{
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				while (rs.next()) {
					String id = rs.getString(1);
					addLayers ( result, "flate_" + id);
				}
				rs.close();
	
				sql = "select distinct naturtype_id from NIVA_GEOMETRY.BIOMANGFOLD_P order by naturtype_id";
				rs = stmt.executeQuery(sql);
				while (rs.next()){
					String id = rs.getString(1);
					addLayers ( result, "punkt_" + id);
				}
				rs.close();
	
				stmt.close();
			}
			catch (SQLException se) {
				se.printStackTrace(System.err);
			}
			finally {
				conn.close();
			}
		}
		catch (Exception ex) {
			LOGGER.severe("Exception in NaturbasenStore when fetching typeNames from Oracle database.");
			throw new IOException("Exception when fetching typeNames from Oracle database.", ex);
		}

		return result;
	}
	
	private void addLayers(ArrayList<Name> list, String prefix) {
		list.add(new NameImpl(prefix+"_aktiv"));
		list.add(new NameImpl(prefix+"_vurdering"));
		list.add(new NameImpl(prefix+"_arbeid"));
	}
	
	ArcSDEDataStore getArcStore() {
		return this.arcStore;
	}

}
