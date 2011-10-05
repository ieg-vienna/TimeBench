package timeBench.calendar;

import java.util.ArrayList;

import timeBench.data.TemporalDataException;

/**
 * The interface used to access any CalendarManager that may exist.
 * 
 * <p>
 * Added:         2011-08-19 / TL<br>
 * Modifications: 
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public interface CalendarManager {

	public Calendar calendar();
	public Calendar getDefaultCalendar();
	public long before(long timeStamp, int granules, int granularityIdentifier) throws TemporalDataException;
	public long after(long timeStamp, int granules, int granularityIdentifier) throws TemporalDataException;
	public long mapGranuleToGranularityAsGranule(long timeStamp,int sourceGranularity, int targetGranularity) throws TemporalDataException;
	public ArrayList<Long> mapGranuleToGranularityAsGranuleList(long timeStamp,int sourceGranularity, int targetGranularity) throws TemporalDataException;
}
