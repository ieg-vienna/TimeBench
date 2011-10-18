package timeBench.calendar;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
@XmlJavaTypeAdapter(Calendar.CalendarAdapter.class) 
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
	public Granule mapGranuleToGranularityAsGranule(long timeStamp,
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
	public ArrayList<Granule> mapGranuleToGranularityAsGranuleList(long timeStamp,
			int sourceGranularity, int targetGranularity) throws TemporalDataException {
		return calendarManager.mapGranuleToGranularityAsGranuleList(timeStamp,
				sourceGranularity,targetGranularity);
	}


//	/**
//	 * @param input
//	 * @param identifier
//	 * @return
//	 * @throws ParseException 
//	 * @throws TemporalDataException 
//	 */
//	public Granule parseStringToGranule(String input, Granularity granularity) throws ParseException, TemporalDataException {
//		return calendarManager.parseStringToGranule(input,granularity);
//	}
//
//    public Granule parseStringToGranule(String input, Granularity granularity,
//            String dateTimePattern) throws ParseException,
//            TemporalDataException {
//        return calendarManager.parseStringToGranule(input, granularity,
//                dateTimePattern);
//    }

    public Granule parseDateToGranule(Date input, Granularity granularity)
            throws TemporalDataException {
        return calendarManager.parseDateToGranule(input, granularity);
    }
    
	public int[] getGranularityIdentifiers() {
		return calendarManager.getGranularityIdentifiers();
	}
	
	public Granularity getGranularityFromIdentifier(int identifier) {
		return new Granularity(this,identifier);
	}


	public Granule parseInfToGranule(long inf,Granularity granularity) throws TemporalDataException {
		return calendarManager.parseInfToGranule(inf,granularity);
	}
	
    static class CalendarAdapter extends XmlAdapter<Integer, Calendar> {
        // TODO think of a better way to marshal calendars 
        // TODO handle different calendars of a calendar manager

        @Override
        public Integer marshal(Calendar arg0) throws Exception {
            // TODO get Integer value of a calendar manager for XML marshaling
            return 0; // arg0.calendarManager;
        }

        @Override
        public Calendar unmarshal(Integer arg0) throws Exception {
            return CalendarManagerFactory.getSingleton(CalendarManagers.fromInt(arg0)).getDefaultCalendar();
        }
        
    }
}
