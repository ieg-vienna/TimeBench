package timeBench.data.relational;

import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.column.Column;
import prefuse.data.event.ColumnListener;
import prefuse.data.event.EventConstants;
import prefuse.data.event.GraphListener;
import prefuse.data.event.TableListener;
import prefuse.data.tuple.CompositeTupleSet;
import prefuse.data.tuple.TupleSet;
import prefuse.data.util.Index;
import prefuse.util.PrefuseConfig;
import prefuse.util.TypeLib;
import prefuse.util.collections.CopyOnWriteArrayList;
import prefuse.util.collections.IntArrayIterator;
import prefuse.util.collections.IntIterator;

/**
 * A bipartite-graph implemented in analogy to {@link Graph}.
 * 
 * <p> Storage
 * It maintain two node tables stored as tuple sets under the names 
 * {@link BipartiteGraph#NODES_1} and {@link BipartiteGraph#NODES_2},
 * as well as an edges-table which stores edges from the first to the second table.
 * Additionally, for performance reasons, it stores the outgoing links from the first set
 * in adjacency lists (which store the row index of the target nodes), and the incoming 
 * links in the same way (where the adjacency lists store the source nodes).
 * 
 * <p> Graph specification
 * While the graph is implemented as directed (edge source always from the first tuple set
 * and edge target always to the second tuple set), but conceptually it is treated as undirected.
 * Multiple edges are supported.
 * 
 * @author bilal
 */
public class BipartiteGraph extends CompositeTupleSet {

	public static interface BipartiteGraphListener {
	    /**
	     * Notification that a graph has changed.
	     * @param g the graph that has changed
	     * @param table the particular table within the graph that has changed
	     * @param start the starting row index of the changed table region
	     * @param end the ending row index of the changed table region
	     * @param col the column that has changed, or
	     * {@link EventConstants#ALL_COLUMNS} if the operation affects all
	     * columns
	     * @param type the type of modification, one of
	     * {@link EventConstants#INSERT}, {@link EventConstants#DELETE}, or
	     * {@link EventConstants#UPDATE}.
	     */
	    public void graphChanged(BipartiteGraph g, String table, 
	            int start, int end, int col, int type);
	    

	}
	
	/** Default data field used to uniquely identify a node */
	public static final String DEFAULT_NODE_KEY = PrefuseConfig
			.get("data.graph.nodeKey");
	/** Default data field used to denote the source node in an edge table */
	public static final String DEFAULT_SOURCE_KEY = PrefuseConfig
			.get("data.graph.sourceKey");
	/** Default data field used to denote the target node in an edge table */
	public static final String DEFAULT_TARGET_KEY = PrefuseConfig
			.get("data.graph.targetKey");
	/** Data group name to identify the nodes of this graph */
	public static final String NODES_1 = "data.graph.nodeGroup1";
	public static final String NODES_2 = "data.graph.nodeGroup2";
	/** Data group name to identify the edges of this graph */
	public static final String EDGES = PrefuseConfig
			.get("data.graph.edgeGroup");

	// -- auxiliary data structures -----

	/** Table containing the adjacency lists for the graph */
	protected Table m_links1;
	protected Table m_links2;
	/** The node key field (for the first Node table) */
	protected String m_n1key;
	/** The node key field (for the second Node table) */
	protected String m_n2key;
	/** The source node key field (for the Edge table) */
	protected String m_skey;
	/** The target node key field (for the Edge table) */
	protected String m_tkey;
	/** Reference to an index over the node key field in first Node table */
	protected Index m_n1idx;
	/** Reference to an index over the node key field  in second Node table */
	protected Index m_n2idx;
	/** Indicates if the key values are of type long */
	protected boolean m_longKey1 = false;
	/** Indicates if the key values are of type long */
	protected boolean m_longKey2 = false;
	/** Update listener */
	private Listener m_listener;
	/** Listener list */
	private CopyOnWriteArrayList m_listeners = new CopyOnWriteArrayList();

	// ------------------------------------------------------------------------
	// Constructors

	/**
	 * Creates a new, empty Graph.
	 * 
	 * @param directed
	 *            true for directed edges, false for undirected
	 */
	public BipartiteGraph() {
		this(new Table(), new Table());
	}

