package niva.geoserver.process;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.BinaryComparisonAbstract;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.geotools.process.vector.SimpleProcessingCollection;
import org.geotools.util.logging.Logging;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

@DescribeProcess(title = "Extract a line betweeen a continous points.")
public class LinearPointToLineProcess implements NivaProcess {
	
	private static Logger LOGGER = Logging.getLogger(LinearPointToLineProcess.class);
	

	@DescribeResult(name = "result", description = "Collection of linear geometries.")
	public SimpleFeatureCollection execute(
			@DescribeParameter(name = "points", description = "Points features (should be sorted)") SimpleFeatureCollection points,
			@DescribeParameter(name = "distance", description = "Maximum distance for points to be connected.") Double distance) {
		
		LOGGER.fine("Collection of linear geometries created");
		String geomField;
		geomField = points.getSchema().getGeometryDescriptor().getLocalName();
		
		FilterFactoryImpl filtFact = new FilterFactoryImpl();
		BinaryComparisonAbstract filter = (BinaryComparisonAbstract) filtFact.dwithin(geomField, null, distance, null);
	
		SimpleFeatureCollection result = new LinearFeatureCollection(points, filter);
		
		return result;
	}
	
	static class LinearFeatureCollection extends SimpleProcessingCollection {
		
		private SimpleFeatureCollection points;
		private SimpleFeatureType schema;
		private BinaryComparisonAbstract filter;
		
		
		LinearFeatureCollection(SimpleFeatureCollection points, BinaryComparisonAbstract filter) {
			this.points = points;
			this.schema = points.getSchema();
			this.filter = filter;
		}

		@Override
		protected SimpleFeatureType buildTargetFeatureType() {
			SimpleFeatureTypeBuilder tbuilder = new SimpleFeatureTypeBuilder();
			tbuilder.setName("Points");
			tbuilder.setCRS(schema.getCoordinateReferenceSystem());
			tbuilder.add("SHAPE", MultiPoint.class);

			return tbuilder.buildFeatureType();
		}
		
		@Override
		public SimpleFeatureIterator features() {
			
			LOGGER.fine("LinearFeatureCollection.features() called.");
			
			return new RunningFilteringIterator(points.features(), filter, new AggregateFeatures() {

				private List<Point> vertices = new ArrayList<Point>();
				SimpleFeatureBuilder builder = new SimpleFeatureBuilder(buildTargetFeatureType());
				GeometryFactory geomFact = JTSFactoryFinder.getGeometryFactory(null);
				private int id = 0;
				
				@Override
				public void add(SimpleFeature feature) {
					Point pnt = (Point)feature.getDefaultGeometry();
					vertices.add(pnt);
				}

				@Override
				public SimpleFeature aggregate() {
					MultiPoint endPoints;
					
					LOGGER.fine("AggregateFeatures.aggregate() called.");
					
					Coordinate[] coordinates = new Coordinate[2];
					coordinates[0] = vertices.get(0).getCoordinate();
					coordinates[1] = vertices.get(vertices.size() - 1).getCoordinate();
					
					endPoints = geomFact.createMultiPoint(coordinates);

					builder.add(endPoints);
					return builder.buildFeature(String.valueOf(id++));	
				}

				@Override
				public void clear() {
					vertices.clear();
				}

				@Override
				public boolean isEmpty() {
					return vertices.isEmpty();
				}
			});
		}

		@Override
		public ReferencedEnvelope getBounds() {
			return points.getBounds();
		}


		@Override
		public int size() {
			RunningFilteringIterator iterator = new RunningFilteringIterator(points.features(), filter, new AggregateFeatures() {
				boolean isEmpty = true;
				
				@Override
				public void add(SimpleFeature feature) {
					isEmpty = false;
				}

				@Override
				public SimpleFeature aggregate() {
					return null;
				}

				@Override
				public void clear() {
					isEmpty = true;
				}

				@Override
				public boolean isEmpty() {
					return isEmpty;
				}
			});
			
			int i = 0;
			
			while (iterator.hasNext()) {
				iterator.next();
				i++;
			}
			
			iterator.close();
			return i;
		}
	}
}
