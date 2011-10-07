package timeBench.data.oo;

import timeBench.calendar.Granularity;
import timeBench.data.TemporalDataException;

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
public class Instant extends AnchoredTemporalElement {
	
	protected Instant(timeBench.data.relational.TemporalElement relationalTemporalElement) throws TemporalDataException {
		super(relationalTemporalElement);
		if (relationalTemporalElement.getKind() != timeBench.data.relational.TemporalDataset.PRIMITIVE_INSTANT)
			throw new TemporalDataException("Cannot generate an Instant object from a temporal element that is not an instant.");
	}
	
	protected Instant(long chronon) {
		super(chronon,chronon);
	}
	
	protected Instant(long chronon, Granularity granularity) {
	    // XXX this is only correct for bottom granularity (other granularity => inf < sup)   
		super(chronon,chronon,granularity);
	}
	
	public long getChronon() {
		return relationalTemporalElement == null ? infWhenDynamic : relationalTemporalElement.getInf();
	}
}