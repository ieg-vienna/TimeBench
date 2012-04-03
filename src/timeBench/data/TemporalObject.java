package timeBench.data;

import ieg.util.lang.CustomIterable;

import java.util.Iterator;

import org.apache.log4j.Logger;

import prefuse.data.Tuple;
import prefuse.data.tuple.TableNode;

/**
 * Relational view of the temporal object. Following the <em>proxy tuple</em>
 * pattern [Heer & Agrawala, 2006] it provides an object oriented proxy for
 * accessing a row of the occurrences table in a {@link TemporalDataset}.
 * 
 * @author Rind
 *
 */
public class TemporalObject extends TableNode {
    
    static Logger logger = Logger.getLogger(TemporalObject.class); 
    
    /**
     * creates an invalid TemporalObject. Use {@link TemporalDataset} as a
     * factory!
     */
    // make constructor protected: TemporalObjectManager extends TupleManager
    public TemporalObject() {
    }

    /**
     * Get the temporal element id.
     * 
     * @return the id
     */
    public long getId() {
        return super.getLong(TemporalDataset.TEMPORAL_OBJECT_ID);
    }

    /**
     * @return the data element
     */
    @Deprecated
    public Tuple getDataElement() {
        return this;
    }
    
    /**
     * @return the temporal element
     */
    public GenericTemporalElement getTemporalElement() {
        long teId = super.getLong(TemporalDataset.TEMPORAL_OBJECT_TEMPORAL_ID);
        // the temporal object graph, is actually the temporal dataset 
        return ((TemporalDataset) m_graph).getTemporalElement(teId);
    }

    /**
     * Get an iterator over all temporal elements that are children of this
     * temporal element.
     * 
     * @return an iterator over children
     */
    @SuppressWarnings("unchecked")
    // TODO rename in childObjectIterator()
    public Iterator<TemporalObject> childElements() {
        return super.inNeighbors();
    }
    
    @SuppressWarnings("unchecked")
    public Iterable<TemporalObject> childObjects() {
        return new CustomIterable(super.inNeighbors());
    }

    /**
     * Gets the number of child temporal elements
     * 
     * @return the number of child temporal elements
     */
    // TODO rename in getChildObjectCount()
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
     * Example: TemporalObject[id=5, temporal id=3]
     * 
     * @return a string representation
     */
    @Override
    public String toString() {
        return "TemporalObject[id=" + super.getRow() + ", temporal id="
                + super.getLong(TemporalDataset.TEMPORAL_OBJECT_TEMPORAL_ID)
                + "]";
    }
}
