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
public class JavaDateSystem {
	protected static JavaDateSystem defaultSystem = null;
	protected Calendar defaultCalendar = null;
	
	public static JavaDateSystem getDefaultSystem() {
		if (defaultSystem == null)
			defaultSystem = new JavaDateSystem();
		return defaultSystem;
	}	
	
	public Calendar calendar() {
		return new Calendar();
	}
	
	public Calendar getDefaultCalendar() {
		if (defaultCalendar == null)
			defaultCalendar = calendar();
		return defaultCalendar;
	}
}
