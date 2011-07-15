package timeBench.data;

/**
 * This exception is thrown when temporal data classes are accessed in an illegal way.
 * 
 * <p>
 * Added:          2011-07-15 / TL<br>
 * Modifications:
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public class TemporalDataException extends Exception {
	private static final long serialVersionUID = 0;
	
	public TemporalDataException(String message) {
		super(message);
	}
}
