package timeBench.data.oo;

import java.util.ArrayList;

import timeBench.calendar.Granularity;
import timeBench.calendar.JavaDateCalendarManager;

/**
 * Class for Temporal Element.
 * 
 * Temporal Element is 
 * a finite union of anchored primitives (Instant, Interval,
 * Indeterminate Instant, and Indeterminate Interval).
 * 
 * <p>
 * Added:         2011-07-13 / TL<br>
 * Modifications:
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public class TemporalElement {
	Granularity granularity = null;	
	ArrayList<TemporalElement> parts = new ArrayList<TemporalElement>();
	Object relationalTemporalElement; // TODO replace with class for relational temporal element
		
	Granularity getGranularity() {
		if (granularity == null)
			return JavaDateCalendarManager.getDefaultSystem().getDefaultCalendar().getDiscreteTimeDomain();
		else
			return granularity;
	}
}
