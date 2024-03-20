package niva.geoserver.process;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.process.ProcessException;
import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.geotools.process.vector.SimpleProcessingCollection;
import org.geotools.util.logging.Logging;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.feature.type.GeometryDescriptor;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

@DescribeProcess(title = "Dissolve features", description = "Groups common records with a union of the respective geometries.")
public class DissolveProcess implements NivaProcess {
	
	private static Logger LOGGER = Logging.getLogger(DissolveProcess.class);
	
	@DescribeResult(name = "result", description = "Features with grouped records.")
	public SimpleFeatureCollection execute(
			@DescribeParameter(name = "features", description = "Featureset that should be dissolved.") SimpleFeatureCollection features) {
	

		SimpleFeatureType origType = features.getSchema();
		
		SimpleFeatureTypeBuilder tBuilder = new SimpleFeatureTypeBuilder();
		tBuilder.setName(new NameImpl(origType.getName().getNamespaceURI(),
									"Dissolved_" + origType.getTypeName()));
		
		GeometryDescriptor origGeom = origType.getGeometryDescriptor();
		tBuilder.setCRS(origGeom.getCoordinateReferenceSystem());
		
		if (origGeom.getType().getBinding().isAssignableFrom(Polygon.class)) {
			tBuilder.add(origGeom.getLocalName(), MultiPolygon.class);
		}
		else if (origGeom.getType().getBinding().isAssignableFrom(MultiPolygon.class)) {
			tBuilder.add(origGeom.getLocalName(), MultiPolygon.class);
		}
		else if (origGeom.getType().getBinding().isAssignableFrom(Point.class)) {
			tBuilder.add(origGeom.getLocalName(), MultiPoint.class);
		}
		else
			throw new ProcessException("Can't dissolve the geometry.");
		
		SimpleFeatureType resType = tBuilder.buildFeatureType();
		
		return new DissolvedFeatureCollection(resType, features);	
	}

	
	static class DissolvedFeatureCollection extends SimpleProcessingCollection {
		
		private SimpleFeatureType schema;
		private String[] fields;
		private List<Object[]> values;
		private SimpleFeatureCollection features;
		
		DissolvedFeatureCollection(SimpleFeatureType schema, SimpleFeatureCollection features) {
			this.schema = schema;
			this.features = features;
			
			this.values = new LinkedList<Object[]>();
			this.values.add(new Object[] {});
			this.fields = null;
			
		}

		DissolvedFeatureCollection(SimpleFeatureType schema, String[] fields, List<Object[]> values, SimpleFeatureCollection features) {
			this.schema = schema;
			this.fields = fields;
			this.values = values;
			this.features = features;
		}
		
		
		@Override
		protected SimpleFeatureType buildTargetFeatureType() {
			return schema;
		}
		
		
		@Override
		public SimpleFeatureIterator features() {
			LOGGER.fine("DissolvedFeatureCollection.features() called");
			final Iterator<Object[]> valIter = values.iterator();
			final SimpleFeatureBuilder fBuild = new SimpleFeatureBuilder(schema);
			
			return new SimpleFeatureIterator() {
				
				private int i = 0;
				
				@Override
				public boolean hasNext() {
					return valIter.hasNext();
				}

				@Override
				public SimpleFeature next() throws NoSuchElementException {
					LOGGER.fine("DissolvedFeatureCollection.features().next() called");
					
					valIter.next();
					
					if (fields == null) {
						
						SimpleFeatureIterator origIter = features.features();
						LinkedList<Geometry> geoms = new LinkedList<Geometry>();
						
						while (origIter.hasNext()) {
							geoms.add((Geometry) origIter.next().getDefaultGeometry());
						}
						origIter.close();
						
						fBuild.add(combineIntoOneGeometry(geoms));
					}
					else {
						
					}
					return fBuild.buildFeature(String.valueOf(i++));
				}

				@Override
				public void close() {
				}
				
				
				private Geometry combineIntoOneGeometry (Collection<Geometry> origGeoms) {
					LOGGER.fine("DissolvedFeatureCollection.combineIntoOneGeometry() called");
					GeometryFactory fact = JTSFactoryFinder.getGeometryFactory(null);
					GeometryCollection collection = (GeometryCollection) fact.buildGeometry(origGeoms);
					return collection.union();
				}
				
			};
		}

		@Override
		public ReferencedEnvelope getBounds() {
			LOGGER.fine("DissolvedFeatureCollection.getBounds() called");
			return features.getBounds();
		}

		@Override
		public int size() {
			LOGGER.fine("DissolvedFeatureCollection.size() called");
			return values.size();
		}
		
	}
}