	/**
	 * Create a new Graph using the provided table of node data and an empty set
	 * of edges.
	 * 
	 * @param nodes
	 *            the backing table to use for node data. Node instances of this
	 *            graph will get their data from this table.
	 * @param directed
	 *            true for directed edges, false for undirected
	 */
	public BipartiteGraph(Table nodes1, Table nodes2) {
		this(nodes1, nodes2, DEFAULT_NODE_KEY, DEFAULT_NODE_KEY,
				DEFAULT_SOURCE_KEY, DEFAULT_TARGET_KEY);
	}

	/**
	 * Create a new Graph using the provided table of node data and an empty set
	 * of edges.
	 * 
	 * @param nodes
	 *            the backing table to use for node data. Node instances of this
	 *            graph will get their data from this table.
	 * @param directed
	 *            true for directed edges, false for undirected
	 * @param nodeKey
	 *            data field used to uniquely identify a node. If this field is
	 *            null, the node table row numbers will be used
	 * @param sourceKey
	 *            data field used to denote the source node in an edge table
	 * @param targetKey
	 *            data field used to denote the target node in an edge table
	 */
	public BipartiteGraph(Table nodes1, Table nodes2, String node1Key,
			String node2Key, String sourceKey, String targetKey) {
		Table edges = new Table();
		edges.addColumn(sourceKey, int.class, new Integer(-1));
		edges.addColumn(targetKey, int.class, new Integer(-1));
		init(nodes1, nodes2, edges, node1Key, node2Key, sourceKey, targetKey);
	}

	/**
	 * Create a new Graph, using node table row numbers to uniquely identify
	 * nodes in the edge table's source and target fields.
	 * 
	 * @param nodes
	 *            the backing table to use for node data. Node instances of this
	 *            graph will get their data from this table.
	 * @param edges
	 *            the backing table to use for edge data. Edge instances of this
	 *            graph will get their data from this table.
	 * @param directed
	 *            true for directed edges, false for undirected
	 */
	public BipartiteGraph(Table nodes1, Table nodes2, Table edges) {
		this(nodes1, nodes2, edges, DEFAULT_NODE_KEY, DEFAULT_NODE_KEY,
				DEFAULT_SOURCE_KEY, DEFAULT_TARGET_KEY);
	}

	/**
	 * Create a new Graph, using node table row numbers to uniquely identify
	 * nodes in the edge table's source and target fields.
	 * 
	 * @param nodes
	 *            the backing table to use for node data. Node instances of this
	 *            graph will get their data from this table.
	 * @param edges
	 *            the backing table to use for edge data. Edge instances of this
	 *            graph will get their data from this table.
	 * @param directed
	 *            true for directed edges, false for undirected
	 * @param sourceKey
	 *            data field used to denote the source node in an edge table
	 * @param targetKey
	 *            data field used to denote the target node in an edge table
	 */
	public BipartiteGraph(Table nodes1, Table nodes2, Table edges,
			String sourceKey, String targetKey) {
		init(nodes1, nodes2, edges, DEFAULT_NODE_KEY, DEFAULT_NODE_KEY,
				sourceKey, targetKey);
	}

	/**
	 * Create a new Graph.
	 * 
	 * @param nodes
	 *            the backing table to use for node data. Node instances of this
	 *            graph will get their data from this table.
	 * @param edges
	 *            the backing table to use for edge data. Edge instances of this
	 *            graph will get their data from this table.
	 * @param directed
	 *            true for directed edges, false for undirected
	 * @param nodeKey
	 *            data field used to uniquely identify a node. If this field is
	 *            null, the node table row numbers will be used
	 * @param sourceKey
	 *            data field used to denote the source node in an edge table
	 * @param targetKey
	 *            data field used to denote the target node in an edge table
	 */
	public BipartiteGraph(Table nodes1, Table nodes2, Table edges,
			String node1Key, String node2Key, String sourceKey, String targetKey) {
		init(nodes1, nodes2, edges, node1Key, node2Key, sourceKey, targetKey);
	}

	// ------------------------------------------------------------------------
	// Initialization

