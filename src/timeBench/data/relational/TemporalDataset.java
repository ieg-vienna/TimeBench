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
import prefuse.util.collections.IntIterator;
import timeBench.data.TemporalDataException;
import timeBench.data.util.IntervalComparator;
import timeBench.data.util.IntervalIndex;
import timeBench.data.util.IntervalTreeIndex;

/**
 * This class maintains data structures that encompass a temporal dataset. It
 * consists of a {@link Table} of DataElements and a {@link Graph} of temporal
 * elements. Temporal occurrences of data elements are saved in an additional
 * {@link Table}. Furthermore, the class provides utility methods to index and
 * query the dataset.
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
public class TemporalDataset implements Cloneable {
	
	private BipartiteGraph graph;
	
	private Graph temporalElements;
	
	private Graph occurrences;
	
	private Table dataElements;

    private TemporalElementManager temporalPrimitives;

	// predefined column names for temporal elements 
	public static final String INF = "inf";

	public static final String SUP = "sup";

	public static final String GRANULARITY_ID = "granularityID";

	public static final String GRANULARITY_CONTEXT_ID = "granularityContextID";
	
	public static final String KIND = "kind";
	
	/**
	 * Constructs an empty {@link TemporalDataset}
	 */
	public TemporalDataset() {
		this(new Table(), new Table());
		// define temporal element columns for nodes of the temporal e. graph
		this.getTemporalElements().addColumns(this.getTemporalElementSchema());

		this.setTemporalElementManager(new TemporalElementManager(this, true));
	}
	
	/**
	 * Constructs a {@link TemporalDataset} with the given data- and temporal-elements 
	 * @param dataElements a {@link Table} containing the data elements
	 * @param temporalElements a {@link Table} containing the temporal elements 
	 */
    public TemporalDataset(Table dataElements, Table temporalElements) {
        // here we cannot call the other constructor, because that would throw
        // an exception that is impossible to catch
        this.dataElements = dataElements;
        this.temporalElements = new Graph(temporalElements, true);
        graph = new BipartiteGraph(dataElements, getTemporalElements());
        occurrences = new Graph(graph.getEdgeTable(), true);
        // set a tuple manager for the edge table of the bipartite graph
        // so that its tuples are instances of TemporalObject
        TupleManager tempObjectManager = 
                new BipartiteEdgeManager(graph.getEdgeTable(), occurrences, 
                        graph, TemporalObject.class);
        graph.getEdgeTable().setTupleManager(tempObjectManager);
        
        TupleManager tempObjectEdgeManager = new TupleManager(
                occurrences.getEdgeTable(), occurrences,
                TableEdge.class);
        occurrences.setTupleManagers(tempObjectManager, tempObjectEdgeManager);
        occurrences.getNodeTable().setTupleManager(tempObjectManager);
        occurrences.getEdgeTable().setTupleManager(tempObjectEdgeManager);
        
        this.temporalPrimitives = new TemporalElementManager(this, false);
        this.temporalPrimitives.invalidateAutomatically();
    }
		
	/**
	 * Constructs a {@link TemporalDataset} with the given data- and temporal-elements 
	 * @param dataElements a {@link Table} containing the data elements
	 * @param temporalElements a directed {@link Graph} containing the temporal elements   
	 * and how they are related
	 * @throws TemporalDataException if the temporal element Graph is not directed
	 */
	public TemporalDataset(Table dataElements, Graph temporalElements) throws TemporalDataException {
		if (!temporalElements.isDirected()) {
			throw new TemporalDataException("The graph of the temporal elements must be directed");
		}
		this.dataElements = dataElements;
		this.temporalElements = temporalElements;		
		graph = new BipartiteGraph(dataElements, getTemporalElements());
        occurrences = new Graph(graph.getEdgeTable(), true);
        // set a tuple manager for the edge table of the bipartite graph 
        // so that its tuples are instances of TemporalObject
        TupleManager tempObjectManager = 
            new BipartiteEdgeManager(graph.getEdgeTable(), occurrences, 
                    graph, TemporalObject.class);
        graph.getEdgeTable().setTupleManager(tempObjectManager);
        occurrences.getNodeTable().setTupleManager(tempObjectManager);

        this.temporalPrimitives = new TemporalElementManager(this, false);
        this.temporalPrimitives.invalidateAutomatically();
    }
	
	
	/**
	 * Performs a semi-deep clone of the {@link TemporalDataset}, cloning the containing tables,
	 * but not the data in den tables.
	 */
	public Object clone() {
		TemporalDataset result = new TemporalDataset();
		
		result.dataElements = new Table();
		result.dataElements.addColumns(this.dataElements.getSchema());
		for(int i=0; i<this.dataElements.getRowCount(); i++ )
			result.dataElements.addTuple(this.dataElements.getTuple(i));

		result.temporalElements = new Graph();
		result.temporalElements.getNodeTable().addColumns(this.temporalElements.getNodeTable().getSchema());
		for(int i=0; i<this.temporalElements.getNodeTable().getRowCount(); i++ )
			result.temporalElements.getNodeTable().addTuple(this.temporalElements.getNodeTable().getTuple(i));
		result.temporalElements.getEdgeTable().addColumns(this.temporalElements.getEdgeTable().getSchema());
		for(int i=0; i<this.temporalElements.getEdgeTable().getRowCount(); i++ )
			result.temporalElements.getEdgeTable().addTuple(this.temporalElements.getEdgeTable().getTuple(i));
		
		result.graph = new BipartiteGraph(this.graph.getNode1Table(),this.graph.getNode2Table());
		
		result.occurrences = new Graph();
		result.occurrences.getNodeTable().addColumns(this.occurrences.getNodeTable().getSchema());
		for(int i=0; i<this.occurrences.getNodeTable().getRowCount(); i++ )
			result.occurrences.getNodeTable().addTuple(this.occurrences.getNodeTable().getTuple(i));
		result.occurrences.getEdgeTable().addColumns(this.occurrences.getEdgeTable().getSchema());
		for(int i=0; i<this.occurrences.getEdgeTable().getRowCount(); i++ )
			result.occurrences.getEdgeTable().addTuple(this.occurrences.getEdgeTable().getTuple(i));
		
		result.temporalPrimitives = this.temporalPrimitives;
		
		return result;
	}


	/**
	 * Gets the data elements in the dataset
	 * @return a {@link Table} containing the data elements
	 */
	public Table getDataElements() {
		return dataElements;
	}
	
	/**
	 * Gets the temporal elements in the dataset
	 * @return a {@link Table} containing the temporal elements.
	 */
	public Table getTemporalElements() {
		return temporalElements.getNodeTable();
	}
	
	/**
	 * Gets the temporal elements in the dataset
	 * @return a {@link Graph} containing the temporal elements and how they are related.
	 */
	public Graph getTemporalElementsGraph() {
		return temporalElements;
	}
	
    /**
     * Get the TemporalElement instance corresponding to its id.
     * @param n element id (temporal element table row number)
     * @return the TemporalElement instance corresponding to the element id
     */
	public GenericTemporalElement getTemporalElement(int n) {
	    return (GenericTemporalElement) temporalElements.getNode(n);
	}

    /**
     * Get the temporal primitive corresponding to its id.
     * @param n element id (temporal element table row number)
     * @return the temporal primitive corresponding to the element id
     */
    public TemporalElement getTemporalPrimitive(int n) {
        return (TemporalElement) this.temporalPrimitives.getTuple(n);
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
        return graph.getEdgeTable().tuples();
    }

    /**
     * Get the TemporalObject instance corresponding to its id.
     * @param n object id (occurrences table row number)
     * @return the TemporalObject instance corresponding to the object id
     */
    public TemporalObject getTemporalObject(int n) {
        return (TemporalObject) graph.getEdgeTable().getTuple(n);
    }

	/**
	 * Gets all (temporal) occurrences of data elements
	 * @return a {@link Table} containing all temporal occurrences
	 * 
	 * TL 2011-11-07: not deprecated because needed to generate VisualTable 
	 */
	public Table getOccurrences() {
		return occurrences.getNodeTable();
	}

	/**
	 * Gets the graph that contains all (temporal) occurrences of data elements,
	 * and the relations between these occurrences.
	 * @return a {@link Graph} containing all occurrences and relations between them
	 */
	public Graph getOccurrencesGraph() {
		return occurrences;
	}
	
	/**
	 * Adds an occurrence of a data element at a given temporal element
	 * @param dataElementInd the index of the data element in the {@link Table} of data elements
	 * @param temporalElementInd the index of the temporal element in the {@link Table} of temporal elements
	 * @return the index of the added occurrence in the {@link Table} of occurrences
	 */
	public int addOccurrence(int dataElementInd, int temporalElementInd) {
		return graph.addEdge(dataElementInd, temporalElementInd);
	}
	
	/**
	 * Creates an {@link IntervalIndex} for the temporal elements. It helps in querying
	 * the elements based on intervals.
	 * @param comparator an {@link IntervalComparator} to compare intervals for indexing any querying purposes.
	 */
	// XXX this method does not yet exclude spans
	public IntervalIndex createTemporalIndex(IntervalComparator comparator) {
		Table elements = getTemporalElements();
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
		Table nodeTable = temporalElements.getNodeTable();
		int row = nodeTable.addRow();
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
     * Set a tuple manager for temporal elements and use it in the underlying
     * data structures.
     * 
     * <p>
     * Use this method carefully, as it will cause all existing Tuples retrieved
     * from this dataset to be invalidated.
     * 
     * @param manager
     *            tuple manager for temporal elements
     */
    private void setTemporalElementManager(TemporalElementManager manager) {
//        TupleManager temporalTuples = new TupleManager(
//                temporalElements.getNodeTable(), temporalElements,
//                GenericTemporalElement.class);
        TupleManager temporalTuples = manager;

        // we also need to set a manager for edge tuples
        TupleManager edgeTuples = new TupleManager(
                temporalElements.getEdgeTable(), temporalElements,
                TableEdge.class);

        temporalElements.setTupleManagers(temporalTuples, edgeTuples);
        temporalElements.getNodeTable().setTupleManager(temporalTuples);
        temporalElements.getEdgeTable().setTupleManager(edgeTuples);
    }
    
    /**
     * Get an instance of the default {@link Schema} used for
     * {@link TemporalElement} instances. Contains the data members internally
     * used to model a temporal element, i.e. inf, sup, granularity, and kind.
     * 
     * @return the TemporalElement data Schema
     */
    public Schema getTemporalElementSchema() {
        Schema s = new Schema();

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
     * Example:TemporalDataset [7 temporal elements, 6 data elements, 6 temporal
     * objects]
     * 
     * @return a string representation
     */
    @Override
    public String toString() {
        return "TemporalDataset [" + this.getTemporalElements().getRowCount()
                + " temporal elements, " + this.getDataElements().getRowCount()
                + " data elements, " + this.graph.getEdgeCount()
                + " temporal objects]";
    }
}
