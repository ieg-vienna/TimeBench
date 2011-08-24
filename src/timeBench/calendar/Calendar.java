package timeBench.calendar;

import java.util.ArrayList;

import timeBench.data.TemporalDataException;

/**
 * Currently maps calendar functionality to JavaDataCalendarManager.
 * In the future, will also be able to map to tauZaman and to more than
 * one calendar and/or calendric system.
 * 
 * <p>
 * Added:         2011-07-19 / TL<br>
 * Modifications: 
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public class Calendar {
	private CalendarManager calendarManager = null;
	
	
	/**
	 * Default constructor.
	 * @param calendarManager Link to calendarManager this calendar maps to.
	 */
	public Calendar(CalendarManager calendarManager) {
		this.calendarManager = calendarManager;
	}
	
	
	/**
	 * Provide the discrete time domain of the given calendar and calendarManager as granularity.
	 * @return The discrete time domain as granularity.
	 */
	public Granularity getDiscreteTimeDomain() {
		return new Granularity(this,0);
	}

	
	/**
	 * Calculate the timeStamp which is a number of granules in a given granularity before another timeStamp.
	 * @param timeStamp The base timeStamp.
	 * @param granules The number of granules.
	 * @param granularityIdentifier The granularityIdentifier given as integer (which might have different meaning based on the calendar and calendarManager).
	 * @return The resulting timeStamp.
	 */
	public long before(long timeStamp, int granules, int granularityIdentifier) throws TemporalDataException {
		if (calendarManager instanceof JavaDateCalendarManager)
			return ((JavaDateCalendarManager)calendarManager).before(timeStamp,granules,granularityIdentifier);
		else
			throw new TemporalDataException("Unknown or missing calendar manager");		
	}
	

	/**
	 * Calculate the timeStamp which is a number of granules in a given granularity after another timeStamp.
	 * @param timeStamp The base timeStamp.
	 * @param granules The number of granules.
	 * @param granularityIdentifier The granularityIdentifier given as integer (which might have different meaning based on the calendar and calendarManager).
	 * @return The resulting timeStamp.
	 */
	public long after(long timeStamp, int granules, int granularityIdentifier) throws TemporalDataException {
		if (calendarManager instanceof JavaDateCalendarManager)
			return ((JavaDateCalendarManager)calendarManager).after(timeStamp,granules,granularityIdentifier);
		else
			throw new TemporalDataException("Unknown or missing calendar manager");		
	}


	/**
	 * Converts a granule in a granularity to another granularity, but returns only
	 * one granule, using heuristics to decide which one if more would be correct.
	 * @param timeStamp The number of the granule in the original granularity.
	 * @param sourceGranularity Identifier of the source granularity
	 * @param targetGranularity Identifier of the target granularity
	 * @return The number of the corresponding granule in the new granularity.
	 * @throws TemporalDataException 
	 */
	public long mapGranuleToGranularityAsGranule(long timeStamp,
			int sourceGranularity, int targetGranularity) throws TemporalDataException {
		return calendarManager.mapGranuleToGranularityAsGranule(timeStamp,
				sourceGranularity,targetGranularity);
	}


	/**
	 *  Converts a granule in a granularity to another granularity and returns a list of all granules that are part of
	 * it. Use heuristics if necessary.
	 * @param timeStamp The number of the granule in the original granularity.
	 * @param sourceGranularity Identifier of the source granularity
	 * @param targetGranularity Identifier of the target granularity
	 * @return The list of numbers of the corresponding granules in the new granularity.
	 * @throws TemporalDataException 
	 */
	public ArrayList<Long> mapGranuleToGranularityAsGranuleList(long timeStamp,
			int sourceGranularity, int targetGranularity) throws TemporalDataException {
		return calendarManager.mapGranuleToGranularityAsGranuleList(timeStamp,
				sourceGranularity,targetGranularity);
	}
}
