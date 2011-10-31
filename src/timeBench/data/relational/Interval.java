package timeBench.data.relational;

/**
 * Interval in the relational view. Following the <em>proxy tuple</em> pattern
 * [Heer & Agrawala, 2006] it provides an object oriented proxy for accessing a
 * row of the temporal elements table.
 * 
 * <p>
 * This class assumes that the underlying data tuple is an interval.
 * 
 * @author Rind
 */
public class Interval extends AnchoredTemporalElement {

    /**
     * relational temporal elements should only be created by the
     * {@link TemporalElementManager}
     */
    protected Interval() {
    }

    // nothing to do here :-)

    // TODO special handling of children e.g. getBegin() : Instant
}
