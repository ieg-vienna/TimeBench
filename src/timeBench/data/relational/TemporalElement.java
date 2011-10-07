package timeBench.data.relational;

import java.util.Iterator;

import prefuse.data.tuple.TableNode;
import prefuse.util.collections.IntIterator;

/**
 * Relational view of the temporal element. Following the <em>proxy tuple</em>
 * pattern [Heer & Agrawala, 2006] it provides an object oriented proxy for
 * accessing a row of the temporal elements table.
 * 
 * <p>
 * This class makes no assumptions on the type of temporal element and allows
 * direct read write access to the underlying table.
 * 
 * @author Rind
 */
public class TemporalElement extends TableNode {

    // if this implemented Lifespan, it should handle unanchored primitives
    // differently (e.g., exception)

    /**
     * keeps track if the temporal element is anchored or not. The value of this
     * variable will only be determined, when the {@link #isAnchored()} method
     * is called; before that it is set to dirty (lazy).
     */
    // alternatives: 2 boolean, int with constants, enum
    private Boolean anchoredCache = null;

    // The default constructor needs to stay public so that it can be called by
    // the {@link TupleManager}. However, it is inadvisable to create it
    // yourself.

    /**
     * Indicates if the temporal element is anchored in time.
     * 
     * <p>
     * Anchored temporal elements represent one or more granules on an
     * underlying granularity. They have an infimum and an supremum in the
     * discrete time domain.
     * 
     * <p>
     * <b>Warning:</b> The method returns a cached value (see
     * {@link #resetAnchored()})
     * 
     * @return true if the element is anchored
     */
    public boolean isAnchored() {
        if (this.anchoredCache == null) {
            // recursive search for an anchor (if we have a set with only spans)
            this.anchoredCache = Boolean.valueOf(isAnchored(super.m_row));
        }
        return (this.anchoredCache.booleanValue());
    }

    /**
     * recursive helper method to find out if a temporal element is anchored.
     * 
     * <p>
     * A temporal element is anchored iff its kind is instant or interval or its
     * kind is set and at least one child is anchored.
     * 
     * @param row
     *            row number in the node table.
     * @return true if the temporal element is anchored.
     */
    private boolean isAnchored(int row) {
        // use low level functions, though here tuple functions could also be
        // used here
        int kind = super.m_graph.getNodeTable().getInt(row,
                TemporalDataset.KIND);
        if (kind == TemporalDataset.PRIMITIVE_INSTANT
                || kind == TemporalDataset.PRIMITIVE_INTERVAL)
            return true;
        else if (kind == TemporalDataset.PRIMITIVE_SET) {
            IntIterator iter = m_graph.inEdgeRows(row);
            while (iter.hasNext())
                if (isAnchored(m_graph.getSourceNode(iter.nextInt())))
                    return true;
        }
        return false;
    }

    /**
     * informs this object that its cached anchoredness state may be out-dated.
     * This method should be called after child temporal elements have been
     * added, removed, or changed their anchoredness.
     */
    public void resetAnchored() {
        this.anchoredCache = null;
    }

    /**
     * Gets the length of the temporal element.
     * 
     * <p>This can be either
     * <li>the number of chronons in the bottom granularity for anchored temporal elements or   
     * <li>the number of granules in the current granularity for unanchored temporal elements.
     * 
     * @return the length of the temporal element
     */
    @Deprecated
    public long getLength() {
        if (isAnchored())
            return getSup() - getInf() + 1;
        else
            return getSup();
    }

    /**
     * Get the value of the inf attribute (begin of lifespan for anchored time
     * primitives or granule count for spans).
     * 
     * @return value of the inf attribute
     */
    public long getInf() {
        return super.getLong(TemporalDataset.INF);
    }

    /**
     * Set the value of the inf attribute (begin of lifespan for anchored time
     * primitives or granule count for spans).
     * 
     * <p>
     * However, this does not affect parent temporal elements.
     * 
     * @param infimum
     *            the value of the inf attribute
     */
    public void setInf(long infimum) {
        super.setLong(TemporalDataset.INF, infimum);
    }

    /**
     * get the value of the sup attribute (end of lifespan for anchored time
     * primitives or granule count for spans).
     * 
     * @return value of the sup attribute
     */
    public long getSup() {
        return super.getLong(TemporalDataset.SUP);
    }

    /**
     * Set the value of the sup attribute (end of lifespan for anchored time
     * primitives or granule count for spans).
     * 
     * <p>
     * However, this does not affect parent temporal elements.
     * 
     * @param supremum
     *            the value of the sup attribute
     */
    public void setSup(long supremum) {
        super.setLong(TemporalDataset.SUP, supremum);
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
     * Set the granularity id.
     * 
     * @param granularityId
     *            the granularity id
     */
    public void setGranularityId(int granularityId) {
        super.setInt(TemporalDataset.GRANULARITY_ID, granularityId);
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
     * Set the kind of temporal element.
     * 
     * @param kind
     *            the kind of temporal element
     */
    public void setKind(int kind) {
        this.anchoredCache = null;
        super.setInt(TemporalDataset.KIND, kind);
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
     * @return the number of child temporal elements
     */
    public int getChildElementCount() {
        return super.getInDegree();
    }

    /**
     * creates a human-readable string from a {@link TemporalElement}.
     * <p>
     * Example: TemporalElement[inf=3, sup=14, granularityId=1, kind=1]
     * 
     * @return a string representation
     */
    @Override
    public String toString() {
        return "TemporalElement[inf=" + getInf() + ", sup=" + getSup()
                + ", granularityId=" + getGranularityId() + ", kind="
                + getKind() + "]";
    }
}
