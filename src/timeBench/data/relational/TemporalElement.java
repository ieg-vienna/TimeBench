package timeBench.data.relational;

import java.util.Iterator;

import prefuse.data.tuple.TableNode;
import timeBench.data.Lifespan;
import timeBench.data.TemporalDataException;

/**
 * Relational view of the temporal element. Following the <em>proxy tuple</em>
 * pattern [Heer & Agrawala, 2006] it provides an object oriented proxy for
 * accessing a row of the temporal elements table.
 * 
 * @author Rind
 */
public class TemporalElement extends TableNode implements Lifespan {
    
    /**
     * Indicates if the temporal element is anchored in time.
     * 
     * <p>Anchored temporal elements represent one or more granules on an 
     * underlying granularity. They have an infimum and an supremum in the 
     * discrete time domain. 
     * @return true if the element is anchored
     */
    public boolean isAnchored() {
        // TODO what if we have a set with only spans
        return (this.getKind() != TemporalDataset.Primitives.SPAN.kind); 
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
    public long getLength() {
        if (isAnchored())
            // TODO +1? E.g., does a single chronon have length 1 or zero?  
            return getSup() - getInf() + 1;
        else
            return getSup();
    }
    
    // TODO do we need setLength 

    /* (non-Javadoc)
     * @see timeBench.data.relational.Lifespan#getInf()
     */
    @Override
    public long getInf() {
        if (isAnchored())
            return super.getLong(TemporalDataset.INF);
        else 
            // TODO alternative: return Long.MIN_VALUE; 
            throw new TemporalDataException("unanchored temporal element have no lifespan");
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
        // TODO should this be prohibited for spans? or should we rename this method to setInfColumn? do we need setters at all?
        super.setLong(TemporalDataset.INF, infimum);
    }

    /* (non-Javadoc)
     * @see timeBench.data.relational.Lifespan#getSup()
     */
    @Override
    public long getSup() {
        if (isAnchored())
            return super.getLong(TemporalDataset.SUP);
        else 
            // TODO alternative: return Long.MAX_VALUE; 
            throw new TemporalDataException("unanchored temporal element have no lifespan");
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
        // TODO should this be prohibited for spans? or should we rename this method? 
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
