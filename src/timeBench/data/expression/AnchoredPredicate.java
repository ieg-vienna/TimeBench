package timeBench.data.expression;

import prefuse.data.Tuple;
import prefuse.data.expression.AbstractPredicate;
import prefuse.data.expression.Expression;
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

    /**
     * @see prefuse.data.expression.Function#addParameter(prefuse.data.expression.Expression)
     */
    public void addParameter(Expression e) {
        throw new IllegalStateException("This function takes 0 parameters");
    }

    /**
     * @see prefuse.data.expression.Function#getParameterCount()
     */
    public int getParameterCount() {
        return 0;
    }
}
