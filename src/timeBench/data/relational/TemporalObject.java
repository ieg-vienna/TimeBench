package timeBench.data.relational;

import prefuse.data.Tuple;
import timeBench.data.Lifespan;

/**
 * Relational view of the temporal object. Following the <em>proxy tuple</em>
 * pattern [Heer & Agrawala, 2006] it provides an object oriented proxy for
 * accessing a row of the occurrences table in a {@link TemporalDataset}.
 * 
 * @author Rind
 *
 */
public class TemporalObject extends BipartiteEdge implements Lifespan {
    
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

    @Override
    public long getInf() {
        return getTemporalElement().getInf();
    }

    @Override
    public long getSup() {
        return getTemporalElement().getSup();
    }
    
    /**
     * creates a human-readable string from a {@link TemporalObject}.
     * <p>
     * Example:  + ", data#=" + bGraph.getSourceNode(getRow())
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
