package org.timebench.data;


/**
 * Unanchored temporal element in the relational view. Following the
 * <em>proxy tuple</em> pattern [Heer & Agrawala, 2006] it provides an object
 * oriented proxy for accessing a row of the temporal elements table.
 * 
 * <p>
 * This class assumes that the underlying data tuple is unanchored, i.e. either
 * a span or a set with no anchored element.
 * 
 * @author Rind
 */
public class UnanchoredTemporalElement extends TemporalElement {

    /**
     * relational temporal elements should only be created by the
     * {@link TemporalDataset}
     */
    protected UnanchoredTemporalElement() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see timeBench.data.relational.TemporalElement#isAnchored()
     */
    @Override
    public boolean isAnchored() {
        return false;
    }

    /**
     * Gets the length of the temporal element. This is the number of granules
     * in the current granularity.
     * 
     * @return the length of the temporal element
     */
    @Override
    public long getLength() {
        return super.getLong(TemporalElement.INF);
    }
    
    /**
     * Sets the length of the temporal element. This is the number of granules
     * in the current granularity.
     * 
     * @value the length of the temporal element
     */
    @Override
    public void setLength(long value) {
        super.setLong(TemporalElement.INF,value);
    }

    // TODO do we need setLength --> I don't think so

    @Override
    public TemporalElement asPrimitive() {
        return this;
    }
}
