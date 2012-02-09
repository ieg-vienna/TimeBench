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
import timeBench.data.util.IntervalComparator;
import timeBench.data.util.IntervalIndex;
import timeBench.data.util.IntervalTreeIndex;

/**
 * This class maintains data structures that encompass a temporal dataset. It
 * consists of a {@link Graph} of temporal objects and a {@link Graph} of 
 * temporal elements. Temporal objects are in a 1:n relation with temporal 
 * elements to encompass temporal occurrence. Furthermore, the class provides 
 * utility methods to index and query the dataset.
 * 
 * <p>
 * <b>Warning:</b> If a pre-existing temporal elements table is used, it needs
 * to provide four columns for inf, sup, kind, and granularity id. Furthermore,
 * the temporal elements table should yield tuples of class
 * {@link GenericTemporalElement}.
 * 
 * @author BA, AR, TL
 * 
 */
public class TemporalDataset extends Graph implements Cloneable {
	
	private Graph temporalElements;
	
	private long[] roots = null;	// if we have a forest or tree of temporal objects, null for tables
	
    private TemporalElementManager temporalPrimitives;
    
    private Index indexObjects;

    private Index indexElements;

    private Index indexObjectsByElements;
    
    // predefined column names for temporal objects 
    public static final String TEMPORAL_OBJECT_ID = "id";

    public static final String TEMPORAL_OBJECT_TEMPORAL_ID = "temporal_id";

    // predefined column names for temporal elements 
    public static final String TEMPORAL_ELEMENT_ID = "id";

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
        this.temporalElements.getNodeTable().addColumns(this.getTemporalElementSchema());
        
        // add temporal objects columns (primary and foreign key) 
        super.addColumn(TEMPORAL_OBJECT_ID, long.class, -1);
        super.addColumn(TEMPORAL_OBJECT_TEMPORAL_ID, long.class, -1);
        
        // add indices
        this.indexObjects = super.getNodeTable().index(TEMPORAL_OBJECT_ID);
        this.indexObjectsByElements = super.getNodeTable().index(TEMPORAL_OBJECT_TEMPORAL_ID);
        this.indexElements = this.temporalElements.getNodeTable().index(TEMPORAL_ELEMENT_ID);
        
