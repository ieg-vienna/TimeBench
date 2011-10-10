package timeBench.data.relational;

import java.util.Iterator;

import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.column.Column;
import prefuse.data.tuple.TableEdge;
import prefuse.data.tuple.TupleManager;
import timeBench.data.TemporalDataException;
import timeBench.data.util.IntervalComparator;
import timeBench.data.util.IntervalIndex;
import timeBench.data.util.IntervalTreeIndex;

/**
 * This class maintains data structures that encompass a temporal dataset.
 * It consists of a {@link Table} of DataElements and a {@link Graph} of temporal elements.
 * Temporal occurrences of data elements are saved in an additional {@link Table}.
 * Furthermore, the class provides utility methods to index and query the dataset.
 * 
 * <p><b>Warning:</b> If a pre-existing temporal elements table is used, 
 * it needs to provide four columns for inf, sup, kind, and granularity id. 
 * Furthermore, it needs to have {@link TemporalElement} as its tuple type. 
 *  
 * @author bilal
 *
 */
public class TemporalDataset {
	
	private BipartiteGraph graph;
	
	private Graph temporalElements;
	
	private Table dataElements;
	
	// predefined column names for temporal elements 
	public static final String INF = "inf";

	public static final String SUP = "sup";

	public static final String GRANULARITY_ID = "granularityID";

	public static final String KIND = "kind";
	
	/**
	 * Constructs an empty {@link TemporalDataset}
	 */
	public TemporalDataset() {
		this(new Table(), new Table());
		// define temporal element columns for nodes of the temporal e. graph
		this.getTemporalElements().addColumns(this.getTemporalElementSchema());

		// create specific tuple managers and set them to underlying structures
		// invalidates existing tuples
		TupleManager temporalTuples = new TupleManager(temporalElements.getNodeTable(), temporalElements, TemporalElement.class);
		TupleManager edgeTuples = new TupleManager(temporalElements.getEdgeTable(), temporalElements, TableEdge.class);
		temporalElements.setTupleManagers(temporalTuples, edgeTuples);
		temporalElements.getNodeTable().setTupleManager(temporalTuples);
		temporalElements.getEdgeTable().setTupleManager(edgeTuples);
	}
	
	/**
	 * Constructs a {@link TemporalDataset} with the given data- and temporal-elements 
	 * @param dataElements a {@link Table} containing the data elements
	 * @param temporalElements a {@link Table} containing the temporal elements 
	 */
	public TemporalDataset(Table dataElements, Table temporalElements) {
		this(dataElements , new Graph(temporalElements, true));
	}
		
	/**
	 * Constructs a {@link TemporalDataset} with the given data- and temporal-elements 
	 * @param dataElements a {@link Table} containing the data elements
	 * @param temporalElements a directed {@link Graph} containing the temporal elements   
	 * and how they are related
	 */
	public TemporalDataset(Table dataElements, Graph temporalElements) {
		if (!temporalElements.isDirected()) {
			throw new TemporalDataException("The graph of the temporal elements must be directed");
		}
		this.dataElements = dataElements;
		this.temporalElements = temporalElements;		
		graph = new BipartiteGraph(dataElements, getTemporalElements());
		
        // set a tuple manager for the edge table of the bipartite graph 
        // so that its tuples are instances of TemporalObject
        graph.getEdgeTable().setTupleManager(
                new BipartiteEdgeManager(graph.getEdgeTable(), graph,
                        TemporalObject.class));
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
	public TemporalElement getTemporalElement(int n) {
	    return (TemporalElement) temporalElements.getNode(n);
	}

    /**
     * Get an iterator over all temporal elements in the temporal dataset.
     * @return an iterator over TemporalElement instances
     */
    @SuppressWarnings("unchecked")
    public Iterator<TemporalElement> temporalElements() {
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
	 * @deprecated
	 */
	public Table getOccurrences() {
		return graph.getEdgeTable();
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
		return new IntervalTreeIndex(elements, elements.rows(), colLo, colHi, comparator);
	}

	/**
	 * Adds a new temporal element to the dataset
	 * @param inf the lower end of the temporal element
	 * @param sup the upper end of the temporal element
	 * @param granularityId the granularityID of the temporal element
	 * @param kind the kind of the temporal element
	 * @return the index of the created element in the table of temporal elements
	 */
	public int addTemporalElement(long inf, long sup, int granularityId, int kind) {
		Table nodeTable = temporalElements.getNodeTable();
		int row = nodeTable.addRow();
		nodeTable.set(row, INF, inf);
		nodeTable.set(row, SUP, sup);
		nodeTable.set(row, GRANULARITY_ID, granularityId);
		nodeTable.set(row, KIND, kind);
		return row;
	}
	
	// TODO do we want/need methods like this
    /**
     * Add a new instant to the dataset. This method returns a proxy tuple this
     * instant, which is of class {@link TemporalElement}.
     * 
     * @param inf
     *            the lower end of the temporal element
     * @param sup
     *            the upper end of the temporal element
     * @param granularityId
     *            the granularityID of the temporal element
     * @return a proxy tuple of the created temporal element
     */
    public TemporalElement addInstant(long inf, long sup, int granularityId) {
        int row = this.addTemporalElement(inf, sup, granularityId,
                TemporalDataset.PRIMITIVE_INSTANT);
        return this.getTemporalElement(row);
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
}
