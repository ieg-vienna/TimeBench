package timeBench.calendar;

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
public class CalendarManagerFactory {
	public static CalendarManager getSingleton(CalendarManagers type) {
		CalendarManager result = null;
		
		switch(type) {
			case JavaDate:
				result = JavaDateCalendarManager.getSingleton();
				break;
		}
		
		return result;
	}
	
	public static CalendarManager getNewInstance(CalendarManagers type) {
		CalendarManager result = null;
		
		switch(type) {
			case JavaDate:
				result = new JavaDateCalendarManager();
				break;
		}
		
		return result;
	}
}
