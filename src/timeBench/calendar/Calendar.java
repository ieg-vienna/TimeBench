package timeBench.calendar;

import timeBench.data.TemporalDataException;

/**
 * 
 * 
 * <p>
 * Added:          / TL<br>
 * Modifications: 
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public class Calendar {
	Object calendarManager = null;
	
	public Calendar(Object calendarManager) {
		this.calendarManager = calendarManager;
	}
	
	public Granularity discreteTimeDomain() {
		return new Granularity(this,0);
	}

	/**
	 * @param timeStamp
	 * @param granules
	 * @param identifier
	 * @return
	 */
	public long before(long timeStamp, int granules, int identifier) throws TemporalDataException {
		if (calendarManager instanceof JavaDateCalendarManager)
			return ((JavaDateCalendarManager)calendarManager).before(timeStamp,granules,identifier);
		else
			throw new TemporalDataException("Unknown or missing calendar manager");		
	}
	

	/**
	 * @param timeStamp
	 * @param granules
	 * @param identifier
	 * @return
	 */
	public long after(long timeStamp, int granules, int identifier) throws TemporalDataException {
		if (calendarManager instanceof JavaDateCalendarManager)
			return ((JavaDateCalendarManager)calendarManager).after(timeStamp,granules,identifier);
		else
			throw new TemporalDataException("Unknown or missing calendar manager");		
	}
}
