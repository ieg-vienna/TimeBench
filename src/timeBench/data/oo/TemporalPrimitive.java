package timeBench.data.oo;

import java.util.ArrayList;

import timeBench.calendar.Granularity;
import timeBench.calendar.JavaDateCalendarManager;

/**
 * TemporalPrimitive is the base class for Temporal Primitives.
 * (Instant, Interval, Indeterminate Instant, Indeterminate Interval,
 * and Span).
 * 
 * While a Temporal Primitive does not need to be a Temporal Element,
 * in our code, it is derived from it as Temporal Primitives are only
 * used as Temporal Elements here. 
 * 
 * <p>
 * Added:         2011-07-13 / TL<br>
 * Modifications:
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public class TemporalPrimitive extends TemporalElement {
}
