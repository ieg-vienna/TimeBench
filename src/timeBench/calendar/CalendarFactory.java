package timeBench.calendar;

/**
 * A factory class for all classes in the calendar package.
 * 
 * <p>
 * Added:         2013-07-23 / TL<br>
 * Modifications: 
 * </p>
 * 
 * @author Tim Lammarsch
 *
 *31 30 29 28 27 26 25 24 23 22 21 20 19 18 17 16 15 14 13 12 11 10 09 08 07 06 05 04 03 02 01 00

 M  M  M  M  M  V  V  V  V  V  V  V  V  C  C  C  C  C  C  C  T  T  T  T  T  G  G  G  G  G  G  G
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
	
	public CalendarManager createCalendarManager(int identifier) {
		return createCalendarManager(identifier,false);
	}
	
	/**
	 * Creates and returns an instance of the calendar manager described by the identifier
	 * @param identifier the identifier to get the calendar manager for
	 * @param enforceVersion true means that a special version of a calendar manager is needed, otherwise that does not matter
	 * @return
	 */
	public CalendarManager createCalendarManager(int identifier,boolean enforceVersion) {
		if ((enforceVersion && identifier == JavaDateCalendarManager.getIdentifier()) || 
				(identifier & 0x0000) == (JavaDateCalendarManager.getIdentifier() & 0x0000))
			return JavaDateCalendarManager.getSingleton();
		else
			return null;
	}
	
	public Calendar getCalendar(int identifier) {
		return null;
	}
}
