package timeBench.data.oo;

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
		if (relationalTemporalElement.getKind() != 2)
			throw new TemporalDataException("Cannot generate an Instant object from a temporal element that is not an instant.");
	}
}