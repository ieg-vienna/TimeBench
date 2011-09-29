package timeBench.data.oo;

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
		if (relationalTemporalElement.getKind() != 3)
			throw new TemporalDataException("Cannot generate an Interval object from a temporal element that is not an interval.");	}
}
