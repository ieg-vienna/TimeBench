package timeBench.data.oo;

import timeBench.calendar.Granularity;
import timeBench.calendar.Granule;
import timeBench.calendar.JavaDateCalendarManager;
import timeBench.data.TemporalDataException;

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
public class Interval extends AnchoredTemporalElement {
	protected Interval(timeBench.data.relational.TemporalElement relationalTemporalElement) throws TemporalDataException  {
		super(relationalTemporalElement);
		if (relationalTemporalElement.getKind() != timeBench.data.relational.TemporalDataset.PRIMITIVE_INTERVAL)
			throw new TemporalDataException("Cannot generate an Interval object from a temporal element that is not an interval.");	}
	
	protected Interval(long inf,long sup) throws TemporalDataException {
		this(inf,sup,JavaDateCalendarManager.getSingleton().getDefaultCalendar().getDiscreteTimeDomain());
	}
	
	public Interval(long inf, long sup, Granularity granularity) throws TemporalDataException {
		super(inf,sup,granularity);
		parts.add(new Instant(new Granule(inf,inf,granularity)));
		parts.add(new Instant(new Granule(sup,sup,granularity)));
	}

	protected Interval(Instant inf, Instant sup) {
		this(inf,sup,JavaDateCalendarManager.getSingleton().getDefaultCalendar().getDiscreteTimeDomain());
	}
	
	protected Interval(Instant inf, Instant sup, Granularity granularity) {
		super(inf.getInf(),sup.getSup(),granularity);
		parts.add(inf);
		parts.add(sup);
	}
}
