package timeBench.data.oo;

import java.util.ArrayList;

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
	
	/**
    * Calculates, on chronon level, the first (earliest) instant that is part of the temporal primitive, or, in case it starts with
    * an unanchored temporal primitive, the instant resulting from deducing that from the first instant.
    * @return The first instant.
    * @throws TemporalDataException
    */
	public Instant sup() throws TemporalDataException {
		Granularity discreteTimeDomain = granularity.getCalendar().getDiscreteTimeDomain();
		ArrayList<Long> granuleList = granularity.mapGranuleToGranularityAsGranuleList(timeStamp,discreteTimeDomain);  
		return new Instant(granuleList.get(0),discreteTimeDomain);
	}

	/**
	 * Calculates, on chronon level, the last (latest) instant that is part of the temporal primitive, or, in case it starts with
	 * an unanchored temporal primitive, the instant resulting from deducing that from the first instant.
	 * @return The first instant.
	 * @throws TemporalDataException
	 */
	public Instant inf() throws TemporalDataException {
		Granularity discreteTimeDomain = granularity.getCalendar().getDiscreteTimeDomain();
		ArrayList<Long> granuleList = granularity.mapGranuleToGranularityAsGranuleList(timeStamp,discreteTimeDomain);  
		return new Instant(granuleList.get(granuleList.size()-1),discreteTimeDomain);
		}	
}
