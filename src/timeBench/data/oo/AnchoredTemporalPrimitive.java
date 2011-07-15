package timeBench.data.oo;

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
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public class AnchoredTemporalPrimitive extends TemporalPrimitive implements TemporalElement {
	private Interval lifeSpanBuffer;
    private boolean lifeSpanBufferDirty = true;
	
	public Interval lifeSpan() throws TemporalDataException {
		if (lifeSpanBufferDirty)
			rebuildLifeSpanBuffer();
		
		return lifeSpanBuffer;
	}
	
	private void rebuildLifeSpanBuffer() throws TemporalDataException {
		Instant start = null;
		Instant stop = null;
		
		if(parts.size()<2)
			throw new TemporalDataException("");
		lifeSpanBuffer = new Interval(start,stop);
	}
}
