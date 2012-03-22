package timeBench.data.relational;

import java.util.Iterator;

import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.column.Column;
import prefuse.data.expression.AbstractPredicate;
import prefuse.data.tuple.TableEdge;
import prefuse.data.tuple.TupleManager;
import prefuse.data.util.Index;
import prefuse.util.collections.IntIterator;
import timeBench.calendar.Granule;
import timeBench.data.Lifespan;
import timeBench.data.TemporalDataException;
import timeBench.data.util.IntervalComparator;
import timeBench.data.util.IntervalIndex;
import timeBench.data.util.IntervalTreeIndex;

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
public class TemporalDataset extends Graph implements Lifespan, Cloneable {

    /**
     * {@link Graph} of temporal elements
     */
    private Graph temporalElements;

    // TODO roots as a linked list?
    private long[] roots = null; // if we have a forest or tree of temporal
                                 // objects, null for tables

    /**
     * tuple manager for primitives (e.g., {@link Instance})
     */
    private TemporalElementManager temporalPrimitives;
    /**
     * tuple manager for {@link GenericTemporalElement}s)
     */
    private TemporalElementManager temporalGenerics;

    /**
     * index for {@link TemporalObject} row numbers by ID. 
     */
    private Index indexObjects;

    /**
     * index for {@link TemporalElement} row numbers by ID. 
     */
    private Index indexElements;

    /**
     * index for {@link TemporalObject} row numbers by {@link TemporalElement} ID. 
     */
    private Index indexObjectsByElements;
    
    private Schema dataColumns;
    
    // /**
    // * largest id assigned to an temporal element in this dataset
    // */
    // private ieg.prefuse.data.ExtremeValueListener maximumTemporalElementId
    // = new ieg.prefuse.data.ExtremeValueListener();

    // predefined column names for temporal objects
    // TODO move constants to TemporalObject?
    public static final String TEMPORAL_OBJECT_ID = "id";

    public static final String TEMPORAL_OBJECT_TEMPORAL_ID = "temporal_id";

    // predefined column names for temporal elements
    public static final String TEMPORAL_ELEMENT_ID = "id";

    // TODO move constants to TemporalElement or add a prefix?
    public static final String INF = "inf";

    public static final String SUP = "sup";

    public static final String GRANULARITY_ID = "granularityID";

    public static final String GRANULARITY_CONTEXT_ID = "granularityContextID";

    public static final String KIND = "kind";

    /**
     * Constructs an empty {@link TemporalDataset}
     */
    public TemporalDataset() {
        // temporal objects are by default in an directed graph
        super(true);

        this.temporalElements = new Graph(true);
        // define temporal element columns for nodes of the temporal e. graph
        this.temporalElements.getNodeTable().addColumns(
                this.getTemporalElementSchema());

        // add temporal objects columns (primary and foreign key)
        super.addColumn(TEMPORAL_OBJECT_ID, long.class, -1);
        super.addColumn(TEMPORAL_OBJECT_TEMPORAL_ID, long.class, -1);
        
        this.dataColumns = new Schema();

        // add indices
        this.indexObjects = super.getNodeTable().index(TEMPORAL_OBJECT_ID);
        this.indexObjectsByElements = super.getNodeTable().index(
                TEMPORAL_OBJECT_TEMPORAL_ID);
        this.indexElements = this.temporalElements.getNodeTable().index(
                TEMPORAL_ELEMENT_ID);

        // this.temporalElements.getNodeTable().getColumn(TEMPORAL_ELEMENT_ID)
        // .addColumnListener(maximumTemporalElementId);

        initTupleManagers();
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
     * Add a data column with the given name and data type to the temporal
     * objects.
     * 
     * @param name
     *            the data field name for the column
     * @param type
     *            the data type, as a Java Class, for the column
     * @param defaultValue
     *            the default value for column data values
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
        if (name.equals(TEMPORAL_OBJECT_ID)
                || name.equals(TEMPORAL_OBJECT_TEMPORAL_ID)) {
            throw new TemporalDataException("The column names "
                    + TEMPORAL_OBJECT_ID + " and "
                    + TEMPORAL_OBJECT_TEMPORAL_ID + " are reserved.");
        }

        super.getNodeTable().addColumn(name, type, defaultValue);
        
        this.dataColumns.addColumn(name, type, defaultValue);
    }
    
    public Schema getDataColumnSchema() {
        return (Schema) dataColumns.clone();
    }
    
    public int getDataColumnCount() {
        return dataColumns.getColumnCount();
    }
    
