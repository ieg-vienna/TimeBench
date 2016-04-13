package org.timebench.data.expression;

import org.timebench.data.TemporalElement;
import org.timebench.data.TemporalElementStore;

import prefuse.data.Tuple;
import prefuse.data.expression.ColumnExpression;
import prefuse.data.expression.ComparisonPredicate;
import prefuse.data.expression.Expression;
import prefuse.data.expression.NumericLiteral;
import prefuse.data.expression.Predicate;

/**
 * Predicate that indicates if a {@link TemporalElement} is of a certain kind.
 * It extends {@link ComparisonPredicate} to use index optimizations in
 * {@link prefuse.data.util.FilterIteratorFactory}.
 * 
 * @author Rind
 */
public class KindPredicate extends ComparisonPredicate {

    // TODO support TemporalObject
    // TODO extend Function and register at FunctionTable

    /** convenience instance for instants. */
    public static final Predicate INSTANT = new KindPredicate(
            TemporalElementStore.PRIMITIVE_INSTANT);
    /** convenience instance for intervals. */
    public static final Predicate INTERVAL = new KindPredicate(
            TemporalElementStore.PRIMITIVE_INTERVAL);
    /** convenience instance for intervals. */
    public static final Predicate SPAN = new KindPredicate(
            TemporalElementStore.PRIMITIVE_SPAN);
    /** convenience instance for intervals. */
    public static final Predicate SET = new KindPredicate(
            TemporalElementStore.PRIMITIVE_SET);

    private int kind;

    /**
     * create a new KindPredicate
     * 
     * @param kind
     *            the kind code to match by this predicate
     */
    public KindPredicate(int kind) {
        super(ComparisonPredicate.EQ,
                new ColumnExpression(TemporalElement.KIND), new NumericLiteral(
                        kind));
        this.kind = kind;
    }

    @Override
    public boolean getBoolean(Tuple t) {
        int tupleKind = t.getInt(TemporalElement.KIND);
        return (kind == tupleKind);
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
