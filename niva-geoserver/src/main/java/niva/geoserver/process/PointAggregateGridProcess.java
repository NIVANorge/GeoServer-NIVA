package niva.geoserver.process;


import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;


import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.store.ReprojectingFeatureCollection;
import org.geotools.feature.collection.AbstractFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.process.ProcessException;
import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.geotools.process.vector.ClipProcess;


import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Point;



/**
 * Creates a grid based on input of image size / cell size, collects features into cells, sums attribute values and creates centroid point.
 * 
 * @author Roar Brænden, NIVA
 *
 */
@DescribeProcess(title = "Aggregate points grid", description = "Collects points into different grid cells based on input. Sums up attributes and finds centroid point.")
public class PointAggregateGridProcess implements NivaProcess {
	

	@DescribeResult(name = "result", description = "centroid point with sum of attributes")
	public SimpleFeatureCollection execute(
				@DescribeParameter(name = "points", description = "Point features that should be aggregated") SimpleFeatureCollection points,
				@DescribeParameter(name = "outputBBOX", description = "Boundary of result image") ReferencedEnvelope outputBbox,
				@DescribeParameter(name = "outputWidth", description = "Width of result image") Integer outputWidth,
				@DescribeParameter(name = "outputHeight", description = "Height of result image") Integer outputHeight,
				@DescribeParameter(name = "cellSize", description = "Size of grid cells") Integer cellSize,
				@DescribeParameter(name = "aggregateAttributes", description = "Attributes to aggregate on", collectionType=String.class) Set<String> aggregateAttributes
			) throws ProcessException {
		
		
		final ClipProcess cp = new ClipProcess();
		
		
		// Create result with same crs as outputBbox, Point as geometry and the given attributes
		
		CoordinateReferenceSystem crs = outputBbox.getCoordinateReferenceSystem();
		String[] arr = new String[aggregateAttributes.size()];
		aggregateAttributes.toArray(arr);
		
		SimpleFeatureType schema = points.getSchema();
		
		String missingAttributes = null;
		
		for (String a: arr) {
			if (schema.getDescriptor(a) == null)
				missingAttributes = (missingAttributes == null ? a : ", " + a);
		}
		
		if (missingAttributes != null)
			throw new ProcessException("AggregateAttributes has some attributes that doesn't exists: " + missingAttributes);
		
		CoordinateReferenceSystem pcrs = schema.getCoordinateReferenceSystem();
		
		// If points and outputBbox doesn't have the same crs, reproject envelope before clipping
		Polygon rect = createOuterBound(outputBbox, pcrs);
		
		// Clip points with outputBbox
		points = cp.execute(points, rect, true);
		
		double dy = outputBbox.getHeight() * ((double)cellSize/(double)outputHeight);
		double dx = outputBbox.getWidth() * ((double)cellSize/(double)outputWidth);
		
		int y1 = (int)Math.floor(outputBbox.getMinY() / dy);
		int x1 = (int)Math.floor(outputBbox.getMinX() / dx);
		
		int my = (int)Math.floor(outputBbox.getMaxY() / dy) - y1;
		int mx = (int)Math.floor(outputBbox.getMaxX() / dx) - x1;
		

		SimpleFeatureType resultType = createResultType(schema, arr, crs);
		
		AggregatedFeatureCollection result = new AggregatedFeatureCollection(resultType, arr, mx, my);
		
		// If points and outputBbox doesn't have the same crs, reproject points
		if ( pcrs != null && !crs.equals(pcrs) ) {
			points = new ReprojectingFeatureCollection(points, pcrs, crs);
		}
	
		HashMap<String, LinkedList<Point>> pointCollections = new HashMap<String, LinkedList<Point>>();
		SimpleFeatureIterator iter = points.features();
		
		while (iter.hasNext()) {
			SimpleFeature feat = iter.next();
			
			Point pnt = (Point)feat.getDefaultGeometry();
			int y = (int)Math.floor(pnt.getCoordinate().y / dy) - y1;
			int x = (int)Math.floor(pnt.getCoordinate().x / dx) - x1;
			
			if (x >= 0 && x <= mx && y >= 0 && y <= my) {
				SimpleFeature cell = result.addPoint(x, y, feat);
				
				String pid = cell.getID();
				
				LinkedList<Point> list = pointCollections.get( pid );
				
				if (list == null) {
					list = new LinkedList<Point>();
					pointCollections.put(pid, list);
					
					Polygon bnd = JTS.toGeometry( new Envelope((x1+x) * dx, (x1+x+1) * dx, (y1+y) * dy, (y1+y+1) * dy) );
					cell.setAttribute("CELL_BOUNDS", bnd);
				}
				
				list.add(pnt);
			}
		}
		
		result.createPointGeometry(pointCollections);
		
		iter.close();

		return result;
	}


