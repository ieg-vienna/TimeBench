package timeBench.calendar;

import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.builder.ToStringBuilder;

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
	private int identifier;
	
	/**
	 * Constructs a Calendar using a given {@link CalendarManager}.
	 * @param calendarManager the {@link CalendarManager} the new calendar maps to
	 */
	public Calendar(CalendarManager calendarManager,int identifier) {
		this.calendarManager = calendarManager;
		this.identifier = identifier;
	}	
	
	/**
	 * Provide the bottom granularity of the this calendar as {@link Granularity}.
	 * For many calendars, it consists of the chronons of the discrete time domain.
	 * @return the bottom granularity as as {@link Granularity}
	 */
	public Granularity getBottomGranularity() {
		return calendarManager.getBottomGranularity(this);
	}
	
	/**
	 * Returns the top granularity of the this calendar as {@link Granularity}.
	 * This is the highest possible granularity of the calendar.
	 * Usually, this is a granularity where one granule is composed of all the time the calendar is defined for.
	 * Let all calendars that would normally have this be modified so they have one. 
	 * @return the top granularity identifier
	 */
	public Granularity getTopGranularity() {
		return calendarManager.getTopGranularity(this);
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
            return arg0.getIdentifier();
        }

        @Override
        public Calendar unmarshal(Integer arg0) throws Exception {
            return CalendarFactory.getSingleton().getCalendar(arg0);
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
	 * 
	 */
	public int getIdentifier() {
		return identifier;
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

	/**
	 * Provide the minimum identifier value that granules of a granularity can assume.
	 * @param granularity the granularity
	 * @return the minimum granule identifier value
	 * @throws TemporalDataException thrown when granularity has illegal identifiers
	 */
	public long getMinGranuleIdentifier(Granularity granularity) throws TemporalDataException {
		return calendarManager.getMinGranuleIdentifier(granularity);
	}

	/**
	 * Provide the maximum identifier value that granules of a granularity can assume.
	 * @param granularity the granularity
	 * @return the maximum granule identifier value
	 * @throws TemporalDataException thrown when granularity has illegal identifiers
	 */
	public long getMaxGranuleIdentifier(Granularity granularity) throws TemporalDataException {
		return calendarManager.getMaxGranuleIdentifier(granularity);
	}
	
	public long getMaxLengthInIdentifiers(Granularity granularity) throws TemporalDataException {
		return calendarManager.getMaxLengthInIdentifiers(granularity);
	}

	public boolean contains(Granule granule, long chronon) throws TemporalDataException {
		return calendarManager.contains(granule,chronon);
	}
	
	public Granularity getGranularity(String granularityName,String contextGranularityName) {
		return calendarManager.getGranularity(this,granularityName,contextGranularityName);
	}

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("manager", calendarManager.getClass().getSimpleName()).
                toString();
    }

	public Granule getTopGranule() throws TemporalDataException {
		return new Granule(Long.MIN_VALUE,Long.MAX_VALUE,getTopGranularity());
	}
}
