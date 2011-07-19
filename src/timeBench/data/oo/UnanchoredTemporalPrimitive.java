package timeBench.data.oo;

import timeBench.calendar.Granularity;
import timeBench.data.TemporalDataException;

/**
 * UnanchoredTemporalPrimitive is the base class for Unanchored Temporal Primitives.
 * (Spans). 
 * 
 * <p>
 * Added:         2011-07-13 / TL<br>
 * Modifications: 
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public class UnanchoredTemporalPrimitive extends TemporalPrimitive {
	public Instant before(Instant anchor) throws TemporalDataException {
		return before(anchor,null);
	}	
	public Instant before(Instant anchor,Granularity granularity) throws TemporalDataException {
		throw new TemporalDataException("No general way to calculate before() for unanchored temporal primitive.");
	}
	
	public Instant after(Instant anchor) throws TemporalDataException {
		return after(anchor,null);
	}
	public Instant after(Instant anchor,Granularity granularity) throws TemporalDataException {
		throw new TemporalDataException("No general way to calculate before() for unanchored temporal primitive.");
	}
}
