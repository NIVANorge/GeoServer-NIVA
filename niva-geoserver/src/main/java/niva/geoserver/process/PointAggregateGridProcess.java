package niva.geoserver.process;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.collection.AbstractFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.GeometryCoordinateSequenceTransformer;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.process.ProcessException;
import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.geotools.referencing.CRS;
import org.geotools.util.logging.Logging;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.filter.FilterFactory;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.TransformException;



/**
 * Creates a grid based on input of image size / cell size, collects features into cells, sums attribute values and creates centered point.
 * There are one property that hints towards it's original usage. Namely that we're adding attribute: STATION_TYPE
 * @author Roar Brænden, NIVA
 *
 */
@DescribeProcess(title = "Aggregate points grid",
			description = "Collects points into different grid cells based on input. Sums up attributes and finds centroid point.")
public class PointAggregateGridProcess implements NivaProcess {
	
	private static final Logger LOGGER = Logging.getLogger(PointAggregateGridProcess.class);
	
	private static final GeometryFactory GF = new GeometryFactory();
	
	private static final FilterFactory FF = CommonFactoryFinder.getFilterFactory();
	
	/**
	 * The points are 
	 */
	@DescribeResult(name = "result", description = "centroid point with sum of attributes")
	public SimpleFeatureCollection execute(
				@DescribeParameter(name = "points", description = "Point features that should be aggregated") SimpleFeatureCollection points,
				@DescribeParameter(name = "outputBBOX", description = "Boundary of result image") ReferencedEnvelope outputBbox,
				@DescribeParameter(name = "outputWidth", description = "Width of result image") Integer outputWidth,
				@DescribeParameter(name = "outputHeight", description = "Height of result image") Integer outputHeight,
				@DescribeParameter(name = "cellSize", description = "Size of grid cells") Integer cellSize,
				@DescribeParameter(name = "aggregateAttributes", description = "Attributes to aggregate on", collectionType=String.class) Set<String> aggregateAttributes
			) throws ProcessException {
		
		// Create result with same crs as outputBbox, Point as geometry and the given attributes
		LOGGER.fine("points are of class " + points.getClass().getCanonicalName());
		final CoordinateReferenceSystem crs = outputBbox.getCoordinateReferenceSystem();		
		final SimpleFeatureType schema = points.getSchema();
		String missingAttributes = null;
		List<String> foundAttributes = new ArrayList<>(aggregateAttributes.size());
		for (String attr : aggregateAttributes) {
			if (schema.getDescriptor(attr) == null) {
				missingAttributes = (missingAttributes == null ? attr : ", " + attr);
			} else {
				foundAttributes.add(attr);
			}
		}
		if (missingAttributes != null) {
			LOGGER.warning("AggregateAttributes has some attributes that doesn't exists: " + missingAttributes);	
		}
		
		final String[] attributeArr = foundAttributes.toArray(new String[foundAttributes.size()]);
		
		final CoordinateReferenceSystem pcrs = schema.getCoordinateReferenceSystem();
		
		// If points and outputBbox doesn't have the same crs, reproject envelope before clipping
		Polygon rect = createOuterBound(outputBbox, pcrs);
		
		// Filter points with outputBbox
		String geometryField = schema.getGeometryDescriptor().getLocalName();
		points = points.subCollection(FF.intersects(FF.property(geometryField), FF.literal(rect)));
		
		double dy = outputBbox.getHeight() * ((double)cellSize/(double)outputHeight);
		double dx = outputBbox.getWidth() * ((double)cellSize/(double)outputWidth);
		
		int y1 = (int)Math.floor(outputBbox.getMinY() / dy);
		int x1 = (int)Math.floor(outputBbox.getMinX() / dx);
		
		int my = (int)Math.floor(outputBbox.getMaxY() / dy) - y1;
		int mx = (int)Math.floor(outputBbox.getMaxX() / dx) - x1;
		
		// If points and outputBbox doesn't have the same crs, reproject points to crs
	    final GeometryCoordinateSequenceTransformer tx;
		if ( !CRS.equalsIgnoreMetadata(crs, pcrs) ) {
			try {
				tx = new GeometryCoordinateSequenceTransformer();
				tx.setMathTransform(CRS.findMathTransform(pcrs, crs));
			} catch (FactoryException ex) {
				throw new ProcessException("PointAggregateGridProcess has a failure in setup.", ex);
			}
		} else {
			tx = null;
		}
		AggregatedFeatureCollection result = new AggregatedFeatureCollection(schema, crs, tx, attributeArr, dx, dy, x1, y1, mx, my);
		try (SimpleFeatureIterator features = points.features()) {
			while (features.hasNext()) {
				result.addPoint(features.next());
			}
		}
		result.computeCentralPoints();		
		return result;
	}


