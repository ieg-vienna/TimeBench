package timeBench.data.oo;

import timeBench.calendar.Granularity;

/**
 * The class for an indeterminate instant. This is a stub, users have to work
 * manually at the moment
 * 
 * <p>
 * Added:         2011-07-19 / TL<br>
 * Modifications: 
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public class IndeterminateInstant extends Interval {
	protected IndeterminateInstant(long inf,long sup) {
		super(inf,sup);
	}
	
	protected IndeterminateInstant(long inf, long sup, Granularity granularity) {
		super(inf,sup,granularity);
	}

	protected IndeterminateInstant(Instant inf, Instant sup) {
		super(inf,sup);
	}
	
	protected IndeterminateInstant(Instant inf, Instant sup, Granularity granularity) {
		super(inf,sup,granularity);
	}
}
