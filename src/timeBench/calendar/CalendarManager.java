package timeBench.calendar;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import timeBench.data.TemporalDataException;

/**
 * The interface used to access any CalendarManager that may exist.
 * 
 * <p>
 * Added:         2011-08-19 / TL<br>
 * Modifications: 2012-04-12 / TL / inf, sup absolute, identifier in context
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public interface CalendarManager {
	
	/**
	 * Generates an instance of a calendar, only one can currently be provided.
	 * @return The calendar.
	 */
	public Calendar calendar();
	
	/**
	 * Provides access to a singleton instance of a calendar, the only one that can currently be provided.
	 * It does only create one instance and provides that one with every call.
	 * @return The calendar.
	 */
	public Calendar getDefaultCalendar();    
	
	/**
	 * Returns an {@link Array} of granularity identifiers that are provided by the calendar.
	 * @return {@link Array} of granularity identifiers
	 */
	public int[] getGranularityIdentifiers();
	
	/**
	 * Returns the identifier of the bottom granularity
	 * @return the bottom granularity identifier
	 */
	public int getBottomGranularityIdentifier();
	
	/**
	 * Returns the identifier of the top granularity.  This is the highest possible granularity of the calendar.
	 * Usually, this is a granularity where one granule is composed of all the time the calendar is defined for.
	 * Let all calendars that would normally have this be modified so they have one.
	 * @return the top granularity identifier
	 */
	public int getTopGranularityIdentifier();

	/**
	 * Constructs a {@link Granule} from a given {@link Date}. Consider using the adequate constructor of
	 * {@link Granule} instead.
	 * @param date the {@link Date} used to generate the granule
	 * @param granularity granularity the {@link Granularity} to which the granule belongs
	 * @return the constructed {@link Granule}
	 * @throws TemporalDataException TemporalDataException thrown when granularities are not fully implemented
	 */ 
	public Granule createGranule(Date input, Granularity granularity) throws TemporalDataException;
	
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
    public Granule createGranule(long inf, long sup, int mode,
			Granularity granularity) throws TemporalDataException;
    
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
	public Granule[] createGranules(long inf, long sup, double cover,
			Granularity granularity) throws TemporalDataException;
	
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
	public Granule[] createGranules(Granule[] granules, double cover,
			Granularity granularity) throws TemporalDataException;
	
	/**
	 * Calculate and return the identifier of a {@link Granule}. An identifier is a numeric label given in the context
	 * of the {@link Granularity}. Consider using the adequate method of
	 * {@link Granule} instead.
	 * @return the identifier
	 * @throws TemporalDataException thrown when granularities are not fully implemented
	 */
	public long createGranuleIdentifier(Granule granule) throws TemporalDataException;
	
	/**
	 * Calculate and return the human readable label of a {@link Granule}.
	 * Consider using the adequate method of
	 * {@link Granule} instead.
	 * @return the label
	 * @throws TemporalDataException thrown when granularities are not fully implemented
	 */
	public String createGranuleLabel(Granule granule) throws TemporalDataException;
	
	/**
	 * Calculate and return the inf of a {@link Granule}.
	 * @return the inf
	 * @throws TemporalDataException thrown when granularities are not fully implemented
	 */
	public long createInf(Granule granule) throws TemporalDataException;
	
	/**
	 * Calculate and return the sup of a {@link Granule}.
	 * @return the sup
	 * @throws TemporalDataException thrown when granularities are not fully implemented
	 */
	public long createSup(Granule granule) throws TemporalDataException;

	/**
	 * Provide the minimum identifier value that granules of a granularity can assume.
	 * @param granularity the granularity
	 * @return the minimum granule identifier value
	 * @throws TemporalDataException  thrown when granularity has illegal identifiers
	 */
	public long getMinGranuleIdentifier(Granularity granularity) throws TemporalDataException;

	/**
	 * Provide the maximum identifier value that granules of a granularity can assume.
	 * @param granularity the granularity
	 * @return the maximum granule identifier value
	 * @throws TemporalDataException thrown when granularity has illegal identifiers
	 */
	public long getMaxGranuleIdentifier(Granularity granularity) throws TemporalDataException;

	public boolean contains(Granule granule, long chronon) throws TemporalDataException;

	public long getStartOfTime();
	
	public long getEndOfTime();
}