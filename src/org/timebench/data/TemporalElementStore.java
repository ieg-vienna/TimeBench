package org.timebench.data;

import ieg.prefuse.data.ParentChildGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.timebench.calendar.Granularity;
import org.timebench.calendar.Granule;
import org.timebench.data.util.GranuleCache;
import org.timebench.data.util.IntervalIndex;
import org.timebench.util.lang.CustomIterable;

import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.expression.Predicate;
import prefuse.data.tuple.TableEdge;
import prefuse.data.tuple.TupleManager;
import prefuse.data.util.Index;
import prefuse.util.collections.CompositeIterator;
import prefuse.util.collections.IntIterator;

/**
 * This class maintains data structures that encompass a temporal dataset. It
 * consists of a {@link Graph} of temporal objects and a {@link Graph} of
 * temporal elements. Temporal elements are in a 1:n relation with temporal
 * objects to encompass temporal occurrence. Furthermore, the class provides
 * utility methods to index and query the dataset.
 * 
 * @author BA, AR, TL
 * 
 */
public class TemporalElementStore extends ParentChildGraph implements Lifespan, Cloneable {
    
    /**
     * ID of the first temporal element, if it is not externally set.
     */
    private static final long DEFAULT_FIRST_ID = 0l;
    
    /**
     * tuple manager for primitives (e.g., {@link Instance})
     */
    private TemporalElementManager temporalPrimitives;

    /**
     * tuple manager for {@link GenericTemporalElement}s)
     */
    private TemporalElementManager temporalGenerics;

    /**
     * index for {@link TemporalElement} row numbers by {@link TemporalElement#ID}. 
     */
    private Index indexElements;

    /**
     * interval index for anchored {@link TemporalElement}s.
     * Initialized on demand by {@link #intervalIndex()}.
     */
    private IntervalIndex indexElementIntervals = null;
    
    private List<TemporalData> temporalData = new LinkedList<TemporalData>();
    
    /**
     * Cache for first granules of temporal elements (Lazy initialization).
     */
    private GranuleCache granuleCache; 
    
    /**
     * Constructs an empty {@link TemporalElementStore}
     */
    public TemporalElementStore() {
        // temporal objects are by default in an directed graph
        super(new Table(), false);

        // define temporal element columns for nodes of the temporal e. graph
        this.getNodeTable().addColumns(this.getTemporalElementSchema());

        // add indices
        this.indexElements = this.getNodeTable().index(TemporalElement.ID);

        initTupleManagers();
    }

    /**
     * Warning: experimental -- know what you do!
     * 
     * @param temporalObjects
     * @param temporalObjectsEdges
     * @param temporalElements
     * @throws TemporalDataException
     */
    public TemporalElementStore(Table temporalElements, Table temporalElementEdges) throws TemporalDataException {
        super(temporalElements, temporalElementEdges, false);
        
        // TODO check temporal element schema 

        // add indices
        this.indexElements = this.getNodeTable().index(TemporalElement.ID);

        initTupleManagers();
    }

    /**
     * Set tuple managers for temporal elements, temporal primitives, and
     * temporal objects and use them in the underlying data structures.
     * 
     * <p>
     * This method is called from all constructors and will cause all existing
     * Tuples retrieved from this dataset to be invalidated.
     */
    private void initTupleManagers() {
        // nodes of temporal element graph --> GenericTemporalElement
        this.temporalGenerics = new TemporalElementManager(this, true);

        // additional tuple manager for temporal element graph --> temporal
        // primitives
        this.temporalPrimitives = new TemporalElementManager(this, false);
        this.temporalPrimitives.invalidateAutomatically();

        // default manager for edges in graphs
        TupleManager tempElementEdgeManager = new TupleManager(
                this.getEdgeTable(), this, TableEdge.class);

        // assign to temporal element graph
        super.initTupleManagers(temporalGenerics, tempElementEdgeManager);
    }

    @Deprecated
    public TemporalElementStore clone() {
        throw new UnsupportedOperationException("clone no longer needed");
    }
    
    // ----- TEMPORAL ELEMENT ACCESSORS -----

    /**
     * Gets the temporal elements in the dataset
     * 
     * @return a {@link Graph} containing the temporal elements and how they are
     *         related.
     */
    @Deprecated
    public ParentChildGraph getTemporalElements() {
        return this;
    }

