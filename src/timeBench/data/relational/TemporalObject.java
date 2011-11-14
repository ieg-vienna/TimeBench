package timeBench.data.relational;

import java.util.Iterator;

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
        return super.getInDegree();
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
