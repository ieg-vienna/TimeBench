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
	 * Provide the bottom granularity of the this calendar as {@link Granularity}.
	 * For many calendars, it consists of the chronons of the discrete time domain.
	 * @return the bottom granularity as as {@link Granularity}
	 */
	public Granularity getBottomGranularity() {
		return new Granularity(this,calendarManager.getBottomGranularityIdentifier(),calendarManager.getTopGranularityIdentifier());
	}
    
	/**
	 * Provide the granularity identifiers used by this calendar.
	 * @return granularity identifiers as {@link Array} of integers
	 */
	public int[] getGranularityIdentifiers() {
		return calendarManager.getGranularityIdentifiers();
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

	/**
	 * Constructs a {@link Granule} from a given {@link Date}. Consider using the adequate constructor of
	 * {@link Granule} instead.
	 * @param date the {@link Date} used to generate the granule
	 * @param granularity granularity the {@link Granularity} to which the granule belongs
	 * @return the constructed {@link Granule}
	 * @throws TemporalDataException TemporalDataException thrown when granularities are not fully implemented
	 */ 
    protected Granule createGranule(Date date, Granularity granularity) throws TemporalDataException {
		return calendarManager.createGranule(date,granularity);
	}

	/**
	 * Constructs a {@link Granule} from inf to sup using a given {@linkplain Granule#MODE_INF_GANULE mode} and
	 * for a given {@link Granularity}.
	 * Consider using the adequate constructor of {@link Granule} instead.
	 * @param inf the chronon that determines the start of the granule constructed
	 * @param sup the chronon that determines the end of the granule constructed
	 * @param mode the {@linkplain Granule#MODE_INF_GANULE mode} used to construct the granule
	 * @param granularity the {@link Granularity} to use
	 * @return the constructed {@link Granule}
	 * @throws TemporalDataException TemporalDataException thrown when granularities are not fully implemented
	 */
    protected Granule createGranule(long inf, long sup, int mode,
			Granularity granularity) throws TemporalDataException {
		return calendarManager.createGranule(inf,sup,mode, granularity);
	}

	/**
	 * Constructs several {@link Granule} objects from inf to sup that are at least partly in the given interval with
	 * a coverage of a least a given fraction and
	 * for a given {@link Granularity}. Consider using the adequate factory of {@link Granularity} instead.
	 * @param inf the chronon that determines the start of the {@link Granule} range constructed
	 * @param sup the chronon that determines the end of the {@link Granule} range constructed
	 * @param cover the coverage fraction of a granule needed to be included in the result
	 * @param granularity the {@link Granularity} to use
	 * @return the constructed {@link Array} of {@link Granule}
	 * @throws TemporalDataException TemporalDataException thrown when granularities are not fully implemented
	 */
    protected Granule[] createGranules(long inf, long sup, double cover,
			Granularity granularity) throws TemporalDataException {
		return calendarManager.createGranules(inf,sup,cover,granularity);
	}

	/**
	 * Constructs several {@link Granule} objects from other {@link Granule} objects for a given {@link Granularity}
	 * that can (and most likely
	 * will) be in a different {@link Granularity}. All {@link Granule} with
	 * a coverage of a least a given fraction are returned.
	 * Consider using the adequate factory of {@link Granularity} instead.
	 * @param Granule[] the {@link Array} of {@link Granule} used as source
	 * @param cover the coverage fraction of a granule needed to be included in the result
	 * @param granularity the {@link Granularity} to use
	 * @return the constructed {@link Array} of {@link Granule}
	 * @throws TemporalDataException TemporalDataException thrown when granularities are not fully implemented
	 */
    protected Granule[] createGranules(Granule[] granules, double cover,
			Granularity granularity) throws TemporalDataException {
		return calendarManager.createGranules(granules,cover, granularity);
	}

	/**
	 * Calculate and return the identifier of a {@link Granule}. An identifier is a numeric label given in the context
	 * of the {@link Granularity}. Consider using the adequate method of
	 * {@link Granule} instead.
	 * @return the identifier
	 * @throws TemporalDataException thrown when granularities are not fully implemented
	 */
    protected long createGranuleIdentifier(Granule granule) throws TemporalDataException {
		return calendarManager.createGranuleIdentifier(granule);
	}

	/**
	 * Calculate and return the human readable label of a {@link Granule}.
	 * Consider using the adequate method of
	 * {@link Granule} instead.
	 * @return the label
	 * @throws TemporalDataException thrown when granularities are not fully implemented
	 */
    protected String createGranuleLabel(Granule granule) throws TemporalDataException {
		return calendarManager.createGranuleLabel(granule);
	}

	/**
	 * Calculate and return the inf of a {@link Granule}.
	 * @return the inf
	 * @throws TemporalDataException thrown when granularities are not fully implemented
	 */
    protected long createInf(Granule granule) throws TemporalDataException {
		return calendarManager.createInf(granule);
	}

	/**
	 * Calculate and return the sup of a {@link Granule}.
	 * @return the sup
	 * @throws TemporalDataException thrown when granularities are not fully implemented
	 */
    protected long createSup(Granule granule) throws TemporalDataException {
		return calendarManager.createSup(granule);
	}
}
