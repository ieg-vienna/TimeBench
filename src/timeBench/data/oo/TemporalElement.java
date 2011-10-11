package timeBench.data.oo;

import java.util.ArrayList;
import java.util.Iterator;

import timeBench.calendar.Granularity;
import timeBench.calendar.JavaDateCalendarManager;
import timeBench.data.TemporalDataException;

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
	protected ArrayList<TemporalElement> parts = new ArrayList<TemporalElement>();
	protected timeBench.data.relational.TemporalElement relationalTemporalElement = null;
		
	/**
	 * Generates a TemporalElement from a relational TemporalElement.
	 * 
	 * @param temporalElement
	 * @throws TemporalDataException 
	 */
	public static TemporalElement createFromRelationalTemporalElement(timeBench.data.relational.TemporalElement relationalTemporalElement) throws TemporalDataException {
		TemporalElement result = null;
		switch(relationalTemporalElement.getKind()) {
			case timeBench.data.relational.TemporalDataset.PRIMITIVE_SPAN: 
				result = new Span(relationalTemporalElement);
				break;
			case timeBench.data.relational.TemporalDataset.PRIMITIVE_SET: 
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
			case timeBench.data.relational.TemporalDataset.PRIMITIVE_INSTANT:
				result = new Instant(relationalTemporalElement);
				break;
			case timeBench.data.relational.TemporalDataset.PRIMITIVE_INTERVAL:
				result = new Interval(relationalTemporalElement);
		}
		
		return result;
	}
	
	/**
	 * Constructor of TemporalElement. Protected for a reason: TemporalElement objects should have a class based on their
	 * kind. The Factory can do this and the constructors of derived classes come back to this constructor.
	 * 
	 * @param relationalTemporalElement
	 */
	protected TemporalElement(timeBench.data.relational.TemporalElement relationalTemporalElement) {
		granularity = new Granularity(JavaDateCalendarManager.getSingleton().getDefaultCalendar(),relationalTemporalElement.getGranularityId());
		this.relationalTemporalElement = relationalTemporalElement;
	}
	
	protected TemporalElement(Granularity granularity) {
		this.granularity = granularity;
	}

	Granularity getGranularity() {
		if (granularity == null)
			return JavaDateCalendarManager.getSingleton().getDefaultCalendar().getDiscreteTimeDomain();
		else
			return granularity;
	}
}