	private Polygon createOuterBound(ReferencedEnvelope outputBbox,	CoordinateReferenceSystem pcrs) {
		Polygon rect;
		CoordinateReferenceSystem crs = outputBbox.getCoordinateReferenceSystem();
		
		if (!CRS.equalsIgnoreMetadata(crs, pcrs)) {
			boolean lenient = false;
			int numPoints = 20;
			try {
				ReferencedEnvelope projEnv = outputBbox.transform(pcrs, lenient, numPoints);
				rect = JTS.toGeometry(projEnv);
			}
			catch (FactoryException | TransformException e) {
				throw new ProcessException("Failure while transforming envelope to crs [" + pcrs + "]", e);
			}
		}
		else {
			rect = JTS.toGeometry(outputBbox);
		}
		return rect;
	}



	
	/**
	 * Creating a memory-based SimpleFeatureCollection.
	 *
	 */
	private static class AggregatedFeatureCollection extends AbstractFeatureCollection {

		private static final int CEN_INX = 0;

		private static final int STTY_INX = 2;
		
		private static final int CNT_INX = 3;
		
		private static final int ATT_INX = 4;
		
		// EMPTY field is placed after the attributes. Might be a point to keep it like that
		private final int emyInx;
		
		// Array of the cells
		private final SimpleFeature[][] matrix;
		
		// Builder to create cell features
		private final SimpleFeatureBuilder builder;
		
		// Attributes that are collected, index of original feature
		private final int[] attrInx;
		
		private final Map<String, List<Point>> pointCollections;
		
		private final GeometryCoordinateSequenceTransformer tx;
		
		private final double dy;
		private final double dx;
		
		private final int y1;
		private final int x1;
		
		private final int mx;
		private final int my;
		

		AggregatedFeatureCollection(SimpleFeatureType pntType, CoordinateReferenceSystem crs, GeometryCoordinateSequenceTransformer tx, 
									String[] attributes, 
									double dx, double dy, int x1, int y1, int mx, int my) {
			super(createResultType(pntType, attributes, crs));
			this.attrInx = new int[attributes.length];
			for (int i = 0; i < attributes.length; i++) {
				attrInx[i] = pntType.indexOf(attributes[i]);
			}
			this.matrix = new SimpleFeature[mx + 1][my + 1];
			this.builder = new SimpleFeatureBuilder(schema);
			this.tx = tx;
			pointCollections = new ConcurrentHashMap<>();
			this.dx = dx;
			this.dy = dy;
			this.x1 = x1;
			this.y1 = y1;
			this.mx = mx;
			this.my = my;
			emyInx = 4 + attributes.length;
		}
		
		private static SimpleFeatureType createResultType(SimpleFeatureType source, String[] attributes, CoordinateReferenceSystem crs) {
			
			final SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
			builder.setName(source.getName().getLocalPart() + "_aggregated");
			builder.setCRS(crs);
			builder.add("CENTRAL_POINT", Point.class);
			builder.add("CELL_BOUNDS", Polygon.class);
			builder.add("STATION_TYPE", String.class);
			builder.add("COUNT", Integer.class);
			for (String attr : attributes) {
				builder.add(attr, Integer.class);
			}
			builder.add("EMPTY", Integer.class);
			builder.setDefaultGeometry("CENTRAL_POINT");
			
			return builder.buildFeatureType();
		}
		
