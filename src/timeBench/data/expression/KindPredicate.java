package timeBench.data.expression;

import prefuse.data.Tuple;
import prefuse.data.expression.AbstractPredicate;
import prefuse.data.expression.Predicate;
import timeBench.data.relational.TemporalDataset;
import timeBench.data.relational.TemporalElement;

/**
 * Predicate that indicates if a {@link TemporalElement} is of a certain kind.
 * 
 * @author Rind
 */
public class KindPredicate extends AbstractPredicate {

    // TODO extend ComparionPredicate to use optimizations in FilterIteratorFactory#getComparisonIterator(...), also needs an index on kind
    // TODO support TemporalObject
    
    /** convenience instance for instants. */
    public static final Predicate INSTANT = new KindPredicate(
            TemporalDataset.PRIMITIVE_INSTANT);
    /** convenience instance for intervals. */
    public static final Predicate INTERVAL = new KindPredicate(
            TemporalDataset.PRIMITIVE_INTERVAL);
    /** convenience instance for intervals. */
    public static final Predicate SPAN = new KindPredicate(
            TemporalDataset.PRIMITIVE_SPAN);
    /** convenience instance for intervals. */
    public static final Predicate SET = new KindPredicate(
            TemporalDataset.PRIMITIVE_SET);

    private int kind;

    /**
     * create a new KindPredicate
     * 
     * @param kind
     *            the kind code to match by this predicate
     */
    public KindPredicate(int kind) {
        this.kind = kind;
    }

    @Override
    public boolean getBoolean(Tuple t) {
        int tupleKind = t.getInt(TemporalDataset.KIND);
        return (kind == tupleKind);
    }
}
