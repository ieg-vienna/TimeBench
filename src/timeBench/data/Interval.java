package timeBench.data;

import timeBench.calendar.Granule;

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
    
    public Span getSpan() throws TemporalDataException {
        TemporalElement last = (TemporalElement) this.getLastChild();
        if (last != null) {
            last = last.asPrimitive();
            
            if(last instanceof Span)
                return (Span) last;
            else if(this.getChildCount() == 2 && last instanceof Instant && this.getFirstChildPrimitive() instanceof Instant) {
                throw new RuntimeException("Not implemented yet");
            } else
                throw new TemporalDataException("Syntax error in temporal element of type interval");
            
        } else
            throw new TemporalDataException("Syntax error in temporal element of type interval");
    }
    
    public void setBegin(Granule granule) throws TemporalDataException {
        TemporalElement first = (TemporalElement) this.getFirstChildPrimitive();
        TemporalElement last = (TemporalElement) this.getLastChildPrimitive();
        if (first != null && last != null) {
            if (first instanceof Instant && last instanceof Span) {
                ((Instant) first).set(granule);
                // TODO convert begin to span granularity
                long endId = granule.getIdentifier() + ((Span) last).getLength() - 1;
                Granule endGranule = new Granule(endId, granule.getGranularity());
                this.setLong(INF, granule.getInf());
                this.setLong(SUP, endGranule.getSup());
            } else
                throw new RuntimeException("Not implemented yet");
        } else
            throw new TemporalDataException(
                    "Syntax error in temporal element of type interval");
    }
}
