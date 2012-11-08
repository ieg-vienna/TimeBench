package timeBench.data;


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
     * {@link TemporalDataset}
     */
    protected Interval() {
    }

    // nothing to do here :-)

    // TODO special handling of children e.g. getBegin() : Instant in conjunction with spans
    
    public Instant getBegin() throws TemporalDataException {
    	if(this.getFirstChild() instanceof Instant)
    		return (Instant)this.getFirstChild();
    	else if(this.getChildElementCount() == 2 && this.getFirstChild() instanceof Span && this.getLastChild() instanceof Instant) {
        	throw new RuntimeException("Not implemented");
    	} else
    		throw new TemporalDataException("Syntax error in temporal element of type interval");
    }
    
    public Instant getEnd() throws TemporalDataException {
    	if(this.getLastChild() instanceof Instant)
    		return (Instant)this.getLastChild();
    	else if(this.getChildCount() == 2 && this.getLastChild() instanceof Span && this.getFirstChild() instanceof Instant) {
        	throw new RuntimeException("Not implemented");
    	} else
    		throw new TemporalDataException("Syntax error in temporal element of type interval");
    }
}
