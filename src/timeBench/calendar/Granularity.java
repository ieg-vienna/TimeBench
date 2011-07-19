package timeBench.calendar;

import timeBench.data.TemporalDataException;

/**
 * A granularity of a calendar. 
 * <p>
 * Added:         2011-07-19 / TL<br>
 * Modifications: 
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public class Granularity {
	private Calendar calendar = null;
	private int identifier;	
	
	
	/**
	 * The default constructor.
	 * @param calendar The calendar the granularity belongs to.
	 * @param identifier The identifier of the granularity whose meaning depends on the calendar.
	 */
	Granularity(Calendar calendar, int identifier){
		this.calendar = calendar;
		this.identifier = identifier;
	}
	
	
    /**
     * Calculate a timeStamp a given number of granules before another timeStamp.
     * @param timeStamp The base timeStamp.
     * @param granules The number of granules.
     * @return The resulting timeStamp.
     * @throws TemporalDataException
     */
	public long before(long timeStamp,int granules) throws TemporalDataException
	{
		return calendar.before(timeStamp,granules,identifier);
	}

	
    /**
     * Calculate a timeStamp a given number of granules after another timeStamp.
     * @param timeStamp The base timeStamp.
     * @param granules The number of granules.
     * @return The resulting timeStamp.
     * @throws TemporalDataException
     */	public long after(long timeStamp,int granules) throws TemporalDataException
	{
		return calendar.after(timeStamp,granules,identifier);
	}
}
