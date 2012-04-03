package timeBench.data.expression;

import prefuse.data.Tuple;
import prefuse.data.expression.ColumnExpression;
import prefuse.data.expression.ComparisonPredicate;
import prefuse.data.expression.Expression;
import prefuse.data.expression.NumericLiteral;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalElement;

/**
 * Predicate that indicates if a {@link TemporalElement} is of a certain context
 * granularity. Ignores checks granularity id.
 * 
 * @author Rind
 */
public class GranularityContextPredicate extends ComparisonPredicate {

    private int granularityContextId;

    public GranularityContextPredicate(int granularityContextId) {
        super(ComparisonPredicate.EQ, new ColumnExpression(
                TemporalDataset.GRANULARITY_CONTEXT_ID), new NumericLiteral(
                granularityContextId));

        this.granularityContextId = granularityContextId;
    }

    @Override
    public boolean getBoolean(Tuple t) {
        int tupleGCid = t.getInt(TemporalDataset.GRANULARITY_CONTEXT_ID);
        return (granularityContextId == tupleGCid);
    }

    @Override
    public void setLeftExpression(Expression e) {
        throw new UnsupportedOperationException("readonly");
    }

    @Override
    public void setRightExpression(Expression e) {
        throw new UnsupportedOperationException("readonly");
    }
}
