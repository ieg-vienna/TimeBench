package timeBench.data.oo;

import timeBench.calendar.Granularity;
import timeBench.data.TemporalDataException;

/**
 *  This class represents a span.
 * 
 * <p>
 * Added:          2011-07-19 / TL<br>
 * Modifications: 
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public class Span extends UnanchoredTemporalElement {
	private int granules = 0;	// RELATIONAL: replace with reference to relational table
	
	protected Span(timeBench.data.relational.TemporalElement relationalTemporalElement)  throws TemporalDataException {
		super(relationalTemporalElement);
		if (relationalTemporalElement.getKind() != 0)
			throw new TemporalDataException("Cannot generate an Span object from a temporal element that is not a span.");
	}
	
	public Instant before(Instant anchor,Granularity granularity) throws TemporalDataException {
		if (granularity == null)
			granularity = getGranularity();
		
		long timeStamp = granularity.before(anchor.getChronon(),granules);
		return new Instant(timeStamp,granularity);
	}
	
	
	public Instant after(Instant anchor,Granularity granularity) throws TemporalDataException {
		if (granularity == null)
			granularity = getGranularity();
		
		long timeStamp = granularity.after(anchor.getChronon(),granules);
		return new Instant(timeStamp,granularity);
	}
}
