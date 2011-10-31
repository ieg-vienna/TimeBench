package timeBench.data.oo;

import timeBench.calendar.Granularity;
import timeBench.calendar.JavaDateCalendarManager;
import timeBench.data.TemporalDataException;

/**
 * AnchoredTemporalPrimitive is the base class for Anchored Temporal Primitives.
 * (Instant, Interval, Indeterminate Instant, and Indeterminate Interval).
 * It implements TemporalElement as a Temporal Element can be any Anchored
 * Temporal Primitive. 
 * 
 * <p>
 * Added:         2011-07-13 / TL<br>
 * Modifications: 
 * 
 * @author Tim Lammarsch
 *
 */
public class AnchoredTemporalElement extends TemporalElement {
//	private Instant startBuffer = null;
//    private boolean startBufferDirty = true;
//	private Instant stopBuffer = null;
//    private boolean stopBufferDirty = true;
	
	private Interval lifeSpanBuffer = null;
    private boolean lifeSpanBufferDirty = true;

	long infWhenDynamic;
	long supWhenDynamic;
	
	protected AnchoredTemporalElement(timeBench.data.relational.TemporalElement relationalTemporalElement) throws TemporalDataException {
		super(relationalTemporalElement);
		if (! (super.relationalTemporalElement instanceof timeBench.data.relational.AnchoredTemporalElement))
		    throw new TemporalDataException("not anchored");
	}
	
	public AnchoredTemporalElement(long inf,long sup) {
		this(inf,sup,JavaDateCalendarManager.getSingleton().getDefaultCalendar().getDiscreteTimeDomain());
	}
	
	public AnchoredTemporalElement(long inf,long sup,Granularity granularity) {
		super(granularity);
		infWhenDynamic = inf;
		supWhenDynamic = sup;
	}
    
//    /**
//     * Calculates the first (earliest) instant that is part of the temporal primitive, or, in case it starts with
//     * an unanchored temporal primitive, the instant resulting from deducing that from the first instant.
//     * @return The first instant.
//     * @throws TemporalDataException
//     */
//	public Instant start() throws TemporalDataException {
//		if (startBufferDirty) {
//			if(this instanceof Instant) {
//				startBuffer = (Instant)this;
//			}
//			else if (parts.size()<2)
//				throw new TemporalDataException("Trying to calculate start of illegal anchored temporal primitive.");
//			else {
//				int c = 0;
//				while(!(parts.get(c) instanceof AnchoredTemporalElement))
//					c++;
//				startBuffer = ((AnchoredTemporalElement)parts.get(c)).start();
//				for(int i=c-1; i>=0; i--) {
//					startBuffer = ((UnanchoredTemporalElement)parts.get(c)).before(startBuffer);
//				}
//			}
//			startBufferDirty = false;
//		}
//		return startBuffer;
//	}
	
	public boolean isDynamic() {
		return relationalTemporalElement == null;
	}

	public long getSup() {
		return relationalTemporalElement == null ? supWhenDynamic : ((timeBench.data.relational.AnchoredTemporalElement) relationalTemporalElement).getSup();
	}
		
	public long getInf() {
		return relationalTemporalElement == null ? infWhenDynamic : ((timeBench.data.relational.AnchoredTemporalElement) relationalTemporalElement).getInf();
	}
	
	
//    /**
//     * Calculates the last (latest) instant that is part of the temporal primitive, or, in case it starts with
//     * an unanchored temporal primitive, the instant resulting from adding that to the last instant.
//     * @return The last instant.
//     * @throws TemporalDataException
//     */
//	public Instant stop() throws TemporalDataException {
//		if (stopBufferDirty) {
//			if(this instanceof Instant) {
//				stopBuffer = (Instant)this;
//			}
//			else if (parts.size()<2)
//				throw new TemporalDataException("Trying to calculate stop of illegal anchored temporal primitive.");
//			else {
//				int c = 0;
//				while(!(parts.get(c) instanceof AnchoredTemporalElement))
//					c++;
//				stopBuffer = ((AnchoredTemporalElement)parts.get(c)).start();
//				for(int i=c-1; i>=0; i--) {
//					stopBuffer = ((UnanchoredTemporalElement)parts.get(c)).after(stopBuffer);
//				}
//			}
//			stopBufferDirty = false;
//		}
//		return stopBuffer;
//	}

    
    /**
     * Calculates the lifespan of the temporal primitive.
     * @return The lifespan as Interval.
     * @throws TemporalDataException
     */
	public Interval lifeSpan() throws TemporalDataException {
		if (lifeSpanBufferDirty) {
			lifeSpanBuffer = new Interval(getSup(),getInf());
			lifeSpanBufferDirty = false;
		}		
				
		return lifeSpanBuffer;
	}
}
