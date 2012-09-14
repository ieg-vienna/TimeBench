package timeBench.action.analytical;

/**
 * The Settings for TimeAggregationTree per granularity.
 * 
 * <p>
 * Added: 2011-12-12 / TL<br>
 * Modifications:
 * 2012-09-09 / MB
 * </p>
 * 
 * @author Tim Lammarsch
 * 
 */
// TODO rename to singular or keep all granularities in this class
public class GranularityAggregationSettings {

    // private static final long serialVersionUID = 1380115803468529882L;
    private int identifier;
    private int contextIdentifier;
    private transient GranularityAggregationFunction aggFct;

    /**
     * The parameterless constructor is only for serialization.
     */
    @Deprecated
    public GranularityAggregationSettings() {
    }

    /**
     * The constructor has to provide the granularity information.
     * 
     * @param identifier
     * @param contextIdentifier
     */
    public GranularityAggregationSettings(int identifier, int contextIdentifier) {
        this.identifier = identifier;
        this.contextIdentifier = contextIdentifier;
        aggFct = new GranularityAggregationMean();
    }
    
    /**
     * The constructor has to provide the granularity information and the Aggregation Function for the Granularity
     * 
     * @param identifier
     * @param contextIdentifier
     */
    public GranularityAggregationSettings(int identifier, int contextIdentifier, GranularityAggregationFunction agg) {
        this.identifier = identifier;
        this.contextIdentifier = contextIdentifier;
        aggFct = agg;
    }

    /**
     * @return the identifier
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier
     *            the identifier to set
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
     * @param context
     *            identifier the context identifier to set
     */
    public void setContextIdentifier(int contextIdentifier) {
        this.contextIdentifier = contextIdentifier;
    }

    /**
     * @return the aggregation function object
     */
	public GranularityAggregationFunction getAggregationFct() {
		return aggFct;
	}
	
	/**
     * @param aggregationFct
     *            Function to use for the granularity aggregation
     */
	public  void setAggregationFct(GranularityAggregationFunction aggregationFct) {
		this.aggFct = aggregationFct;
	}
}