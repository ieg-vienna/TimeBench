package timeBench.data.oo;

import timeBench.calendar.Granularity;

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
	public Instant before(Instant anchor) {
		return before(anchor,granularity());
	}	
	public Instant before(Instant anchor,Granularity granularity) {
		return anchor;
	}
	
	public Instant after(Instant anchor) {
		return after(anchor,granularity());
	}
	public Instant after(Instant anchor,Granularity granularity) {
		return anchor;
	}
}
