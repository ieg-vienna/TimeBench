package timeBench.data.expression;

import prefuse.data.Tuple;
import prefuse.data.expression.AbstractPredicate;
import timeBench.data.relational.TemporalDataset;
import timeBench.data.relational.TemporalElement;

/**
 * Predicate that indicates if a {@link TemporalElement} is of a certain
 * granularity. Optionally checks granularity context.
 * 
 * @author Rind
 */
public class GranularityPredicate extends AbstractPredicate {

    private int granularityId;
    private int granularityContextId;

    /**
     * if granularity context is not set, we do not check it
     */
    private boolean contextSet;
    
    // TODO if we need to check granularity context only --> separate class

    /**
     * create a new GranularityPredicate. Ignores granularity context.
     * 
     * @param granularityId
     *            the granularity id to match by this predicate
     */
    public GranularityPredicate(int granularityId) {
        this.granularityId = granularityId;
        this.contextSet = false;
    }

    /**
     * create a new GranularityPredicate with granularity context.
     * 
     * @param granularityId
     *            the granularity id to match by this predicate
     * @param granularityContextId
     *            the granularity context id to match by this predicate
     */
    public GranularityPredicate(int granularityId, int granularityContextId) {
        this.granularityId = granularityId;
        this.granularityContextId = granularityContextId;
        this.contextSet = true;
    }

    @Override
    public boolean getBoolean(Tuple t) {
        if (contextSet) {
            int tupleContextId = t
                    .getInt(TemporalDataset.GRANULARITY_CONTEXT_ID);
            if (granularityContextId != tupleContextId) {
                return false;
            }
        }
        int tupleGranularityId = t.getInt(TemporalDataset.GRANULARITY_ID);
        return (granularityId == tupleGranularityId);
    }
}
