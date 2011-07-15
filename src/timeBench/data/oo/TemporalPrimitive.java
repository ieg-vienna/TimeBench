package timeBench.data.oo;

import java.util.ArrayList;

/**
 * TemporalPrimitive is the base class for Temporal Primitives.
 * (Instant, Interval, Indeterminate Instant, Indeterminate Interval,
 * and Span).
 * 
 * <p>
 * Added:         2011-07-13 / TL<br>
 * Modifications:
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public class TemporalPrimitive {
	ArrayList<TemporalPrimitive> parts = new ArrayList<TemporalPrimitive>();
}
