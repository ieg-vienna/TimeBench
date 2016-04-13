package org.timebench.data;

/**
 * This exception is thrown when temporal data classes are accessed in an illegal way.
 * 
 * <p>
 * Added:          2011-07-15 / TL<br>
 * Modifications:  2012-02-14 / AR: add usual constructors<br>
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public class TemporalDataException extends Exception {
	private static final long serialVersionUID = 0;
	
	/**
	 * The default constructor.
	 * @param message The message of the exception.
	 */
	public TemporalDataException(String message) {
		super(message);
	}

    public TemporalDataException(Throwable cause) {
        super(cause);
    }
    
    public TemporalDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