	private Polygon createOuterBound(ReferencedEnvelope outputBbox,	CoordinateReferenceSystem pcrs) {
		Polygon rect;
		CoordinateReferenceSystem crs = outputBbox.getCoordinateReferenceSystem();
		
		if (!crs.equals(pcrs)) {
			boolean lenient = false;
			int numPoints = 20;
			try {
				ReferencedEnvelope projEnv = outputBbox.transform(pcrs, lenient, numPoints);
				rect = JTS.toGeometry(projEnv);
			}
			catch (TransformException te) {
				throw new ProcessException(te);
			}
			catch (FactoryException fe) {
				throw new ProcessException(fe);
			}
		}
		else {
			rect = JTS.toGeometry(outputBbox);
		}
		return rect;
	}

	private SimpleFeatureType createResultType(SimpleFeatureType source, String[] attributes, CoordinateReferenceSystem crs) {
		
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setCRS(crs);
		Name srName = source.getName();
		
		builder.setName(srName.getLocalPart() + "_aggregated");
		
		builder.add("CENTRAL_POINT", Point.class);
		
		builder.add("CELL_BOUNDS", Polygon.class);
		
		builder.add("STATION_TYPE", String.class);
		
		builder.add("COUNT", Integer.class);
		
		
		for (String s : attributes)
			builder.add(s, Integer.class);
		
		builder.add("EMPTY", Integer.class);
		
		builder.setDefaultGeometry("CENTRAL_POINT");
		
		return builder.buildFeatureType();
	}
	
	static class AggregatedFeatureCollection extends AbstractFeatureCollection {
		SimpleFeature[][] matrix;
		SimpleFeatureBuilder builder;
		String[] attributes;
		GeometryFactory gf = new GeometryFactory();
		
		AggregatedFeatureCollection(SimpleFeatureType schema, String[] attributes, int mx, int my) {
			super(schema);
			this.attributes = attributes;
			this.matrix = new SimpleFeature[mx + 1][my + 1];
			this.builder = new SimpleFeatureBuilder(schema);
		}
		
		SimpleFeature addPoint(int x, int y, SimpleFeature pnt) {
			SimpleFeature cell = matrix[x][y];
			String id = Integer.toString(x) + ":" + Integer.toString(y);
			
			if (cell == null) {
				
				builder.add( null );
				
				builder.add( null );
				
				builder.add(pnt.getAttribute("STATION_TYPE"));
				
				builder.add( 1 );
				
				boolean empty = true;
				
				for (String attr : attributes) {
					Integer i = (Integer)pnt.getAttribute(attr);
					empty = (empty && (i == 0));
					builder.add( i );
				}
				
				builder.add( (empty ? 1 : 0) );
				
				cell = builder.buildFeature( id );
				
				this.matrix[x][y] = cell;
			}
			else {
				int cnt = (Integer)cell.getAttribute("COUNT");
				if (cnt == 1) {
					cell.setAttribute("STATION_TYPE", null);
				}
				cell.setAttribute("COUNT", ++cnt);
				
				boolean empty = true;
				
				for (String attr : attributes) {
					Integer i = (Integer)pnt.getAttribute(attr);
					if (i != 0) {
						empty = false;
						cell.setAttribute(attr, i + (Integer)cell.getAttribute(attr));
					}
				}
				
				if (empty) {
					cell.setAttribute("EMPTY", 1 + (Integer)cell.getAttribute("EMPTY"));
				}
			}
			
			return cell;
		}
		
