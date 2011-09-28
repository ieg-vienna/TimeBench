package timeBench.data;

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