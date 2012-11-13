package timeBench.data;

import ieg.prefuse.data.DataHelper;


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
    	if(this.getFirstChildPrimitive() instanceof Instant)
    		return (Instant)this.getFirstChildPrimitive();
    	else if(this.getChildCount() == 2 && this.getFirstChildPrimitive() instanceof Span && this.getLastChild() instanceof Instant) {
        	throw new RuntimeException("Not implemented yet");
    	} else {
    		throw new TemporalDataException("Syntax error in temporal element of type interval");
    	}
    }
    
    public Instant getEnd() throws TemporalDataException {
        TemporalElement last = (TemporalElement) this.getLastChild();
        if (last != null) {
            last = last.asPrimitive();
            
            if(last instanceof Instant)
                return (Instant) last;
            else if(this.getChildCount() == 2 && last instanceof Span && this.getFirstChildPrimitive() instanceof Instant) {
                throw new RuntimeException("Not implemented yet");
            } else
                throw new TemporalDataException("Syntax error in temporal element of type interval");
            
        } else
            throw new TemporalDataException("Syntax error in temporal element of type interval");
        
    }
}
