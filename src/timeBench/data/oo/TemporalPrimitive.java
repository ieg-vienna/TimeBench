package timeBench.data.oo;

import java.util.ArrayList;

import timeBench.calendar.Granularity;
import timeBench.calendar.JavaDateSystem;

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
	Granularity givenGranularity = null;	
	ArrayList<TemporalPrimitive> parts = new ArrayList<TemporalPrimitive>();
	
	Granularity granularity() {
		if (givenGranularity == null)
			return JavaDateSystem.getDefaultSystem().getDefaultCalendar().discreteTimeDomain();
		else
			return givenGranularity;
	}
}
