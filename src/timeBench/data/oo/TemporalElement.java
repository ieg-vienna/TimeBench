package timeBench.data.oo;

import java.util.ArrayList;
import java.util.Iterator;

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
	private Granularity granularity = null;	
	private ArrayList<TemporalElement> parts = new ArrayList<TemporalElement>();
	protected timeBench.data.relational.TemporalElement relationalTemporalElement;
		
	/**
	 * Generates a TemporalElement from a relational TemporalElement.
	 * 
	 * @param temporalElement
	 */
	public static TemporalElement createFromRelationalTemporalElement(timeBench.data.relational.TemporalElement relationalTemporalElement) {
		TemporalElement result = null;
		switch(relationalTemporalElement.getKind()) {
			case 0: 
				result = new Span(relationalTemporalElement);
				break;
			case 1: 
				if (relationalTemporalElement.isAnchored())
					result = new AnchoredTemporalElement(relationalTemporalElement);
				else
					result = new UnanchoredTemporalElement(relationalTemporalElement);
				Iterator<timeBench.data.relational.TemporalElement> iChilds = relationalTemporalElement.childElements();
				while(iChilds.hasNext())
				{
					result.parts.add(TemporalElement.createFromRelationalTemporalElement(iChilds.next()));
				}
				break;
			case 2:
				result = new Instant(relationalTemporalElement);
				break;
			case 3:
				result = new Interval(relationalTemporalElement);
		}
		
		return result;
	}
	
	protected TemporalElement(timeBench.data.relational.TemporalElement relationalTemporalElement) {
		granularity = new Granularity(JavaDateCalendarManager.getDefaultSystem().getDefaultCalendar(),relationalTemporalElement.getGranularityId());
		this.relationalTemporalElement = relationalTemporalElement;
	}

	Granularity getGranularity() {
		if (granularity == null)
			return JavaDateCalendarManager.getDefaultSystem().getDefaultCalendar().getDiscreteTimeDomain();
		else
			return granularity;
	}
}
