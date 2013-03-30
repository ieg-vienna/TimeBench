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
	 * Constructs a JavaDateCalendarManager. Consider using the
	 * {@linkplain JavaDateCalendarManager#getDefaultCalendar() singleton} instead.
	 */
	public JavaDateCalendarManager() {
		javaCalendar = java.util.Calendar.getInstance();
		javaCalendar.setLenient(true);
	}	
	
	/**
	 * The granularities enumeration supports a certain number of granularities.
	 *  
	 * <p>
	 * Added:          2011-07-19 / TL<br>
	 * Modifications: 
	 * </p>
	 * 
	 * @author Tim Lammarsch
	 *
	 */
	public enum Granularities {
		Millisecond (0),
		Second (1),
		Minute (2),
		Hour (3),
		Day (4),
		Week (5),
		Month (6),
		Quarter (7),
		Year (8),
		Decade(10),
		Calendar (16383),	// Calendar has one granule from start to end of the calendar
		Top (32767);	// Top has one granule from start to end of time

		
		private int intValue;
		
		Granularities(int toInt) {
			intValue = toInt;
		}
		
		
		/**
		 * Converts a granularity from the enumeration to an identifier.
		 * This identifier is not globally unique, it depends on calendarManager and calendar.
		 * @return The equivalent identifier.
		 */
		public int toInt()
		{
			return intValue;
		}
		
		
		/**
		 * Converts an identifier to a granularity from the enumeration.
		 * This identifier is not globally unique, it depends on calendarManager and calendar.
		 * @param intValue The identifier.
		 * @return The granularity from the enumeration.
		 * @throws TemporalDataException
		 */
		public static Granularities fromInt(int intValue) throws TemporalDataException {
			switch(intValue) {
				case 0: return Granularities.Millisecond;
				case 1: return Granularities.Second;
				case 2: return Granularities.Minute;
				case 3: return Granularities.Hour;
				case 4: return Granularities.Day;
				case 5: return Granularities.Week;
				case 6: return Granularities.Month;
				case 7: return Granularities.Quarter;
				case 8: return Granularities.Year;
                case 10: return Granularities.Decade;
				case 16383: return Granularities.Calendar; 
				case 32767: return Granularities.Top; 
				default: throw new TemporalDataException("Unknown Granularity: " + intValue);
			}
		}
	}
	
	
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

        switch (Granularities.fromInt(granularity.getIdentifier())) {
        	case Millisecond:
        		result = new int[0];
        		break;
        	case Second:
        		result = new int[] { java.util.Calendar.MILLISECOND };
        		break;
        	case Minute:
        		result = new int[] { java.util.Calendar.SECOND, java.util.Calendar.MILLISECOND };
        		break;
        	case Hour:
        		result = new int[] { java.util.Calendar.MINUTE,
        				java.util.Calendar.SECOND, java.util.Calendar.MILLISECOND };
        		break;
        	case Day:
        		result = new int[] {
        				java.util.Calendar.AM_PM, java.util.Calendar.HOUR,
        				java.util.Calendar.HOUR_OF_DAY, java.util.Calendar.MINUTE,
        				java.util.Calendar.SECOND, java.util.Calendar.MILLISECOND };
        		break;
        	case Week:
        		result = new int[] { // java.util.Calendar.DAY_OF_WEEK, commented out because only works manually
        				java.util.Calendar.AM_PM, java.util.Calendar.HOUR,
        				java.util.Calendar.HOUR_OF_DAY, java.util.Calendar.MINUTE,
        				java.util.Calendar.SECOND, java.util.Calendar.MILLISECOND };
        		break;
        	case Month:
        		result = new int[] { java.util.Calendar.DAY_OF_MONTH,
        				java.util.Calendar.AM_PM, java.util.Calendar.HOUR,
        				java.util.Calendar.HOUR_OF_DAY, java.util.Calendar.MINUTE,
        				java.util.Calendar.SECOND, java.util.Calendar.MILLISECOND };
        		break;
        	case Quarter:
        		result = new int[] { java.util.Calendar.DAY_OF_MONTH,
        				java.util.Calendar.AM_PM, java.util.Calendar.HOUR,
        				java.util.Calendar.HOUR_OF_DAY, java.util.Calendar.MINUTE,
        				java.util.Calendar.SECOND, java.util.Calendar.MILLISECOND };
        		break;
            case Year:
        	case Decade:
        		result = new int[] { java.util.Calendar.DAY_OF_YEAR,
        				java.util.Calendar.AM_PM, java.util.Calendar.HOUR,
        				java.util.Calendar.HOUR_OF_DAY, java.util.Calendar.MINUTE,
        				java.util.Calendar.SECOND, java.util.Calendar.MILLISECOND };
        		break;
        	case Calendar:
        	case Top:
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
		
		switch(Granularities.fromInt(granule.getGranularity().getIdentifier())) {
			case Millisecond:
				switch(Granularities.fromInt(granule.getGranularity().getGranularityContextIdentifier())) {
					case Second:
						result = granule.getInf()%1000L;
						break;
					case Minute:
						result = granule.getInf()%60000L;
						break;
					case Hour:
						result = granule.getInf()%3600000L;
						break;
					case Day:
						result = granule.getInf()%86400000L;
						break;
					case Week: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = ((cal.get(GregorianCalendar.DAY_OF_WEEK)+5)%7)*86400000L + granule.getInf()%86400000L;
						break;
					}
					case Month: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = (cal.get(GregorianCalendar.DAY_OF_MONTH)-1)*86400000L + granule.getInf()%86400000L;
						break;
					}
					case Quarter: 
						result = getDayInQuarter(granule.getInf())*86400000L + granule.getInf()%86400000L;
						break;
					case Year: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = (cal.get(GregorianCalendar.DAY_OF_YEAR)-1)*86400000L + granule.getInf()%86400000L;
						break;
					}
                    case Decade: 
                        throw new UnsupportedOperationException("Not implemented yet.");
					default:
						result = granule.getInf();					
				}
				break;
			case Second:
				result = granule.getInf()/1000L;
				switch(Granularities.fromInt(granule.getGranularity().getGranularityContextIdentifier())) {
					case Minute:
						result %= 60L;
						break;
					case Hour:
						result %= 3600L;
						break;
					case Day:
						result %= 86400L;
						break;
					case Week: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = ((cal.get(GregorianCalendar.DAY_OF_WEEK)+5)%7)*86400L + result % 86400L;
						break;
					}
					case Month: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = (cal.get(GregorianCalendar.DAY_OF_MONTH)-1)*86400L + result % 86400L;
						break;
					}
					case Quarter: 
						result = getDayInQuarter(granule.getInf())*86400L + result % 86400L;
						break;
					case Year: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = (cal.get(GregorianCalendar.DAY_OF_YEAR)-1)*86400L + result % 86400L;
						break;
					}
                    case Decade: 
                        throw new UnsupportedOperationException("Not implemented yet.");
				}
				break;
			case Minute:
				result = granule.getInf()/60000L;
				switch(Granularities.fromInt(granule.getGranularity().getGranularityContextIdentifier())) {
					case Hour:
						result %= 60;
						break;
					case Day:
						result %= 1440;
						break;
					case Week: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = ((cal.get(GregorianCalendar.DAY_OF_WEEK)+5)%7)*1440L + result % 1440L;
						break;
					}
					case Month: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = (cal.get(GregorianCalendar.DAY_OF_MONTH)-1)*1440L + result % 1440L;
						break;
					}
					case Quarter: 
						result = getDayInQuarter(granule.getInf())*1440L + result % 1440L;
						break;
					case Year: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = (cal.get(GregorianCalendar.DAY_OF_YEAR)-1)*1440L + result % 1440L;
						break;
					}
                    case Decade: 
                        throw new UnsupportedOperationException("Not implemented yet.");
				}
				break;
			case Hour:
				result = granule.getInf()/3600000L;
				switch(Granularities.fromInt(granule.getGranularity().getGranularityContextIdentifier())) {
					case Day:
						result %= 24;
						break;
					case Week: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = ((cal.get(GregorianCalendar.DAY_OF_WEEK)+5)%7)*24 + result % 24;
						break;
					}
					case Month: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = (cal.get(GregorianCalendar.DAY_OF_MONTH)-1)*24 + result % 24;
						break;
					}
					case Quarter: 
						result = getDayInQuarter(granule.getInf())*24 + result % 24;
						break;
					case Year: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = (cal.get(GregorianCalendar.DAY_OF_YEAR)-1)*24 + result % 24;
						break;
					}
                    case Decade: 
                        throw new UnsupportedOperationException("Not implemented yet.");
				}
				break;
			case Day:
				switch(Granularities.fromInt(granule.getGranularity().getGranularityContextIdentifier())) {
					case Week: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = (cal.get(GregorianCalendar.DAY_OF_WEEK)+5)%7;
						break;
					}
					case Month: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = cal.get(GregorianCalendar.DAY_OF_MONTH)-1;
						break;
					}
					case Quarter: 
						result = getDayInQuarter(granule.getInf());
						break;
					case Year: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = cal.get(GregorianCalendar.DAY_OF_YEAR)-1;
						break;
					}
                    case Decade:
                        throw new UnsupportedOperationException("Not implemented yet.");
					default:
						result = granule.getInf() / 86400000L;
				}
				break;
			case Week:
				// TODO add context granule to make this "clean"
				switch(Granularities.fromInt(granule.getGranularity().getGranularityContextIdentifier())) {
					case Month: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = cal.get(GregorianCalendar.WEEK_OF_MONTH)-1;
						break;
						}
					case Quarter: {
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
					case Year: {
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(granule.getInf());
						result = cal.get(GregorianCalendar.WEEK_OF_YEAR)-1;
						break;
						}
                    case Decade: 
                        throw new UnsupportedOperationException("Not implemented yet.");
					default:
						result = (granule.getInf() + 259200000L) / 604800000L; // Add the 3 days of week zero that were not in 1970
						break;
				}
				break;
			case Month: {
				GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
				cal.setTimeInMillis(granule.getInf());
				switch(Granularities.fromInt(granule.getGranularity().getGranularityContextIdentifier())) {
					case Quarter:
						result = cal.get(GregorianCalendar.MONTH) % 3;
						break;
					case Year:
						result = cal.get(GregorianCalendar.MONTH);
						break;
                    case Decade: 
                        result = cal.get(GregorianCalendar.MONTH) + 
                                ((cal.get(GregorianCalendar.YEAR)-1) % 10) * 12;
                        break;
					default:
                        result = (cal.get(GregorianCalendar.YEAR)-1970) * 12 +
                                cal.get(GregorianCalendar.MONTH);
						break;
					}
				break;}
			case Quarter: {
                GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
                cal.setTimeInMillis(granule.getInf());
				switch(Granularities.fromInt(granule.getGranularity().getGranularityContextIdentifier())) {
					case Year:
						result = cal.get(GregorianCalendar.MONTH) / 3;
						break;
                    case Decade: 
                        result = cal.get(GregorianCalendar.MONTH) / 3 + 
                                ((cal.get(GregorianCalendar.YEAR)-1) % 10) * 4;
                        break;
					default:
						result = (cal.get(GregorianCalendar.YEAR)-1970) * 4 +
								cal.get(GregorianCalendar.MONTH) / 3;
						break;
				}
				break; }
			case Year: {
				GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
				cal.setTimeInMillis(granule.getInf());
                switch(Granularities.fromInt(granule.getGranularity().getGranularityContextIdentifier())) {
                case Decade: 
                    result = (cal.get(GregorianCalendar.YEAR)-1) % 10;
                    break;
                default:
                    result = cal.get(GregorianCalendar.YEAR)-1970;
                }
				break;}
            case Decade: {
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
	public int getBottomGranularityIdentifier() {
		return 0;
	}
	
	/**
	 * Returns the identifier of the top granularity. This is the highest possible granularity of the calendar.
	 * Usually, this is a granularity where one granule is composed of all the time the calendar is defined for.
	 * Let all calendars that would normally have this be modified so they have one. 
	 * @return the top granularity identifier
	 */
	public int getTopGranularityIdentifier() {
		return Granularities.Top.toInt();
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
		
		switch(Granularities.fromInt(granule.getGranularity().getIdentifier())) {
			case Day:
				if(Granularities.fromInt(granule.getGranularity().getGranularityContextIdentifier()) == Granularities.Week ) {
					GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
					cal.setTimeInMillis(granule.getInf());
					result = cal.getDisplayName(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.LONG, Locale.getDefault());
				} else
					result = String.format("%d",granule.getIdentifier()+1);
				break;
			case Week:
				if(Granularities.fromInt(granule.getGranularity().getGranularityContextIdentifier()) == Granularities.Month || 
					Granularities.fromInt(granule.getGranularity().getGranularityContextIdentifier()) == Granularities.Quarter ) {
					result = String.format("%d",granule.getIdentifier()); 
				}
				else
					result = String.format("%d",granule.getIdentifier()+1);
				break;
			case Month:
				if(Granularities.fromInt(granule.getGranularity().getGranularityContextIdentifier()) == Granularities.Year ) {
					GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
					cal.setTimeInMillis(granule.getInf());
					result = cal.getDisplayName(GregorianCalendar.MONTH, GregorianCalendar.LONG, Locale.getDefault());
				} else
					result = String.format("%d",granule.getIdentifier()+1);
				break;
			case Year:
                if(Granularities.Decade == Granularities.fromInt(granule.getGranularity().getGranularityContextIdentifier())) {
                    result = String.format("%d",granule.getIdentifier() + 1);
                } else {
                    result = String.format("%d",granule.getIdentifier()+1970);
                }
				break;
            case Decade:
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
				
		switch(Granularities.fromInt(granule.getGranularity().getIdentifier())) {
			case Millisecond:
				result = granule.getIdentifier();
				if (contextGranule.getGranularity().getIdentifier() != Granularities.Top.toInt())
					result += contextGranule.getInf();
				break;
			case Second:
				result = granule.getIdentifier()*1000L;
				if (contextGranule.getGranularity().getIdentifier() != Granularities.Top.toInt())
					result += contextGranule.getInf();
				break;
			case Minute:
				result = granule.getIdentifier()*60000L;
				if (contextGranule.getGranularity().getIdentifier() != Granularities.Top.toInt())
					result += contextGranule.getInf();
				break;
			case Hour:
				result = granule.getIdentifier()*3600000L;
				if (contextGranule.getGranularity().getIdentifier() != Granularities.Top.toInt())
					result += contextGranule.getInf();
				break;
			case Day:
			    // Warning does not handle day light saving time 
				result = granule.getIdentifier()*86400000L;
				if (contextGranule.getGranularity().getIdentifier() != Granularities.Top.toInt())
					result += contextGranule.getInf();
				break;
			case Week:				
				switch(Granularities.fromInt(contextGranule.getGranularity().getIdentifier())) {
					case Month:{
						GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
						cal.setTimeInMillis(contextGranule.getInf());
						if(cal.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.FRIDAY ||
							cal.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SATURDAY ||
							cal.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SUNDAY)
							result = cal.getTimeInMillis() + granule.getIdentifier() * 604800000L;
						else
							result = cal.getTimeInMillis() + (granule.getIdentifier()-1) * 604800000L;
						break;}
					case Year:
					case Decade:{
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
					case Calendar:						
					case Top:
					    // 1 Jan 1970 is a Thursday
						result = granule.getIdentifier() * 604800000L - 259200000; // = 3*24*60*60*1000
						break;
					default:
						throw new TemporalDataException("Unknown context granularity");					
				}
				break;
			case Month:{
				GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
				cal.setTimeInMillis(contextGranule.getInf());
				switch(Granularities.fromInt(contextGranule.getGranularity().getIdentifier())) {
				case Quarter:
					cal.set(GregorianCalendar.MONTH, (int)(granule.getIdentifier()*3));
					break;
				case Year:
					cal.set(GregorianCalendar.MONTH, (int)(granule.getIdentifier()));
					break;
				case Decade:
					cal.set(GregorianCalendar.MONTH, (int)(granule.getIdentifier()%12));
					break;
				default:
					cal.setTimeInMillis(0);
	                int year = ((int) granule.getIdentifier()) / 12;
	                if (Granularities.Decade == Granularities.fromInt(contextGranule.getGranularity().getIdentifier())) {
	                    cal.set(GregorianCalendar.YEAR, year+1970+1);
	                } else {
	                    cal.set(GregorianCalendar.YEAR, year+1970);
	                }
					cal.set(GregorianCalendar.MONTH, (int)(granule.getIdentifier()%12));
					result = cal.getTimeInMillis();
				}
				break;}
			case Quarter:{
				GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
				cal.setTimeInMillis(contextGranule.getInf());
				switch(Granularities.fromInt(contextGranule.getGranularity().getIdentifier())) {
				case Year:
					cal.set(GregorianCalendar.MONTH, (int)(granule.getIdentifier())%4*3);
					break;
				case Decade:
					cal.set(GregorianCalendar.MONTH, (int)(granule.getIdentifier())%12*3);
					break;
				default:
					cal.setTimeInMillis(0);
					int year = ((int) granule.getIdentifier()) / 4;
					if (Granularities.Decade == Granularities.fromInt(contextGranule.getGranularity().getIdentifier())) {
						cal.set(GregorianCalendar.YEAR, year+1970+1);
					} else {
						cal.set(GregorianCalendar.YEAR, year+1970);
					}
//					System.out.println(cal.get(GregorianCalendar.YEAR) + " " + year + " " + granule.getGranularity().getGranularityContextIdentifier());
					cal.set(GregorianCalendar.MONTH, (int)(granule.getIdentifier()%4*3));
					result = cal.getTimeInMillis();
				}
				break;}
			case Year:{
				GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
				cal.setTimeInMillis(0);
				int year = ((int) granule.getIdentifier());
				if (Granularities.Decade == Granularities.fromInt(contextGranule.getGranularity().getIdentifier())) {
	                cal.set(GregorianCalendar.YEAR, year+1971);
				} else {
				    cal.set(GregorianCalendar.YEAR, year+1970);
				}
				result = cal.getTimeInMillis();
				break;}
            case Decade:{
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
		
		switch(Granularities.fromInt(granule.getGranularity().getIdentifier())) {
			case Millisecond:
				result = granule.getInf();
				break;
			case Second:
				result = granule.getInf()+999L;
				break;
			case Minute:
				result = granule.getInf()+59999L;
				break;
			case Hour:
				result = granule.getInf()+3599999L;
				break;
			case Day:
				result = granule.getInf()+86399999L;
				break;
			case Week:
                result = granule.getInf() + 604799999;
                break;
			case Month:
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
			case Quarter:
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
			case Year:{
				result = granule.getInf();
				GregorianCalendar cal = new GregorianCalendar(); cal.setTimeZone(TimeZone.getTimeZone("UTC"));
				cal.setTimeInMillis(result);
				if(cal.isLeapYear((int)granule.getIdentifier()+1970))
					result += 31622399999L;
				else
					result += 31535999999L;
				break;}
            case Decade:{
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
		switch(Granularities.fromInt(granularity.getIdentifier())) {
			case Millisecond:
				switch(Granularities.fromInt(granularity.getGranularityContextIdentifier())) {
					case Calendar:
					case Top:
						return Long.MIN_VALUE;
					default:
						return 0;						
				}
			case Second:
				switch(Granularities.fromInt(granularity.getGranularityContextIdentifier())) {
					case Calendar:
					case Top:
						return Long.MIN_VALUE;
					default:
						return 0;						
				}
			case Minute:
				switch(Granularities.fromInt(granularity.getGranularityContextIdentifier())) {
					case Calendar:
					case Top:
						return Long.MIN_VALUE;
					default:
						return 0;						
				}
			case Hour:
				switch(Granularities.fromInt(granularity.getGranularityContextIdentifier())) {
					case Calendar:
					case Top:
						return Long.MIN_VALUE;
					default:
						return 0;						
				}
			case Day:
				switch(Granularities.fromInt(granularity.getGranularityContextIdentifier())) {
					case Calendar:
					case Top:
						return Long.MIN_VALUE;
					default:
						return 0;						
				}
			case Week:
				switch(Granularities.fromInt(granularity.getGranularityContextIdentifier())) {
					case Calendar:
					case Top:
						return Long.MIN_VALUE;
					default:
						return 0;						
				}
			case Month:
				switch(Granularities.fromInt(granularity.getGranularityContextIdentifier())) {
					case Calendar:
					case Top:
						return Long.MIN_VALUE;
					default:
						return 0;						
				}
			case Quarter:
				switch(Granularities.fromInt(granularity.getGranularityContextIdentifier())) {
					case Calendar:
					case Top:
						return Long.MIN_VALUE;
					default:
						return 0;						
				}
			case Year:
            case Decade:
				switch(Granularities.fromInt(granularity.getGranularityContextIdentifier())) {
					case Calendar:
					case Top:
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
		switch(Granularities.fromInt(granularity.getIdentifier())) {
			case Millisecond:
				switch(Granularities.fromInt(granularity.getGranularityContextIdentifier())) {
				    case Millisecond:
				        return 0L;
					case Second:
						return 999L;
					case Minute:
						return 59999L;
					case Hour:
						return 3599999L;
					case Day:
						return 86399999L;
					case Week:
						return 604799999L;
					case Month:
						return 2678399999L;
					case Quarter:
						return 7948799999L;
					case Year:
						return 31622399999L;
					case Decade:
                        return 315619199999L;
					case Calendar:
					case Top:
						return Long.MAX_VALUE;
					default:
	                    throw new UnsupportedOperationException();
//						return 1000L;						
				}
			case Second:
				switch(Granularities.fromInt(granularity.getGranularityContextIdentifier())) {
					case Minute:
						return 59L;
					case Hour:
						return 3599L;
					case Day:
						return 86399L;
					case Week:
						return 604799L;
					case Month:
						return 2678399L;
					case Quarter:
						return 7948799L;
					case Year:
						return 31622399L;
                    case Decade:
                        return 315619199L;
					case Calendar:
					case Top:
						return Long.MAX_VALUE;
					default:
	                    throw new UnsupportedOperationException();
				}
			case Minute:
				switch(Granularities.fromInt(granularity.getGranularityContextIdentifier())) {
					case Hour:
						return 59L;
					case Day:
						return 1439L;
					case Week:
						return 10079L;
					case Month:
						return 44639L;
					case Quarter:
						return 132479L;
					case Year:
						return 527039L;
                    case Decade:
                        return 5260319L;
					case Calendar:
						case Top:
						return Long.MAX_VALUE;
					default:
	                    throw new UnsupportedOperationException();
			}
		case Hour:
			switch(Granularities.fromInt(granularity.getGranularityContextIdentifier())) {
				case Day:
					return 23L;
				case Week:
					return 167L;
				case Month:
					return 743L;
				case Quarter:
					return 2207L;
				case Year:
					return 8783L;
                case Decade:
                    return 87671L;
				case Calendar:
				case Top:
					return Long.MAX_VALUE;
				default:
                    throw new UnsupportedOperationException();
			}
		case Day:
			switch(Granularities.fromInt(granularity.getGranularityContextIdentifier())) {
				case Week:
					return 6L;
				case Month:
					return 30L;
				case Quarter:
					return 91L;				
				case Year:
					return 365L;				
                case Decade:
                    return 3652L;
				case Calendar:
				case Top:
					return Long.MAX_VALUE;
				default:
                    throw new UnsupportedOperationException();
			}
		case Week:
			switch(Granularities.fromInt(granularity.getGranularityContextIdentifier())) {
				case Month:
					return 5L;
				case Quarter:
					return 13L;
				case Year:
					return 52L;				
                case Decade:
                    return 521L;
				case Calendar:
				case Top:
					return Long.MAX_VALUE;
				default:
                    throw new UnsupportedOperationException();
			}
		case Month:
			switch(Granularities.fromInt(granularity.getGranularityContextIdentifier())) {
				case Quarter:
					return 2L;
				case Year:
					return 11L;				
                case Decade:
                    return 119; // = 10 * 12 - 1
				case Calendar:
				case Top:
					return Long.MAX_VALUE;
				default:
                    throw new UnsupportedOperationException();
			}
		case Quarter:
			switch(Granularities.fromInt(granularity.getGranularityContextIdentifier())) {
				case Year:
					return 3L;
                case Decade:
                    return 39; // = 10 * 4 - 1
				case Calendar:
				case Top:
					return Long.MAX_VALUE;
				default:
                    throw new UnsupportedOperationException();
			}
		case Year:
			switch(Granularities.fromInt(granularity.getGranularityContextIdentifier())) {
			    case Decade:
			        return 9;
				case Calendar:
				case Top:
					return Long.MAX_VALUE;
				default:
                    throw new UnsupportedOperationException();
			}
        case Decade:
            switch(Granularities.fromInt(granularity.getGranularityContextIdentifier())) {
                case Calendar:
                case Top:
                    return Long.MAX_VALUE;
                default:
                    throw new UnsupportedOperationException();
            }
	}
		
		return 0;
	}

	@Override
	public long getMaxLengthInIdentifiers(Granularity granularity) throws TemporalDataException {
		switch(Granularities.fromInt(granularity.getIdentifier())) {
			case Millisecond:
				switch(Granularities.fromInt(granularity.getGranularityContextIdentifier())) {
				    case Millisecond:
				        return 1L;
					case Second:
						return 1000L;
					case Minute:
						return 60000L;
					case Hour:
						return 3600000L;
					case Day:
						return 86400000L;
					case Week:
						return 604800000L;
					case Month:
						return 2678400000L;
					case Quarter:
						return 7948800000L;
					case Year:
						return 31622400000L;
					case Decade:
                        return 315619200000L;
					case Calendar:
					case Top:
						return Long.MAX_VALUE;
					default:
	                    throw new UnsupportedOperationException();
//						return 1000L;						
				}
			case Second:
				switch(Granularities.fromInt(granularity.getGranularityContextIdentifier())) {
					case Minute:
						return 60L;
					case Hour:
						return 3600L;
					case Day:
						return 86400L;
					case Week:
						return 604800L;
					case Month:
						return 267840L;
					case Quarter:
						return 7948800L;
					case Year:
						return 31622400L;
                    case Decade:
                        return 315619200L;
					case Calendar:
					case Top:
						return Long.MAX_VALUE;
					default:
	                    throw new UnsupportedOperationException();
				}
			case Minute:
				switch(Granularities.fromInt(granularity.getGranularityContextIdentifier())) {
					case Hour:
						return 60L;
					case Day:
						return 1440L;
					case Week:
						return 10080L;
					case Month:
						return 44640L;
					case Quarter:
						return 132480L;
					case Year:
						return 527040L;
                    case Decade:
                        return 5260320L;
					case Calendar:
						case Top:
						return Long.MAX_VALUE;
					default:
	                    throw new UnsupportedOperationException();
			}
		case Hour:
			switch(Granularities.fromInt(granularity.getGranularityContextIdentifier())) {
				case Day:
					return 24L;
				case Week:
					return 168L;
				case Month:
					return 744L;
				case Quarter:
					return 2208L;
				case Year:
					return 8784L;
                case Decade:
                    return 87672L;
				case Calendar:
				case Top:
					return Long.MAX_VALUE;
				default:
                    throw new UnsupportedOperationException();
			}
		case Day:
			switch(Granularities.fromInt(granularity.getGranularityContextIdentifier())) {
				case Week:
					return 7L;
				case Month:
					return 31L;
				case Quarter:
					return 92L;				
				case Year:
					return 366L;				
                case Decade:
                    return 3653L;
				case Calendar:
				case Top:
					return Long.MAX_VALUE;
				default:
                    throw new UnsupportedOperationException();
			}
		case Week:
			switch(Granularities.fromInt(granularity.getGranularityContextIdentifier())) {
				case Month:
					return 6L;
				case Quarter:
					return 14L;
				case Year:
					return 53L;				
                case Decade:
                    return 521L;
				case Calendar:
				case Top:
					return Long.MAX_VALUE;
				default:
                    throw new UnsupportedOperationException();
			}
		case Month:
			switch(Granularities.fromInt(granularity.getGranularityContextIdentifier())) {
				case Quarter:
					return 3L;
				case Year:
					return 12L;				
                case Decade:
                    return 120L;
				case Calendar:
				case Top:
					return Long.MAX_VALUE;
				default:
                    throw new UnsupportedOperationException();
			}
		case Quarter:
			switch(Granularities.fromInt(granularity.getGranularityContextIdentifier())) {
				case Year:
					return 4L;
                case Decade:
                    return 40L;
				case Calendar:
				case Top:
					return Long.MAX_VALUE;
				default:
                    throw new UnsupportedOperationException();
			}
		case Year:
			switch(Granularities.fromInt(granularity.getGranularityContextIdentifier())) {
			    case Decade:
			        return 10L;
				case Calendar:
				case Top:
					return Long.MAX_VALUE;
				default:
                    throw new UnsupportedOperationException();
			}
        case Decade:
            switch(Granularities.fromInt(granularity.getGranularityContextIdentifier())) {
                case Calendar:
                case Top:
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