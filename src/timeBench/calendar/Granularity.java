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
     
     
     /**
      * Return calendar this granularity belongs to.
      * @return The calendar this granularity belongs to.
      */
     public Calendar getCalendar()
     {
    	 return calendar;
     }


	/** Converts a granule in a granularity to another granularity, but returns only one granule,
	 * using heuristics to decide which one if more would be correct.
	 * @param timeStamp The number of the granule in the original granularity.
	 * @return The number of the corresponding granule in the new granularity.
	 * @throws TemporalDataException 
	 */
	public long mapGranuleToGranularityAsGranule(long timeStamp, Granularity targetGranularity) throws TemporalDataException {
		return calendar.mapGranuleToGranularityAsGranule(timeStamp,identifier,targetGranularity.getIdentifier());
	}


	/**
	 * Returns identifier of this granularity.
	 * @return Identifier of this granularity
	 */
	public int getIdentifier() {
		return identifier;
	}


	/** Converts a granule in a granularity to another granularity and returns a list of all granules that are part of
	 * it. Use heuristics if necessary.
	 * @param timeStamp The number of the granule in the original granularity.
	 * @return The list of numbers of the corresponding granules in the new granularity.
	 * @throws TemporalDataException 
	 */
	public java.util.ArrayList<Long> mapGranuleToGranularityAsGranuleList(long timeStamp,
			Granularity targetGranularity) throws TemporalDataException {
		return calendar.mapGranuleToGranularityAsGranuleList(timeStamp,identifier,targetGranularity.getIdentifier());
	}
}
