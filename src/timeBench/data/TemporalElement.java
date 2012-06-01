package timeBench.data;

import ieg.util.lang.CustomIterable;

import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.tuple.TableNode;
import timeBench.calendar.Granule;

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

    // predefined column names for temporal elements (similar to VisualItem)
    /**
     * the identifier data field for temporal elements. Primary key of the
     * temporal element table.
     */
    public static final String ID = "id";

    /**
     * the &quot;inf&quot; data field for temporal elements.
     */
    public static final String INF = "inf";

    /**
     * the &quot;sup&quot; data field for temporal elements.
     */
    public static final String SUP = "sup";

    /**
     * the &quot;granularity id&quot; data field for temporal elements.
     */
    public static final String GRANULARITY_ID = "granularityID";

    /**
     * the &quot;granularity context id&quot; data field for temporal elements.
     */
    public static final String GRANULARITY_CONTEXT_ID = "granularityContextID";

    /**
     * the &quot;kind&quot; data field for temporal elements.
     */
    public static final String KIND = "kind";

    
    /**
     * the backing temporal data set
     */
    private TemporalDataset tmpds;
    
    /**
     * creates an invalid TemporalElement. Use {@link TemporalDataset} as a
     * factory!
     */
    protected TemporalElement() {
    }
    
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
    public TemporalDataset getTemporalDataset() {
        return tmpds;
    }

    /**
     * Get the temporal element id.
     * 
     * @return the id
     */
    public long getId() {
        return super.getLong(TemporalElement.ID);
    }
    
    /**
     * Get the granularity id.
     * 
     * @return the granularity id
     */
    public int getGranularityId() {
        return super.getInt(TemporalElement.GRANULARITY_ID);
    }
    
    /**
     * Get the granularity context id.
     * 
     * @return the granularity context id
     */
    public int getGranularityContextId() {
        return super.getInt(TemporalElement.GRANULARITY_CONTEXT_ID);
    }

    /**
     * Get the kind of temporal element (0 = span, 1 = set/temporal element, 2 =
     * instant, 3 = interval)
     * 
     * @return the kind of temporal element
     */
    public int getKind() {
        return super.getInt(TemporalElement.KIND);
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
     * Get an iterator over all temporal objects occurring with this temporal 
     * element.
     * 
     * @return temporal objects occurring with the temporal element
     */
    public Iterable<TemporalObject> temporalObjects() {
        return this.tmpds.getTemporalObjectsByElementId(getId());
    }
    
    /**
     * Get an iterator over all temporal elements that are parents of this
     * temporal element.
     * 
     * @return an iterator over parents
     */
    @SuppressWarnings("unchecked")
    public Iterable<GenericTemporalElement> parentElements() {
        return new CustomIterable(super.outNeighbors());
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
    public Iterable<GenericTemporalElement> childElements() {
        return new CustomIterable(super.inNeighbors());
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
     * Gets the first granule of an anchored temporal element. For an
     * {@link Instant}, the granule represents the time of the instant. If it is
     * unanchored, <tt>null</tt> is returned. Granules are cached.
     * 
     * @return the first granule
     * @throws TemporalDataException
     * @see timeBench.data.util.GranuleCache
     */
    public Granule getGranule() throws TemporalDataException {
        return tmpds.getGranuleByRow(m_row);
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
                + super.getLong(TemporalElement.INF) + ", sup="
                + super.getLong(TemporalElement.SUP) + ", granularityId="
                + getGranularityId() + ", granularityContextId="
                + getGranularityContextId() + ", kind=" + getKind() + "]";
    }
}
