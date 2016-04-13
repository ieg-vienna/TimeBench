package org.timebench.data;

import ieg.prefuse.data.ParentChildGraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.timebench.calendar.Granularity;
import org.timebench.calendar.Granule;
import org.timebench.data.expression.AnchoredPredicate;
import org.timebench.data.util.DefaultIntervalComparator;
import org.timebench.data.util.IntervalComparator;
import org.timebench.data.util.IntervalIndex;
import org.timebench.data.util.TemporalIndex;
import org.timebench.util.lang.CustomIterable;

import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.expression.Predicate;
import prefuse.data.util.Index;
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
public class TemporalDataset extends ParentChildGraph implements Lifespan, Cloneable {
    
    /**
     * ID of the first temporal object, if it is not externally set.
     */
    private static final long DEFAULT_FIRST_ID = 0l;
    
    /**
     * store of temporal elements
     */
    private TemporalElementStore temporalElements;

    // TODO roots as a linked list?
    private Set<TemporalObject> roots = new HashSet<TemporalObject>(); 
    private int depth = 1; // 1-> table, otherwise depth of forest

    /**
     * here, derived classes can add their own non-data columns
     */
    protected String[] additionalNonDataColums = new String[0];
    
    public String[] getAdditionalNonDataColums() {
		return additionalNonDataColums;
	}

	public void setAdditionalNonDataColums(String[] additionalNonDataColums) {
		this.additionalNonDataColums = additionalNonDataColums;
	}

	/**
     * index for {@link TemporalObject} row numbers by {@link TemporalObject#ID}. 
     */
    private Index indexObjects;

    /**
     * index for {@link TemporalObject} row numbers by {@link TemporalElement#ID}. 
     */
    private Index indexObjectsByElements;
    
    /**
     * interval index for anchored {@link TemporalElement}s.
     * Initialized on demand by {@link #intervalIndex()}.
     */
    private IntervalIndex indexElementIntervals = null;
    
    /**
     * Constructs an empty {@link TemporalDataset}
     */
    public TemporalDataset() {
        this(new TemporalElementStore());
    }

    /**
     * Constructs an empty {@link TemporalDataset} using the given
     * {@link TemporalElementStore}.
     * 
     * @param temporalElements
     *            data structure to store temporal elements.
     */
    public TemporalDataset(TemporalElementStore temporalElements) {
        // nodes of temporal object graph --> TemporalObject
        super(new TemporalTable(), TemporalObject.class);

        // add temporal objects columns (primary and foreign key)
        TemporalTable temporalObjects = (TemporalTable) super.getNodeTable();

        // WARNING: The methods getDataColumnIndices() assumes that these
        //          two columns have indices 0 -- 2
        temporalObjects.addColumn(TemporalObject.ID, long.class, -1);

        // super.getNodeTable().addColumn(TemporalObject.TEMPORAL_ELEMENT_ID,
        // new TemporalColumn(temporalElements));
        // super.getNodeTable().addColumn(TemporalObject.TEMPORAL_ELEMENT_ID,
        // long.class, -1);
        // temporalElements.register(this.getNodeTable(),
        // TemporalObject.TEMPORAL_ELEMENT_ID);
        temporalObjects.addTemporalColumn(TemporalObject.TEMPORAL_ELEMENT,
                temporalElements);
        // registration at store & index of id column -> TemporalTable

        // TODO warning TEMPORAL_ELEMENT_ID vs. TEMPORAL_ELEMENT

        init(temporalObjects, temporalElements);
    }

    /**
     * Constructs an empty {@link TemporalDataset} with the given schema for
     * data elements.
     * 
     * @param dataColumns
     *            schema for data elements
     * @throws TemporalDataException
     *             if a reserved column name was passed
     */
    public TemporalDataset(Schema dataColumns) throws TemporalDataException {
        this();

        // for loop mimics behavior of table.addColumns(schema)
        for (int i = 0; i < dataColumns.getColumnCount(); ++i) {
            this.addDataColumn(dataColumns.getColumnName(i),
                    dataColumns.getColumnType(i), dataColumns.getDefault(i));
        }
    }
    
