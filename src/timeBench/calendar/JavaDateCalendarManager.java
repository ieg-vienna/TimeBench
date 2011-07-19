package timeBench.calendar;

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
public class JavaDateCalendarManager {
	protected static JavaDateCalendarManager defaultSystem = null;
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
		Year (8);

		
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
				default: throw new TemporalDataException("Unknown Granularity");
			}
		}
	}
	
	
	/**
	 * Provides access to an instance of the class. This is not a generic factiory method. It
	 * does only create one instance and provides that one with every call.
	 * @return The JavaDateCalendarManager instance.
	 */
	public static JavaDateCalendarManager getDefaultSystem() {
		if (defaultSystem == null)
			defaultSystem = new JavaDateCalendarManager();
		return defaultSystem;
	}	
	
	
	/**
	 * Generates an instance of a calendar, the only one currently provided by this class.
	 * @return The calendar.
	 */
	public Calendar calendar() {
		return new Calendar(this);
	}
	
	
	/**
	 * Provides access to an instance of a calendar, the only one currently provided by this class.
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
	public long before(long timeStamp, int granules, int granularityIdentifier) throws TemporalDataException {
		javaCalendar.setTimeInMillis(timeStamp);
		javaCalendar.add(beforeAfterSwitch(granularityIdentifier), -granules * (Granularities.fromInt(granularityIdentifier) == Granularities.Quarter ? 3 : 1));
		return javaCalendar.getTimeInMillis();
	}
	
	
	/**
	 * Calculate the timeStamp which is a number of granules in a given granularity after another timeStamp.
	 * @param timeStamp The base timeStamp.
	 * @param granules The number of granules.
	 * @param granularityIdentifier The granularityIdentifier given as integer (which might have different meaning based on the calendar and calendarManager).
	 * @return The resulting timeStamp.
	 */
	public long after(long timeStamp, int granules, int granularityIdentifier) throws TemporalDataException {
		javaCalendar.setTimeInMillis(timeStamp);
		javaCalendar.add(beforeAfterSwitch(granularityIdentifier), granules * (Granularities.fromInt(granularityIdentifier) == Granularities.Quarter ? 3 : 1));
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
}
