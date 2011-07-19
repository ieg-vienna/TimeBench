package timeBench.data.oo;

import timeBench.calendar.Granularity;

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
public class Instant extends AnchoredTemporalPrimitive {
	private long timeStamp;	// RELATIONAL: replace with reference to relational table
	
	public Instant(long timeStamp,Granularity granularity) {
		this.timeStamp = timeStamp;
		this.granularity = granularity;
	}
	
	/**
	 * @return
	 */
	public long getTimeStamp() {
		return timeStamp;
	}

}
