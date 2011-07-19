package timeBench.data.oo;

import timeBench.calendar.Granularity;

/**
 * This class represents an instant. It currently saves the time itself, future versions will
 * save a reference to the relational data model.
 * 
 * <p>
 * Added:         2011-07-19 / TL<br>
 * Modifications: 
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public class Instant extends AnchoredTemporalPrimitive {
	private long timeStamp;	// RELATIONAL: replace with reference to relational table
	
	
	/**
	 * The default constructor.
	 * @param timeStamp The timestamp.
	 * @param granularity The granularity on which the instant is generated. It does not influence the timestamp
	 * itself (which is given in chronons), but the way operations work when not granularity is given.
	 */
	public Instant(long timeStamp,Granularity granularity) {
		this.timeStamp = timeStamp;
		this.granularity = granularity;
	}
	
	/**
	 * @return Gets the timestamp of the instant in chronons.
	 */
	public long getTimeStamp() {
		return timeStamp;
	}

}
