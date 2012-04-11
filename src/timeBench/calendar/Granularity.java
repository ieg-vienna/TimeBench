package timeBench.calendar;

import java.text.ParseException;
import java.util.Date;

import timeBench.data.TemporalDataException;

/**
 * A granularity of a {@link Calendar}. Contains factories for multiple {@link Granule} instances.  
 * <p>
 * Added:         2011-07-19 / TL<br>
 * Modifications: 2012-04-11 / TL / inf, sup absolte, identifier in context
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
	 * @param granularity granularity the {@link Granularity} to which the granule belongs
	 */ 
    public Granule createGranule(Date date) throws TemporalDataException {
        return calendar.createGranule(date, this);
    }

	public Granule createGranule(long inf,long sup,int mode) throws TemporalDataException {
		return calendar.createGranule(inf,sup,mode,this);
	}
	
	public Granule[] createGranules(long inf,long sup) throws TemporalDataException {
		return this.createGranules(inf,sup,0.0);
	}

	public Granule[] createGranules(long inf,long sup,double cover) throws TemporalDataException {
		return calendar.createGranules(inf,sup,cover,this);
	}
	
	public Granule[] createGranules(Granule[] granules) throws TemporalDataException {
		return this.createGranules(granules,0.0);
	}

	public Granule[] createGranules(Granule[] granules,double cover) throws TemporalDataException {
		return calendar.createGranules(granules,cover,this);
	}
	
	public long createGranuleIdentifier(Granule granule) throws TemporalDataException {
		return calendar.createGranuleIdentifier(granule);		
	}
	
	public String createGranuleLabel(Granule granule) throws TemporalDataException {
		return calendar.createGranuleLabel(granule);		
	}

	public Long createInf(Granule granule) throws TemporalDataException {
		return calendar.createInf(granule);
	}

	public Long createSup(Granule granule) throws TemporalDataException {
		return calendar.createSup(granule);
	}
}
