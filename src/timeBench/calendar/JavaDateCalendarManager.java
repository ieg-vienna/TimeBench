package timeBench.calendar;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

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
	 * The default constructor.
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
				case 16383: return Granularities.Calendar; 
				case 32767: return Granularities.Top; 
				default: throw new TemporalDataException("Unknown Granularity");
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

	
	/**
	 * Calculate the timeStamp which is a number of granules in a given granularity before another timeStamp.
	 * @param timeStamp The base timeStamp.
	 * @param granules The number of granules.
	 * @param granularityIdentifier The granularityIdentifier given as integer (which might have different meaning based on the calendar and calendarManager).
	 * @return The resulting timeStamp.
	 */
	public long before(long timeStamp, long granules, int granularityIdentifier) throws TemporalDataException {
		javaCalendar.setTimeInMillis(timeStamp);
		javaCalendar.add(beforeAfterSwitch(granularityIdentifier), (int) (-granules * (Granularities.fromInt(granularityIdentifier) == Granularities.Quarter ? 3 : 1)));
		return javaCalendar.getTimeInMillis();
	}
	
	
	/**
	 * Calculate the timeStamp which is a number of granules in a given granularity after another timeStamp.
	 * @param timeStamp The base timeStamp.
	 * @param granules The number of granules.
	 * @param granularityIdentifier The granularityIdentifier given as integer (which might have different meaning based on the calendar and calendarManager).
	 * @return The resulting timeStamp.
	 */
	public long after(long timeStamp, long granules, int granularityIdentifier) throws TemporalDataException {
		javaCalendar.setTimeInMillis(timeStamp);
		javaCalendar.add(beforeAfterSwitch(granularityIdentifier), (int) (granules * (Granularities.fromInt(granularityIdentifier) == Granularities.Quarter ? 3 : 1)));
		return javaCalendar.getTimeInMillis();
	}	
	
	
	private int beforeAfterSwitch(int identifier) throws TemporalDataException {
		switch(Granularities.fromInt(identifier)) {
			case Millisecond:
				return java.util.Calendar.MILLISECOND;
			case Second:
				return java.util.Calendar.SECOND;
			case Minute:
				return java.util.Calendar.MINUTE;
			case Hour:
				return java.util.Calendar.HOUR_OF_DAY;
			case Day:
				return java.util.Calendar.DAY_OF_MONTH;
			case Week:
				return java.util.Calendar.WEEK_OF_YEAR;
			case Month:
				return java.util.Calendar.MONTH;
			case Quarter:
				return java.util.Calendar.MONTH;
			case Year:
				return java.util.Calendar.YEAR;
			default: throw new TemporalDataException("Unknown Granularity");
		}
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
		//long resultTimeStamp = 0;
		
		throw new TemporalDataException("No mappings defined yet.");
		
		//return resultTimeStamp;
	}
	
	/**
	 * Converts a granule in a granularity to another granularity and returns a list of all granules that are part of
	 * it. Use heuristics if necessary.
	 * @param timeStamp The number of the granule in the original granularity.
	 * @param sourceGranularity Identifier of the source granularity
	 * @param targetGranularity Identifier of the target granularity
	 * @return The list of numbers of the corresponding granules in the new granularity.
	 * @throws TemporalDataException 
	 */
	public java.util.ArrayList<Granule> mapGranuleToGranularityAsGranuleList(long timeStamp,
			int sourceGranularity, int targetGranularity) throws TemporalDataException {
		
		throw new TemporalDataException("No mappings defined yet.");
		
		//return resultTimeStamp;
	}


