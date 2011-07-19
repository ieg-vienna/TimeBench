package timeBench.data.oo;

import java.util.ArrayList;

import timeBench.calendar.Granularity;
import timeBench.calendar.JavaDateCalendarManager;

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
	Granularity granularity = null;	
	ArrayList<TemporalPrimitive> parts = new ArrayList<TemporalPrimitive>();	// RELATIONAL: replace with reference to bipartite graph
		
	Granularity getGranularity() {
		if (granularity == null)
			return JavaDateCalendarManager.getDefaultSystem().getDefaultCalendar().discreteTimeDomain();
		else
			return granularity;
	}
}