    /**
     * Get the number of temporal elements in this dataset.
     * 
     * @return the number of temporal elements
     */
    public int getTemporalElementCount() {
        return this.getNodeCount();
    }

    /**
     * Get the TemporalElement instance corresponding to its row number.
     * 
     * @param row
     *            temporal element table row number
     * @return the TemporalElement instance corresponding to the row number
     */
    public GenericTemporalElement getTemporalElementByRow(int row) {
        return (GenericTemporalElement) this.getNode(row);
    }

    /**
     * Get the TemporalElement instance corresponding to its id, or
     * <tt>null</tt> if this dataset contains no element for the id.
     * 
     * @param id
     *            element id
     * @return the TemporalElement instance corresponding to the element id, or
     *         <tt>null</tt> if this dataset contains no element for the id.
     */
    public GenericTemporalElement getTemporalElement(long id) {
        int row = this.indexElements.get(id);
        return (row == Integer.MIN_VALUE) ? null : getTemporalElementByRow(row);
    }

    /**
     * Get the temporal primitive corresponding to its row number.
     * 
     * @param row
     *            temporal element table row number
     * @return the temporal primitive corresponding to the row number
     */
    public TemporalElement getTemporalPrimitiveByRow(int row) {
        return (TemporalElement) this.temporalPrimitives.getTuple(row);
    }

    /**
     * Get the temporal primitive corresponding to its id, or <tt>null</tt> if
     * this dataset contains no primitive for the id.
     * 
     * @param n
     *            element id
     * @return the temporal primitive corresponding to the element id, or
     *         <tt>null</tt> if this dataset contains no primitive for the id.
     */
    public TemporalElement getTemporalPrimitive(long id) {
        int row = this.indexElements.get(id);
        return (row == Integer.MIN_VALUE) ? null
                : getTemporalPrimitiveByRow(row);
    }

    /**
     * Get an iterator over all temporal elements in the temporal dataset.
     * 
     * @return an object, which provides an iterator over TemporalElement
     *         instances
     */
    @SuppressWarnings("unchecked")
    public Iterable<GenericTemporalElement> temporalElements() {
        return new CustomIterable(this.nodes());
    }

    /**
     * Get an iterator over {@link TemporalElement}s in the temporal dataset,
     * filtered by the given predicate.
     * 
     * @param filter
     *            predicate to apply to tuples in this set, only tuples for
     *            which the predicate evaluates to true are included in the
     *            iteration
     * @return an object, which provides an iterator over TemporalElement
     *         instances
     */
    @SuppressWarnings("unchecked")
    public Iterable<GenericTemporalElement> temporalElements(Predicate filter) {
        return new CustomIterable(this.getNodeTable()
                .tuples(filter));
    }

    /**
     * Get an iterator over all temporal primitives in the temporal dataset.
     * 
     * @return an object, which provides an iterator over TemporalElement
     *         instances
     */
    @SuppressWarnings("unchecked")
    public Iterable<TemporalElement> temporalPrimitives() {
        return new CustomIterable(temporalPrimitives.iterator(this
                .nodeRows()));
    }

    /**
     * Get an iterator over temporal primitives in the temporal dataset,
     * filtered by the given predicate.
     * 
     * @param filter
     *            predicate to apply to tuples in this set, only tuples for
     *            which the predicate evaluates to true are included in the
     *            iteration
     * @return an object, which provides an iterator over TemporalElement
     *         instances
     */
    @SuppressWarnings("unchecked")
    public Iterable<GenericTemporalElement> temporalPrimitives(Predicate filter) {
        IntIterator iit = this.getNodeTable().rows(filter);
        return new CustomIterable(temporalPrimitives.iterator(iit));
    }

    // ----- TEMPORAL OBJECT ACCESSORS -----

    // TODO need these to be public? -> not if TemporalTable is used
    protected void register(Table table, String field) {
        TemporalData entry = new TemporalData();
        entry.table = table;
        entry.index = table.index(field);
        this.temporalData.add(entry);
    }

    protected boolean unregister(Table table, String field) {
        Index index = table.getIndex(field);
        Iterator<TemporalData> i = temporalData.iterator();
        while (i.hasNext()) {
            TemporalData entry = i.next();
            if (entry.table == table && entry.index == index) {
                i.remove();
                return true;
            }
        }
        return false;
    }

	private static class TemporalData {
	    Table table;
	    Index index;
	}