    public Column getDataColumn(int index) {
        String fieldName = dataColumns.getColumnName(index);
        return super.getNodeTable().getColumn(fieldName);
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
        // nodes of temporal object graph --> TemporalObject
        TupleManager tempObjectManager = new TupleManager(
                super.getNodeTable(), this, TemporalObject.class);

        // nodes of temporal element graph --> GenericTemporalElement
        this.temporalGenerics = new TemporalElementManager(this, true);

        // additional tuple manager for temporal element graph --> temporal
        // primitives
        this.temporalPrimitives = new TemporalElementManager(this, false);
        this.temporalPrimitives.invalidateAutomatically();

        // dummy manager for edges in both graphs
        TupleManager tempObjectEdgeManager = new TupleManager(
                this.getEdgeTable(), this, TableEdge.class);
        TupleManager tempElementEdgeManager = new TupleManager(
                temporalElements.getEdgeTable(), temporalElements,
                TableEdge.class);

        // assign to temporal object graph
        super.setTupleManagers(tempObjectManager, tempObjectEdgeManager);
        super.getNodeTable().setTupleManager(tempObjectManager);
        super.getEdgeTable().setTupleManager(tempObjectEdgeManager);

        // assign to temporal element graph
        temporalElements.setTupleManagers(temporalGenerics,
                tempElementEdgeManager);
        temporalElements.getNodeTable().setTupleManager(temporalGenerics);
        temporalElements.getEdgeTable().setTupleManager(tempElementEdgeManager);
    }

    @Deprecated
    public Object clone() {
        throw new UnsupportedOperationException("clone no longer needed");
    }

    /**
     * Gets the roots if the TemporalObjects form a wood, the root, if they form
     * a tree, or null for tables
     * 
     * @return the roots
     */
    public long[] getRoots() {
        return roots;
    }

    /**
     * Sets the roots: If the TemporalObjects form a wood use the roots, if they
     * form a tree, use the root, for tables set null
     * 
     * @param roots
     *            the roots to set
     */
    public void setRoots(long[] roots) {
        this.roots = roots;
    }

    /**
     * Gets the data elements in the dataset
     * 
     * @return a {@link Table} containing the data elements
     */
    @Deprecated
    public Table getDataElements() {
        return super.getNodeTable();
    }

    // ----- TEMPORAL ELEMENT ACCESSORS -----

    /**
     * Gets the temporal elements in the dataset
     * 
     * @return a {@link Graph} containing the temporal elements and how they are
     *         related.
     */
    public Graph getTemporalElements() {
        return temporalElements;
    }

    /**
     * Get the number of temporal elements in this dataset.
     * 
     * @return the number of temporal elements
     */
    public int getTemporalElementCount() {
        return temporalElements.getNodeCount();
    }

    /**
     * Get the TemporalElement instance corresponding to its row number.
     * 
     * @param row
     *            temporal element table row number
     * @return the TemporalElement instance corresponding to the row number
     */
    public GenericTemporalElement getTemporalElementByRow(int row) {
        return (GenericTemporalElement) temporalElements.getNode(row);
    }

