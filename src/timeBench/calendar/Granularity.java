package timeBench.calendar;

import java.text.ParseException;
import java.util.Date;

import timeBench.data.TemporalDataException;

/**
 * A granularity of a calendar. 
 * <p>
 * Added:         2011-07-19 / TL<br>
 * Modifications: 
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
	 * The default constructor.
	 * @param calendar The calendar the granularity belongs to.
	 * @param identifier The identifier of the granularity whose meaning depends on the calendar.
	 */
	public Granularity(Calendar calendar, int identifier, int contextIdentifier){
		this.calendar = calendar;
		this.identifier = identifier;
		this.contextIdentifier = contextIdentifier;
	}
		
     
     /**
      * Return calendar this granularity belongs to.
      * @return The calendar this granularity belongs to.
      */
     public Calendar getCalendar()
     {
    	 return calendar;
     }

	/**
	 * Returns identifier of this granularity.
	 * @return Identifier of this granularity
	 */
	public int getIdentifier() {
		return identifier;
	}

	public int getGranularityContextIdentifier() {
		return contextIdentifier;
	}
	
 
    public Granule createGranule(Date input) throws TemporalDataException {
        return calendar.createGranule(input, this);
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
