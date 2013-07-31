package timeBench.calendar;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import timeBench.data.TemporalDataException;

/**
 * The calendarManager which maps calendar functionality to the Java Calendar class.
 * 
 * <p>
 * Added:          2011-07-19 / TL<br>
 * Modifications: 
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public class JavaDateCalendarManager implements CalendarManager {
	protected static JavaDateCalendarManager singleton = null;
	protected Calendar defaultCalendar = null;
	protected java.util.Calendar javaCalendar = null;
	
	/**
	 * Return the identifier of this version of the JavaDataCalendarManager
	 * Format:
	 * 		                  MMMMM VVVVVVVV
	 *              CalendarManager  Version
	 *      JavaDataCalendarManager    Count
	 *      
	 * Do not use three-part package version directly, instead increase by 1 for every new version.
	 * 
	 * @return the identifier
	 */
	public static int getIdentifier() {
		return 1;
	}
	
	/**
	 * Constructs a JavaDateCalendarManager. Consider using the
	 * {@linkplain JavaDateCalendarManager#getDefaultCalendar() singleton} instead.
	 */
	public JavaDateCalendarManager() {
		javaCalendar = java.util.Calendar.getInstance();
		javaCalendar.setLenient(true);
	}	
	
	
	/**
	 * The Identifier of Millisecond in the JavaDateCalendarManager Gregorian Calendar
	 */
	private static final int GRANULARITY_MILLISECOND = 0;
	/**
	 * The Identifier of Second in the JavaDateCalendarManager Gregorian Calendar
	 */
	private static final int GRANULARITY_SECOND = 1;
	/**
	 * The Identifier of Minute in the JavaDateCalendarManager Gregorian Calendar
	 */
	private static final int GRANULARITY_MINUTE = 2;
	/**
	 * The Identifier of Hour in the JavaDateCalendarManager Gregorian Calendar
	 */
	private static final int GRANULARITY_HOUR = 3;
	/**
	 * The Identifier of Day in the JavaDateCalendarManager Gregorian Calendar
	 */
	private static final int GRANULARITY_DAY = 4;
	/**
	 * The Identifier of Week in the JavaDateCalendarManager Gregorian Calendar
	 */
	private static final int GRANULARITY_WEEK = 5;
	/**
	 * The Identifier of Month in the JavaDateCalendarManager Gregorian Calendar
	 */
	private static final int GRANULARITY_MONTH = 6;
	/**
	 * The Identifier of Quarter in the JavaDateCalendarManager Gregorian Calendar
	 */
	private static final int GRANULARITY_QUARTER = 7;
	/**
	 * The Identifier of Year in the JavaDateCalendarManager Gregorian Calendar
	 */
	private static final int GRANULARITY_YEAR = 8;
	/**
	 * The Identifier of Decade in the JavaDateCalendarManager Gregorian Calendar
	 */
	private static final int GRANULARITY_DECADE = 10;
	/**
	 * The Identifier of Calendar in the JavaDateCalendarManager Gregorian Calendar
	 * This is a granule from the start of Gregorian Calendar to its end. 
	 */
	private static final int GRANULARITY_CALENDAR = 31 << 7 + 0;	// 1111100000
	/**
	 * The Identifier of Top in the JavaDateCalendarManager Gregorian Calendar
	 * This is a granule from the start of time to its end. 
	 */
	private static final int GRANULARITY_TOP = 31 << 7 + 127;	// 1111111111

	
	/**
	 * Provides access to a singleton instance of the class. This is not a generic factory method. It
	 * does only create one instance and provides that one with every call.
	 * @return The JavaDateCalendarManager instance.
	 */
	public static CalendarManager getSingleton() {
		if (singleton == null)
			singleton = new JavaDateCalendarManager();
		return singleton;
	}	
	
	
	/**
	 * Generates an instance of a calendar, the only one currently provided by this class.
	 * @return The calendar.
	 */
	public Calendar calendar() {
		return new Calendar(this);
	}
	
	
	/**
	 * Provides access to a singleton instance of a calendar, the only one currently provided by this class.
	 * It does only create one instance and provides that one with every call.
	 * @return The calendar.
	 */
	public Calendar getDefaultCalendar() {
		if (defaultCalendar == null)
			defaultCalendar = calendar();
		return defaultCalendar;
	}
    
    private int[] buildGranularityListForCreateGranule(Granularity granularity) throws TemporalDataException {
        int[] result;

        switch (granularity.getIdentifier()) {
        	case GRANULARITY_MILLISECOND:
        		result = new int[0];
        		break;
        	case GRANULARITY_SECOND:
        		result = new int[] { java.util.Calendar.MILLISECOND };
        		break;
        	case GRANULARITY_MINUTE:
        		result = new int[] { java.util.Calendar.SECOND, java.util.Calendar.MILLISECOND };
        		break;
        	case GRANULARITY_HOUR:
        		result = new int[] { java.util.Calendar.MINUTE,
        				java.util.Calendar.SECOND, java.util.Calendar.MILLISECOND };
        		break;
        	case GRANULARITY_DAY:
        		result = new int[] {
        				java.util.Calendar.AM_PM, java.util.Calendar.HOUR,
        				java.util.Calendar.HOUR_OF_DAY, java.util.Calendar.MINUTE,
        				java.util.Calendar.SECOND, java.util.Calendar.MILLISECOND };
        		break;
        	case GRANULARITY_WEEK:
        		result = new int[] { // java.util.Calendar.DAY_OF_WEEK, commented out because only works manually
        				java.util.Calendar.AM_PM, java.util.Calendar.HOUR,
        				java.util.Calendar.HOUR_OF_DAY, java.util.Calendar.MINUTE,
        				java.util.Calendar.SECOND, java.util.Calendar.MILLISECOND };
        		break;
        	case GRANULARITY_MONTH:
        		result = new int[] { java.util.Calendar.DAY_OF_MONTH,
        				java.util.Calendar.AM_PM, java.util.Calendar.HOUR,
        				java.util.Calendar.HOUR_OF_DAY, java.util.Calendar.MINUTE,
        				java.util.Calendar.SECOND, java.util.Calendar.MILLISECOND };
        		break;
        	case GRANULARITY_QUARTER:
        		result = new int[] { java.util.Calendar.DAY_OF_MONTH,
        				java.util.Calendar.AM_PM, java.util.Calendar.HOUR,
        				java.util.Calendar.HOUR_OF_DAY, java.util.Calendar.MINUTE,
        				java.util.Calendar.SECOND, java.util.Calendar.MILLISECOND };
        		break;
            case GRANULARITY_YEAR:
        	case GRANULARITY_DECADE:
        		result = new int[] { java.util.Calendar.DAY_OF_YEAR,
        				java.util.Calendar.AM_PM, java.util.Calendar.HOUR,
        				java.util.Calendar.HOUR_OF_DAY, java.util.Calendar.MINUTE,
        				java.util.Calendar.SECOND, java.util.Calendar.MILLISECOND };
        		break;
        	case GRANULARITY_CALENDAR:
        	case GRANULARITY_TOP:
        		result = new int[] {  
        				java.util.Calendar.DAY_OF_YEAR,
        				java.util.Calendar.AM_PM, java.util.Calendar.HOUR,
        				java.util.Calendar.HOUR_OF_DAY, java.util.Calendar.MINUTE,
        				java.util.Calendar.SECOND, java.util.Calendar.MILLISECOND,
        				java.util.Calendar.YEAR };
        		break;
        	default:
        		throw new TemporalDataException("Granularity not implemented yet");
        }

        return result;    	
    }

    
    protected Granule createGranule(long chronon, Granularity granularity) throws TemporalDataException {
		GregorianCalendar calInf = new GregorianCalendar();
		GregorianCalendar calSup = new GregorianCalendar();
		calInf.setTimeZone(TimeZone.getTimeZone("UTC"));
		calSup.setTimeZone(TimeZone.getTimeZone("UTC"));
		calInf.setTimeInMillis(chronon);
		calSup.setTimeInMillis(chronon);

		return createGranule(calInf,calSup,granularity);
	}
	
	/**
	 * Constructs a {@link Granule} from a given {@link Date}. Consider using the adequate constructor of
	 * {@link Granule} instead.
	 * @param date the {@link Date} used to generate the granule
	 * @param granularity granularity the {@link Granularity} to which the granule belongs
	 * @return the constructed {@link Granule}
	 * @throws TemporalDataException TemporalDataException thrown when granularities are not fully implemented
	 */ 
	public Granule createGranule(Date date, Granularity granularity) throws TemporalDataException {
		GregorianCalendar calInf = new GregorianCalendar();
		GregorianCalendar calSup = new GregorianCalendar();
		calInf.setTimeZone(TimeZone.getTimeZone("UTC"));
		calSup.setTimeZone(TimeZone.getTimeZone("UTC"));
		calInf.setTime(date);
		calSup.setTime(date);

		return createGranule(calInf,calSup,granularity);
	}
	
	private Granule createGranule(GregorianCalendar calInf, GregorianCalendar calSup, Granularity granularity) throws TemporalDataException {

		for (int field : buildGranularityListForCreateGranule(granularity)) {
			calInf.set(field, calInf.getActualMinimum(field));
			calSup.set(field, calSup.getActualMaximum(field));		
		}
		
        if (granularity.getIdentifier() == Granularities.Decade.intValue) {
            calInf.set(GregorianCalendar.YEAR,
                    (calInf.get(GregorianCalendar.YEAR) - 1) / 10 * 10 + 1);
            calSup.set(GregorianCalendar.YEAR,
                    (calSup.get(GregorianCalendar.YEAR) - 1) / 10 * 10 + 10);
        } else if (granularity.getIdentifier() == Granularities.Quarter.intValue) {
		    // calInf DAY_OF_MONTH is already at 1
            // calSup DAY_OF_MONTH needs to be set to 1 first 
		    // because last day may change (e.g., 31 March --June--> 1 July) 
			switch(calInf.get(GregorianCalendar.MONTH)) {
				case GregorianCalendar.JANUARY:
				case GregorianCalendar.FEBRUARY:
				case GregorianCalendar.MARCH:
					calInf.set(GregorianCalendar.MONTH,GregorianCalendar.JANUARY);
					calSup.set(GregorianCalendar.DAY_OF_MONTH, 1);
					calSup.set(GregorianCalendar.MONTH,GregorianCalendar.MARCH);
					calSup.set(GregorianCalendar.DAY_OF_MONTH, 31);
					break;
				case GregorianCalendar.APRIL:
				case GregorianCalendar.MAY:
				case GregorianCalendar.JUNE:
					calInf.set(GregorianCalendar.MONTH,GregorianCalendar.APRIL);
					calSup.set(GregorianCalendar.DAY_OF_MONTH, 1);
					calSup.set(GregorianCalendar.MONTH,GregorianCalendar.JUNE);
					calSup.set(GregorianCalendar.DAY_OF_MONTH, 30);
					break;
				case GregorianCalendar.JULY:
				case GregorianCalendar.AUGUST:
				case GregorianCalendar.SEPTEMBER:
					calInf.set(GregorianCalendar.MONTH,GregorianCalendar.JULY);
					calSup.set(GregorianCalendar.DAY_OF_MONTH, 1);
					calSup.set(GregorianCalendar.MONTH,GregorianCalendar.SEPTEMBER);
					calSup.set(GregorianCalendar.DAY_OF_MONTH, 30);
					break;
				case GregorianCalendar.OCTOBER:
				case GregorianCalendar.NOVEMBER:
				case GregorianCalendar.DECEMBER:
					calInf.set(GregorianCalendar.MONTH,GregorianCalendar.OCTOBER);
					calSup.set(GregorianCalendar.DAY_OF_MONTH, 1);
					calSup.set(GregorianCalendar.MONTH,GregorianCalendar.DECEMBER);
					calSup.set(GregorianCalendar.DAY_OF_MONTH, 31);
					break;
			}
		} else if(granularity.getIdentifier() == Granularities.Week.intValue) {
			long dow = (calInf.get(GregorianCalendar.DAY_OF_WEEK) + 5) % 7;
			long oldInf = calInf.getTimeInMillis();
			calInf.setTimeInMillis(oldInf-dow*86400000L);
			long oldSup = calSup.getTimeInMillis();
			calSup.setTimeInMillis(oldSup+(6-dow)*86400000L);
			if((granularity.getGranularityContextIdentifier() == Granularities.Month.intValue &&
				calInf.get(GregorianCalendar.MONTH) != calSup.get(GregorianCalendar.MONTH)) ||
				(granularity.getGranularityContextIdentifier() == Granularities.Quarter.intValue &&
				calInf.get(GregorianCalendar.MONTH) / 3 != calSup.get(GregorianCalendar.MONTH) / 3) ||
				(granularity.getGranularityContextIdentifier() == Granularities.Year.intValue) &&
				calInf.get(GregorianCalendar.YEAR) != calSup.get(GregorianCalendar.YEAR)) {
				GregorianCalendar calBorder = new GregorianCalendar();
				calBorder.setTimeZone(TimeZone.getTimeZone("UTC"));
				calBorder.setTimeInMillis(calInf.getTimeInMillis());
				calBorder.set(GregorianCalendar.DAY_OF_MONTH, 1);
				calBorder.add(GregorianCalendar.MONTH,1);
				boolean front = oldSup < calBorder.getTimeInMillis();
				if (!front)
					front = calBorder.getTimeInMillis() - oldInf > oldSup - calBorder.getTimeInMillis();
				if (front) {
					calSup.set(GregorianCalendar.DAY_OF_MONTH, 1);
					calSup.set(GregorianCalendar.MONTH, calInf.get(GregorianCalendar.MONTH));
					calSup.set(GregorianCalendar.YEAR, calInf.get(GregorianCalendar.YEAR));
					calSup.set(GregorianCalendar.DAY_OF_MONTH, calSup.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
				} else {
					calInf.set(GregorianCalendar.DAY_OF_MONTH, 1);
					calInf.set(GregorianCalendar.MONTH, calSup.get(GregorianCalendar.MONTH));
					calInf.set(GregorianCalendar.YEAR, calSup.get(GregorianCalendar.YEAR));
				}
			}
		}
		
		return new Granule(calInf.getTimeInMillis(),calSup.getTimeInMillis(),Granule.MODE_FORCE,granularity);
	}


	/**
	 * Returns an {@link Array} of granularity identifiers that are provided by the calendar.
	 * @return {@link Array} of granularity identifiers
	 */
	@Override
	public int[] getGranularityIdentifiers() {
		return new int[] {0,1,2,3,4,5,6,7,8,10,32767};
	}

	/**
	 * Calculate and return the identifier of a {@link Granule}. An identifier is a numeric label given in the context
	 * of the {@link Granularity}. Consider using the adequate method of
	 * {@link Granule} instead.
	 * @return the identifier
	 * @throws TemporalDataException thrown when granularities are not fully implemented
	 */
	@Override
	public long createGranuleIdentifier(Granule granule) throws TemporalDataException {
		long result = 0;
		
		switch(granule.getGranularity().getIdentifier()) {
			case GRANULARITY_MILLISECOND:
				switch(granule.getGranularity().getGranularityContextIdentifier()) {
					case GRANULARITY_SECOND:
						result = granule.getInf()%1000L;
						break;
					case GRANULARITY_MINUTE:
						result = granule.getInf()%60000L;
						break;
					case GRANULARITY_HOUR:
						result = granule.getInf()%3600000L;
						break;
					case GRANULARITY_DAY:
						result = granule.getInf()%86400000L;
						break;
					case GRANULARITY_WEEK: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = ((cal.get(GregorianCalendar.DAY_OF_WEEK)+5)%7)*86400000L + granule.getInf()%86400000L;
						break;
					}
					case GRANULARITY_MONTH: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = (cal.get(GregorianCalendar.DAY_OF_MONTH)-1)*86400000L + granule.getInf()%86400000L;
						break;
					}
					case GRANULARITY_QUARTER: 
						result = getDayInQuarter(granule.getInf())*86400000L + granule.getInf()%86400000L;
						break;
					case GRANULARITY_YEAR: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = (cal.get(GregorianCalendar.DAY_OF_YEAR)-1)*86400000L + granule.getInf()%86400000L;
						break;
					}
                    case GRANULARITY_DECADE: 
                        throw new UnsupportedOperationException("Not implemented yet.");
					default:
						result = granule.getInf();					
				}
				break;
			case GRANULARITY_SECOND:
				result = granule.getInf()/1000L;
				switch(granule.getGranularity().getGranularityContextIdentifier()) {
					case GRANULARITY_MINUTE:
						result %= 60L;
						break;
					case GRANULARITY_HOUR:
						result %= 3600L;
						break;
					case GRANULARITY_DAY:
						result %= 86400L;
						break;
					case GRANULARITY_WEEK: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = ((cal.get(GregorianCalendar.DAY_OF_WEEK)+5)%7)*86400L + result % 86400L;
						break;
					}
					case GRANULARITY_MONTH: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = (cal.get(GregorianCalendar.DAY_OF_MONTH)-1)*86400L + result % 86400L;
						break;
					}
					case GRANULARITY_QUARTER: 
						result = getDayInQuarter(granule.getInf())*86400L + result % 86400L;
						break;
					case GRANULARITY_YEAR: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = (cal.get(GregorianCalendar.DAY_OF_YEAR)-1)*86400L + result % 86400L;
						break;
					}
                    case GRANULARITY_DECADE: 
                        throw new UnsupportedOperationException("Not implemented yet.");
				}
				break;
			case GRANULARITY_MINUTE:
				result = granule.getInf()/60000L;
				switch(granule.getGranularity().getGranularityContextIdentifier()) {
					case GRANULARITY_HOUR:
						result %= 60;
						break;
					case GRANULARITY_DAY:
						result %= 1440;
						break;
					case GRANULARITY_WEEK: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = ((cal.get(GregorianCalendar.DAY_OF_WEEK)+5)%7)*1440L + result % 1440L;
						break;
					}
					case GRANULARITY_MONTH: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = (cal.get(GregorianCalendar.DAY_OF_MONTH)-1)*1440L + result % 1440L;
						break;
					}
					case GRANULARITY_QUARTER: 
						result = getDayInQuarter(granule.getInf())*1440L + result % 1440L;
						break;
					case GRANULARITY_YEAR: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = (cal.get(GregorianCalendar.DAY_OF_YEAR)-1)*1440L + result % 1440L;
						break;
					}
                    case GRANULARITY_DECADE: 
                        throw new UnsupportedOperationException("Not implemented yet.");
				}
				break;
			case GRANULARITY_HOUR:
				result = granule.getInf()/3600000L;
				switch(granule.getGranularity().getGranularityContextIdentifier()) {
					case GRANULARITY_DAY:
						result %= 24;
						break;
					case GRANULARITY_WEEK: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = ((cal.get(GregorianCalendar.DAY_OF_WEEK)+5)%7)*24 + result % 24;
						break;
					}
					case GRANULARITY_MONTH: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = (cal.get(GregorianCalendar.DAY_OF_MONTH)-1)*24 + result % 24;
						break;
					}
					case GRANULARITY_QUARTER: 
						result = getDayInQuarter(granule.getInf())*24 + result % 24;
						break;
					case GRANULARITY_YEAR: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = (cal.get(GregorianCalendar.DAY_OF_YEAR)-1)*24 + result % 24;
						break;
					}
                    case GRANULARITY_DECADE: 
                        throw new UnsupportedOperationException("Not implemented yet.");
				}
				break;
			case GRANULARITY_DAY:
				switch(granule.getGranularity().getGranularityContextIdentifier()) {
					case GRANULARITY_WEEK: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = (cal.get(GregorianCalendar.DAY_OF_WEEK)+5)%7;
						break;
					}
					case GRANULARITY_MONTH: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = cal.get(GregorianCalendar.DAY_OF_MONTH)-1;
						break;
					}
					case GRANULARITY_QUARTER: 
						result = getDayInQuarter(granule.getInf());
						break;
					case GRANULARITY_YEAR: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = cal.get(GregorianCalendar.DAY_OF_YEAR)-1;
						break;
					}
                    case GRANULARITY_DECADE:
                        throw new UnsupportedOperationException("Not implemented yet.");
					default:
						result = granule.getInf() / 86400000L;
				}
				break;
			case GRANULARITY_WEEK:
				// TODO add context granule to make this "clean"
				switch(granule.getGranularity().getGranularityContextIdentifier()) {
					case GRANULARITY_MONTH: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = cal.get(GregorianCalendar.WEEK_OF_MONTH)-1;
						break;
						}
					case GRANULARITY_QUARTER: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						int weekCounter = 0;
						if (cal.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.MONDAY)
							weekCounter--;
						int originalQuarter = cal.get(GregorianCalendar.MONTH) / 3;
						long chronon = granule.getInf();
						while(cal.get(GregorianCalendar.MONTH) / 3 == originalQuarter) {
							weekCounter++;
							chronon -= 604800000L; 
							cal.setTimeInMillis(chronon);
						}
						cal.set(GregorianCalendar.DAY_OF_MONTH, 1);
						cal.add(GregorianCalendar.MONTH,1);
						switch(cal.get(GregorianCalendar.DAY_OF_WEEK)) {
						case GregorianCalendar.MONDAY:
						case GregorianCalendar.TUESDAY:
						case GregorianCalendar.WEDNESDAY:
						case GregorianCalendar.THURSDAY:
							weekCounter++;
						}

						result = weekCounter;
						break;}
					case GRANULARITY_YEAR: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = cal.get(GregorianCalendar.WEEK_OF_YEAR)-1;
						break;
						}
                    case GRANULARITY_DECADE: 
                        throw new UnsupportedOperationException("Not implemented yet.");
					default:
						result = (granule.getInf() + 259200000L) / 604800000L; // Add the 3 days of week zero that were not in 1970
						break;
				}
				break;
			case GRANULARITY_MONTH: {
				GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
				cal.setTimeInMillis(granule.getInf());
				switch(granule.getGranularity().getGranularityContextIdentifier()) {
					case GRANULARITY_QUARTER:
						result = cal.get(GregorianCalendar.MONTH) % 3;
						break;
					case GRANULARITY_YEAR:
						result = cal.get(GregorianCalendar.MONTH);
						break;
                    case GRANULARITY_DECADE: 
                        result = cal.get(GregorianCalendar.MONTH) + 
                                ((cal.get(GregorianCalendar.YEAR)-1) % 10) * 12;
                        break;
					default:
                        result = (cal.get(GregorianCalendar.YEAR)-1970) * 12 +
                                cal.get(GregorianCalendar.MONTH);
						break;
					}
				break;}
			case GRANULARITY_QUARTER: {
                GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
                cal.setTimeInMillis(granule.getInf());
				switch(granule.getGranularity().getGranularityContextIdentifier()) {
					case GRANULARITY_YEAR:
						result = cal.get(GregorianCalendar.MONTH) / 3;
						break;
                    case GRANULARITY_DECADE: 
                        result = cal.get(GregorianCalendar.MONTH) / 3 + 
                                ((cal.get(GregorianCalendar.YEAR)-1) % 10) * 4;
                        break;
					default:
						result = (cal.get(GregorianCalendar.YEAR)-1970) * 4 +
								cal.get(GregorianCalendar.MONTH) / 3;
						break;
				}
				break; }
			case GRANULARITY_YEAR: {
				GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
				cal.setTimeInMillis(granule.getInf());
                switch(granule.getGranularity().getGranularityContextIdentifier()) {
                case GRANULARITY_DECADE: 
                    result = (cal.get(GregorianCalendar.YEAR)-1) % 10;
                    break;
                default:
                    result = cal.get(GregorianCalendar.YEAR)-1970;
                }
				break;}
            case GRANULARITY_DECADE: {
                GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
                cal.setTimeInMillis(granule.getInf());
                result = (cal.get(GregorianCalendar.YEAR)-1) / 10;
//                System.out.println(cal.get(GregorianCalendar.YEAR) + "  " + result);
                break; }
		}
		
		return result;
	}

	private long getDayInQuarter(long inf) {
		GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		cal.setTimeInMillis(inf);
		switch(cal.get(GregorianCalendar.MONTH)) {
			case GregorianCalendar.JANUARY:
				return cal.get(GregorianCalendar.DAY_OF_MONTH)-1;
			case GregorianCalendar.FEBRUARY:
				return 31+cal.get(GregorianCalendar.DAY_OF_MONTH)-1;
			case GregorianCalendar.MARCH:
				return 31+
						(cal.isLeapYear(cal.get(GregorianCalendar.YEAR)) ? 29 : 28) +
						cal.get(GregorianCalendar.DAY_OF_MONTH)-1;
			case GregorianCalendar.APRIL:
				return cal.get(GregorianCalendar.DAY_OF_MONTH)-1;
			case GregorianCalendar.MAY:
				return 30+cal.get(GregorianCalendar.DAY_OF_MONTH)-1;
			case GregorianCalendar.JUNE:
				return 61+cal.get(GregorianCalendar.DAY_OF_MONTH)-1;
			case GregorianCalendar.JULY:
				return cal.get(GregorianCalendar.DAY_OF_MONTH)-1;
			case GregorianCalendar.AUGUST:
				return 31+cal.get(GregorianCalendar.DAY_OF_MONTH)-1;
			case GregorianCalendar.SEPTEMBER:
				return 62+cal.get(GregorianCalendar.DAY_OF_MONTH)-1;
			case GregorianCalendar.OCTOBER:
				return cal.get(GregorianCalendar.DAY_OF_MONTH)-1;
			case GregorianCalendar.NOVEMBER:
				return 31+cal.get(GregorianCalendar.DAY_OF_MONTH)-1;
			default: // GregorianCalendar.DECEMBER:
				return 61+cal.get(GregorianCalendar.DAY_OF_MONTH)-1;
		}
	}

	/**
	 * Returns the identifier of the bottom granularity
	 * @return the bottom granularity identifier
	 */
	public int getBottomGranularityIdentifier(Calendar calendar) {
		return 0;
	}
	
	/**
	 * Returns the identifier of the top granularity. This is the highest possible granularity of the calendar.
	 * Usually, this is a granularity where one granule is composed of all the time the calendar is defined for.
	 * Let all calendars that would normally have this be modified so they have one. 
	 * @return the top granularity identifier
	 */
	public int getTopGranularityIdentifier(Calendar calendar) {
		return GRANULARITY_TOP;
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
	@Override
	public Granule createGranule(long inf, long sup, int mode, Granularity granularity) throws TemporalDataException {
		switch(mode) {
			case Granule.MODE_FORCE:
				return new Granule(inf,sup,mode,granularity);
			case Granule.MODE_INF_GRANULE:
				return createGranule(inf,granularity);
			case Granule.MODE_MIDDLE_GRANULE:
				return createGranule(inf+(sup-inf)/2,granularity);
			case Granule.MODE_SUP_GRANULE:
				return createGranule(sup,granularity);
			default:
				throw new TemporalDataException("Illegal mode in createGranule");
		}
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
	@Override
	public Granule[] createGranules(long inf, long sup, double cover,
			Granularity granularity) throws TemporalDataException {
		Granule first = createGranule(inf,granularity);
		Granule last = createGranule(sup, granularity);
		
		long firstIdentifier = first.getIdentifier();
		long lastIdentifier = last.getIdentifier();
		
		if( (double)(inf-first.getInf()) / (double)(first.getSup()-first.getInf()) < cover) {
			firstIdentifier++;
		}
		if( (double)(last.getSup()-sup) / (double)(last.getSup()-last.getInf()) < cover) {
			lastIdentifier--;
		}

		Granule[] result;
		if(firstIdentifier>lastIdentifier)
			result = new Granule[0];
		else {
			result = new Granule[(int)(lastIdentifier-firstIdentifier+1)];
			result[0] = first;
			for(int i=1; i<(int)(lastIdentifier-firstIdentifier); i++) {
				result[i] = createGranule(result[i-1].getSup()+1,granularity);
			}
			result[(int)(lastIdentifier-firstIdentifier)] = last;	// when first=last set 0 again
		}
		
		return result;
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
	@Override
	public Granule[] createGranules(Granule[] granules, double cover,
			Granularity granularity) throws TemporalDataException {
		ArrayList<Granule> result = new ArrayList<Granule>();
		for(Granule iGran : granules) {
			for(Granule iGran2 : createGranules(iGran.getInf(), iGran.getSup(), cover, granularity)) {
				if(result.get(result.size()-1).getIdentifier() < iGran2.getIdentifier())
					result.add(iGran2);
			}
		}
		
		return (Granule[])result.toArray();
	}

	/**
	 * Calculate and return the human readable label of a {@link Granule}.
	 * Consider using the adequate method of
	 * {@link Granule} instead.
	 * @return the label
	 * @throws TemporalDataException thrown when granularities are not fully implemented
	 */
	@Override
	public String createGranuleLabel(Granule granule) throws TemporalDataException {
		String result = null;
		
		switch(granule.getGranularity().getIdentifier()) {
			case GRANULARITY_DAY:
				if(granule.getGranularity().getGranularityContextIdentifier() == GRANULARITY_WEEK ) {
					GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
					cal.setTimeInMillis(granule.getInf());
					//result = cal.getDisplayName(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.LONG, Locale.getDefault());
					result = cal.getDisplayName(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.SHORT, Locale.getDefault());
				} else
					result = String.format("%d",granule.getIdentifier()+1);
				break;
			case GRANULARITY_WEEK:
				if(granule.getGranularity().getGranularityContextIdentifier() == GRANULARITY_MONTH || 
					granule.getGranularity().getGranularityContextIdentifier() == GRANULARITY_QUARTER ) {
					result = String.format("%d",granule.getIdentifier()); 
				}
				else
					result = String.format("%d",granule.getIdentifier()+1);
				break;
			case GRANULARITY_MONTH:
				if(granule.getGranularity().getGranularityContextIdentifier() == GRANULARITY_YEAR ) {
					GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
					cal.setTimeInMillis(granule.getInf());
					result = cal.getDisplayName(GregorianCalendar.MONTH, GregorianCalendar.LONG, Locale.getDefault());
				} else
					result = String.format("M%d",granule.getIdentifier()+1);
				break;
			case GRANULARITY_QUARTER:
				result = String.format("Q%d",granule.getIdentifier()+1);
				break;
			case GRANULARITY_YEAR:
                if(GRANULARITY_DECADE == granule.getGranularity().getGranularityContextIdentifier()) {
                    result = String.format("%d",granule.getIdentifier() + 1);
                } else {
                    result = String.format("%d",granule.getIdentifier()+1970);
                }
				break;
            case GRANULARITY_DECADE:
                result = String.format("%ds",granule.getIdentifier()*10);
                break;
			default:
				result = String.format("%d",granule.getIdentifier()+1);
		}
		
		return result;
	}


	/**
	 * Calculate and return the inf of a {@link Granule}.
	 * @return the inf
	 * @throws TemporalDataException thrown when granularities are not fully implemented
	 */
	@Override
	public long createInf(Granule granule) throws TemporalDataException {
		if (granule.getContextGranule() == null)
			throw new TemporalDataException("Cannot generate inf without context granule");
		
		if (granule.getGranularity().getIdentifier() < granule.getContextGranule().getGranularity().getIdentifier())
			return createInfLocal(granule,granule.getContextGranule());
		else
			return createInfLocal(granule.getContextGranule(),granule);		
	}
	private long createInfLocal(Granule granule,Granule contextGranule) throws TemporalDataException {
		long result = 0;
				
		switch(granule.getGranularity().getIdentifier()) {
			case GRANULARITY_MILLISECOND:
				result = granule.getIdentifier();
				if (contextGranule.getGranularity().getIdentifier() != GRANULARITY_TOP)
					result += contextGranule.getInf();
				break;
			case GRANULARITY_SECOND:
				result = granule.getIdentifier()*1000L;
				if (contextGranule.getGranularity().getIdentifier() != GRANULARITY_TOP)
					result += contextGranule.getInf();
				break;
			case GRANULARITY_MINUTE:
				result = granule.getIdentifier()*60000L;
				if (contextGranule.getGranularity().getIdentifier() != GRANULARITY_TOP)
					result += contextGranule.getInf();
				break;
			case GRANULARITY_HOUR:
				result = granule.getIdentifier()*3600000L;
				if (contextGranule.getGranularity().getIdentifier() != GRANULARITY_TOP)
					result += contextGranule.getInf();
				break;
			case GRANULARITY_DAY:
			    // Warning does not handle day light saving time 
				result = granule.getIdentifier()*86400000L;
				if (contextGranule.getGranularity().getIdentifier() != GRANULARITY_TOP)
					result += contextGranule.getInf();
				break;
			case GRANULARITY_WEEK:				
				switch(contextGranule.getGranularity().getIdentifier()) {
					case GRANULARITY_MONTH:{
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(contextGranule.getInf());
						if(cal.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.FRIDAY ||
							cal.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SATURDAY ||
							cal.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SUNDAY)
							result = cal.getTimeInMillis() + granule.getIdentifier() * 604800000L;
						else
							result = cal.getTimeInMillis() + (granule.getIdentifier()-1) * 604800000L;
						break;}
					case GRANULARITY_YEAR:
					case GRANULARITY_DECADE:{
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(contextGranule.getInf());
						if(cal.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.FRIDAY ||
							cal.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SATURDAY ||
							cal.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SUNDAY)
							// roll forward to begin of new week in new year 
							result = cal.getTimeInMillis() + (7-((cal.get(GregorianCalendar.DAY_OF_WEEK)+5)%7)) * 86400000L;
						else
							// roll back to begin of week in last year 
							result = cal.getTimeInMillis() - ((cal.get(GregorianCalendar.DAY_OF_WEEK)+5)%7) * 86400000L;
						result += granule.getIdentifier() * 604800000L;
						break;}
					case GRANULARITY_CALENDAR:						
					case GRANULARITY_TOP:
					    // 1 Jan 1970 is a Thursday
						result = granule.getIdentifier() * 604800000L - 259200000; // = 3*24*60*60*1000
						break;
					default:
						throw new TemporalDataException("Unknown context granularity");					
				}
				break;
			case GRANULARITY_MONTH:{
				GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
				cal.setTimeInMillis(contextGranule.getInf());
				switch(contextGranule.getGranularity().getIdentifier()) {
				case GRANULARITY_QUARTER:
					cal.set(GregorianCalendar.MONTH, (int)(granule.getIdentifier()*3));
					break;
				case GRANULARITY_YEAR:
					cal.set(GregorianCalendar.MONTH, (int)(granule.getIdentifier()));
					break;
				case GRANULARITY_DECADE:
					cal.set(GregorianCalendar.MONTH, (int)(granule.getIdentifier()%12));
					break;
				default:
					cal.setTimeInMillis(0);
	                int year = ((int) granule.getIdentifier()) / 12;
	                if (GRANULARITY_DECADE == contextGranule.getGranularity().getIdentifier()) {
	                    cal.set(GregorianCalendar.YEAR, year+1970+1);
	                } else {
	                    cal.set(GregorianCalendar.YEAR, year+1970);
	                }
					cal.set(GregorianCalendar.MONTH, (int)(granule.getIdentifier()%12));
					result = cal.getTimeInMillis();
				}
				break;}
			case GRANULARITY_QUARTER:{
				GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
				cal.setTimeInMillis(contextGranule.getInf());
				switch(contextGranule.getGranularity().getIdentifier()) {
				case GRANULARITY_YEAR:
					cal.set(GregorianCalendar.MONTH, (int)(granule.getIdentifier())%4*3);
					break;
				case GRANULARITY_DECADE:
					cal.set(GregorianCalendar.MONTH, (int)(granule.getIdentifier())%12*3);
					break;
				default:
					cal.setTimeInMillis(0);
					int year = ((int) granule.getIdentifier()) / 4;
					if (GRANULARITY_DECADE == contextGranule.getGranularity().getIdentifier()) {
						cal.set(GregorianCalendar.YEAR, year+1970+1);
					} else {
						cal.set(GregorianCalendar.YEAR, year+1970);
					}
//					System.out.println(cal.get(GregorianCalendar.YEAR) + " " + year + " " + granule.getGranularity().getGranularityContextIdentifier());
					cal.set(GregorianCalendar.MONTH, (int)(granule.getIdentifier()%4*3));
					result = cal.getTimeInMillis();
				}
				break;}
			case GRANULARITY_YEAR:{
				GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
				cal.setTimeInMillis(0);
				int year = ((int) granule.getIdentifier());
				if (GRANULARITY_DECADE == contextGranule.getGranularity().getIdentifier()) {
	                cal.set(GregorianCalendar.YEAR, year+1971);
				} else {
				    cal.set(GregorianCalendar.YEAR, year+1970);
				}
				result = cal.getTimeInMillis();
				break;}
            case GRANULARITY_DECADE:{
                GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
                cal.setTimeInMillis(0);
                cal.set(GregorianCalendar.YEAR, (int)(granule.getIdentifier() * 10 + 1));
                result = cal.getTimeInMillis();
                break;}
		}
		
		return result;
	}

	/**
	 * Calculate and return the sup of a {@link Granule}.
	 * @return the sup
	 * @throws TemporalDataException thrown when granularities are not fully implemented
	 */
	@Override
	public long createSup(Granule granule) throws TemporalDataException {
		if (granule.getContextGranule() == null)
			throw new TemporalDataException("Cannot generate sup without context granule");
		
		if (granule.getGranularity().getIdentifier() < granule.getContextGranule().getGranularity().getIdentifier())
			return createSupLocal(granule,granule.getContextGranule());
		else
			return createSupLocal(granule.getContextGranule(),granule);	
	}
	public long createSupLocal(Granule granule,Granule contextGranule) throws TemporalDataException {
		long result = 0;
		
		// invalid granule ids for context granularities are not defined (insert context granule)
		
		switch(granule.getGranularity().getIdentifier()) {
			case GRANULARITY_MILLISECOND:
				result = granule.getInf();
				break;
			case GRANULARITY_SECOND:
				result = granule.getInf()+999L;
				break;
			case GRANULARITY_MINUTE:
				result = granule.getInf()+59999L;
				break;
			case GRANULARITY_HOUR:
				result = granule.getInf()+3599999L;
				break;
			case GRANULARITY_DAY:
				result = granule.getInf()+86399999L;
				break;
			case GRANULARITY_WEEK:
                result = granule.getInf() + 604799999;
                break;
			case GRANULARITY_MONTH:
				result = granule.getInf();
				int monthId = (int)granule.getIdentifier() % 12;
				if(monthId < 0)
					monthId+=12;
				switch(monthId) {
				case 0:
				case 2:
				case 4:
				case 6:
				case 7:
				case 9:
				case 11: 
					result += 2678399999L;
					break;
				case 1:
					GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
					cal.setTimeInMillis(result);
					if(cal.isLeapYear(cal.get(GregorianCalendar.YEAR)))
						result += 2505599999L;
					else
						result += 2419199999L;
					break;
				default:
					result += 2591999999L;
				}
				break;
			case GRANULARITY_QUARTER:
				result = granule.getInf();
				int quarterId = (int)granule.getIdentifier() % 4;
				if(quarterId < 0)
					quarterId+=4;
				switch(quarterId) {
				case 0:
					// 86400000
					GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
					cal.setTimeInMillis(result);
					if(cal.isLeapYear(cal.get(GregorianCalendar.YEAR)))
						result += 7862399999L;
					else
						result += 7775999999L;
					break;
				case 1:
					result += 7862399999L;
					break;
				default:
					result += 7948799999L;					
				}
				break;
			case GRANULARITY_YEAR:{
				result = granule.getInf();
				GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
				cal.setTimeInMillis(result);
				if(cal.isLeapYear((int)granule.getIdentifier()+1970))
					result += 31622399999L;
				else
					result += 31535999999L;
				break;}
            case GRANULARITY_DECADE:{
				result = granule.getInf();
				GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
				cal.setTimeInMillis(result);
				for(int i = (int)granule.getIdentifier()*10+1; i<(int)granule.getIdentifier()*10+1+10; i++) {
					if(cal.isLeapYear(i))
						result += 31622400000L;
					else
						result += 31536000000L;
				}
				result--;
				break;}
		}
		
		return result;
	}
	
	public static String formatDebugString(long inf) {
		GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		cal.setTimeInMillis(inf);
		return String.format("%04d-%02d-%02d, %02d:%02d:%02d,%03d",cal.get(GregorianCalendar.YEAR),cal.get(GregorianCalendar.MONTH)+1,cal.get(GregorianCalendar.DAY_OF_MONTH),
				cal.get(GregorianCalendar.HOUR_OF_DAY),cal.get(GregorianCalendar.MINUTE),cal.get(GregorianCalendar.SECOND),cal.get(GregorianCalendar.MILLISECOND));
	}

	/**
	 * Provide the minimum identifier value that granules of a granularity can assume.
	 * @param granularity the granularity
	 * @return the minimum granule identifier value
	 * @throws TemporalDataException thrown when granularity has illegal identifiers
	 */
	@Override
	public long getMinGranuleIdentifier(Granularity granularity) throws TemporalDataException {
		switch(granularity.getIdentifier()) {
			case GRANULARITY_MILLISECOND:
				switch(granularity.getGranularityContextIdentifier()) {
					case GRANULARITY_CALENDAR:
					case GRANULARITY_TOP:
						return Long.MIN_VALUE;
					default:
						return 0;						
				}
			case GRANULARITY_SECOND:
				switch(granularity.getGranularityContextIdentifier()) {
					case GRANULARITY_CALENDAR:
					case GRANULARITY_TOP:
						return Long.MIN_VALUE;
					default:
						return 0;						
				}
			case GRANULARITY_MINUTE:
				switch(granularity.getGranularityContextIdentifier()) {
					case GRANULARITY_CALENDAR:
					case GRANULARITY_TOP:
						return Long.MIN_VALUE;
					default:
						return 0;						
				}
			case GRANULARITY_HOUR:
				switch(granularity.getGranularityContextIdentifier()) {
					case GRANULARITY_CALENDAR:
					case GRANULARITY_TOP:
						return Long.MIN_VALUE;
					default:
						return 0;						
				}
			case GRANULARITY_DAY:
				switch(granularity.getGranularityContextIdentifier()) {
					case GRANULARITY_CALENDAR:
					case GRANULARITY_TOP:
						return Long.MIN_VALUE;
					default:
						return 0;						
				}
			case GRANULARITY_WEEK:
				switch(granularity.getGranularityContextIdentifier()) {
					case GRANULARITY_CALENDAR:
					case GRANULARITY_TOP:
						return Long.MIN_VALUE;
					default:
						return 0;						
				}
			case GRANULARITY_MONTH:
				switch(granularity.getGranularityContextIdentifier()) {
					case GRANULARITY_CALENDAR:
					case GRANULARITY_TOP:
						return Long.MIN_VALUE;
					default:
						return 0;						
				}
			case GRANULARITY_QUARTER:
				switch(granularity.getGranularityContextIdentifier()) {
					case GRANULARITY_CALENDAR:
					case GRANULARITY_TOP:
						return Long.MIN_VALUE;
					default:
						return 0;						
				}
			case GRANULARITY_YEAR:
            case GRANULARITY_DECADE:
				switch(granularity.getGranularityContextIdentifier()) {
					case GRANULARITY_CALENDAR:
					case GRANULARITY_TOP:
						return Long.MIN_VALUE;
					default:
						return 0;						
				}
		}
		
		return 0;
	}

	/**
	 * Provide the maximum identifier value that granules of a granularity can assume.
	 * @param granularity the granularity
	 * @return the maximum granule identifier value
	 * @throws TemporalDataException thrown when granularity has illegal identifiers
	 */
	@Override
	public long getMaxGranuleIdentifier(Granularity granularity) throws TemporalDataException {
		switch(granularity.getIdentifier()) {
			case GRANULARITY_MILLISECOND:
				switch(granularity.getGranularityContextIdentifier()) {
				    case GRANULARITY_MILLISECOND:
				        return 0L;
					case GRANULARITY_SECOND:
						return 999L;
					case GRANULARITY_MINUTE:
						return 59999L;
					case GRANULARITY_HOUR:
						return 3599999L;
					case GRANULARITY_DAY:
						return 86399999L;
					case GRANULARITY_WEEK:
						return 604799999L;
					case GRANULARITY_MONTH:
						return 2678399999L;
					case GRANULARITY_QUARTER:
						return 7948799999L;
					case GRANULARITY_YEAR:
						return 31622399999L;
					case GRANULARITY_DECADE:
                        return 315619199999L;
					case GRANULARITY_CALENDAR:
					case GRANULARITY_TOP:
						return Long.MAX_VALUE;
					default:
	                    throw new UnsupportedOperationException();
//						return 1000L;						
				}
			case GRANULARITY_SECOND:
				switch(granularity.getGranularityContextIdentifier()) {
					case GRANULARITY_MINUTE:
						return 59L;
					case GRANULARITY_HOUR:
						return 3599L;
					case GRANULARITY_DAY:
						return 86399L;
					case GRANULARITY_WEEK:
						return 604799L;
					case GRANULARITY_MONTH:
						return 2678399L;
					case GRANULARITY_QUARTER:
						return 7948799L;
					case GRANULARITY_YEAR:
						return 31622399L;
                    case GRANULARITY_DECADE:
                        return 315619199L;
					case GRANULARITY_CALENDAR:
					case GRANULARITY_TOP:
						return Long.MAX_VALUE;
					default:
	                    throw new UnsupportedOperationException();
				}
			case GRANULARITY_MINUTE:
				switch(granularity.getGranularityContextIdentifier()) {
					case GRANULARITY_HOUR:
						return 59L;
					case GRANULARITY_DAY:
						return 1439L;
					case GRANULARITY_WEEK:
						return 10079L;
					case GRANULARITY_MONTH:
						return 44639L;
					case GRANULARITY_QUARTER:
						return 132479L;
					case GRANULARITY_YEAR:
						return 527039L;
                    case GRANULARITY_DECADE:
                        return 5260319L;
					case GRANULARITY_CALENDAR:
						case GRANULARITY_TOP:
						return Long.MAX_VALUE;
					default:
	                    throw new UnsupportedOperationException();
			}
		case GRANULARITY_HOUR:
			switch(granularity.getGranularityContextIdentifier()) {
				case GRANULARITY_DAY:
					return 23L;
				case GRANULARITY_WEEK:
					return 167L;
				case GRANULARITY_MONTH:
					return 743L;
				case GRANULARITY_QUARTER:
					return 2207L;
				case GRANULARITY_YEAR:
					return 8783L;
                case GRANULARITY_DECADE:
                    return 87671L;
				case GRANULARITY_CALENDAR:
				case GRANULARITY_TOP:
					return Long.MAX_VALUE;
				default:
                    throw new UnsupportedOperationException();
			}
		case GRANULARITY_DAY:
			switch(granularity.getGranularityContextIdentifier()) {
				case GRANULARITY_WEEK:
					return 6L;
				case GRANULARITY_MONTH:
					return 30L;
				case GRANULARITY_QUARTER:
					return 91L;				
				case GRANULARITY_YEAR:
					return 365L;				
                case GRANULARITY_DECADE:
                    return 3652L;
				case GRANULARITY_CALENDAR:
				case GRANULARITY_TOP:
					return Long.MAX_VALUE;
				default:
                    throw new UnsupportedOperationException();
			}
		case GRANULARITY_WEEK:
			switch(granularity.getGranularityContextIdentifier()) {
				case GRANULARITY_MONTH:
					return 5L;
				case GRANULARITY_QUARTER:
					return 13L;
				case GRANULARITY_YEAR:
					return 52L;				
                case GRANULARITY_DECADE:
                    return 521L;
				case GRANULARITY_CALENDAR:
				case GRANULARITY_TOP:
					return Long.MAX_VALUE;
				default:
                    throw new UnsupportedOperationException();
			}
		case GRANULARITY_MONTH:
			switch(granularity.getGranularityContextIdentifier()) {
				case GRANULARITY_QUARTER:
					return 2L;
				case GRANULARITY_YEAR:
					return 11L;				
                case GRANULARITY_DECADE:
                    return 119; // = 10 * 12 - 1
				case GRANULARITY_CALENDAR:
				case GRANULARITY_TOP:
					return Long.MAX_VALUE;
				default:
                    throw new UnsupportedOperationException();
			}
		case GRANULARITY_QUARTER:
			switch(granularity.getGranularityContextIdentifier()) {
				case GRANULARITY_YEAR:
					return 3L;
                case GRANULARITY_DECADE:
                    return 39; // = 10 * 4 - 1
				case GRANULARITY_CALENDAR:
				case GRANULARITY_TOP:
					return Long.MAX_VALUE;
				default:
                    throw new UnsupportedOperationException();
			}
		case GRANULARITY_YEAR:
			switch(granularity.getGranularityContextIdentifier()) {
			    case GRANULARITY_DECADE:
			        return 9;
				case GRANULARITY_CALENDAR:
				case GRANULARITY_TOP:
					return Long.MAX_VALUE;
				default:
                    throw new UnsupportedOperationException();
			}
        case GRANULARITY_DECADE:
            switch(granularity.getGranularityContextIdentifier()) {
                case GRANULARITY_CALENDAR:
                case GRANULARITY_TOP:
                    return Long.MAX_VALUE;
                default:
                    throw new UnsupportedOperationException();
            }
	}
		
		return 0;
	}

	@Override
	public long getMaxLengthInIdentifiers(Granularity granularity) throws TemporalDataException {
		switch(granularity.getIdentifier()) {
			case GRANULARITY_MILLISECOND:
				switch(granularity.getGranularityContextIdentifier()) {
				    case GRANULARITY_MILLISECOND:
				        return 1L;
					case GRANULARITY_SECOND:
						return 1000L;
					case GRANULARITY_MINUTE:
						return 60000L;
					case GRANULARITY_HOUR:
						return 3600000L;
					case GRANULARITY_DAY:
						return 86400000L;
					case GRANULARITY_WEEK:
						return 604800000L;
					case GRANULARITY_MONTH:
						return 2678400000L;
					case GRANULARITY_QUARTER:
						return 7948800000L;
					case GRANULARITY_YEAR:
						return 31622400000L;
					case GRANULARITY_DECADE:
                        return 315619200000L;
					case GRANULARITY_CALENDAR:
					case GRANULARITY_TOP:
						return Long.MAX_VALUE;
					default:
	                    throw new UnsupportedOperationException();
//						return 1000L;						
				}
			case GRANULARITY_SECOND:
				switch(granularity.getGranularityContextIdentifier()) {
					case GRANULARITY_MINUTE:
						return 60L;
					case GRANULARITY_HOUR:
						return 3600L;
					case GRANULARITY_DAY:
						return 86400L;
					case GRANULARITY_WEEK:
						return 604800L;
					case GRANULARITY_MONTH:
						return 267840L;
					case GRANULARITY_QUARTER:
						return 7948800L;
					case GRANULARITY_YEAR:
						return 31622400L;
                    case GRANULARITY_DECADE:
                        return 315619200L;
					case GRANULARITY_CALENDAR:
					case GRANULARITY_TOP:
						return Long.MAX_VALUE;
					default:
	                    throw new UnsupportedOperationException();
				}
			case GRANULARITY_MINUTE:
				switch(granularity.getGranularityContextIdentifier()) {
					case GRANULARITY_HOUR:
						return 60L;
					case GRANULARITY_DAY:
						return 1440L;
					case GRANULARITY_WEEK:
						return 10080L;
					case GRANULARITY_MONTH:
						return 44640L;
					case GRANULARITY_QUARTER:
						return 132480L;
					case GRANULARITY_YEAR:
						return 527040L;
                    case GRANULARITY_DECADE:
                        return 5260320L;
					case GRANULARITY_CALENDAR:
						case GRANULARITY_TOP:
						return Long.MAX_VALUE;
					default:
	                    throw new UnsupportedOperationException();
			}
		case GRANULARITY_HOUR:
			switch(granularity.getGranularityContextIdentifier()) {
				case GRANULARITY_DAY:
					return 24L;
				case GRANULARITY_WEEK:
					return 168L;
				case GRANULARITY_MONTH:
					return 744L;
				case GRANULARITY_QUARTER:
					return 2208L;
				case GRANULARITY_YEAR:
					return 8784L;
                case GRANULARITY_DECADE:
                    return 87672L;
				case GRANULARITY_CALENDAR:
				case GRANULARITY_TOP:
					return Long.MAX_VALUE;
				default:
                    throw new UnsupportedOperationException();
			}
		case GRANULARITY_DAY:
			switch(granularity.getGranularityContextIdentifier()) {
				case GRANULARITY_WEEK:
					return 7L;
				case GRANULARITY_MONTH:
					return 31L;
				case GRANULARITY_QUARTER:
					return 92L;				
				case GRANULARITY_YEAR:
					return 366L;				
                case GRANULARITY_DECADE:
                    return 3653L;
				case GRANULARITY_CALENDAR:
				case GRANULARITY_TOP:
					return Long.MAX_VALUE;
				default:
                    throw new UnsupportedOperationException();
			}
		case GRANULARITY_WEEK:
			switch(granularity.getGranularityContextIdentifier()) {
				case GRANULARITY_MONTH:
					return 6L;
				case GRANULARITY_QUARTER:
					return 14L;
				case GRANULARITY_YEAR:
					return 53L;				
                case GRANULARITY_DECADE:
                    return 521L;
				case GRANULARITY_CALENDAR:
				case GRANULARITY_TOP:
					return Long.MAX_VALUE;
				default:
                    throw new UnsupportedOperationException();
			}
		case GRANULARITY_MONTH:
			switch(granularity.getGranularityContextIdentifier()) {
				case GRANULARITY_QUARTER:
					return 3L;
				case GRANULARITY_YEAR:
					return 12L;				
                case GRANULARITY_DECADE:
                    return 120L;
				case GRANULARITY_CALENDAR:
				case GRANULARITY_TOP:
					return Long.MAX_VALUE;
				default:
                    throw new UnsupportedOperationException();
			}
		case GRANULARITY_QUARTER:
			switch(granularity.getGranularityContextIdentifier()) {
				case GRANULARITY_YEAR:
					return 4L;
                case GRANULARITY_DECADE:
                    return 40L;
				case GRANULARITY_CALENDAR:
				case GRANULARITY_TOP:
					return Long.MAX_VALUE;
				default:
                    throw new UnsupportedOperationException();
			}
		case GRANULARITY_YEAR:
			switch(granularity.getGranularityContextIdentifier()) {
			    case GRANULARITY_DECADE:
			        return 10L;
				case GRANULARITY_CALENDAR:
				case GRANULARITY_TOP:
					return Long.MAX_VALUE;
				default:
                    throw new UnsupportedOperationException();
			}
        case GRANULARITY_DECADE:
            switch(granularity.getGranularityContextIdentifier()) {
                case GRANULARITY_CALENDAR:
                case GRANULARITY_TOP:
                    return Long.MAX_VALUE;
                default:
                    throw new UnsupportedOperationException();
            }
	}
		
		return 0;
	}
	
	@Override
	public boolean contains(Granule granule, long chronon) throws TemporalDataException {
		if(granule.getGranularity().getGranularityContextIdentifier() ==
				Granularities.Top.intValue || granule.getGranularity().getGranularityContextIdentifier() ==
						Granularities.Calendar.intValue) {
			return chronon>=granule.getInf() && chronon<=granule.getSup();
		} else {
			Granule g2 = new Granule(chronon,chronon,granule.getGranularity());
			return granule.getIdentifier() == g2.getIdentifier();
		}
	}
	
	@Override
	public long getStartOfTime() {
		return Long.MIN_VALUE;
	}
	
	@Override
	public long getEndOfTime() {
		return Long.MAX_VALUE;
	}
}