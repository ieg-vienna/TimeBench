package org.timebench.data;

/**
 * Generic temporal element in the relational view. Following the
 * <em>proxy tuple</em> pattern [Heer & Agrawala, 2006] it provides an object
 * oriented proxy for accessing a row of the temporal elements table.
 * 
 * <p>
 * This class makes no assumptions on the type of temporal element and allows
 * direct read write access to the underlying table.
 * 
 * @author Rind
 */
public class GenericTemporalElement extends TemporalElement {

    // if this implemented Lifespan, it should handle unanchored primitives
    // differently

    /**
     * keeps track if the temporal element is anchored or not. The value of this
     * variable will only be determined, when the {@link #isAnchored()} method
     * is called; before that it is set to dirty (lazy).
     */
    // alternatives: 2 boolean, int with constants, enum
    private Boolean anchoredCache = null;

    /**
     * Creates a GenericTemporalElement that is managed directly by the
     * {@link TemporalElementManager}.
     * 
     * <p>
     * This constructor should only be called by the
     * {@link TemporalElementManager}.
     */
    protected GenericTemporalElement() {
    }

    /**
     * set multiple fields of a temporal element at once.
     * 
     * @param inf
     *            the value of the inf attribute
     * @param sup
     *            the value of the sup attribute
     * @param granularityId
     *            the granularity id
     * @param granularityContextId
     *            the granularity context id
     */
    public void set(long inf, long sup, int granularityId,
            int granularityContextId) {
        if (isValid()) {
            m_table.set(m_row, TemporalElement.INF, inf);
            m_table.set(m_row, TemporalElement.SUP, sup);
            m_table.set(m_row, TemporalElement.GRANULARITY_ID, granularityId);
            m_table.set(m_row, TemporalElement.GRANULARITY_CONTEXT_ID,
                    granularityContextId);
        }
    }

    /**
     * set all fields of a temporal element at once.
     * 
     * @param inf
     *            the value of the inf attribute
     * @param sup
     *            the value of the sup attribute
     * @param granularityId
     *            the granularity id
     * @param granularityContextId
     *            the granularity context id
     * @param kind
     *            the kind of temporal element
     */
    public void set(long inf, long sup, int granularityId,
            int granularityContextId, int kind) {
        if (isValid()) {
            m_table.set(m_row, TemporalElement.INF, inf);
            m_table.set(m_row, TemporalElement.SUP, sup);
            m_table.set(m_row, TemporalElement.GRANULARITY_ID, granularityId);
            m_table.set(m_row, TemporalElement.GRANULARITY_CONTEXT_ID,
                    granularityContextId);
            m_table.set(m_row, TemporalElement.KIND, kind);
            this.anchoredCache = null;
        }
    }

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
            this.anchoredCache = Boolean.valueOf(TemporalElementManager
                    .isAnchored(super.m_graph, super.m_row));
        }
        return (this.anchoredCache.booleanValue());
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
     * <p>
     * This can be either
     * <li>the number of chronons in the bottom granularity for anchored
     * temporal elements or
     * <li>the number of granules in the current granularity for unanchored
     * temporal elements.
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
     * Sets the length of the temporal element.
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
    @Deprecated
    public void setLength(long value) {
        if (isAnchored())
            setSup(value + getInf() - 1);
        else
            setSup(value);
    }

    /**
     * Get the value of the inf attribute (begin of lifespan for anchored time
     * primitives or granule count for spans).
     * 
     * @return value of the inf attribute
     */
    public long getInf() {
        return super.getLong(TemporalElement.INF);
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
        super.setLong(TemporalElement.INF, infimum);
    }

    /**
     * get the value of the sup attribute (end of lifespan for anchored time
     * primitives or granule count for spans).
     * 
     * @return value of the sup attribute
     */
    public long getSup() {
        return super.getLong(TemporalElement.SUP);
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
        super.setLong(TemporalElement.SUP, supremum);
    }

    /**
     * Set the granularity id.
     * 
     * @param granularityId
     *            the granularity id
     */
    public void setGranularityId(int granularityId) {
        super.setInt(TemporalElement.GRANULARITY_ID, granularityId);
    }

    /**
     * Set the granularity context id.
     * 
     * @param granularityContextId
     *            the granularity context id
     */
    public void setGranularityContextId(int granularityContextId) {
        super.setInt(TemporalElement.GRANULARITY_CONTEXT_ID,
                granularityContextId);
    }

    /**
     * Set the kind of temporal element.
     * 
     * @param kind
     *            the kind of temporal element
     */
    public void setKind(int kind) {
        this.anchoredCache = null;
        super.setInt(TemporalElement.KIND, kind);
    }

    @Override
    public GenericTemporalElement asGeneric() {
        return this;
    }
}