    /**
     * Warning: experimental -- know what you do!
     * 
     * @param temporalObjects
     *            the table with temporal objects. A {@link Table} with the
     *            columns {@link TemporalObject#ID} and
     *            {@link TemporalObject#TEMPORAL_ELEMENT_ID} already present.
     * @param temporalObjectsEdges
     * @param temporalElements
     * @throws TemporalDataException
     */
    public TemporalDataset(Table temporalObjects, Table temporalObjectsEdges, TemporalElementStore temporalElements) throws TemporalDataException {
        super(temporalObjects, temporalObjectsEdges, TemporalObject.class);
        
        // TODO check temporal objects columns (primary and foreign key) -- exception in constructor?
        // WARNING: The methods getDataColumnIndices() assumes that these
        //          two columns have indices 0 -- 2

        // TODO derived TemporalObjects currently not available by element.temporalObject() 
//        temporalElements.register(this.getNodeTable(),
//                TemporalObject.TEMPORAL_ELEMENT_ID);

        init(temporalObjects, temporalElements);
    }

    /**
     * common initialization of a TemporalDataset.
     * 
     * @param temporalObjects
     * @param temporalElements
     */
    private void init(Table temporalObjects,
            TemporalElementStore temporalElements) {

        this.temporalElements = temporalElements;

        // add indices
        this.indexObjects = temporalObjects.index(TemporalObject.ID);
        this.indexObjectsByElements = temporalObjects
                .index(TemporalObject.TEMPORAL_ELEMENT_ID);
    }

    /**
     * Add a data column with the given name and data type to the temporal
     * objects.
     * 
     * @param name
     *            the data field name for the column
     * @param type
     *            the data type, as a Java Class, for the column
     * @param defaultValue
     *            the default value for column data values or <tt>null</tt>
     * @throws TemporalDataException
     *             if a reserved column name was passed
     * @see prefuse.data.tuple.TupleSet#addColumn(java.lang.String,
     *      java.lang.Class, java.lang.Object)
     */
    public void addDataColumn(String name,
            @SuppressWarnings("rawtypes") Class type, Object defaultValue)
            throws TemporalDataException {
        // check that schema does not interfere with primary and foreign key
        // schema.getColumnIndex(s) would build a HashMap --> less efficient
        if (name == null || name.startsWith("_")) {
            throw new TemporalDataException("The column names starting with "
                    + "\"_\" are reserved.");
        }

        super.getNodeTable().addColumn(name, type, defaultValue);
    }
    
    /**
     * Get schema of the data columns in this temporal dataset.
     * The schema can be used to create a temporal dataset with the  
     * 
     * @return schema of the data columns
     */
    public Schema getDataColumnSchema() {
        Schema dataColumns = new Schema();
        int[] cols = this.getDataColumnIndices();
        Table table = this.getNodeTable();
        for (int i = 0; i < cols.length; i++) {
            dataColumns.addColumn(table.getColumnName(cols[i]), 
                    table.getColumnType(cols[i]), 
                    table.getColumn(cols[i]).getDefaultValue());
        }
        return dataColumns;
    }
    
    /**
     * Get column indices for application-specific data.
     * 
     * @return array of column indices.
     */
    public int[] getDataColumnIndices() {
        // WARNING: The methods assumes that the non-data columns have indices 0 -- 2 
        final int TEMPORAL_OBJECT_NONDATA_COLUMS = 3;
        int[] cols;

        if (additionalNonDataColums.length == 0) {        
        	cols = new int[super.getNodeTable().getColumnCount()
        	                     - TEMPORAL_OBJECT_NONDATA_COLUMS];
        	for (int i = 0; i < cols.length; i++) {
        		cols[i] = i + TEMPORAL_OBJECT_NONDATA_COLUMS;
        	}
        } else {
        	ArrayList<Integer> removedlist = new ArrayList<Integer>();                	
        	for (int i = TEMPORAL_OBJECT_NONDATA_COLUMS; i < getNodeTable().getColumnCount(); i++) {
        		boolean removed = false;
        		for(String iAdditionalNonDataColumn : additionalNonDataColums) {
        			if(getNodeTable().getColumnName(i) == iAdditionalNonDataColumn) {
        				removedlist.add(i);
        				break;
        			}
        		}
        		if (removed)
        			break;
        		if (getNodeTable().getColumnName(i).endsWith(".kind")) {
    				removedlist.add(i);
    				break;        			
        		}
    		}
    		cols = new int[super.getNodeTable().getColumnCount() - TEMPORAL_OBJECT_NONDATA_COLUMS - removedlist.size()];
    		int j=0;
        	for (int i = TEMPORAL_OBJECT_NONDATA_COLUMS; i < getNodeTable().getColumnCount(); i++) {
        		if(!removedlist.contains(i))
        			cols[j++] = i;
        	}
        }
        
        return cols;
    }
    