    /**
     * Get an iterator over all {@link TemporalObject}s occurring with the given
     * temporal element.
     * 
     * @param temporalId
     *            temporal element id
     * @return an object, which provides an iterator over temporal objects
     *         occurring with the temporal element
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Iterable<Tuple> getTemporalObjectsByElementId(
            long temporalId) {
        
        ArrayList<Iterator> iis = new ArrayList<Iterator>();
        for (TemporalData data : temporalData) {
            // id + index -> rows iterator
            IntIterator rows = data.index.rows(temporalId);
            // rows + table -> tuple iterator
            Iterator ii = data.table.tuples(rows);
            if (ii.hasNext()) {
                // only consider if at least one tuple is present
                iis.add(ii);
            }
        }
        
        Iterator result;
        if (iis.size() < 1) {
            // handle element store without datasets or without objs. for el.
            result = Collections.emptyList().iterator();
        } else if (iis.size() == 1) {
            result = iis.get(0);
        } else {
            Iterator[] iiArray = new Iterator[iis.size()];
            iiArray = iis.toArray(iiArray);
            result = new CompositeIterator(iiArray);
        }
            
        return new CustomIterable(result);
    }

    /**
     * Create (if necessary) and return the {@link IntervalIndex} for
     * {@link TemporalElement}s. It helps in querying the elements based on
     * intervals. The first call to this method will cause the index to be
     * created and stored. Subsequent calls will simply return the stored index.
     * To attempt to retrieve an index without triggering creation of a new
     * index, use the {@link #getIntervalIndex()} method.
     * 
     * @return the interval index
     */
    public IntervalIndex intervalIndex() {
        if (indexElementIntervals != null) {
            return indexElementIntervals; // already indexed
        }
        
        // XXX no idea if this should be here or in TemporalDataset
//        IntervalComparator comparator = new DefaultIntervalComparator();
//        indexElementIntervals = new TemporalIndex(this, new AnchoredPredicate(), comparator);
        

        // TODO predicate to take advantage of interval index
        

        return indexElementIntervals;
    }

    /**
     * Retrieve, without creating, the interval index for
     * {@link TemporalElement}s.
     * 
     * @return the stored interval index , or null if no index has been created
     */
    public IntervalIndex getIntervalIndex() {
        return indexElementIntervals;
    }
    
    /**
     * Adds a new temporal element to the dataset but does not return a proxy
     * tuple.
     * 
     * @param inf
     *            the lower end of the temporal element
     * @param sup
     *            the upper end of the temporal element
     * @param granularityId
     *            the granularityID of the temporal element
     * @param granularityContextId
     *            the granularityContextID of the temporal element
     * @param kind
     *            the kind of the temporal element
     * @return the index of the created element in the table of temporal
     *         elements
     */
    private int addTemporalElementAsRow(long inf, long sup, int granularityId,
            int granularityContextId, int kind) {
        long id = (indexElements.size() > 0) ? this.getNodeTable()
                .getLong(this.indexElements.maximum(), TemporalElement.ID) + 1
                : DEFAULT_FIRST_ID;
        return addTemporalElementAsRow(id, inf, sup, granularityId,
                granularityContextId, kind);
    }

    /**
     * Adds a new temporal element to the dataset but does not return a proxy
     * tuple.
     * 
     * @param id
     *            the id of the temporal element
     * @param inf
     *            the lower end of the temporal element
     * @param sup
     *            the upper end of the temporal element
     * @param granularityId
     *            the granularityID of the temporal element
     * @param granularityContextId
     *            the granularityContextID of the temporal element
     * @param kind
     *            the kind of the temporal element
     * @return the index of the created element in the table of temporal
     *         elements
     */
    private int addTemporalElementAsRow(long id, long inf, long sup,
            int granularityId, int granularityContextId, int kind) {
        Table nodeTable = this.getNodeTable();
        int row = nodeTable.addRow();
        nodeTable.set(row, TemporalElement.ID, id);
        nodeTable.set(row, TemporalElement.INF, inf);
        nodeTable.set(row, TemporalElement.SUP, sup);
        nodeTable.set(row, TemporalElement.GRANULARITY_ID, granularityId);
        nodeTable.set(row, TemporalElement.GRANULARITY_CONTEXT_ID, granularityContextId);
        nodeTable.set(row, TemporalElement.KIND, kind);

        // only proxy tuple is GenericTemporalElement -> no need to invalidate

        return row;
    }