	/**
	 * Initialize this Graph instance.
	 * 
	 * @param nodes
	 *            the node table
	 * @param edges
	 *            the edge table
	 * @param directed
	 *            the edge directionality
	 * @param nodeKey
	 *            data field used to uniquely identify a node
	 * @param sourceKey
	 *            data field used to denote the source node in an edge table
	 * @param targetKey
	 *            data field used to denote the target node in an edge table
	 */
	protected void init(Table nodes1, Table nodes2, Table edges,
			String node1Key, String node2Key, String sourceKey, String targetKey) {
		// sanity check
		if ((node1Key != null && !TypeLib.isIntegerType(nodes1
				.getColumnType(node1Key)))
				|| (node2Key != null && !TypeLib.isIntegerType(nodes2
						.getColumnType(node2Key)))
				|| !TypeLib.isIntegerType(edges.getColumnType(sourceKey))
				|| !TypeLib.isIntegerType(edges.getColumnType(targetKey))) {
			throw new IllegalArgumentException(
					"Incompatible column types for graph keys");
		}

		removeAllSets();
		super.addSet(EDGES, edges);
		super.addSet(NODES_1, nodes1);
		super.addSet(NODES_2, nodes2);

		// INVARIANT: these three should all reference the same type
		// currently limited to int
		m_n1key = node1Key;
		m_n2key = node2Key;
		m_skey = sourceKey;
		m_tkey = targetKey;

		// set up indices
		if (node1Key != null) {
			if (nodes1.getColumnType(node1Key) == long.class)
				m_longKey1 = true;
			nodes1.index(node1Key);
			m_n1idx = nodes1.getIndex(node1Key);
		}

		// set up indices
		if (node2Key != null) {
			if (nodes2.getColumnType(node2Key) == long.class)
				m_longKey2 = true;
			nodes2.index(node1Key);
			m_n2idx = nodes2.getIndex(node1Key);
		}

		// set up node attribute optimization
		initLinkTable();

		// set up listening
		if (m_listener == null)
			m_listener = new Listener();
		nodes1.addTableListener(m_listener);
		nodes2.addTableListener(m_listener);
		edges.addTableListener(m_listener);
		m_listener.setEdgeTable(edges);

	}

	/**
	 * Dispose of this graph. Unregisters this graph as a listener to its
	 * included tables.
	 */
	public void dispose() {
		getNode1Table().removeTableListener(m_listener);
		getNode2Table().removeTableListener(m_listener);
		getEdgeTable().removeTableListener(m_listener);
	}

	/**
	 * Updates this graph to use a different edge structure for the same nodes.
	 * All other settings will remain the same (e.g., directionality, keys)
	 * 
	 * @param edges
	 *            the new edge table.
	 */
	public void setEdgeTable(Table edges) {
		Table oldEdges = getEdgeTable();
        oldEdges.removeTableListener(m_listener);
		m_links1.clear();
		m_links2.clear();

		init(getNode1Table(), getNode2Table(), edges, m_n1key, m_n2key, m_skey,
				m_tkey);
	}

	// ------------------------------------------------------------------------
	// Data Access Optimization

	/**
	 * Initialize the link table, which holds adjacency lists for this graph.
	 */
	protected void initLinkTable() {
		// set up cache of node data
		m_links1 = createLinkTable(getNode1Table().getMaximumRow() + 1);
		m_links2 = createLinkTable(getNode2Table().getMaximumRow() + 1);

		IntIterator edges = getEdgeTable().rows();
		while (edges.hasNext()) {
			updateDegrees(edges.nextInt(), 1);
		}
	}

	/**
	 * Instantiate and return the link table.
	 * 
	 * @return the created link table
	 */
	protected Table createLinkTable(int size) {
		return LINKS_SCHEMA.instantiate(size);
	}

	/**
	 * Internal method for updating the linkage of this graph.
	 * 
	 * @param e
	 *            the edge id for the updated link
	 * @param incr
	 *            the increment value, 1 for an added link, -1 for a removed
	 *            link
	 */
	protected void updateDegrees(int e, int incr) {
		if (!getEdgeTable().isValidRow(e))
			return;
		int s = getSourceNode(e);
		int t = getTargetNode(e);
		if (s < 0 || t < 0)
			return;
		updateDegrees(e, s, t, incr);
	}

