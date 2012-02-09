package timeBench.data.relational;

import java.util.Iterator;

import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.tuple.TableNode;

/**
 * Relational view of the temporal element. Following the <em>proxy tuple</em>
 * pattern [Heer & Agrawala, 2006] it provides an object oriented proxy for
 * accessing a row of the temporal elements table.
 * 
 * <p>
 * Note that this class provides only a thin layer over {@link TableNode} and
 * {@link TableTuple} for increased convenience. Methods such as
 * {@link #set(int, Object)} can still be called, which might put this object or
 * the whole temporal dataset in an inconsistent state.
 * 
 * @author Rind
 */
public abstract class TemporalElement extends TableNode {

    /**
     * the backing temporal data set
     */
    private TemporalDataset tmpds;

    /**
     * Initialize a new temporal element backed by a node table. This method is
     * used by the appropriate TupleManager instance, and should not be called
     * directly by client code, unless by a client-supplied custom TupleManager.
     * 
     * @param table
     *            the backing table
     * @param graph
     *            the backing graph
     * @param tmpds
     *            the backing temporal dataset
     * @param row
     *            the row in the node table to which this temporal element
     *            corresponds
     */
    protected void init(Table table, Graph graph, TemporalDataset tmpds, int row) {
        super.init(table, graph, row);
        this.tmpds = tmpds;
    }

    /**
     * Indicates if the temporal element is anchored in time.
     * 
     * <p>
     * Anchored temporal elements represent one or more granules on an
     * underlying granularity. They have an infimum and an supremum in the
     * discrete time domain.
     * 
     * @return true if the element is anchored
     */
    public abstract boolean isAnchored();

    /**
     * Gets the length of the temporal element.
     * 
     * <p>
     * This can be either
     * <li>the number of chronons in the bottom granularity for anchored
     * temporal elements or
     * <li>the number of granules in the current granularity for unanchored
     * temporal elements.
     * 
     * @return the length of the temporal element
     */
    // TODO does it make sense to have a common method for all temporal elements?
    public abstract long getLength();

    /**
     * Get the temporal dataset of which this element is a member.
     * 
     * @return the backing temporal dataset
     */
    // TODO should a temporal element know its dataset? (disadvantage: memory)
    public TemporalDataset getTemporalDataset() {
        return tmpds;
    }

    /**
     * Get the temporal element id.
     * 
     * @return the id
     */
    public long getId() {
        return super.getLong(TemporalDataset.TEMPORAL_ELEMENT_ID);
    }
    
    /**
     * Get the granularity id.
     * 
     * @return the granularity id
     */
    public int getGranularityId() {
        return super.getInt(TemporalDataset.GRANULARITY_ID);
    }
    
    /**
     * Get the granularity context id.
     * 
     * @return the granularity context id
     */
    public int getGranularityContextId() {
        return super.getInt(TemporalDataset.GRANULARITY_CONTEXT_ID);
    }

    /**
     * Get the kind of temporal element (0 = span, 1 = set/temporal element, 2 =
     * instant, 3 = interval)
     * 
     * @return the kind of temporal element
     */
    public int getKind() {
        return super.getInt(TemporalDataset.KIND);
    }
    
    /**
     * converts to a generic temporal element.
     * 
     * @return a generic temporal element of the same underlying data row.
     */
    public GenericTemporalElement asGeneric() {
        return this.tmpds.getTemporalElementByRow(this.m_row);
    }

    /**
     * converts to a temporal primitive.
     * 
     * @return a temporal primitive of the same underlying data row.
     */
    public TemporalElement asPrimitive() {
        return this.tmpds.getTemporalPrimitiveByRow(this.m_row);
    }
    
    /**
     * Get an iterator over all temporal elements that are parents of this
     * temporal element.
     * 
     * @return an iterator over parents
     */
    @SuppressWarnings("unchecked")
    public Iterator<TemporalElement> parentElements() {
        return super.outNeighbors();
    }

    /**
     * Gets the number of parent temporal elements
     * 
     * @return the number of parent temporal elements
     */
    public int getParentElementCount() {
        return super.getOutDegree();
    }

    /**
     * Get an iterator over all temporal elements that are children of this
     * temporal element.
     * 
     * @return an iterator over children
     */
    @SuppressWarnings("unchecked")
    public Iterator<TemporalElement> childElements() {
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
     * creates a human-readable string from a {@link TemporalElement}.
     * <p>
     * Example: GenericTemporalElement[id=2, inf=3, sup=14, granularityId=1, granularityContextId=1, kind=1]
     * 
     * @return a string representation
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[id=" + getId() + ", inf="
                + super.getLong(TemporalDataset.INF) + ", sup="
                + super.getLong(TemporalDataset.SUP) + ", granularityId="
                + getGranularityId() + ", granularityContextId="
                + getGranularityContextId() + ", kind=" + getKind() + "]";
    }
}
