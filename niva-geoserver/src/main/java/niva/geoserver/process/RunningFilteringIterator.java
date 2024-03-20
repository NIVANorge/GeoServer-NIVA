package niva.geoserver.process;

import java.util.NoSuchElementException;

import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.filter.BinaryComparisonAbstract;
import org.geotools.filter.ConstantExpression;
import org.geotools.filter.spatial.DWithinImpl;
import org.geotools.api.feature.simple.SimpleFeature;

/**
 * Iterator that takes one Collection and creates a new collection based on a filter at the original collection.
 * The filter is running in the sense that it switches focus as it passes along the original collection.
 * 
 * The new features can be a aggregate of the original features at each step.
 * 
 * @author Roar Brænden
 *
 */
public class RunningFilteringIterator implements SimpleFeatureIterator {

	private final SimpleFeatureIterator delegate;
	private final BinaryComparisonAbstract filter;
	private final AggregateFeatures aggregator;
	   
	private SimpleFeature nextTop = null;

	private SimpleFeature next = null;
	
	
	public RunningFilteringIterator(SimpleFeatureIterator delegate, BinaryComparisonAbstract filter, AggregateFeatures aggregator) {
		this.delegate = delegate;
		this.filter = filter;
		
		this.aggregator = aggregator;
	}
	
	public boolean hasNext() {
		// Successive calls to hasNext before a call to next
		
		if (next == null && delegate.hasNext()) {
			
			if (nextTop == null) {
				SimpleFeature top = delegate.next();
				setFocusFeature(top);
				aggregator.add(top);
			}
			else {
				setFocusFeature(nextTop);
				aggregator.add(nextTop);
				nextTop = null;
			}
			while (delegate.hasNext()) {
				SimpleFeature act = delegate.next();
				if (filter.evaluate(act)) {
					aggregator.add(act);
				}
				else {
					nextTop = act;
					break;
				}
			}
			
			next = aggregator.aggregate();
			aggregator.clear();
		}
		
		return (next != null);
	}
	
	private void setFocusFeature(SimpleFeature feature) {
		if (filter instanceof DWithinImpl)
			filter.setExpression2(ConstantExpression.constant(feature.getDefaultGeometry()));
		else
			filter.setExpression2(ConstantExpression.constant(feature));
	}

	public SimpleFeature next() {
		if (next == null) {
			throw new NoSuchElementException();
		}
		else {
			SimpleFeature ret = next;
			next = null;
			return ret;
		}
	}
	
	public SimpleFeatureIterator getDelegate() {
		return delegate;
	}
	

	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() {
		delegate.close();
	}
}
