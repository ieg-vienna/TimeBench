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
	static JavaDateSystem defaultSystem = null;
	
	static Calendar calendar() {
		if (defaultSystem == null)
			defaultSystem = new JavaDateSystem();
		return defaultSystem.calendar();
	}
	
	Calendar calendar() {
		return new Calendar();
	}
}