		/**
		 * add a new point in cell x,y - feature is original while pnt could be the transformed version
		 */
		void addPoint(SimpleFeature feature) {
			Point pnt;
			try {
				pnt = (Point)(tx == null ? feature.getDefaultGeometry() : tx.transform((Geometry)feature.getDefaultGeometry()));
			} catch (TransformException e) {
				throw new ProcessException("Error with transformation.", e);
			}
			
			final int y = (int)Math.floor(pnt.getCoordinate().y / dy) - y1;
			final int x = (int)Math.floor(pnt.getCoordinate().x / dx) - x1;
			
			if (x >= 0 && x <= mx && y >= 0 && y <= my) {
				SimpleFeature cell = matrix[x][y];
				final String id = Integer.toString(x) + ":" + Integer.toString(y);
				
				if (cell == null) {
					builder.add(null);
					builder.add(JTS.toGeometry(new Envelope((x1+x) * dx, (x1+x+1) * dx, (y1+y) * dy, (y1+y+1) * dy)));
					builder.add(feature.getAttribute("STATION_TYPE"));
					builder.add(1);
					boolean empty = true;
					
					for (int inx : attrInx) {
						Integer i = (Integer)feature.getAttribute(inx);
						empty &= (i == 0);
						builder.add( i );
					}
					builder.add((empty ? 1 : 0));
					cell = builder.buildFeature(id);
					
					this.matrix[x][y] = cell;
				}
				else {
					int cnt = (Integer)cell.getAttribute(CNT_INX);
					if (cnt == 1) {
						cell.setAttribute(STTY_INX, null);
					}
					cell.setAttribute(CNT_INX, ++cnt);
					
					boolean empty = true;
					for (int i = 0; i < attrInx.length; i++) {
						Integer val = (Integer)feature.getAttribute(attrInx[i]);
						if (val != 0) {
							empty = false;
							cell.setAttribute(ATT_INX + i, val + (Integer)cell.getAttribute(ATT_INX + i));
						}
					}
					
					if (empty) {
						cell.setAttribute(emyInx, 1 + (Integer)cell.getAttribute(emyInx));
					}
				}

				pointCollections.computeIfAbsent(cell.getID(), k -> new LinkedList<>()).add(pnt);
			}
		}
		
		Point getMatrixCenter(int x, int y)  {
			return (matrix[x][y] != null) ? (Point)matrix[x][y].getDefaultGeometry() : null;
		}
		
		/**
		 * create a MultiPoint of the points in the cell
		 */
		void computeCentralPoints() {
			for (int x = 0; x < this.matrix.length; x++) {
				for (int y = 0; y < this.matrix[x].length; y++) {
					SimpleFeature cell = this.matrix[x][y];
					if (cell != null) {
						List<Point> points = pointCollections.get(cell.getID());
						Point[] arr = points.toArray(new Point[points.size()]);
						MultiPoint mp = GF.createMultiPoint(arr);
						cell.setAttribute(CEN_INX, mp.getCentroid());
					}
				}
			}
		}

		@Override
		protected Iterator<SimpleFeature> openIterator() {
			return new Iterator<SimpleFeature>() {
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
					Point cellCenter = getMatrixCenter(x, y);
					if (cellCenter != null) {
						x1 = Math.min(x1, cellCenter.getX());
					}
				}
				x++;
			}
			
			if (x1 == Double.MAX_VALUE) {
				return new ReferencedEnvelope(this.schema.getCoordinateReferenceSystem());
			}
			
			x2 = Double.MIN_VALUE;
			x = this.matrix.length - 1;
			while (x2 == Double.MAX_VALUE) {
				for (int y = 0; y < this.matrix[x].length; y++) {
					Point cellCenter = getMatrixCenter(x, y);
					if (cellCenter != null) {
						x2 = Math.max(x2, cellCenter.getX());
					}
				}
				x++;
			}
			
			y1 = Double.MAX_VALUE;
			int y = this.matrix[0].length - 1;
			while (y1 == Double.MAX_VALUE) {
				for (int j = 0; j < this.matrix.length; j++) {
					Point cellCenter = getMatrixCenter(j, y);
					if (cellCenter != null) {
						y1 = Math.min( y1, cellCenter.getY());
					}
				}
			}
			
			y2 = Double.MIN_VALUE;
			y = 0;
			while (y2 == Double.MIN_VALUE) {
				for (int j = 0; j < this.matrix.length; j++) {
					Point cellCenter = getMatrixCenter(j, y);
					if (cellCenter != null) {
						y2 = Math.max( y2,  cellCenter.getY());
					}
				}
			}
			
			return new ReferencedEnvelope(x1, x2, y1, y2, this.schema.getCoordinateReferenceSystem());
		}
	}
}