    @Deprecated
    public TemporalDataset clone() {
        throw new UnsupportedOperationException("clone no longer needed");
    }
    
    // ----- ROOT ACCESSORS -----

    public int getRootCount() {
        return roots.size();
    }

    /**
     * Gets the roots if the TemporalObjects form a forest, the root, if they
     * form a tree, or null for tables
     * 
     * @return the roots as an iterable collection
     */
    public Iterable<TemporalObject> roots() {
        return this.roots;
    }

    public boolean isRoot(TemporalObject obj) {
        return obj.getTemporalDataset().roots.contains(obj);
    }

    public void setRoot(TemporalObject obj, boolean root) {
        if (root) {
            obj.getTemporalDataset().roots.add(obj);
        } else {
            obj.getTemporalDataset().roots.remove(obj);
        }
    }

    @Deprecated
    public long[] getRoots() {
        long[] r = new long[getRootCount()];
        int i=0;
        for (TemporalObject obj : roots) {
            r[i++] = obj.getId();
        }
        return r;
    }

    public int getDepth() {
        return depth;
    }
    
    public void setDepth(int depth) {
        this.depth = depth;
    }

    // ----- TEMPORAL ELEMENT ACCESSORS -----

    /**
     * Gets the temporal elements in the dataset
     * 
     * @return a {@link Graph} containing the temporal elements and how they are
     *         related.
     */
    public TemporalElementStore getTemporalElements() {
        return temporalElements;
    }

    /**
     * Get the number of temporal elements in this dataset.
     * 
     * @return the number of temporal elements
     */
    @Deprecated
    public int getTemporalElementCount() {
        return temporalElements.getTemporalElementCount();
    }

    /**
     * Get the TemporalElement instance corresponding to its row number.
     * 
     * @param row
     *            temporal element table row number
     * @return the TemporalElement instance corresponding to the row number
     */
    @Deprecated
    public GenericTemporalElement getTemporalElementByRow(int row) {
        return temporalElements.getTemporalElementByRow(row);
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
        return temporalElements.getTemporalElement(id);
    }

    /**
     * Get the temporal primitive corresponding to its row number.
     * 
     * @param row
     *            temporal element table row number
     * @return the temporal primitive corresponding to the row number
     */
    @Deprecated
    public TemporalElement getTemporalPrimitiveByRow(int row) {
        return temporalElements.getTemporalPrimitiveByRow(row);
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
        return temporalElements.getTemporalPrimitive(id);
    }

