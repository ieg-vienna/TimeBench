package org.timebench.data;

/**
 * Extent on the bottom granularity.
 * 
 * <p>
 * Every anchored temporal element has a lifespan (extent on the bottom
 * granularity) bounded by Inf (infimum - minimum chronon) and Sup (supremum -
 * maximum chronon)
 * 
 * @author Rind
 * 
 */
public interface Lifespan {

    /**
     * Get the infimum (begin of lifespan for anchored time primitives).
     * 
     * @return the infimum
     */
    public abstract long getInf();

    /**
     * Get the supremum (end of lifespan for anchored time primitives).
     * 
     * @return the supremum
     */
    public abstract long getSup();

}