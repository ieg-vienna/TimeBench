package timeBench.data.oo;

import timeBench.calendar.Granularity;

/**
 *  This class represents an interval. Currently, it is part stub.
 * 
 * <p>
 * Added:         2011-07-19 / TL<br>
 * Modifications: 
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public class Interval extends AnchoredTemporalPrimitive {
	/**
	 * The constructor for forming an interval of two instants.
	 * @param start The start instant.
	 * @param stop The stop instant.
	 */
	public Interval(Instant start,Instant stop,Granularity granularity) {
		parts.add(start);
		parts.add(stop);
		this.granularity = granularity;
	}
}
