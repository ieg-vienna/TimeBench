package timeBench.calendar;

import java.text.ParseException;
import java.util.Date;

import timeBench.data.TemporalDataException;

/**
 * A granularity of a {@link Calendar}. Contains factories for multiple {@link Granule} instances.  
 * <p>
 * Added:         2011-07-19 / TL<br>
 * Modifications: 2012-04-11 / TL / inf, sup absolute, identifier in context
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public class Granularity {
	private Calendar calendar = null;
	private int identifier;
	private int contextIdentifier;
		
	/**
	 * Constructs a Granularity using a given {@link Calendar}, with identifiers for granularity
	 * and context given as integers from the {@link CalendarManager}.
	 * @param calendar The {@link Calendar} the granularity belongs to.
	 * @param identifier The identifier of the granularity whose meaning depends on the {@link CalendarManager}.
	 * @param contextIdentifier The context identifier of the granularity whose meaning depends on the {@link CalendarManager}.
	 */
	public Granularity(Calendar calendar, int identifier, int contextIdentifier){
		this.calendar = calendar;
		this.identifier = identifier;
		this.contextIdentifier = contextIdentifier;
	}
     
     /**
      * Return {@link Calendar} this granularity belongs to.
      * @return the {@link Calendar} this granularity belongs to
      */
     public Calendar getCalendar()
     {
    	 return calendar;
     }

	/**
	 * Returns identifier of this granularity whose meaning depends on the {@link CalendarManager}.
	 * @return identifier of this granularity whose meaning depends on the {@link CalendarManager}
	 */
	public int getIdentifier() {
		return identifier;
	}

	/**
	 * Returns context identifier of this granularity whose meaning depends on the {@link CalendarManager}.
	 * @return context identifier of this granularity whose meaning depends on the {@link CalendarManager}
	 */
	public int getGranularityContextIdentifier() {
		return contextIdentifier;
	}
	
	/**
	 * Constructs a {@link Granule} from a given {@link Date}. Consider using the adequate constructor of
	 * {@link Granule} instead.
	 * @param date the {@link Date} used to generate the granule
	 * @return the constructed {@link Granule}
	 * @throws TemporalDataException TemporalDataException thrown when granularities are not fully implemented
	 */ 
    public Granule createGranule(Date date) throws TemporalDataException {
        return calendar.createGranule(date, this);
    }

	/**
	 * Constructs a {@link Granule} from inf to sup using a given {@linkplain Granule#MODE_INF_GANULE mode}.
	 * Consider using the adequate constructor of
	 * {@link Granule} instead.
	 * @param inf the chronon that determines the start of the granule constructed
	 * @param sup the chronon that determines the end of the granule constructed
	 * @param mode the {@linkplain Granule#MODE_INF_GANULE mode} used to construct the granule
	 * @return the constructed {@link Granule}
	 * @throws TemporalDataException TemporalDataException thrown when granularities are not fully implemented
	 */
    public Granule createGranule(long inf,long sup,int mode) throws TemporalDataException {
		return calendar.createGranule(inf,sup,mode,this);
	}
	
	/**
	 * Constructs several {@link Granule} objects from inf to sup that are at least partly in the given interval.
	 * @param inf the chronon that determines the start of the {@link Granule} range constructed
	 * @param sup the chronon that determines the end of the {@link Granule} range constructed
	 * @return the constructed {@link Array} of {@link Granule}
	 * @throws TemporalDataException TemporalDataException thrown when granularities are not fully implemented
	 */
    public Granule[] createGranules(long inf,long sup) throws TemporalDataException {
		return this.createGranules(inf,sup,0.0);
	}

	/**
	 * Constructs several {@link Granule} objects from inf to sup that are at least partly in the given interval with
	 * a coverage of a least a given fraction.
	 * @param inf the chronon that determines the start of the {@link Granule} range constructed
	 * @param sup the chronon that determines the end of the {@link Granule} range constructed
	 * @param cover the coverage fraction of a granule needed to be included in the result
	 * @return the constructed {@link Array} of {@link Granule}
	 * @throws TemporalDataException TemporalDataException thrown when granularities are not fully implemented
	 */
    public Granule[] createGranules(long inf,long sup,double cover) throws TemporalDataException {
		return calendar.createGranules(inf,sup,cover,this);
	}
	
	/**
	 * Constructs several {@link Granule} objects from other {@link Granule} objects that can (and most likely
	 * will) be in a different granularity. All {@link Granule} that are at least partly covered are
	 * returned.
	 * @param Granule[] the {@link Array} of {@link Granule} used as source
	 * @return the constructed {@link Array} of {@link Granule}
	 * @throws TemporalDataException TemporalDataException thrown when granularities are not fully implemented
	 */
    public Granule[] createGranules(Granule[] granules) throws TemporalDataException {
		return this.createGranules(granules,0.0);
	}

	/**
	 * Constructs several {@link Granule} objects from other {@link Granule} objects that can (and most likely
	 * will) be in a different granularity. All {@link Granule} with
	 * a coverage of a least a given fraction are returned.
	 * @param Granule[] the {@link Array} of {@link Granule} used as source
	 * @param cover the coverage fraction of a granule needed to be included in the result
	 * @return the constructed {@link Array} of {@link Granule}
	 * @throws TemporalDataException TemporalDataException thrown when granularities are not fully implemented
	 */
    public Granule[] createGranules(Granule[] granules,double cover) throws TemporalDataException {
		return calendar.createGranules(granules,cover,this);
	}
	
	/**
	 * Calculate and return the identifier of a {@link Granule}. An identifier is a numeric label given in the context
	 * of the granularity. Consider using the adequate method of
	 * {@link Granule} instead.
	 * @return the identifier
	 * @throws TemporalDataException thrown when granularities are not fully implemented
	 */
    public long createGranuleIdentifier(Granule granule) throws TemporalDataException {
		return calendar.createGranuleIdentifier(granule);		
	}
	
	/**
	 * Calculate and return the human readable label of a {@link Granule}.
	 * Consider using the adequate method of
	 * {@link Granule} instead.
	 * @return the label
	 * @throws TemporalDataException thrown when granularities are not fully implemented
	 */
    public String createGranuleLabel(Granule granule) throws TemporalDataException {
		return calendar.createGranuleLabel(granule);		
	}

	/**
	 * Calculate and return the inf of a {@link Granule}.
	 * @return the inf
	 * @throws TemporalDataException thrown when granularities are not fully implemented
	 */
    public Long createInf(Granule granule) throws TemporalDataException {
		return calendar.createInf(granule);
	}

	/**
	 * Calculate and return the sup of a {@link Granule}.
	 * @return the sup
	 * @throws TemporalDataException thrown when granularities are not fully implemented
	 */
    public Long createSup(Granule granule) throws TemporalDataException {
		return calendar.createSup(granule);
	}
}