    /**
     * Adds a new temporal element to the dataset
     * 
     * @param inf
     *            the lower end of the temporal element
     * @param sup
     *            the upper end of the temporal element
     * @param granularityId
     *            the granularityID of the temporal element
     * @param granularityContextId
     *            the granularityContextID of the temporal element
     * @param kind
     *            the kind of the temporal element
     * @return the created temporal element
     */
    public GenericTemporalElement addTemporalElement(long inf, long sup,
            int granularityId, int granularityContextId, int kind) {
        int row = addTemporalElementAsRow(inf, sup, granularityId,
                granularityContextId, kind);
        return (GenericTemporalElement) this.temporalGenerics.getTuple(row);

    }

    /**
     * Adds a new temporal element to the dataset
     * 
     * @param id
     *            the id of the temporal element
     * @param inf
     *            the lower end of the temporal element
     * @param sup
     *            the upper end of the temporal element
     * @param granularityId
     *            the granularityID of the temporal element
     * @param granularityContextId
     *            the granularityContextID of the temporal element
     * @param kind
     *            the kind of the temporal element
     * @return the created temporal element
     */
    public GenericTemporalElement addTemporalElement(long id, long inf,
            long sup, int granularityId, int granularityContextId, int kind) {
        int row = addTemporalElementAsRow(id, inf, sup, granularityId,
                granularityContextId, kind);
        return (GenericTemporalElement) this.temporalGenerics.getTuple(row);

    }
    
    /**
     * Adds a batch of temporal elements to the temporal dataset. All will be of
     * the given kind.
     * 
     * @param nTuples
     *            the number of elements to add.
     * @param kind
     *            the kind of the temporal elements.
     * @return an array of proxy tuples for the new temporal elements.
     */
    public GenericTemporalElement[] addTemporalElements(int nTuples, int kind) {
        Table nodeTable = this.getNodeTable();

        long firstId = (indexElements.size() > 0) ? nodeTable.getLong(
                this.indexElements.maximum(), TemporalElement.ID) + 1
                : DEFAULT_FIRST_ID;

        int[] rows = nodeTable.addRows(nTuples);
        GenericTemporalElement[] elems = new GenericTemporalElement[nTuples];

        for (int i = 0; i < nTuples; i++) {
            nodeTable.set(rows[i], TemporalElement.ID, firstId + i);
            nodeTable.set(rows[i], TemporalElement.KIND, kind);
            elems[i] = (GenericTemporalElement) this.temporalGenerics
                    .getTuple(rows[i]);
        }

        return elems;
    }
    
    /**
     * Add a new instant to the dataset. This method returns a proxy tuple of
     * this instant, which is of class {@link Instant}.
     * 
     * @param inf
     *            the lower end of the temporal element
     * @param sup
     *            the upper end of the temporal element
     * @param granularityId
     *            the granularityID of the temporal element
     * @param granularityContextId
     *            the granularityContextID of the temporal element
     * @return a proxy tuple of the created temporal element
     */
    public Instant addInstant(long inf, long sup, int granularityId,
            int granularityContextId) {
        int row = this.addTemporalElementAsRow(inf, sup, granularityId,
                granularityContextId, TemporalElementStore.PRIMITIVE_INSTANT);
        Instant result = (Instant) this.temporalPrimitives.getTuple(row);
        return result;
    }
    
    public Instant addInstant(long inf, long sup, Granularity granularity) {
    	return addInstant(inf,sup,granularity.getIdentifier(),granularity.getGranularityContextIdentifier());
    }


    /**
     * Add a new instant to the dataset. This method returns a proxy tuple of
     * this instant, which is of class {@link Instant}.
     * 
     * @param id
     *            the id of the temporal element
     * @param inf
     *            the lower end of the temporal element
     * @param sup
     *            the upper end of the temporal element
     * @param granularityId
     *            the granularityID of the temporal element
     * @param granularityContextId
     *            the granularityContextID of the temporal element
     * @return a proxy tuple of the created temporal element
     */
    public Instant addInstant(long id, long inf, long sup, int granularityId,
            int granularityContextId) {
        int row = this.addTemporalElementAsRow(id, inf, sup, granularityId,
                granularityContextId, TemporalElementStore.PRIMITIVE_INSTANT);
        Instant result = (Instant) this.temporalPrimitives.getTuple(row);
        return result;
    }
    
