package org.timebench.data.expression;

import org.timebench.calendar.Granularity;
import org.timebench.data.TemporalElement;

import prefuse.data.Tuple;
import prefuse.data.expression.ColumnExpression;
import prefuse.data.expression.ComparisonPredicate;
import prefuse.data.expression.Expression;
import prefuse.data.expression.NumericLiteral;

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
                granularity.getGranularityContextIdentifier()));

        this.granularity = granularity;
    }

    @Override
    public boolean getBoolean(Tuple t) {
        int tupleGCid = t.getInt(TemporalElement.GRANULARITY_CONTEXT_ID);
        return (granularity.getGranularityContextIdentifier() == tupleGCid);
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
