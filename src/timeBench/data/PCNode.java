package timeBench.data;

import java.util.Iterator;

import org.apache.log4j.Logger;

import prefuse.data.Edge;
import prefuse.data.Node;
import prefuse.data.tuple.TableNode;

public class PCNode extends TableNode {

    // @Override
    // public PCGraph getGraph() {
    // return (PCGraph) super.getGraph();
    // }

    // ----- child methods -----

    @Override
    public int getChildCount() {
        return super.m_graph.getInDegree(this);
    }

    @Override
    public PCNode getChild(int idx) {
        // // TODO needs access to m_links -> move to subclass of graph
        throw new UnsupportedOperationException("todo");
    }

    @Override
    public PCNode getFirstChild() {
        // TODO consider more efficient solution using m_links or code like
        // getFirstChildPrimitive()
        @SuppressWarnings("rawtypes")
        Iterator objs = super.inNeighbors();
        return objs.hasNext() ? (GenericTemporalElement) objs.next() : null;
    }

    @Override
    public PCNode getLastChild() {
        // // TODO needs access to m_links -> move to subclass of graph
        throw new UnsupportedOperationException("todo");
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
    public Edge linkWithChild(PCNode child) {
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

    public PCNode getParent(int idx) {
        // // TODO needs access to m_links -> move to subclass of graph
        throw new UnsupportedOperationException("todo");
    }

    public PCNode getFirstParent() {
        // TODO consider more efficient solution using m_links or code like
        // getFirstChildPrimitive()
        @SuppressWarnings("rawtypes")
        Iterator objs = super.outNeighbors();
        return objs.hasNext() ? (PCNode) objs.next() : null;
    }

    public PCNode getLastParent() {
        // // TODO needs access to m_links -> move to subclass of graph
        throw new UnsupportedOperationException("todo");
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
    public Edge linkWithParent(PCNode parent) {
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
