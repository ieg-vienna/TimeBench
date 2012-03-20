package timeBench.data.relational;

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
    @Deprecated
    protected GenericTemporalElement() {
    }

    @Deprecated
    public GenericTemporalElement(TemporalDataset tmpds, long id, long inf, long sup, int granularityId, int granularityContextId, int kind) {
        super(tmpds);
        // TODO check for duplicate id
        this.setLong(TemporalDataset.TEMPORAL_ELEMENT_ID, id);
        set(inf, sup, granularityId, granularityContextId, kind);
    }
    
    public void set(long inf, long sup, int granularityId, int granularityContextId, int kind) {
        if (isValid()) {
            m_table.set(m_row, TemporalDataset.INF, inf);
            m_table.set(m_row, TemporalDataset.SUP, sup);
            m_table.set(m_row, TemporalDataset.GRANULARITY_ID, granularityId);
            m_table.set(m_row, TemporalDataset.GRANULARITY_CONTEXT_ID, granularityContextId);
            m_table.set(m_row, TemporalDataset.KIND, kind);
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
     * Set the granularity id.
     * 
     * @param granularityId
     *            the granularity id
     */
    public void setGranularityId(int granularityId) {
        super.setInt(TemporalDataset.GRANULARITY_ID, granularityId);
    }

    /**
     * Set the kind of temporal element.
     * 
     * @param granularityId
     *            the kind of temporal element
     */
    public void setKind(int kind) {
        this.anchoredCache = null;
        super.setInt(TemporalDataset.KIND, kind);
    }

    @Override
    public GenericTemporalElement asGeneric() {
        return this;
    }
}
