package timeBench.data.util;

import java.util.Iterator;

import prefuse.util.collections.IntIterator;
import timeBench.data.TemporalObject;

// TODO pass comparator for each query not for building the tree

/**
 * Represents an index over two column of data, allowing quick lookups of
 * ranges that lie within a given data value.
 * 
 * @author bilal
 */
public interface IntervalIndex {

    /**
     * Get the comparator used to compare column data values.
     * @return the sort comparator
     */
    public IntervalComparator getComparator();
    
    /**
     * Get the row (or one of the rows) with the minimum data value.
     * @return a row with a minimum data value
     */
    public int minimum();
    
    /**
     * Get the row (or one of the rows) with the maximum data value.
     * @return a row with a maximum data value
     */
    public int maximum();
    
    /**
     * Get the size of this index, the number of data value / row
     * pairs included.
     * @return the size of the index
     */
    public int size();
    

    /**
     * Get an iterator over all rows whose ranges overlap with the given interval
     * @param lo the minimum data value
     * @param hi the maximum data value
     * @return an iterator over a range of rows
     */
    public IntIterator rows(long lo, long hi);
    
    /**
     * Get an iterator over all rows whose ranges contain the given data value.
     * @param val the data value
     * @return an iterator over all rows matching the data value
     */
    public IntIterator rows(long val);
    
	Iterator<TemporalObject> temporalObjects(long value);
	
	Iterator<TemporalObject> temporalObjects(long low, long high);

}
