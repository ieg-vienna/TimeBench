package timeBench.data;

import java.util.Iterator;

import org.apache.log4j.Logger;

import prefuse.data.Edge;
import prefuse.data.Node;
import prefuse.data.Tree;
import prefuse.data.tuple.TableNode;

/**
 * Tuple representing a node in a {@link ParentChildGraph}. These nodes can have multiple
 * "child" nodes and multiple "parent" nodes (in principle these are nodes
 * connected by incoming and outgoing edges). The class provides convenience
 * methods to access these nodes (e.g., {@link ParentChildNode#getLastChild()}) and
 * throws exceptions in methods that do not make sense here (e.g.,
 * {@link #getNextSibling()}).
 * 
 * <p>
 * Note that edges are directed from child to parent, which is opposite from
 * {@link Tree} nodes.
 * 
 * @author Rind
 */
public class ParentChildNode extends TableNode {

    @Override
    public ParentChildGraph getGraph() {
        return (ParentChildGraph) super.getGraph();
    }

    // ----- child methods -----

    @Override
    public int getChildCount() {
        return super.m_graph.getInDegree(this);
    }

    @Override
    public ParentChildNode getChild(int idx) {
        int c = getGraph().getChildRow(m_row, idx);
        return (ParentChildNode) (c < 0 ? null : m_graph.getNode(c));
    }

    @Override
    public ParentChildNode getFirstChild() {
        return getChild(0);
    }

    @Override
    public ParentChildNode getLastChild() {
        return getChild(getChildCount() - 1);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Iterator children() {
        return super.inNeighbors();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Iterator childEdges() {
        return super.inEdges();
    }

    /**
     * Links a node as child to this node.
     * 
     * @param child
     *            The node that will be added as child.
     * @return the edge that was added between the two nodes
     */
    public Edge linkWithChild(ParentChildNode child) {
        if (Logger.getLogger(this.getClass()).isTraceEnabled()) {
            Logger.getLogger(this.getClass()).trace(
                    "link with child: " + this.getRow() + " <- "
                            + child.getRow() + " my childs: "
                            + this.getChildCount() + " total childs: "
                            + super.m_graph.getEdgeCount() + " total nodes: "
                            + super.m_graph.getNodeCount());
        }

        return super.m_graph.addEdge(child, this);
    }

    // ----- parent methods -----

    /**
     * Gets the number of parent nodes
     * 
     * @return the number of parent nodes
     */
    public int getParentCount() {
        return super.m_graph.getOutDegree(this);
    }

    public ParentChildNode getParent(int idx) {
        int c = getGraph().getParentRow(m_row, idx);
        return (ParentChildNode) (c < 0 ? null : m_graph.getNode(c));
    }

    public ParentChildNode getFirstParent() {
        return getParent(0);
    }

    public ParentChildNode getLastParent() {
        return getParent(getParentCount() - 1);
    }

    @SuppressWarnings("rawtypes")
    public Iterator parents() {
        return super.outNeighbors();
    }

    @SuppressWarnings("rawtypes")
    public Iterator parentEdges() {
        return super.outEdges();
    }

    /**
     * Links a node as parent to this node.
     * 
     * @param parent
     *            The node that will be added as parent.
     * @return the edge that was added between the two nodes
     */
    public Edge linkWithParent(ParentChildNode parent) {
        if (Logger.getLogger(this.getClass()).isTraceEnabled()) {
            Logger.getLogger(this.getClass()).trace(
                    "link with parent: " + this.getRow() + " -> "
                            + parent.getRow() + " my childs: "
                            + this.getChildCount() + " total childs: "
                            + super.m_graph.getEdgeCount() + " total nodes: "
                            + super.m_graph.getNodeCount());
        }

        return super.m_graph.addEdge(this, parent);
    }

    // ----- methods that do not make sense -----

    @Override
    public Node getParent() {
        throw new UnsupportedOperationException();
        // return super.getParent();
    }

    @Override
    public Edge getParentEdge() {
        throw new UnsupportedOperationException();
        // return super.getParentEdge();
    }

    @Override
    public int getChildIndex(Node child) {
        throw new UnsupportedOperationException();
        // return super.getChildIndex(child);
    }

    @Override
    public Node getPreviousSibling() {
        throw new UnsupportedOperationException();
        // return super.getPreviousSibling();
    }

    @Override
    public Node getNextSibling() {
        throw new UnsupportedOperationException();
        // return super.getNextSibling();
    }
}