    /**
     * Get an iterator over all temporal elements in the temporal dataset.
     * 
     * @return an object, which provides an iterator over TemporalElement
     *         instances
     */
    public Iterable<GenericTemporalElement> temporalElements() {
        return temporalElements.temporalElements();
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
    public Iterable<GenericTemporalElement> temporalElements(Predicate filter) {
        return temporalElements.temporalElements(filter);
    }

    /**
     * Get an iterator over all temporal primitives in the temporal dataset.
     * 
     * @return an object, which provides an iterator over TemporalElement
     *         instances
     */
    public Iterable<TemporalElement> temporalPrimitives() {
        return temporalElements.temporalPrimitives();
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
    public Iterable<GenericTemporalElement> temporalPrimitives(Predicate filter) {
        return temporalElements.temporalPrimitives(filter);
    }

    // ----- TEMPORAL OBJECT ACCESSORS -----

    /**
     * Get the number of temporal objects in this dataset.
     * 
     * @return the number of temporal objects
     */
    public int getTemporalObjectCount() {
        return super.getNodeCount();
    }

    /**
     * Get an iterator over all {@link TemporalObject}s in the temporal dataset.
     * 
     * @return an object, which provides an iterator over TemporalObject
     *         instances
     */
    @SuppressWarnings("unchecked")
    public Iterable<TemporalObject> temporalObjects() {
        return new CustomIterable(super.nodes());
    }

    /**
     * Get an iterator over {@link TemporalObject}s in the temporal dataset,
     * filtered by the given predicate.
     * 
     * @param filter
     *            predicate to apply to tuples in this set, only tuples for
     *            which the predicate evaluates to true are included in the
     *            iteration
     * @return an object, which provides an iterator over TemporalObject
     *         instances
     */
    @SuppressWarnings("unchecked")
    public Iterable<TemporalObject> temporalObjects(Predicate filter) {
        return new CustomIterable(super.getNodeTable().tuples(filter));
    }

    /**
     * Get the {@link TemporalObject} instance corresponding to its id, or
     * <tt>null</tt> if this dataset contains no object for the id.
     * 
     * @param id
     *            object id
     * @return the TemporalObject instance corresponding to the object id, or
     *         <tt>null</tt> if this dataset contains no object for the id.
     */
    public TemporalObject getTemporalObject(long id) {
        int row = this.indexObjects.get(id);
        return (row == Integer.MIN_VALUE) ? null : 
            (TemporalObject) super.getNode(row);
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
    @SuppressWarnings("unchecked")
    public Iterable<TemporalObject> getTemporalObjectsByElementId(
            long temporalId) {
        return new CustomIterable(getTemporalObjectsByElementIdIterator(temporalId));
    }

    @SuppressWarnings("unchecked")
    protected Iterator<TemporalObject> getTemporalObjectsByElementIdIterator(
            long temporalId) {
        IntIterator rows = this.indexObjectsByElements.rows(temporalId);
        return super.getNodeTable().tuples(rows);
    }
    /**
     * Gets all (temporal) occurrences of data elements
     * 
     * @return a {@link Table} containing all temporal occurrences
     * 
     *         TL 2011-11-07: not deprecated because needed to generate
     *         VisualTable
     *         AR 2012-04-03: renamed to getTemporalObjectTable()
     */
    public Table getTemporalObjectTable() {
        return super.getNodeTable();
    }

    /**
     * Adds a temporal object to the temporal dataset.
     * 
     * @param temporalObjectId
     *            the id of the temporal object
     * @param temporalElementId
     *            the id of the temporal element
     * @return the proxy tuple of the new temporal object.
     */
    public TemporalObject addTemporalObject(long temporalObjectId,
            long temporalElementId) {
//        if (this.indexObjects.get(temporalObjectId) != Integer.MIN_VALUE)
//            throw new TemporalDataException("Duplicate temporal object id");
//        if (this.indexElements.get(temporalElementId) == Integer.MIN_VALUE)
//            throw new TemporalDataException(
//                    "Temporal element id does not exist");

        TemporalObject object = (TemporalObject) super.addNode();
        object.set(TemporalObject.ID, temporalObjectId);
        object.set(TemporalObject.TEMPORAL_ELEMENT_ID, temporalElementId);
        return object;
    }

    /**
     * Adds a temporal object to the temporal dataset.
     * 
     * @param temporalElementId
     *            the id of the temporal element
     * @return the proxy tuple of the new temporal object.
     */
    public TemporalObject addTemporalObject(long temporalElementId) {
        long id = (indexObjects.size() > 0) ? super.getNodeTable().getLong(
                this.indexObjects.maximum(), TemporalObject.ID) + 1
                : DEFAULT_FIRST_ID;

        return addTemporalObject(id, temporalElementId);
    }

    /**
     * Adds a temporal object to the temporal dataset.
     * 
     * @param temporalElement
     *            the temporal element
     * @return the proxy tuple of the new temporal object.
     */
    public TemporalObject addTemporalObject(TemporalElement temporalElement) {
        return addTemporalObject(temporalElement.getId());
    }

    /**
     * Adds a batch of temporal objects to the temporal dataset. For each
     * temporal element a new temporal object will be created.
     * 
     * @param elements
     *            an array of temporal elements.
     * @return an array of proxy tuples for the new temporal objects.
     */
    public TemporalObject[] addTemporalObjects(TemporalElement[] elements) {
        long firstId = (indexObjects.size() > 0) ? super.getNodeTable()
                .getLong(this.indexObjects.maximum(), TemporalObject.ID) + 1
                : DEFAULT_FIRST_ID;

        int[] rows = super.getNodeTable().addRows(elements.length);
        TemporalObject[] objs = new TemporalObject[elements.length];

        for (int i = 0; i < elements.length; i++) {
            objs[i] = (TemporalObject) super.getNode(rows[i]);
            objs[i].set(TemporalObject.ID, firstId + i);
            objs[i].set(TemporalObject.TEMPORAL_ELEMENT_ID, elements[i].getId());
        }

        return objs;
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
        IntervalComparator comparator = new DefaultIntervalComparator();
        indexElementIntervals = new TemporalIndex(this, new AnchoredPredicate(), comparator);
        

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
        return temporalElements.addTemporalElement(inf, sup, granularityId, 
                granularityContextId, kind);
    }
    
    public GenericTemporalElement addTemporalElement(long inf, long sup,
    		Granularity granularity,int kind) {
    	return addTemporalElement(inf, sup, granularity.getIdentifier(), granularity.getGranularityContextIdentifier(), kind);
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
        return temporalElements.addTemporalElement(id, inf, sup, granularityId, 
                granularityContextId, kind);
    }
    
    public GenericTemporalElement addTemporalElement(long id, long inf,
            long sup, Granularity granularity, int kind) {
        return addTemporalElement(id, inf, sup, granularity.getIdentifier(), 
                granularity.getGranularityContextIdentifier(), kind);
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
        return temporalElements.addTemporalElements(nTuples, kind);
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
        return temporalElements.addInstant(inf, sup, granularityId, 
                granularityContextId);
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
        return temporalElements.addInstant(id, inf, sup, granularityId, 
                granularityContextId);
    }
    
    public Instant addInstant(long inf,long sup,Granularity granularity) {
    	return addInstant(inf,sup,granularity.getIdentifier(),granularity.getGranularityContextIdentifier());
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
        return temporalElements.addInstant(granule);
    }
    
    public Span addSpan(long length, int granularityId) {
        return temporalElements.addSpan(length, granularityId);
    }

    public Interval addInterval(Instant begin, Instant end)
            throws TemporalDataException {
        return temporalElements.addInterval(begin, end);
    }
    
    public Interval addInterval(Instant begin, Span span)
            throws TemporalDataException {
        return temporalElements.addInterval(begin, span);
    }
    
    public Interval addInterval(Span span, Instant end)
            throws TemporalDataException {
        return temporalElements.addInterval(span, end);
    }
    
	public AnchoredTemporalElement addIndeterminateInterval(Interval begin, Span maxLength, Span minLength, Interval end) {
		return temporalElements.addIndeterminateInterval(begin, maxLength, minLength, end);
	}
    
    public AnchoredTemporalElement addAnchoredSet(TemporalElement... elements) throws TemporalDataException {
        return temporalElements.addAnchoredSet(elements);
    }
    
    /**
     * Remove a TemporalElement from the TemporalDataset.
     * @param te the TemporalElement to remove from the TemporalDataset
     * @return true if the TemporalElement was successfully removed, false if the
     * TemporalElement was not found in this graph
     */   
    public boolean removeTemporalElement(TemporalElement te) {
    	return temporalElements.removeTemporalElement(te);
    }
    
    /**
     * creates a human-readable string from a {@link TemporalDataset}.
     * <p>
     * Example:TemporalDataset [7 temporal elements, 9 temporal objects, 8
     * object relationships]
     * 
     * @return a string representation
     */
    @Override
    public String toString() {
        return "TemporalDataset [" + super.getNodeCount()
                + " temporal objects, " + super.getEdgeCount()
                + " object relationships, "+ temporalElements.toString() + "]";
    }

    @Override
    public long getInf() {
        // TODO use IntervalIndex instead (?)
        IntIterator elIterator = temporalElements.getNodeTable()
                .index(TemporalElement.INF).allRows(Index.TYPE_ASCENDING);
        while (elIterator.hasNext()) {
            TemporalElement el = temporalElements
                    .getTemporalPrimitiveByRow(elIterator.nextInt());
            if (el.isAnchored()) {
                return el.getLong(TemporalElement.INF);
            }
        }
        // no anchored elements in dataset
        return Long.MIN_VALUE;
    }

    @Override
    public long getSup() {
        // TODO use IntervalIndex instead (?)
        IntIterator elIterator = temporalElements.getNodeTable()
                .index(TemporalElement.SUP).allRows(Index.TYPE_DESCENDING);
        while (elIterator.hasNext()) {
            TemporalElement el = temporalElements
                    .getTemporalPrimitiveByRow(elIterator.nextInt());
            if (el.isAnchored()) {
                return el.getLong(TemporalElement.SUP);
            }
        }
        // no anchored elements in dataset
        return Long.MAX_VALUE;
    }

	public TemporalObject addCloneOf(TemporalObject source) {					
		TemporalObject result = addTemporalObject(temporalElements.addCloneOf(source.getTemporalElement().asGeneric()));
		for(int i : getDataColumnIndices()) {
			result.set(i, source.get(i));
		}
		
		return result;
	}
}
