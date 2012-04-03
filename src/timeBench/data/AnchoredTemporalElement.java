package timeBench.data;


/**
 * Anchored temporal element in the relational view. Following the
 * <em>proxy tuple</em> pattern [Heer & Agrawala, 2006] it provides an object
 * oriented proxy for accessing a row of the temporal elements table.
 * 
 * <p>
 * This class assumes that the underlying data tuple is anchored, i.e. either an
 * instant, an interval, or a set with at least one anchored element. Thus it 
 * implements the {@link Lifespan} interface.
 * 
 * @author Rind
 */
public class AnchoredTemporalElement extends TemporalElement implements
        Lifespan {

    /**
     * relational temporal elements should only be created by the
     * {@link TemporalElementManager}
     */
    protected AnchoredTemporalElement() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see timeBench.data.relational.TemporalElement#isAnchored()
     */
    @Override
    public boolean isAnchored() {
        return true;
    }

    /**
     * Gets the length of the temporal element. This is the number of chronons
     * in the bottom granularity.
     * 
     * @return the length of the temporal element
     */
    public long getLength() {
        return getSup() - getInf() + 1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see timeBench.data.relational.Lifespan#getInf()
     */
    @Override
    public long getInf() {
        return super.getLong(TemporalElement.INF);
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
    @Deprecated
    public void setInf(long infimum) {
        // TODO do we need setters at all?
        super.setLong(TemporalElement.INF, infimum);
    }

    /*
     * (non-Javadoc)
     * 
     * @see timeBench.data.relational.Lifespan#getSup()
     */
    @Override
    public long getSup() {
        return super.getLong(TemporalElement.SUP);
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
    @Deprecated
    public void setSup(long supremum) {
        super.setLong(TemporalElement.SUP, supremum);
    }

    @Override
    public TemporalElement asPrimitive() {
        return this;
    }
}