    public Instant addInstant(long id, long inf, long sup, Granularity granularity) {
    	return addInstant(id,inf,sup,granularity.getIdentifier(),granularity.getGranularityContextIdentifier());
    }

    /**
     * Add a new instant to the dataset from a granule. This method returns a
     * proxy tuple of this instant, which is of class {@link Instant}. The
     * {@link Granule} is cached.
     * 
     * @param granule
     * @return a proxy tuple of the created temporal element
     * @throws TemporalDataException
     */
    public Instant addInstant(Granule granule) throws TemporalDataException {
        if (this.granuleCache == null) {
            granuleCache = new GranuleCache(this);
        }
        Instant instant = addInstant(granule.getInf(), granule.getSup(),
                granule.getGranularity().getIdentifier(), granule
                        .getGranularity().getGranularityContextIdentifier());
        granuleCache.addGranule(instant.getRow(), granule);
        return instant;
    }
    
    protected void set(Instant instant, Granule granule)
            throws TemporalDataException {
        GenericTemporalElement el = instant.asGeneric();
        el.setInf(granule.getInf());
        el.setSup(granule.getSup());
        el.setGranularityId(granule.getGranularity().getIdentifier());
        el.setGranularityId(granule.getGranularity()
                .getGranularityContextIdentifier());
        granuleCache.addGranule(instant.getRow(), granule);
    }
    
    public Span addSpan(long length, int granularityId) {
        int row = this.addTemporalElementAsRow(length, length, granularityId,
                -1, TemporalElementStore.PRIMITIVE_SPAN);
        Span result = (Span) this.temporalPrimitives.getTuple(row);
        return result;
    }

    public Interval addInterval(Instant begin, Instant end)
            throws TemporalDataException {

        // XXX make this more correct using Tim's classes (e.g., check &
        // handle different granularities)

        GenericTemporalElement interval = addTemporalElement(begin.getInf(),
                end.getSup(), begin.getGranularityId(),
                begin.getGranularityContextId(),
                TemporalElementStore.PRIMITIVE_INTERVAL);

        // add edges to temporal element graph
        interval.linkWithChild(begin);
        interval.linkWithChild(end);

        return (Interval) interval.asPrimitive();
    }
    
    public Interval addInterval(Instant begin, Span span)
            throws TemporalDataException {

        // TODO change granularities to solve more general cases
        if (span.getGranularityId() != begin.getGranularityId()) {
            throw new TemporalDataException(
                    "Begin and span need to be the same granularity");
        }

        Granule granule = begin.getGranule();
        Granularity granularity = granule.getGranularity();

        if (!granularity.isInTopContext()) {
            throw new TemporalDataException(
                    "Granularity must be in top context");
        }

        long endId = granule.getIdentifier() + span.getLength() - 1;
        granule = new Granule(endId, granularity, granularity.getCalendar().getTopGranule());
        long sup = granule.getSup();

        GenericTemporalElement interval = addTemporalElement(begin.getInf(),
                sup, begin.getGranularityId(),
                begin.getGranularityContextId(),
                TemporalElementStore.PRIMITIVE_INTERVAL);

        // add edges to temporal element graph
        interval.linkWithChild(begin);
        interval.linkWithChild(span);

        return (Interval) interval.asPrimitive();
    }
    
    public Interval addInterval(Span span, Instant end)
            throws TemporalDataException {
        throw new UnsupportedOperationException();
    }
    
	public AnchoredTemporalElement addIndeterminateInterval(Interval begin, Span maxLength, Span minLength, Interval end) {
        GenericTemporalElement interval = addTemporalElement(begin.getInf(),
                end.getSup(), begin.getGranularityId(),
                begin.getGranularityContextId(),
                TemporalElementStore.PRIMITIVE_SET);

        // add edges to temporal element graph
        interval.linkWithChild(begin);
        interval.linkWithChild(maxLength);
        interval.linkWithChild(minLength);
        interval.linkWithChild(end);

        return (AnchoredTemporalElement) interval.asPrimitive();
	}
    
