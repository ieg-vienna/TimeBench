package timeBench.data.oo;

import timeBench.calendar.Granularity;
import timeBench.data.TemporalDataException;

/**
 * 
 * 
 * <p>
 * Added:          / TL<br>
 * Modifications: 
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public class Span extends UnanchoredTemporalPrimitive {
	private int granules = 0;	// RELATIONAL: replace with reference to relational table
	
	public Instant before(Instant anchor,Granularity granularity) throws TemporalDataException {
		if (granularity == null)
			granularity = getGranularity();
		
		long timeStamp = granularity.before(anchor.getTimeStamp(),granules);
		return new Instant(timeStamp,granularity);
	}
	
	public Instant after(Instant anchor,Granularity granularity) throws TemporalDataException {
		if (granularity == null)
			granularity = getGranularity();
		
		long timeStamp = granularity.after(anchor.getTimeStamp(),granules);
		return new Instant(timeStamp,granularity);
	}
}