	/**
	 * Internal method for updating the linkage of this graph.
	 * 
	 * @param e
	 *            the edge id for the updated link
	 * @param s
	 *            the source node id for the updated link
	 * @param t
	 *            the target node id for the updated link
	 * @param incr
	 *            the increment value, 1 for an added link, -1 for a removed
	 *            link
	 */
	protected void updateDegrees(int e, int s, int t, int incr) {
		int d1 = m_links1.getInt(s, DEGREE);
		int d2 = m_links2.getInt(t, DEGREE);
		// update adjacency lists
		if (incr > 0) {
			// add links
			addLink(m_links1, d1, s, e);
			addLink(m_links2, d2, t, e);
		} else if (incr < 0) {
			// remove links
			remLink(m_links1, d1, s, e);
			remLink(m_links1, d2, t, e);
		}
		// update degree counts
		m_links1.setInt(s, DEGREE, d1 + incr);
		m_links2.setInt(t, DEGREE, d2 + incr);
	}

	/**
	 * Internal method for adding a link to an adjacency list
	 * 
	 * @param field
	 *            which adjacency list (inlinks or outlinks) to use
	 * @param len
	 *            the length of the adjacency list
	 * @param n
	 *            the node id of the adjacency list to use
	 * @param e
	 *            the edge to add to the list
	 */
	protected void addLink(Table links, int len, int n, int e) {
		int[] array = (int[]) links.get(n, LINKS);
		if (array == null) {
			array = new int[] { e };
			links.set(n, LINKS, array);
			return;
		} else if (len == array.length) {
			int[] narray = new int[Math.max(3 * array.length / 2, len + 1)];
			System.arraycopy(array, 0, narray, 0, array.length);
			array = narray;
			links.set(n, LINKS, array);
		}
		array[len] = e;
	}

	/**
	 * Internal method for removing a link from an adjacency list
	 * 
	 * @param field
	 *            which adjacency list (inlinks or outlinks) to use
	 * @param len
	 *            the length of the adjacency list
	 * @param n
	 *            the node id of the adjacency list to use
	 * @param e
	 *            the edge to remove from the list
	 * @return true if the link was removed successfully, false otherwise
	 */
	protected boolean remLink(Table links, int len, int n, int e) {
		int[] array = (int[]) links.get(n, LINKS);
		for (int i = 0; i < len; ++i) {
			if (array[i] == e) {
				System.arraycopy(array, i + 1, array, i, len - i - 1);
				return true;
			}
		}
		return false;
	}

	/**
	 * Update the link table to accomodate an inserted or deleted node
	 * 
	 * @param r
	 *            the node id, also the row number into the link table
	 * @param added
	 *            indicates if a node was added or removed
	 */
	protected void updateNodeData(Table links, int r, boolean added) {
		if (added) {
			links.addRow();
		} else {
			links.removeRow(r);
		}
	}

	// ------------------------------------------------------------------------
	// Key Transforms

	/**
	 * Get the data field used to uniquely identify a node
	 * 
	 * @return the data field used to uniquely identify a node
	 */
	public String getNode1KeyField() {
		return m_n1key;
	}

	/**
	 * Get the data field used to uniquely identify a node
	 * 
	 * @return the data field used to uniquely identify a node
	 */
	public String getNode2KeyField() {
		return m_n2key;
	}

	/**
	 * Get the data field used to denote the source node in an edge table.
	 * 
	 * @return the data field used to denote the source node in an edge table.
	 */
	public String getEdgeSourceField() {
		return m_skey;
	}

	/**
	 * Get the data field used to denote the target node in an edge table.
	 * 
	 * @return the data field used to denote the target node in an edge table.
	 */
	public String getEdgeTargetField() {
		return m_tkey;
	}

	/**
	 * Given a node id (a row number in the node table), get the value of the
	 * node key field.
	 * 
	 * @param node
	 *            the node id
	 * @return the value of the node key field for the given node
	 */
	public long get1Key(int node) {
		return m_n1key == null ? node : getNode1Table().getLong(node, m_n1key);
	}