    public AnchoredTemporalElement addAnchoredSet(TemporalElement... elements) throws TemporalDataException {
        TemporalElement anchor = null;
        long inf = Long.MAX_VALUE;
        long sup = Long.MIN_VALUE;

        for (TemporalElement el : elements) {
            if (el.isAnchored()) {
                anchor = el;
                inf = Math.min(inf, el.getLong(TemporalElement.INF));
                sup = Math.max(sup, el.getLong(TemporalElement.SUP));
            }
        }

        if (anchor == null) {
            throw new TemporalDataException(
                    "Anchored set needs at least one anchored child.");
        }

        GenericTemporalElement set = addTemporalElement(inf, sup,
                anchor.getGranularityId(), anchor.getGranularityContextId(),
                TemporalElementStore.PRIMITIVE_SET);

        // add edges to temporal element graph
        for (TemporalElement el : elements) {
            set.linkWithChild(el);
        }

        return (AnchoredTemporalElement) set.asPrimitive();
    }
    
    /**
     * Remove a TemporalElement from the TemporalDataset.
     * @param te the TemporalElement to remove from the TemporalDataset
     * @return true if the TemporalElement was successfully removed, false if the
     * TemporalElement was not found in this graph
     */   
    public boolean removeTemporalElement(TemporalElement te) {
    	return this.removeNode(te);
    }
    
    /**
     * Gets the first granule of an anchored temporal element. For an
     * {@link Instant}, the granule represents the time of the instant. If it is
     * unanchored, <tt>null</tt> is returned. Granules are cached.
     * 
     * @param row
     *            temporal element table row number
     * @return the first granule
     * @throws TemporalDataException
     * @see GranuleCache
     */
    protected Granule[] getGranulesByRow(int row) throws TemporalDataException {
        if (this.granuleCache == null) {
            granuleCache = new GranuleCache(this);
        }
        return granuleCache.getGranules(row);
    }
    
    /**
     * Get an instance of the default {@link Schema} used for
     * {@link TemporalElement} instances. Contains the data members internally
     * used to model a temporal element, i.e. inf, sup, granularity, granularity
     * context, and kind.
     * 
     * @return the TemporalElement data Schema
     */
    private Schema getTemporalElementSchema() {
        Schema s = new Schema();

        s.addColumn(TemporalElement.ID, long.class, -1);
        s.addColumn(TemporalElement.INF, long.class, Long.MIN_VALUE);
        s.addColumn(TemporalElement.SUP, long.class, Long.MAX_VALUE);
        s.addColumn(TemporalElement.GRANULARITY_ID, int.class, -1);
        s.addColumn(TemporalElement.GRANULARITY_CONTEXT_ID, int.class, -1);
        s.addColumn(TemporalElement.KIND, int.class, -1);

        return s;
    }

    // predefined kinds of temporal elements
    // TODO move to TemporalElement to be more visible
    public static final int PRIMITIVE_SPAN = 0;
    public static final int PRIMITIVE_SET = 1;
    public static final int PRIMITIVE_INSTANT = 2;
    public static final int PRIMITIVE_INTERVAL = 3;

    /**
     * creates a human-readable string from a {@link TemporalElementStore}.
     * <p>
     * Example:TemporalDataset [7 temporal elements, 9 temporal objects, 8
     * object relationships]
     * 
     * @return a string representation
     */
    @Override
    public String toString() {
        return "TemporalElementStore [" + this.getNodeCount()
                + " temporal elements, " + super.getEdgeCount()
                + " element relationships]";
    }

    @Override
    public long getInf() {
        // TODO ignore unanchored temporal elements (use IntervalIndex?)
        Table elem = this.getNodeTable(); 
        return elem.getLong(elem.getMetadata(TemporalElement.INF).getMinimumRow(), TemporalElement.INF);
    }

    @Override
    public long getSup() {
        // TODO ignore unanchored temporal elements (use IntervalIndex?)
        Table elem = this.getNodeTable(); 
        return elem.getLong(elem.getMetadata(TemporalElement.SUP).getMaximumRow(), TemporalElement.SUP);
    }

	/**
	 * @param temporalElement
	 * @return
	 */
	public TemporalElement addCloneOf(GenericTemporalElement temporalElement) {
		TemporalElement result = addTemporalElement(temporalElement.getInf(), temporalElement.getSup(),
				temporalElement.getGranularityId(),temporalElement.getGranularityContextId(),
				temporalElement.getKind());
		
		for(GenericTemporalElement iTemporalElement : temporalElement.childElements())
			result.linkWithChild(addCloneOf(iTemporalElement));
		
		return result;
	}
}