		void createPointGeometry(HashMap<String, LinkedList<Point>> pointCollections) {
			for (int x = 0; x < this.matrix.length; x++) {
				for (int y = 0; y < this.matrix[x].length; y++) {
					SimpleFeature cell = this.matrix[x][y];
					if (cell != null) {
						LinkedList<Point> points = pointCollections.get(cell.getID());
						Point[] arr = new Point[points.size()];
						points.toArray(arr);
						
						MultiPoint mp = gf.createMultiPoint(arr);
						
						cell.setAttribute("CENTRAL_POINT", mp.getCentroid());
					}
				}
			}
		}

		@Override
		protected Iterator<SimpleFeature> openIterator() {
			Iterator<SimpleFeature> result = new Iterator<SimpleFeature>() {
				int x = 0;
				int y = 0;
				SimpleFeature next = null;
				
				@Override
				public boolean hasNext() {
					SimpleFeature cell = null;
					while (cell == null && x < matrix.length) {
						while (cell == null && y < matrix[x].length) {
							cell = matrix[x][y++];
						}
						
						if (y == matrix[x].length) {
							x++;
							y = 0;
						}
					}
					next = cell;
					return (next != null);
				}
				

				@Override
				public SimpleFeature next() throws NoSuchElementException {
					if (next != null) {
						return next;
					}
					else {
						throw new NoSuchElementException();
					}
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
				
			};
			return result;
		}

		@Override
		public int size() {
			
			int ant = 0;
			for (int j = 0; j < this.matrix.length; j++) {
				for (int i = 0; i < this.matrix[j].length; i++) {
					if (this.matrix[j][i] != null) ant++; 
				}
			}
			return ant;
		}

		@Override
		public ReferencedEnvelope getBounds() {
			double x1,x2,y1,y2;
			x1 = Double.MAX_VALUE;
			int x = 0;
			while (x1 == Double.MAX_VALUE && x < this.matrix.length) {
				for (int y = 0; y < this.matrix[x].length; y++) {
					SimpleFeature cell = this.matrix[x][y];
					if (cell != null) {
						x1 = Math.min(x1, ((Point)cell.getDefaultGeometry()).getX());
					}
				}
				x++;
			}
			
			if (x1 == Double.MAX_VALUE)
				return new ReferencedEnvelope();
			
			x2 = Double.MIN_VALUE;
			x = this.matrix.length - 1;
			while (x2 == Double.MAX_VALUE) {
				for (int y = 0; y < this.matrix[x].length; y++) {
					SimpleFeature cell = this.matrix[x][y];
					if (cell != null) {
						x2 = Math.max(x2, ((Point)cell.getDefaultGeometry()).getX());
					}
				}
				x++;
			}
			
			y1 = Double.MAX_VALUE;
			int y = this.matrix[0].length - 1;
			while (y1 == Double.MAX_VALUE) {
				for (int j = 0; j < this.matrix.length; j++) {
					SimpleFeature cell = this.matrix[j][y];
					if (cell != null) {
						y1 = Math.min( y1, ((Point)cell.getDefaultGeometry()).getY());
					}
				}
			}
			
			y2 = Double.MIN_VALUE;
			y = 0;
			while (y2 == Double.MIN_VALUE) {
				for (int j = 0; j < this.matrix.length; j++) {
					SimpleFeature cell = this.matrix[j][y];
					if (cell != null) {
						y2 = Math.max( y2,  ((Point)cell.getDefaultGeometry()).getY());
					}
				}
			}
			
			return new ReferencedEnvelope(x1, x2, y1, y2, this.schema.getCoordinateReferenceSystem());
		}
	}

}
