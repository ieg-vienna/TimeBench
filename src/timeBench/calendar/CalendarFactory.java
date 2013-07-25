package timeBench.calendar;

/**
 * A factory class for all classes in the calendar package.
 * 
 * The buildup of the granularity identifier is like this:
 * 31 30 29 28 27 26 25 24 23 22 21 20 19 18 17 16 15 14 13 12 11 10 09 08 07 06 05 04 03 02 01 00
 *  M  M  M  M  M  V  V  V  V  V  V  V  V  C  C  C  C  C  C  C  T  T  T  T  T  G  G  G  G  G  G  G
 *  
 * M: Calendar Manager
 * V: Version of the Calendar Manager
 * C: Calendar
 * T: Type Granularity (e.g., western modern (week), metric (kilosecond), ...)
 * G: Granularity (e.g., week, kilosecond, ...)
 *
 * <p>
 * Added:         2013-07-23 / TL<br>
 * Modifications: 
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */

public class CalendarFactory {
	private static CalendarFactory singleton = new CalendarFactory();
	
	/**
	 * Static method to get a singleton of this class that always exists.
	 * 
	 * @return the singleton
	 */
	public static CalendarFactory getSingleton() {
		return singleton;
	}
	
	/**
	 * Creates if necessary and returns an instance of the calendar manager described by the identifier
	 * (ignoring the version).
	 * @param identifier the identifier to get the calendar manager for
	 * @return the calendar manager if possible, null otherwise
	 */
	public CalendarManager getCalendarManager(int identifier) {
		return getCalendarManager(identifier,false);
	}
	
	/**
	 * Creates if necessary and returns an instance of the calendar manager described by the identifier.
	 * @param identifier the identifier to get the calendar manager for
	 * @param enforceVersion true means that a special version of a calendar manager is needed, otherwise that does not matter
	 * @return the calendar manager if possible, null otherwise
	 */
	public CalendarManager getCalendarManager(int identifier,boolean enforceVersion) {
		if ((enforceVersion && identifier == JavaDateCalendarManager.getIdentifier()) || 
				(identifier & 0x00) == (JavaDateCalendarManager.getIdentifier() & 0x00))
			return JavaDateCalendarManager.getSingleton();
		else
			return null;
	}
	
	/**
	 * Creates if necessary and returns an instance of the calendar described by the identifier
	 * (ignoring the version).
	 * Can create a calendar manager on demand.
	 * @param identifier the identifier to get the calendar for
	 * @return the calendar manager if possible, null otherwise
	 */
	public Calendar getCalendar(int identifier) {
		return getCalendar(identifier,false);
	}
	
	/**
	 * Creates if necessary and returns an instance of the calendar described by the identifier.
	 * Can create a calendar manager on demand.
	 * @param identifier the identifier to get the calendar for
	 * @param enforceVersion true means that a special version of a calendar manager is needed, otherwise that does not matter
	 * @return the calendar manager if possible, null otherwise
	 */
	public Calendar getCalendar(int identifier,boolean enforceVersion) {
		CalendarManager calendarManager = getCalendarManager(
				getCalendarManagerIdentifierFromCalendarIdentifier(identifier),enforceVersion);
		if(calendarManager == null)
			return null;
		else {
			return calendarManager.getCalendar(identifier);
		}
	}

	/**
	 * Creates and returns an instance of a granularity described by the identifier and context identifier
	 * (ignoring the version).
	 * Can create a calendar manager and calendar on demand.
	 * @param granularityID the identifier of the granularity
	 * @param contextGranularityID the identifier of the granularity's context granularity
	 * @return the granularity if possible, null otherwise
	 */
	public Granularity getGranularity(int granularityID, int contextGranularityID) {
		return getGranularity(granularityID, contextGranularityID,false);
	}
	
	/**
	 * Creates and returns an instance of a granularity described by the identifier and context identifier.
	 * Can create a calendar manager and calendar on demand.
	 * @param granularityID the identifier of the granularity
	 * @param contextGranularityID the identifier of the granularity's context granularity
	 * @param enforceVersion true means that a special version of a calendar manager is needed, otherwise that does not matter
	 * @return the granularity if possible, null otherwise
	 */
	public Granularity getGranularity(int granularityID, int contextGranularityID,boolean enforceVersion) {
		int calendarIdentifier = getCalendarIdentifierFromGranularityIdentifier(granularityID);
		if (calendarIdentifier != getCalendarIdentifierFromGranularityIdentifier(contextGranularityID))
			return null;
		Calendar calendar = getCalendar(calendarIdentifier,enforceVersion);
		if (calendar == null)
			return null;
		else {
			return new Granularity(calendar,granularityID,contextGranularityID);
		}
	}

	/**
	 * Calculates the calendar manager, version, and calendar parts of a granularity identifier from
	 * a calendar identifier.
	 * @param identifier
	 * @return the resulting identifier
	 */
	public int getGranularityIdentifierSummandFromCalendarIdentifier(
			int identifier) {
		return identifier << 12;
	}
	
	/**
	 * Calculates the calendar manager identifier from
	 * a calendar identifier.
	 * @param identifier
	 * @return the resulting identifier
	 */
	public int getCalendarManagerIdentifierFromCalendarIdentifier(
			int identifier) {
		return identifier >> 7;
	}
	
	/**
	 * Calculates the calendar identifier from
	 * a granularity identifier.
	 * @param identifier
	 * @return the resulting identifier
	 */
	public int getCalendarIdentifierFromGranularityIdentifier(
			int identifier) {
		return identifier >> 12;
	}
}
