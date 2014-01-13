package timeBench.data.expression;

import prefuse.data.Tuple;
import prefuse.data.expression.ColumnExpression;
import prefuse.data.expression.ComparisonPredicate;
import prefuse.data.expression.Expression;
import prefuse.data.expression.NumericLiteral;
import timeBench.calendar.Granularity;
import timeBench.data.TemporalElement;

/**
 * Predicate that indicates if a {@link TemporalElement} is of a certain context
 * granularity. Ignores checks granularity id.
 * 
 * @author Rind
 */
public class GranularityContextPredicate extends ComparisonPredicate {

    private Granularity granularity;

    public GranularityContextPredicate(Granularity granularity) {
        super(ComparisonPredicate.EQ, new ColumnExpression(
                TemporalElement.GRANULARITY_CONTEXT_ID), new NumericLiteral(
                granularity.getContextGranularity().getGlobalGranularityIdentifier()));

        this.granularity = granularity;
    }

    @Override
    public boolean getBoolean(Tuple t) {
        int tupleGCid = t.getInt(TemporalElement.GRANULARITY_CONTEXT_ID);
        return (granularity.getContextGranularity().getGlobalGranularityIdentifier() == tupleGCid);
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