    /**
     * Get the TemporalElement instance corresponding to its id.
     * 
     * @param id
     *            element id
     * @return the TemporalElement instance corresponding to the element id
     */
    public GenericTemporalElement getTemporalElement(long id) {
        return getTemporalElementByRow(this.indexElements.get(id));
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
     * Get the temporal primitive corresponding to its id.
     * 
     * @param n
     *            element id
     * @return the temporal primitive corresponding to the element id
     */
    public TemporalElement getTemporalPrimitive(long id) {
        return getTemporalPrimitiveByRow(this.indexElements.get(id));
    }

    /**
     * Get an iterator over all temporal elements in the temporal dataset.
     * 
     * @return an iterator over TemporalElement instances
     */
    @SuppressWarnings("unchecked")
    public Iterator<GenericTemporalElement> temporalElements() {
        return temporalElements.nodes();
    }

    /**
     * allows iteration over all temporal elements.
     * 
     * @return an object, which provides an iterator
     */
    @Deprecated
    public Iterable<TemporalElement> temporalElementsIterable() {
        return new Iterable<TemporalElement>() {
            @SuppressWarnings("unchecked")
            @Override
            public Iterator<TemporalElement> iterator() {
                return temporalElements.nodes();
            }
        };
    }

    /**
     * Get an iterator over all temporal primitives in the temporal dataset.
     * 
     * @return an iterator over TemporalElement instances
     */
    @SuppressWarnings("unchecked")
    public Iterator<TemporalElement> temporalPrimitives() {
        return this.temporalPrimitives.iterator(temporalElements.nodeRows());
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
     * Get an iterator over all temporal objects in the temporal dataset. The
     * temporal object is a proxy tuple for a row in the occurrences table.
     * 
     * @return an iterator over TemporalObject instances
     */
    @SuppressWarnings("unchecked")
    public Iterator<TemporalObject> temporalObjects() {
        return super.nodes();
    }

    /**
     * Get the TemporalObject instance corresponding to its id.
     * 
     * @param id
     *            object id
     * @return the TemporalObject instance corresponding to the object id
     */
    public TemporalObject getTemporalObject(long id) {
        int row = this.indexObjects.get(id);
        return (TemporalObject) super.getNode(row);
    }

    /**
     * Get an iterator over all temporal objects occurring with the given
     * temporal element.
     * 
     * @param temporalId
     *            temporal element id
     * @return temporal objects occurring with the temporal element
     */
    @SuppressWarnings("unchecked")
    public Iterator<TemporalObject> getTemporalObjectsByElementId(
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
     */
    @Deprecated
    public Table getOccurrences() {
        return super.getNodeTable();
    }

    /**
     * Gets the graph that contains all (temporal) occurrences of data elements,
     * and the relations between these occurrences.
     * 
     * @return a {@link Graph} containing all occurrences and relations between
     *         them
     */
    @Deprecated
    public Graph getOccurrencesGraph() {
        return this;
    }

    /**
     * Adds an occurrence of a data element at a given temporal element
     * 
     * @param dataElementInd
     *            the index of the data element in the {@link Table} of data
     *            elements
     * @param temporalElementInd
     *            the index of the temporal element in the {@link Table} of
     *            temporal elements
     * @return the index of the added occurrence in the {@link Table} of
     *         occurrences
     */
    @Deprecated
    public int addOccurrence(int dataElementInd, int temporalElementInd) {
        try {
            addTemporalObject(dataElementInd, temporalElementInd);
        } catch (TemporalDataException e) {
            e.printStackTrace();
        }
        return this.indexObjects.get(temporalElementInd);
    }

    /**
     * Adds a temporal object.
     * 
     * @param temporalObjectId
     *            the id of the temporal object
     * @param temporalElementId
     *            the id of the temporal element
     * @throws TemporalDataException
     */
    public TemporalObject addTemporalObject(long temporalObjectId,
            long temporalElementId) throws TemporalDataException {
//        if (this.indexObjects.get(temporalObjectId) != Integer.MIN_VALUE)
//            throw new TemporalDataException("Duplicate temporal object id");
//        if (this.indexElements.get(temporalElementId) == Integer.MIN_VALUE)
//            throw new TemporalDataException(
//                    "Temporal element id does not exist");

        TemporalObject object = (TemporalObject) super.addNode();
        object.set(TEMPORAL_OBJECT_ID, temporalObjectId);
        object.set(TEMPORAL_OBJECT_TEMPORAL_ID, temporalElementId);
        return object;
    }
    
    /**
     * Adds a temporal object.
     * 
     * @param temporalElementId
     *            the id of the temporal element
     * @throws TemporalDataException
     */
    public TemporalObject addTemporalObject(long temporalElementId)
            throws TemporalDataException {
        long id = (indexObjects.size() > 0) ? super.getNodeTable()
                .getLong(this.indexObjects.maximum(), TEMPORAL_OBJECT_ID) + 1
                : 1;

        return addTemporalObject(id, temporalElementId);
    }

    /**
     * Adds a temporal object.
     * 
     * @param temporalElement
     *            the temporal element
     * @throws TemporalDataException
     */
    public TemporalObject addTemporalObject(TemporalElement temporalElement)
            throws TemporalDataException {
        return addTemporalObject(temporalElement.getId());
    }

    /**
     * Creates an {@link IntervalIndex} for the temporal elements. It helps in
     * querying the elements based on intervals.
     * 
     * @param comparator
     *            an {@link IntervalComparator} to compare intervals for
     *            indexing any querying purposes.
     */
    public IntervalIndex createTemporalIndex(IntervalComparator comparator) {
        Table elements = this.temporalElements.getNodeTable();
        Column colLo = elements.getColumn(INF);
        Column colHi = elements.getColumn(SUP);
        // XXX this method does not yet exclude unanchored sets
        IntIterator rows = elements.rows(new AbstractPredicate() {
            @Override
            public boolean getBoolean(Tuple t) {
                return t.getInt(KIND) != PRIMITIVE_SPAN;
            }
        });
        return new IntervalTreeIndex(elements, rows, colLo, colHi, comparator);
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
        long id = (indexElements.size() > 0) ? temporalElements.getNodeTable()
                .getLong(this.indexElements.maximum(), TEMPORAL_ELEMENT_ID) + 1
                : 1;
        // long id = this.maximumTemporalElementId.getMaximum() + 1;
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
        Table nodeTable = temporalElements.getNodeTable();
        int row = nodeTable.addRow();
        nodeTable.set(row, TEMPORAL_ELEMENT_ID, id);
        nodeTable.set(row, INF, inf);
        nodeTable.set(row, SUP, sup);
        nodeTable.set(row, GRANULARITY_ID, granularityId);
        nodeTable.set(row, GRANULARITY_CONTEXT_ID, granularityContextId);
        nodeTable.set(row, KIND, kind);

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
        int row = addTemporalElementAsRow(inf, sup, granularityId,
                granularityContextId, kind);
        return (GenericTemporalElement) this.temporalGenerics.getTuple(row);

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
                granularityContextId, TemporalDataset.PRIMITIVE_INSTANT);
        Instant result = (Instant) this.temporalPrimitives.getTuple(row);
        return result;
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
                granularityContextId, TemporalDataset.PRIMITIVE_INSTANT);
        Instant result = (Instant) this.temporalPrimitives.getTuple(row);
        return result;
    }

    public Instant addInstant(Granule granule) throws TemporalDataException {
        return addInstant(granule.getInf(), granule.getSup(), granule
                .getGranularity().getIdentifier(), granule.getGranularity()
                .getGranularityContextIdentifier());
    }

    public Interval addInterval(Instant begin, Instant end)
            throws TemporalDataException {

        // XXX make this more correct using Tim's classes (e.g., check &
        // handle different granularities)

        GenericTemporalElement interval = addTemporalElement(begin.getInf(),
                end.getSup(), begin.getGranularityId(),
                begin.getGranularityContextId(),
                TemporalDataset.PRIMITIVE_INTERVAL);

        // add edges to temporal element graph
        this.getTemporalElements().addEdge(begin, interval);
        this.getTemporalElements().addEdge(end, interval);

        return (Interval) interval.asPrimitive();
    }
    
    public Interval addInterval(Instant begin, Span span)
            throws TemporalDataException {
        throw new UnsupportedOperationException();
    }
    
    public Interval addInterval(Span span, Instant end)
            throws TemporalDataException {
        throw new UnsupportedOperationException();
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

        s.addColumn(TEMPORAL_ELEMENT_ID, long.class, -1);
        s.addColumn(INF, long.class, Long.MIN_VALUE);
        s.addColumn(SUP, long.class, Long.MAX_VALUE);
        s.addColumn(GRANULARITY_ID, int.class, -1);
        s.addColumn(GRANULARITY_CONTEXT_ID, int.class, -1);
        s.addColumn(KIND, int.class, -1);

        return s;
    }

    // predefined kinds of temporal elements
    public static final int PRIMITIVE_SPAN = 0;
    public static final int PRIMITIVE_SET = 1;
    public static final int PRIMITIVE_INSTANT = 2;
    public static final int PRIMITIVE_INTERVAL = 3;

    // TODO move this enumeration to a separate file???
    // TODO use constants instead of enumeration?
    @Deprecated
    public enum Primitives {
        SPAN(0), SET(1), INSTANT(2), INTERVAL(3);

        public final int kind;

        private Primitives(int kind) {
            this.kind = kind;
        }
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
        return "TemporalDataset [" + this.temporalElements.getNodeCount()
                + " temporal elements, " + super.getNodeCount()
                + " temporal objects, " + super.getEdgeCount()
                + " object relationships]";
    }

    @Override
    public long getInf() {
        // TODO ignore unanchored temporal elements (use IntervalIndex?)
        Table elem = temporalElements.getNodeTable(); 
        return elem.getLong(elem.getMetadata(INF).getMinimumRow(), INF);
    }

    @Override
    public long getSup() {
        // TODO ignore unanchored temporal elements (use IntervalIndex?)
        Table elem = temporalElements.getNodeTable(); 
        return elem.getLong(elem.getMetadata(SUP).getMaximumRow(), SUP);
    }
}