	/**
	 * Given a node id (a row number in the node table), get the value of the
	 * node key field.
	 * 
	 * @param node
	 *            the node id
	 * @return the value of the node key field for the given node
	 */
	public long get2Key(int node) {
		return m_n2key == null ? node : getNode2Table().getLong(node, m_n2key);
	}

	/**
	 * Given a value of the node key field, get the node id (the row number in
	 * the node table).
	 * 
	 * @param key
	 *            a node key field value
	 * @return the node id (the row number in the node table)
	 */
	public int getNode1Index(long key) {
		if (m_n1idx == null) {
			return (int) key;
		} else {
			int idx = m_longKey1 ? m_n1idx.get(key) : m_n1idx.get((int) key);
			return idx < 0 ? -1 : idx;
		}
	}

	/**
	 * Given a value of the node key field, get the node id (the row number in
	 * the node table).
	 * 
	 * @param key
	 *            a node key field value
	 * @return the node id (the row number in the node table)
	 */
	public int getNode2Index(long key) {
		if (m_n2idx == null) {
			return (int) key;
		} else {
			int idx = m_longKey2 ? m_n2idx.get(key) : m_n2idx.get((int) key);
			return idx < 0 ? -1 : idx;
		}
	}

	// ------------------------------------------------------------------------
	// Graph Mutators

	/**
	 * Add row to the node table, thereby adding a node to the graph.
	 * 
	 * @return the node id (node table row number) of the added node
	 */
	public int addNode1Row() {
		return getNode1Table().addRow();
	}

	/**
	 * Add row to the node table, thereby adding a node to the graph.
	 * 
	 * @return the node id (node table row number) of the added node
	 */
	public int addNode2Row() {
		return getNode2Table().addRow();
	}

	/**
	 * Add an edge to the graph. Both multiple edges between two nodes and edges
	 * from a node to itself are allowed.
	 * 
	 * @param s
	 *            the source node id
	 * @param t
	 *            the target node id
	 * @return the edge id (edge table row number) of the added edge
	 */
	public int addEdge(int s, int t) {
		// get keys for the nodes
		long key1 = get1Key(s);
		long key2 = get2Key(t);

		// add edge row, set source/target fields
		Table edges = getEdgeTable();
		int r = edges.addRow();
		if (m_longKey1) {
			edges.setLong(r, m_skey, key1);
		} else {
			edges.setInt(r, m_skey, (int) key1);
		}
		if (m_longKey2) {
			edges.setLong(r, m_tkey, key2);
		} else {
			edges.setInt(r, m_tkey, (int) key2);
		}
		return r;
	}

	// /**
	// * Remove a node from the graph, also removing all incident edges.
	// * @param node the node id (node table row number) of the node to remove
	// * @return true if the node was successfully removed, false if the
	// * node id was not found or was not valid
	// */
	// public boolean removeNode(int node) {
	// Table nodeTable = getNodeTable();
	// if ( nodeTable.isValidRow(node) ) {
	// int id = getInDegree(node);
	// if ( id > 0 ) {
	// int[] links = (int[])m_links.get(node, INLINKS);
	// for ( int i=id; --i>=0; )
	// removeEdge(links[i]);
	// }
	// int od = getOutDegree(node);
	// if ( od > 0 ) {
	// int[] links = (int[])m_links.get(node, OUTLINKS);
	// for ( int i=od; --i>=0; )
	// removeEdge(links[i]);
	// }
	// }
	// return nodeTable.removeRow(node);
	// }
	//
	/**
	 * Remove an edge from the graph.
	 * 
	 * @param edge
	 *            the edge id (edge table row number) of the edge to remove
	 * @return true if the edge was successfully removed, false if the edge was
	 *         not found or was not valid
	 */
	public boolean removeEdge(int edge) {
		return getEdgeTable().removeRow(edge);
	}

	/**
	 * Internal method for clearing the edge table, removing all edges.
	 */
	protected void clearEdges() {
		getEdgeTable().clear();
	}

	// ------------------------------------------------------------------------
	// Node Accessor Methods

