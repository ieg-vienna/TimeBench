package timeBench.calendar;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import timeBench.data.TemporalDataException;

/**
 * Currently maps calendar functionality to CalendarManager.
 * In the future, will be able to map to more than
 * one calendar and/or calendric system.
 * 
 * <p>
 * Added:         2011-07-19 / TL<br>
 * Modifications: 2012-04-11 / TL / inf, sup absolute, identifier in context, not JavaDate-only
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
@XmlJavaTypeAdapter(Calendar.CalendarAdapter.class) 
public class Calendar {
	private CalendarManager calendarManager = null;	
	
	/**
	 * Constructs a Calendar using a given {@link CalendarManager}.
	 * @param calendarManager the {@link CalendarManager} the new calendar maps to
	 */
	public Calendar(CalendarManager calendarManager) {
		this.calendarManager = calendarManager;
	}	
	
	/**
	 * Provide the discrete time domain of the this calendar as {@link Granularity}.
	 * @return the discrete time domain as as {@link Granularity}
	 */
	public Granularity getDiscreteTimeDomain() {
		return new Granularity(this,calendarManager.getBottomGranularityIdentifier(),calendarManager.getTopGranularityIdentifier());
	}
    
	/**
	 * Provide the granularity identifiers used by this calendar.
	 * @return granularity identifiers as {@link Array} of integers
	 */
	public int[] getGranularityIdentifiers() {
		return calendarManager.getGranularityIdentifiers();
	}
	
	public Granularity getGranularityFromIdentifier(int identifier,int contextIdentifier) {
		return new Granularity(this,identifier,contextIdentifier);
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

	public Granule createGranule(Date input, Granularity granularity) throws TemporalDataException {
		return calendarManager.createGranule(input,granularity);
	}

	public Granule createGranule(long inf, long sup, int mode,
			Granularity granularity) throws TemporalDataException {
		return calendarManager.createGranule(inf,sup,mode, granularity);
	}

	public Granule[] createGranules(long inf, long sup, double cover,
			Granularity granularity) throws TemporalDataException {
		return calendarManager.createGranules(inf,sup,cover,granularity);
	}

	public Granule[] createGranules(Granule[] granules, double cover,
			Granularity granularity) throws TemporalDataException {
		return calendarManager.createGranules(granules,cover, granularity);
	}

	public long createGranuleIdentifier(Granule granule) throws TemporalDataException {
		return calendarManager.createGranuleIdentifier(granule);
	}

	public String createGranuleLabel(Granule granule) throws TemporalDataException {
		return calendarManager.createGranuleLabel(granule);
	}

	public long createInf(Granule granule) throws TemporalDataException {
		return calendarManager.createInf(granule);
	}

	public long createSup(Granule granule) throws TemporalDataException {
		return calendarManager.createSup(granule);
	}
}
