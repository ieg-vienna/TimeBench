package timeBench.data.expression;

import prefuse.data.Tuple;
import prefuse.data.expression.AbstractPredicate;
import timeBench.data.relational.TemporalElement;
import timeBench.data.relational.TemporalObject;

/**
 * Expression that indicates if a {@link TemporalObject} or
 * {@link TemporalElement} is anchored.
 * 
 * @author Rind
 */
public class AnchoredPredicate extends AbstractPredicate {

    @Override
    public boolean getBoolean(Tuple t) {
        if (t instanceof TemporalObject) {
            return ((TemporalObject) t).getTemporalElement().isAnchored();
        } else if (t instanceof TemporalElement) {
            return ((TemporalElement) t).asGeneric().isAnchored();
        } else {
            throw new UnsupportedOperationException(
                    "AnchoredPredicate only supports temporal objects and elements.");
        }
    }
}