	/**
	 * Get the collection of nodes as a TupleSet. Returns the same result as
	 * {@link CompositeTupleSet#getSet(String)} using {@link #NODES} as the
	 * parameter.
	 * 
	 * @return the nodes of this graph as a TupleSet instance
	 */
	public TupleSet getNodes1() {
		return getSet(NODES_1);
	}

	/**
	 * Get the collection of nodes as a TupleSet. Returns the same result as
	 * {@link CompositeTupleSet#getSet(String)} using {@link #NODES} as the
	 * parameter.
	 * 
	 * @return the nodes of this graph as a TupleSet instance
	 */
	public TupleSet getNodes2() {
		return getSet(NODES_2);
	}

	/**
	 * Get the backing node table.
	 * 
	 * @return the table of node values
	 */
	public Table getNode1Table() {
		return (Table) getSet(NODES_1);
	}

	/**
	 * Get the backing node table.
	 * 
	 * @return the table of node values
	 */
	public Table getNode2Table() {
		return (Table) getSet(NODES_2);
	}

	/**
	 * Get the number of nodes in this graph.
	 * 
	 * @return the number of nodes
	 */
	public int getNode1Count() {
		return getNode1Table().getRowCount();
	}

	/**
	 * Get the number of nodes in this graph.
	 * 
	 * @return the number of nodes
	 */
	public int getNode2Count() {
		return getNode2Table().getRowCount();
	}

	/**
	 * Get the degree of a node, the number of edges for which a node is either
	 * the source or the target.
	 * 
	 * @param node
	 *            the node id (node table row number)
	 * @return the total degree of the node
	 */
	public int getDegree1(int node) {
		return m_links1.getInt(node, DEGREE);
	}

	/**
	 * Get the degree of a node, the number of edges for which a node is either
	 * the source or the target.
	 * 
	 * @param node
	 *            the node id (node table row number)
	 * @return the total degree of the node
	 */
	public int getDegree2(int node) {
		return m_links2.getInt(node, DEGREE);
	}

	// ------------------------------------------------------------------------
	// Edge Accessor Methods

	/**
	 * Get the collection of edges as a TupleSet. Returns the same result as
	 * {@link CompositeTupleSet#getSet(String)} using {@link #EDGES} as the
	 * parameter.
	 * 
	 * @return the edges of this graph as a TupleSet instance
	 */
	public TupleSet getEdges() {
		return getSet(EDGES);
	}

	/**
	 * Get the backing edge table.
	 * 
	 * @return the table of edge values
	 */
	public Table getEdgeTable() {
		return (Table) getSet(EDGES);
	}

	/**
	 * Get the number of edges in this graph.
	 * 
	 * @return the number of edges
	 */
	public int getEdgeCount() {
		return getEdgeTable().getRowCount();
	}

	/**
	 * Returns an edge from the source node to the target node. This method
	 * returns the first such edge found; in the case of multiple edges there
	 * may be more.
	 */
	public int getEdge(int source, int target) {
		int outd = getDegree1(source);
		if (outd > 0) {
			int[] edges = (int[]) m_links1.get(source, LINKS);
			for (int i = 0; i < outd; ++i) {
				if (getTargetNode(edges[i]) == target)
					return edges[i];
			}
		}
		return -1;
	}

	/**
	 * Get the source node id (node table row number) for the given edge id
	 * (edge table row number).
	 * 
	 * @param edge
	 *            an edge id (edge table row number)
	 * @return the source node id (node table row number)
	 */
	public int getSourceNode(int edge) {
		return getNode1Index(getEdgeTable().getLong(edge, m_skey));
	}

	/**
	 * Get the target node id (node table row number) for the given edge id
	 * (edge table row number).
	 * 
	 * @param edge
	 *            an edge id (edge table row number)
	 * @return the target node id (node table row number)
	 */
	public int getTargetNode(int edge) {
		return getNode2Index(getEdgeTable().getLong(edge, m_tkey));
	}

	// ------------------------------------------------------------------------
	// Iterators

	// -- table row iterators ----

	/**
	 * Get an iterator over all node ids (node table row numbers).
	 * 
	 * @return an iterator over all node ids (node table row numbers)
	 */
	public IntIterator nodeRows1() {
		return getNode1Table().rows();
	}

