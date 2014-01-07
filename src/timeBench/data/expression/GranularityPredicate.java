package timeBench.data.expression;

import prefuse.data.Tuple;
import prefuse.data.expression.AndPredicate;
import prefuse.data.expression.ColumnExpression;
import prefuse.data.expression.ComparisonPredicate;
import prefuse.data.expression.NumericLiteral;
import prefuse.data.expression.Predicate;
import timeBench.calendar.Granularity;
import timeBench.data.TemporalElement;

/**
 * Predicate that indicates if a {@link TemporalElement} is of a certain
 * granularity. Optionally checks granularity context.
 * 
 * @author Rind
 */
public class GranularityPredicate extends AndPredicate {

    private Granularity granularity;

    /**
     * if granularity context is not set, we do not check it
     */
    private boolean contextSet;

    public GranularityPredicate(Granularity granularity) {
        this(granularity,true);
    }

    public GranularityPredicate(Granularity granularity,boolean ignoreContext) {
        super();

        if(ignoreContext) {       
        	super.add(new ComparisonPredicate(ComparisonPredicate.EQ,
        			new ColumnExpression(TemporalElement.GRANULARITY_ID),
        			new NumericLiteral(granularity.getGlobalGranularityIdentifier())));
        } else {               
        	super.add(new GranularityContextPredicate(granularity));
        	super.add(new ComparisonPredicate(ComparisonPredicate.EQ,
                new ColumnExpression(TemporalElement.GRANULARITY_ID),
                new NumericLiteral(granularity.getGlobalGranularityIdentifier())));
        }

        this.granularity = granularity;
        this.contextSet = !ignoreContext;
    }

    @Override
    public boolean getBoolean(Tuple t) {
        if (contextSet) {
            int tupleContextId = t
                    .getInt(TemporalElement.GRANULARITY_CONTEXT_ID);
            if (granularity.getGranularityContextIdentifier() != tupleContextId) {
                return false;
            }
        }
        int tupleGranularityId = t.getInt(TemporalElement.GRANULARITY_ID);
        return (granularity.getGlobalGranularityIdentifier() == tupleGranularityId);
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
