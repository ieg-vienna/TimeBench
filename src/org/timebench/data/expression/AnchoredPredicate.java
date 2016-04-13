package org.timebench.data.expression;

import org.timebench.data.TemporalElement;
import org.timebench.data.TemporalObject;

import prefuse.data.Tuple;
import prefuse.data.expression.AbstractPredicate;

/**
 * Expression that indicates if a {@link TemporalObject} or
 * {@link TemporalElement} is anchored.
 * 
 * @author Rind
 */
public class AnchoredPredicate extends AbstractPredicate {

    // XXX extend ColumnExpression so that ExpressionAnalyzer#hasDependency() works?  
    
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