	/**
	 * Get an iterator over all node ids (node table row numbers).
	 * 
	 * @return an iterator over all node ids (node table row numbers)
	 */
	public IntIterator nodeRows2() {
		return getNode2Table().rows();
	}

	/**
	 * Get an iterator over all edge ids (edge table row numbers).
	 * 
	 * @return an iterator over all edge ids (edge table row numbers)
	 */
	public IntIterator edgeRows() {
		return getEdgeTable().rows();
	}

	/**
	 * Get an iterator edge ids for edges incident on the given node.
	 * 
	 * @param node
	 *            a node id (node table row number)
	 * @return an iterator over all edge ids for edges incident on the given
	 *         node
	 */
	public IntIterator edgeRows1(int node) {
		int[] edges = (int[]) m_links1.get(node, LINKS);
		return new IntArrayIterator(edges, 0, getDegree1(node));
	}

	/**
	 * Get an iterator edge ids for edges incident on the given node.
	 * 
	 * @param node
	 *            a node id (node table row number)
	 * @return an iterator over all edge ids for edges incident on the given
	 *         node
	 */
	public IntIterator edgeRows2(int node) {
		int[] edges = (int[]) m_links2.get(node, LINKS);
		return new IntArrayIterator(edges, 0, getDegree2(node));
	}

	// ------------------------------------------------------------------------
	// TupleSet Interface

	/**
	 * Clear this graph, removing all nodes and edges.
	 * 
	 * @see prefuse.data.tuple.TupleSet#clear()
	 */
	public void clear() {
		super.clear();
		m_links1.clear();
		m_links2.clear();
	}

	// ------------------------------------------------------------------------
	// Graph Listeners

	/**
	 * Add a listener to be notified of changes to the graph.
	 * 
	 * @param listnr
	 *            the listener to add
	 */
	public void addGraphModelListener(GraphListener listnr) {
		if (!m_listeners.contains(listnr))
			m_listeners.add(listnr);
	}

	/**
	 * Remove a listener from this graph.
	 * 
	 * @param listnr
	 *            the listener to remove
	 */
	public void removeGraphModelListener(GraphListener listnr) {
		m_listeners.remove(listnr);
	}

	/**
	 * Removes all listeners on this graph
	 */
	public void removeAllGraphModelListeners() {
		m_listeners.clear();
	}

	/**
	 * Fire a graph change event
	 * 
	 * @param t
	 *            the backing table where the change occurred (either a node
	 *            table or an edge table)
	 * @param first
	 *            the first modified table row
	 * @param last
	 *            the last (inclusive) modified table row
	 * @param col
	 *            the number of the column modified, or
	 *            {@link prefuse.data.event.EventConstants#ALL_COLUMNS} for
	 *            operations affecting all columns
	 * @param type
	 *            the type of modification, one of
	 *            {@link prefuse.data.event.EventConstants#INSERT},
	 *            {@link prefuse.data.event.EventConstants#DELETE}, or
	 *            {@link prefuse.data.event.EventConstants#UPDATE}.
	 */
	protected void fireGraphEvent(Table t, int first, int last, int col,
			int type) {
		String table = (t == getNode1Table() ? NODES_1
				: t == getNode1Table() ? NODES_2 : EDGES);

		if (type != EventConstants.UPDATE) {
			// fire event to all tuple set listeners
			fireTupleEvent(t, first, last, type);
		}

		if (!m_listeners.isEmpty()) {
			// fire event to all listeners
			Object[] lstnrs = m_listeners.getArray();
			for (int i = 0; i < lstnrs.length; ++i) {
				 ((BipartiteGraphListener)lstnrs[i]).graphChanged(
				 this, table, first, last, col, type);
			}
		}
	}

	// ------------------------------------------------------------------------
	// Table and Column Listener

	/**
	 * Listener class for tracking updates from node and edge tables, and their
	 * columns that determine the graph linkage structure.
	 */
	/**
	 * Listener class for tracking updates from node and edge tables, and their
	 * columns that determine the graph linkage structure.
	 */
	protected class Listener implements TableListener, ColumnListener {

