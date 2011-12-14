package timeBench.action.analytical;

/**
 * The Settings for TimeAggregationTree per granularity.
 * 
 * <p>
 * Added:         2011-12-12 / TL<br>
 * Modifications: 
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public class GranularityAggregationSettings implements java.io.Serializable {

	private static final long serialVersionUID = 1380115803468529882L;
	private int identifier;
	private int contextIdentifier;

	/**
	 * The parameterless constructor is only for serialization.
	 */
	public @Deprecated GranularityAggregationSettings() {		
	}
	
	/**
	 * The constructor has to provide the granularity information.
	 * 
	 * @param identifier
	 * @param contextIdentifier
	 */
	public GranularityAggregationSettings(int identifier,int contextIdentifier) {
		this.identifier = identifier;
		this.contextIdentifier = contextIdentifier;
	}

	/**
	 * @return the identifier
	 */
	public int getIdentifier() {
		return identifier;
	}
	
	/**
	 * @param identifier the identifier to set
	 */
	public void setIdentifier(int identifier) {
		this.identifier = identifier;
	}
	
	/**
	 * @return the context identifier
	 */
	public int getContextIdentifier() {
		return contextIdentifier;
	}
	
	/**
	 * @param context identifier the context identifier to set
	 */
	public void setContextIdentifier(int contextIdentifier) {
		this.contextIdentifier = contextIdentifier;
	}
}