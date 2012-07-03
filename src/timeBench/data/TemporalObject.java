package timeBench.data;

import java.util.Iterator;

import ieg.util.lang.CustomIterable;

import org.apache.log4j.Logger;

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
    
    // predefined column names for temporal objects (similar to VisualItem)
    
    /**
     * the identifier data field for temporal objects. Primary key of the
     * temporal object table.
     */
    public static final String ID = "_id";

    /**
     * the data field containing the identifier of the temporal element. Foreign
     * key to the temporal element table.
     */
    public static final String TEMPORAL_ELEMENT_ID = "_temporal_id"; 
    
    
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
        return super.getLong(TemporalObject.ID);
    }

    /**
     * @return the temporal element
     */
    public GenericTemporalElement getTemporalElement() {
        long teId = super.getLong(TemporalObject.TEMPORAL_ELEMENT_ID);
        // the temporal object graph, is actually the temporal dataset 
        return ((TemporalDataset) m_graph).getTemporalElement(teId);
    }

    /**
     * Get an iterator over all temporal objects that are parent of this
     * temporal object.
     * 
     * @return an object, which provides an iterator over parents
     */
    @SuppressWarnings("unchecked")
    public Iterable<TemporalObject> parentObjects() {
        return new CustomIterable(super.outNeighbors());
    }

    /**
     * Get the first or only parent temporal object.
     * 
     * @return a temporal object that is parent of this temporal object or
     *         <tt>null</tt>.
     */
    public TemporalObject getFirstParentObject() {
        @SuppressWarnings("rawtypes")
        Iterator objs = super.outNeighbors();
        return objs.hasNext() ? (TemporalObject) objs.next() : null;
    }
    
    /**
     * Get an iterator over all temporal objects that are children of this
     * temporal object.
     * 
     * @return an object, which provides an iterator over children
     */
    @SuppressWarnings("unchecked")
    public Iterable<TemporalObject> childObjects() {
        return new CustomIterable(super.inNeighbors());
    }

    /**
     * Get the first or only child temporal object.
     * 
     * @return a temporal object that is child of this temporal object or
     *         <tt>null</tt>.
     */
    public TemporalObject getFirstChildObject() {
        @SuppressWarnings("rawtypes")
        Iterator objs = super.inNeighbors();
        return objs.hasNext() ? (TemporalObject) objs.next() : null;
    }
    
    /**
     * Gets the number of child temporal objects.
     * 
     * @return the number of child temporal objects.
     */
    public int getChildObjectCount() {
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
                + " my childs: " + this.getChildObjectCount() 
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
                + super.getLong(TemporalObject.TEMPORAL_ELEMENT_ID)
                + "]";
    }

	/**
	 * Returns the level of the TemporalObject if it belongs to a tree. Otherwise,
	 * and for root nodes, it returns 0.
	 * @return the tree level
	 */
	public int getTreeLevel() {
		@SuppressWarnings("rawtypes")
		Iterator parents = super.outNeighbors(); 
		if (parents.hasNext()) {
			return ((TemporalObject) parents.next()).getTreeLevel() + 1; 
		} else {
			return 0;
		}
	}
}