		private Table m_edges;
		private Column m_scol, m_tcol;
		private int m_sidx, m_tidx;

		public void setEdgeTable(Table edges) {
			// remove any previous listeners
			if (m_scol != null)
				m_scol.removeColumnListener(this);
			if (m_tcol != null)
				m_tcol.removeColumnListener(this);
			m_scol = m_tcol = null;
			m_sidx = m_tidx = -1;

			m_edges = edges;

			// register listeners
			if (m_edges != null) {
				m_sidx = edges.getColumnNumber(m_skey);
				m_tidx = edges.getColumnNumber(m_tkey);
				m_scol = edges.getColumn(m_sidx);
				m_tcol = edges.getColumn(m_tidx);
				m_scol.addColumnListener(this);
				m_tcol.addColumnListener(this);
			}
		}

		public void tableChanged(Table t, int start, int end, int col, int type) {
			if (!containsSet(t))
				throw new IllegalStateException(
						"Graph shouldn't be listening to an unrelated table");

			if (type != EventConstants.UPDATE) {
				if (t == getNode1Table()) {
					// update the linkage structure table
					if (col == EventConstants.ALL_COLUMNS) {
						boolean added = type == EventConstants.INSERT;
						for (int r = start; r <= end; ++r)
							updateNodeData(m_links1, r, added);
					}
				} else if (t == getNode2Table()) {
					// update the linkage structure table
					if (col == EventConstants.ALL_COLUMNS) {
						boolean added = type == EventConstants.INSERT;
						for (int r = start; r <= end; ++r)
							updateNodeData(m_links2, r, added);
					}
				} else {
					// update the linkage structure table
					if (col == EventConstants.ALL_COLUMNS) {
						boolean added = type == EventConstants.INSERT;
						for (int r = start; r <= end; ++r)
							updateDegrees(start, added ? 1 : -1);
					}
				}
			}
			fireGraphEvent(t, start, end, col, type);
		}

		public void columnChanged(Column src, int idx, int prev) {
			columnChanged(src, idx, (long) prev);
		}

		public void columnChanged(Column src, int idx, long prev) {
			if (src == m_scol || src == m_tcol) {
				boolean isSrc = src == m_scol;
				int e = m_edges.getTableRow(idx, isSrc ? m_sidx : m_tidx);
				if (e == -1)
					return; // edge not in this graph
				int s = getSourceNode(e);
				int t = getTargetNode(e);
				int p = isSrc ? getNode1Index(prev) : getNode2Index(prev);
				if (p > -1 && ((isSrc && t > -1) || (!isSrc && s > -1)))
					updateDegrees(e, isSrc ? p : s, isSrc ? t : p, -1);
				if (s > -1 && t > -1)
					updateDegrees(e, s, t, 1);
			} else {
				throw new IllegalStateException();
			}
		}

		public void columnChanged(Column src, int type, int start, int end) {
			// should never be called
			throw new IllegalStateException();
		}

		public void columnChanged(Column src, int idx, float prev) {
			// should never be called
			throw new IllegalStateException();
		}

		public void columnChanged(Column src, int idx, double prev) {
			// should never be called
			throw new IllegalStateException();
		}

		public void columnChanged(Column src, int idx, boolean prev) {
			// should never be called
			throw new IllegalStateException();
		}

		public void columnChanged(Column src, int idx, Object prev) {
			// should never be called
			throw new IllegalStateException();
		}
	} // end of inner class Listener

	// ------------------------------------------------------------------------
	// Graph Linkage Schema

	/** In-degree data field for the links table */
	protected static final String DEGREE = "_degree";
	/** In-links adjacency list data field for the links table */
	protected static final String LINKS = "_inlinks";
	/** Schema used for the internal graph linkage table */
	protected static final Schema LINKS_SCHEMA = new Schema();
	static {
		Integer defaultValue = new Integer(0);
		LINKS_SCHEMA.addColumn(DEGREE, int.class, defaultValue);
		LINKS_SCHEMA.addColumn(LINKS, int[].class);
		LINKS_SCHEMA.lockSchema();
	}

} // end of class Graph