//    /*
//     * (non-Javadoc)
//     * 
//     * @see
//     * timeBench.calendar.CalendarManager#parseStringToGranule(java.lang.String,
//     * int)
//     */
//    @Override
//    public Granule parseStringToGranule(String input, Granularity granularity)
//            throws ParseException, TemporalDataException {
//        return parseStringToGranule(input, granularity,
//                DateFormat.getDateInstance(java.text.DateFormat.DEFAULT,
//                        Locale.US));
//    }
//
//    @Override
//    public Granule parseStringToGranule(String input, Granularity granularity,
//            String dateTimePattern) throws ParseException,
//            TemporalDataException {
//        // TODO allow specification of Locale via encoding (e.g. needed for month names)
//        return parseStringToGranule(input, granularity,
//                new java.text.SimpleDateFormat(dateTimePattern, Locale.US));
//    }

    private Granule parseStringToGranule(String input, Granularity granularity,
            DateFormat format) throws ParseException, TemporalDataException {
        return parseDateToGranule(format.parse(input), granularity);
    }

    @Override
    public Granule parseDateToGranule(Date date, Granularity granularity)
            throws TemporalDataException {
    	return createGranule(date, granularity);
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
        		result = new int[] { java.util.Calendar.DAY_OF_WEEK,
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
        		result = new int[] { java.util.Calendar.DAY_OF_YEAR,
        				java.util.Calendar.AM_PM, java.util.Calendar.HOUR,
        				java.util.Calendar.HOUR_OF_DAY, java.util.Calendar.MINUTE,
        				java.util.Calendar.SECOND, java.util.Calendar.MILLISECOND };
        		break;
        	default:
        		throw new TemporalDataException("Granularity not implemented yet");
        }

        return result;    	
    }

	private Granule createGranule(long chronon, Granularity granularity) throws TemporalDataException {
		GregorianCalendar calInf = new GregorianCalendar();
		GregorianCalendar calSup = new GregorianCalendar();
		calInf.setTimeInMillis(chronon);
		calSup.setTimeInMillis(chronon);

		return createGranule(calInf,calSup,granularity);
	}
	
	private Granule createGranule(Date date, Granularity granularity) throws TemporalDataException {
		GregorianCalendar calInf = new GregorianCalendar();
		GregorianCalendar calSup = new GregorianCalendar();
		calInf.setTime(date);
		calSup.setTime(date);

		return createGranule(calInf,calSup,granularity);
	}
	
	private Granule createGranule(GregorianCalendar calInf, GregorianCalendar calSup, Granularity granularity) throws TemporalDataException {
		for (int field : buildGranularityListForCreateGranule(granularity)) {
			calInf.set(field, calInf.getActualMinimum(field));
			calSup.set(field, calSup.getActualMaximum(field));		
		}
		
		if(granularity.getIdentifier() == Granularities.Quarter.intValue) {
			switch(calInf.get(GregorianCalendar.MONTH)) {
				case GregorianCalendar.JANUARY:
				case GregorianCalendar.FEBRUARY:
				case GregorianCalendar.MARCH:
					calInf.set(GregorianCalendar.MONTH,GregorianCalendar.JANUARY);
					calSup.set(GregorianCalendar.MONTH,GregorianCalendar.MARCH);
					break;
				case GregorianCalendar.APRIL:
				case GregorianCalendar.MAY:
				case GregorianCalendar.JUNE:
					calInf.set(GregorianCalendar.MONTH,GregorianCalendar.APRIL);
					calSup.set(GregorianCalendar.MONTH,GregorianCalendar.JUNE);
					break;
				case GregorianCalendar.JULY:
				case GregorianCalendar.AUGUST:
				case GregorianCalendar.SEPTEMBER:
					calInf.set(GregorianCalendar.MONTH,GregorianCalendar.JULY);
					calSup.set(GregorianCalendar.MONTH,GregorianCalendar.SEPTEMBER);
					break;
				case GregorianCalendar.OCTOBER:
				case GregorianCalendar.NOVEMBER:
				case GregorianCalendar.DECEMBER:
					calInf.set(GregorianCalendar.MONTH,GregorianCalendar.OCTOBER);
					calSup.set(GregorianCalendar.MONTH,GregorianCalendar.DECEMBER);
					break;
			}
		}
		
		return new Granule(calInf.getTimeInMillis(),calSup.getTimeInMillis(),Granule.MODE_FORCE,granularity);
	}


	/* (non-Javadoc)
	 * @see timeBench.calendar.CalendarManager#getGranularityIdentifiers()
	 */
	@Override
	public int[] getGranularityIdentifiers() {
		return new int[] {0,1,2,3,4,5,6,7,8,32767};
	}


	/* (non-Javadoc)
	 * @see timeBench.calendar.CalendarManager#parseInfToGranule(long)
	 */
	@Override
	public Granule parseInfToGranule(long inf,Granularity granularity) throws TemporalDataException {
		return createGranule(inf,granularity);
	}


	@Override
	public long getGranuleIdentifier(Granule granule) throws TemporalDataException {
		long result = 0;
		
		switch(Granularities.fromInt(granule.getGranularity().getIdentifier())) {
			case Millisecond:
				result = granule.getInf();
				break;
			case Second:
				result = granule.getInf()/1000;
				break;
			case Minute:
				result = granule.getInf()/60000;
				break;
			case Hour:
				switch(Granularities.fromInt(granule.getGranularity().getGranularityContextIdentifier())) {
				case Day:
					GregorianCalendar cal = new GregorianCalendar();
					cal.setTimeInMillis(granule.getInf());
					result = cal.get(GregorianCalendar.HOUR_OF_DAY);
					break;
				default:
					result = granule.getInf()/360000;
				}
				break;
			case Day:
				switch(Granularities.fromInt(granule.getGranularity().getGranularityContextIdentifier())) {
					case Week:
						GregorianCalendar cal = new GregorianCalendar();
						cal.setTimeInMillis(granule.getInf());
						result = (cal.get(GregorianCalendar.DAY_OF_WEEK)+5)%7;
						break;
					default:
						result = granule.getInf()/8640000;
				}
				break;
			case Week:
				switch(Granularities.fromInt(granule.getGranularity().getGranularityContextIdentifier())) {
					case Month:
						GregorianCalendar cal = new GregorianCalendar();
						cal.setTimeInMillis(granule.getInf());
						result = cal.get(GregorianCalendar.WEEK_OF_MONTH);
						break;
					default:
						result = granule.getInf()/60480000;
						break;
				}
				break;
			case Month:{
				GregorianCalendar cal = new GregorianCalendar();
				cal.setTimeInMillis(granule.getInf());
				switch(Granularities.fromInt(granule.getGranularity().getGranularityContextIdentifier())) {
					case Quarter:
						result = (cal.get(GregorianCalendar.MONTH)-1)%4;
						break;
					case Year:
						result = cal.get(GregorianCalendar.MONTH)-1;
						break;
					default:
						result = (cal.get(GregorianCalendar.YEAR)-1970)*12+(cal.get(GregorianCalendar.MONTH)-1);
						break;
					}
				break;}
			case Quarter:
				switch(Granularities.fromInt(granule.getGranularity().getGranularityContextIdentifier())) {
					case Year:{
						GregorianCalendar cal = new GregorianCalendar();
						cal.setTimeInMillis(granule.getInf());
						result = (cal.get(GregorianCalendar.MONTH)-1)/3;
						break;}
					default:{
						GregorianCalendar cal = new GregorianCalendar();
						cal.setTimeInMillis(granule.getInf());
						result = (cal.get(GregorianCalendar.YEAR)-1970)*4+(cal.get(GregorianCalendar.MONTH)-1)/3;
					break;}
				}
				break;
			case Year:
				GregorianCalendar cal = new GregorianCalendar();
				result = cal.get(GregorianCalendar.YEAR)-1970;
				break;
		}
		
		return result;
	}


	@Override
	public Long getInf(Granule granule) throws TemporalDataException {
		long result = 0;
		
		switch(Granularities.fromInt(granule.getGranularity().getIdentifier())) {
			case Millisecond:
				result = granule.getIdentifier();
				break;
			case Second:
				result = granule.getIdentifier()*1000;
				break;
			case Minute:
				result = granule.getIdentifier()*60000;
				break;
			case Hour:
				result = granule.getIdentifier()*360000;
				break;
			case Day:
				result = granule.getIdentifier()*8640000;
				break;
			case Week:
				result = granule.getIdentifier()*60480000;
				break;
			case Month:{
				GregorianCalendar cal = new GregorianCalendar();
				cal.setTimeInMillis(0);
				cal.set(GregorianCalendar.YEAR, (int)(granule.getIdentifier()/12+1970));
				cal.set(GregorianCalendar.MONTH, (int)(granule.getIdentifier()%12+1));
				result = cal.getTimeInMillis();
				break;}
			case Quarter:{
				GregorianCalendar cal = new GregorianCalendar();
				cal.setTimeInMillis(0);
				cal.set(GregorianCalendar.YEAR, (int)(granule.getIdentifier()/4+1970));
				cal.set(GregorianCalendar.MONTH, (int)(granule.getIdentifier()%4*3+1));
				result = cal.getTimeInMillis();
				break;}
			case Year:{
				GregorianCalendar cal = new GregorianCalendar();
				cal.setTimeInMillis(0);
				cal.set(GregorianCalendar.YEAR, (int)(granule.getIdentifier()+1970));
				result = cal.getTimeInMillis();
				break;}
		}
		
		return result;
	}


	@Override
	public Long getSup(Granule granule) throws TemporalDataException {
		long result = 0;
		
		switch(Granularities.fromInt(granule.getGranularity().getIdentifier())) {
			case Millisecond:
				result = granule.getIdentifier();
				break;
			case Second:
				result = granule.getIdentifier()*1000+999;
				break;
			case Minute:
				result = granule.getIdentifier()*60000+59999;
				break;
			case Hour:
				result = granule.getIdentifier()*360000+359999;
				break;
			case Day:
				result = granule.getIdentifier()*8640000+8639999;
				break;
			case Week:
				result = granule.getIdentifier()*60480000+60479999;
				break;
			case Month:{
				GregorianCalendar cal = new GregorianCalendar();
				cal.setTimeInMillis(0);
				cal.set(GregorianCalendar.YEAR, (int)(granule.getIdentifier()/12+1970));
				cal.set(GregorianCalendar.MONTH, (int)(granule.getIdentifier()%12+1));
				result = cal.getTimeInMillis()+cal.getActualMaximum(GregorianCalendar.DAY_OF_MONTH)*8640000-1;
				break;}
			case Quarter:{
				GregorianCalendar cal = new GregorianCalendar();
				cal.setTimeInMillis(0);
				cal.set(GregorianCalendar.YEAR, (int)(granule.getIdentifier()/4+1970));
				cal.set(GregorianCalendar.MONTH, (int)(granule.getIdentifier()%4*3+1));
				result = cal.getTimeInMillis()+cal.getActualMaximum(GregorianCalendar.DAY_OF_MONTH)*8640000-1;
				cal.add(GregorianCalendar.MONTH, 1);
				result += cal.getActualMaximum(GregorianCalendar.DAY_OF_MONTH)*8640000;
				cal.add(GregorianCalendar.MONTH, 1);
				result += cal.getActualMaximum(GregorianCalendar.DAY_OF_MONTH)*8640000;
				break;}
			case Year:{
				GregorianCalendar cal = new GregorianCalendar();
				cal.setTimeInMillis(0);
				cal.set(GregorianCalendar.YEAR, (int)(granule.getIdentifier()+1970));
				result = cal.getTimeInMillis()+cal.getActualMaximum(GregorianCalendar.DAY_OF_YEAR)*8640000-1;
				break;}
		}
		
		return result;
	}
	
	public int getBottomGranularityIdentifier() {
		return 0;
	}
	
	public int getTopGranularityIdentifier() {
		return 16383;
	}
}