        initTupleManagers();
	}
	
	/**
	 * Constructs an empty {@link TemporalDataset} with the given schema for data elements. 
	 * @param dataColumns schema for data elements
	 */
    public TemporalDataset(Schema dataColumns) {
        this();
        
        // TODO check that it does not interfere with primary and foreign key 
        super.getNodeTable().addColumns(dataColumns);
    }
		
    /**
     * Set tuple managers for temporal elements, temporal primitives, and
     * temporal objects and use them in the underlying data structures.
     * 
     * <p>
     * This method is called from all constructors and will cause 
     * all existing Tuples retrieved from this dataset to be invalidated.
     */
    private void initTupleManagers() {
        // nodes of temporal object graph --> TemporalObject
        TupleManager tempObjectManager = new TupleManager(super.getNodeTable(), this, TemporalObject.class);
        
        // nodes of temporal element graph --> GenericTemporalElement
        TupleManager temporalTuples = new TemporalElementManager(this, true);

        // additional tuple manager for temporal element graph --> temporal primitives
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
        temporalElements.setTupleManagers(temporalTuples, tempElementEdgeManager);
        temporalElements.getNodeTable().setTupleManager(temporalTuples);
        temporalElements.getEdgeTable().setTupleManager(tempElementEdgeManager);
    }

    @Deprecated
    public Object clone() {
        throw new UnsupportedOperationException("clone no longer needed");
    }
    
	/**
	 * Gets the roots if the TemporalObjects form a wood, the root, if they form a tree, or null for tables
	 * 
	 * @return the roots
	 */
	public long[] getRoots() {
		return roots;
	}

	/**
	 * Sets the roots: If the TemporalObjects form a wood use the roots, if they form a tree, use the root, for tables set null
	 * 
	 * @param roots the roots to set
	 */
	public void setRoots(long[] roots) {
		this.roots = roots;
	}

	/**
	 * Gets the data elements in the dataset
	 * @return a {@link Table} containing the data elements
	 */
	@Deprecated
	public Table getDataElements() {
		return super.getNodeTable();
	}
	
	/**
	 * Gets the temporal elements in the dataset
	 * @return a {@link Graph} containing the temporal elements and how they are related.
	 */
	public Graph getTemporalElements() {
		return temporalElements;
	}
	
    /**
     * Get the TemporalElement instance corresponding to its row number.
     * @param row temporal element table row number
     * @return the TemporalElement instance corresponding to the row number
     */
	public GenericTemporalElement getTemporalElementByRow(int row) {
	    return (GenericTemporalElement) temporalElements.getNode(row);
	}

    /**
     * Get the TemporalElement instance corresponding to its id.
     * @param id element id 
     * @return the TemporalElement instance corresponding to the element id
     */
	public GenericTemporalElement getTemporalElement(long id) {
	    return getTemporalElementByRow(this.indexElements.get(id));
	}

    /**
     * Get the temporal primitive corresponding to its row number.
     * @param row temporal element table row number
     * @return the temporal primitive corresponding to the row number
     */
	public TemporalElement getTemporalPrimitiveByRow(int row) {
        return (TemporalElement) this.temporalPrimitives.getTuple(row);
    }

    /**
     * Get the temporal primitive corresponding to its id.
     * @param n element id
     * @return the temporal primitive corresponding to the element id
     */
    public TemporalElement getTemporalPrimitive(long id) {
        return getTemporalPrimitiveByRow(this.indexElements.get(id));
    }

    /**
     * Get an iterator over all temporal elements in the temporal dataset.
     * @return an iterator over TemporalElement instances
     */
    @SuppressWarnings("unchecked")
    public Iterator<GenericTemporalElement> temporalElements() {
        return temporalElements.nodes();
    }

    /**
     * allows iteration over all temporal elements. 
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
     * @return an iterator over TemporalElement instances
     */
    @SuppressWarnings("unchecked")
    public Iterator<TemporalElement> temporalPrimitives() {
        return this.temporalPrimitives.iterator(temporalElements.nodeRows());
    }

    /**
     * Get an iterator over all temporal objects in the temporal dataset.
     * The temporal object is a proxy tuple for a row in the occurrences table.
     * @return an iterator over TemporalObject instances
     */
    @SuppressWarnings("unchecked")
    public Iterator<TemporalObject> temporalObjects() {
        return super.nodes();
    }

    /**
     * Get the TemporalObject instance corresponding to its id.
     * @param id object id 
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
     * @param temporalId temporal element id 
     * @return temporal objects occurring with the temporal element
     */
    @SuppressWarnings("unchecked")
    public Iterator<TemporalObject> getTemporalObjectsByElementId(long temporalId) {
        IntIterator rows = this.indexObjectsByElements.rows(temporalId);
        return super.getNodeTable().tuples(rows);
    }

	/**
	 * Gets all (temporal) occurrences of data elements
	 * @return a {@link Table} containing all temporal occurrences
	 * 
	 * TL 2011-11-07: not deprecated because needed to generate VisualTable 
	 */
    @Deprecated
	public Table getOccurrences() {
		return super.getNodeTable();
	}

	/**
	 * Gets the graph that contains all (temporal) occurrences of data elements,
	 * and the relations between these occurrences.
	 * @return a {@link Graph} containing all occurrences and relations between them
	 */
    @Deprecated
	public Graph getOccurrencesGraph() {
		return this;
	}
	
	/**
	 * Adds an occurrence of a data element at a given temporal element
	 * @param dataElementInd the index of the data element in the {@link Table} of data elements
	 * @param temporalElementInd the index of the temporal element in the {@link Table} of temporal elements
	 * @return the index of the added occurrence in the {@link Table} of occurrences
	 */
    @Deprecated
	public int addOccurrence(int dataElementInd, int temporalElementInd) {
        addTemporalObject(dataElementInd, temporalElementInd);
	    return this.indexObjects.get(temporalElementInd);
	}
	
    /**
     * Adds a temporal object.
     * @param temporalObjectId the id of the temporal object 
     * @param temporalElementId the id of the temporal element 
     */
	public void addTemporalObject(long temporalObjectId, long temporalElementId) {
		int row = super.addNodeRow();
		super.getNodeTable().set(row, TEMPORAL_OBJECT_ID, temporalObjectId);
        super.getNodeTable().set(row, TEMPORAL_OBJECT_TEMPORAL_ID, temporalElementId);
	}
	
	/**
	 * Creates an {@link IntervalIndex} for the temporal elements. It helps in querying
	 * the elements based on intervals.
	 * @param comparator an {@link IntervalComparator} to compare intervals for indexing any querying purposes.
	 */
	// XXX this method does not yet exclude spans
	public IntervalIndex createTemporalIndex(IntervalComparator comparator) {
		Table elements = this.temporalElements.getNodeTable();
		Column colLo = elements.getColumn(INF);
		Column colHi = elements.getColumn(SUP);	
		IntIterator rows = elements.rows(new AbstractPredicate() {
			@Override
			public boolean getBoolean(Tuple t) {
				return t.getInt(KIND) != PRIMITIVE_SPAN;
			}
		});
		return new IntervalTreeIndex(elements, rows, colLo, colHi, comparator);
	}

	/**
	 * Adds a new temporal element to the dataset
	 * @param inf the lower end of the temporal element
	 * @param sup the upper end of the temporal element
	 * @param granularityId the granularityID of the temporal element
	 * @param granularityContextId the granularityContextID of the temporal element
	 * @param kind the kind of the temporal element
	 * @return the index of the created element in the table of temporal elements
	 */
	public int addTemporalElement(long inf, long sup, int granularityId, int granularityContextId, int kind) {
        // set id to 
        long id = temporalElements.getNodeTable().getLong(this.indexElements.maximum(), TEMPORAL_ELEMENT_ID) + 1;
        return addTemporalElement(id, inf, sup, granularityId, granularityContextId, kind);
	}
	
    /**
     * Adds a new temporal element to the dataset
     * @param id the id of the temporal element
     * @param inf the lower end of the temporal element
     * @param sup the upper end of the temporal element
     * @param granularityId the granularityID of the temporal element
     * @param granularityContextId the granularityContextID of the temporal element
     * @param kind the kind of the temporal element
     * @return the index of the created element in the table of temporal elements
     */
    public int addTemporalElement(long id, long inf, long sup, int granularityId, int granularityContextId, int kind) {
        Table nodeTable = temporalElements.getNodeTable();
        int row = nodeTable.addRow();
        nodeTable.set(row, TEMPORAL_ELEMENT_ID, id);
		nodeTable.set(row, INF, inf);
		nodeTable.set(row, SUP, sup);
		nodeTable.set(row, GRANULARITY_ID, granularityId);
		nodeTable.set(row, GRANULARITY_CONTEXT_ID, granularityContextId);
		nodeTable.set(row, KIND, kind);

        // enforce that class of proxy tuple is reconsidered
        // this is safe because the object is not known outside of this method
        // (exception are tuple listeners)
        // this.temporalTuples.invalidate(row);

		return row;
	}
	
   // TODO do we want/need methods like this
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
    public Instant addInstant(long inf, long sup, int granularityId,int granularityContextId) {
        int row = this.addTemporalElement(inf, sup, granularityId, granularityContextId,
                TemporalDataset.PRIMITIVE_INSTANT);
        Instant result = (Instant) this.temporalPrimitives.getTuple(row);
        return result;
    }
    
    /**
     * Get an instance of the default {@link Schema} used for
     * {@link TemporalElement} instances. Contains the data members internally
     * used to model a temporal element, i.e. inf, sup, granularity, 
     * granularity context, and kind.
     * 
     * @return the TemporalElement data Schema
     */
    public Schema getTemporalElementSchema() {
        Schema s = new Schema();

        // TODO insert ID column
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
	    SPAN(0),
	    SET(1),
	    INSTANT(2),
	    INTERVAL(3);
	    
	    public final int kind;
	    
	    private Primitives(int kind) {
            this.kind = kind;
        }
	}
    
    /**
     * creates a human-readable string from a {@link TemporalDataset}.
     * <p>
     * Example:TemporalDataset [7 temporal elements, 9 temporal objects, 
     * 8 object relationships]
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
}
