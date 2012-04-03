package timeBench.data.expression;

import prefuse.data.Tuple;
import prefuse.data.expression.AndPredicate;
import prefuse.data.expression.ColumnExpression;
import prefuse.data.expression.ComparisonPredicate;
import prefuse.data.expression.NumericLiteral;
import prefuse.data.expression.Predicate;
import timeBench.data.relational.TemporalDataset;
import timeBench.data.relational.TemporalElement;

/**
 * Predicate that indicates if a {@link TemporalElement} is of a certain
 * granularity. Optionally checks granularity context.
 * 
 * @author Rind
 */
public class GranularityPredicate extends AndPredicate {

    private int granularityId;
    private int granularityContextId;

    /**
     * if granularity context is not set, we do not check it
     */
    private boolean contextSet;

    /**
     * create a new GranularityPredicate. Ignores granularity context.
     * 
     * @param granularityId
     *            the granularity id to match by this predicate
     */
    public GranularityPredicate(int granularityId) {
        super();
        super.add(new ComparisonPredicate(ComparisonPredicate.EQ,
                new ColumnExpression(TemporalDataset.GRANULARITY_ID),
                new NumericLiteral(granularityId)));

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
        super();
        super.add(new GranularityContextPredicate(granularityContextId));
        super.add(new ComparisonPredicate(ComparisonPredicate.EQ,
                new ColumnExpression(TemporalDataset.GRANULARITY_ID),
                new NumericLiteral(granularityId)));

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

    @Override
    public Predicate getSubPredicate(Predicate p) {
        for (int i = 0; i < m_clauses.size(); ++i) {
            Predicate pp = (Predicate) m_clauses.get(i);
            if (p != pp) {
                return pp;
            }
        }
        return null;
    }

    @Override
    public void add(Predicate p) {
        throw new UnsupportedOperationException("readonly");
    }

    @Override
    public boolean remove(Predicate p) {
        throw new UnsupportedOperationException("readonly");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("readonly");
    }

    @Override
    public void set(Predicate p) {
        throw new UnsupportedOperationException("readonly");
    }

    @Override
    public void set(Predicate[] p) {
        throw new UnsupportedOperationException("readonly");
    }
}
