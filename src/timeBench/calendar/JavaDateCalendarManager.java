package timeBench.calendar;

import timeBench.data.TemporalDataException;

/**
 * 
 * 
 * <p>
 * Added:          / TL<br>
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
	
	public JavaDateCalendarManager() {
		javaCalendar = java.util.Calendar.getInstance();
		javaCalendar.setLenient(true);
	}
	
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
		
		public int toInt()
		{
			return intValue;
		}
		
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
	
	public static JavaDateCalendarManager getDefaultSystem() {
		if (defaultSystem == null)
			defaultSystem = new JavaDateCalendarManager();
		return defaultSystem;
	}	
	
	public Calendar calendar() {
		return new Calendar(this);
	}
	
	public Calendar getDefaultCalendar() {
		if (defaultCalendar == null)
			defaultCalendar = calendar();
		return defaultCalendar;
	}

	/**
	 * @param timeStamp
	 * @param granules
	 * @param identifier
	 * @return
	 */
	public long before(long timeStamp, int granules, int identifier) throws TemporalDataException {
		javaCalendar.setTimeInMillis(timeStamp);
		javaCalendar.add(beforeAfterSwitch(identifier), -granules * (Granularities.fromInt(identifier) == Granularities.Quarter ? 3 : 1));
		return javaCalendar.getTimeInMillis();
	}
	
	/**
	 * @param timeStamp
	 * @param granules
	 * @param identifier
	 * @return
	 */
	public long after(long timeStamp, int granules, int identifier) throws TemporalDataException {
		javaCalendar.setTimeInMillis(timeStamp);
		javaCalendar.add(beforeAfterSwitch(identifier), granules * (Granularities.fromInt(identifier) == Granularities.Quarter ? 3 : 1));
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
