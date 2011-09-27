package timeBench.data.relational;

import java.util.Iterator;

import prefuse.data.tuple.TableNode;

/**
 * Relational view of the temporal element. Following the <em>proxy tuple</em>
 * pattern [Heer & Agrawala, 2006] it provides an object oriented proxy for
 * accessing a row of the temporal elements table.
 * 
 * @author Rind
 */
public class TemporalElement extends TableNode {

    /**
     * Get the infimum (begin of lifespan for anchored time primitives or
     * granule count for spans).
     * 
     * @return the infimum
     */
    public long getInf() {
        return super.getLong(TemporalDataset.INF);
    }

    /**
     * Set the infimum (begin of lifespan for anchored time primitives or
     * granule count for spans).
     * 
     * <p>
     * However, this does not affect parent temporal elements.
     * 
     * @param infimum
     *            the infimum
     */
    public void setInf(long infimum) {
        super.setLong(TemporalDataset.INF, infimum);
    }

    /**
     * Get the supremum (end of lifespan for anchored time primitives or granule
     * count for spans).
     * 
     * @return the supremum
     */
    public long getSup() {
        return super.getLong(TemporalDataset.SUP);
    }

    /**
     * Set the supremum (end of lifespan for anchored time primitives or granule
     * count for spans).
     * 
     * <p>
     * However, this does not affect parent temporal elements.
     * 
     * @param supremum
     *            the supremum
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
     * @param granularityId
     *            the kind of temporal element
     */
    public void setKind(int kind) {
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
