package timeBench.data.relational;

import java.util.Iterator;

import org.apache.log4j.Logger;

import prefuse.data.Tuple;

/**
 * Relational view of the temporal object. Following the <em>proxy tuple</em>
 * pattern [Heer & Agrawala, 2006] it provides an object oriented proxy for
 * accessing a row of the occurrences table in a {@link TemporalDataset}.
 * 
 * @author Rind
 *
 */
public class TemporalObject extends BipartiteEdge {
    
    static Logger logger = Logger.getLogger(TemporalObject.class); 
    
    /**
     * @return the data element
     */
    public Tuple getDataElement() {
        return bGraph.getNode1Table().getTuple(bGraph.getSourceNode(getRow()));
    }
    
    /**
     * @return the temporal element
     */
    public TemporalElement getTemporalElement() {
        return (TemporalElement) bGraph.getNode2Table().getTuple(
                bGraph.getTargetNode(getRow()));
    }

    /**
     * Get an iterator over all temporal elements that are children of this
     * temporal element.
     * 
     * @return an iterator over children
     */
    @SuppressWarnings("unchecked")
    public Iterator<TemporalObject> childElements() {
        return super.inNeighbors();
    }

    /**
     * Gets the number of child temporal elements
     * 
     * @return the number of child temporal elements
     */
    public int getChildElementCount() {
        return super.m_graph.getInDegree(this);
    }
    
    /**
     * Links a TemporalObject as child to this Temporalobject.
     * 
     * @param child The Temporal Object that will be added as child.
     */
    public void linkWithChild(TemporalObject child) {
        super.m_graph.addEdge(child, this);
        logger.trace("link with child: " + this.getRow() + " <- " + child.getRow() 
                + " my childs: " + this.getChildElementCount() 
                + " total childs: " + super.m_graph.getEdgeCount() 
                + " total nodes: " + super.m_graph.getNodeCount());
    } 

    /**
     * creates a human-readable string from a {@link TemporalObject}.
     * <p>
     * Example: TemporalObject[occ#=5, data#=5, temporal#=6]
     * 
     * @return a string representation
     */
    @Override
    public String toString() {
        return "TemporalObject[occ#=" + super.getRow() + ", data#="
                + bGraph.getSourceNode(getRow()) + ", temporal#="
                + bGraph.getTargetNode(getRow()) + "]";
    }
}
