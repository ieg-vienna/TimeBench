package timeBench.data.expression;

import prefuse.data.Schema;
import prefuse.data.Tuple;
import prefuse.data.expression.ColumnExpression;
import prefuse.data.expression.ExpressionVisitor;
import timeBench.data.TemporalElement;
import timeBench.data.TemporalElementStore;

// TODO should this class be exposed to the public or hidden inside TemporalTable
// TODO should it be flexible to accept id as expression (e.g., [field]+1)? 
// TODO rename to TemporalElementByIdExpression?
public class TemporalColumnExpression extends TemporalExpression {

    protected final String field;
    protected final TemporalElementStore store;
    protected final boolean primitive;

    public TemporalColumnExpression(String field) {
        this(field, new TemporalElementStore(), false);
    }

    public TemporalColumnExpression(String field, TemporalElementStore store,
            boolean primitive) {
        this.field = field;
        this.store = store;
        this.primitive = primitive;
    }

    // TODO should this extend TemporalExpression? in that case getType() inherited
    @SuppressWarnings("rawtypes")
    @Override
    public Class getType(Schema s) {
        return TemporalElement.class;
    }

    @Override
    public Object get(Tuple t) {
        long id = t.getLong(this.field);
        return primitive ? store.getTemporalPrimitive(id) : store
                .getTemporalElement(id);
    }

    public String getField() {
        return field;
    }

    public TemporalElementStore getTemporalElementStore() {
        return store;
    }

    public boolean isPrimitive() {
        return primitive;
    }

    @Override
    public void visit(ExpressionVisitor v) {
        super.visit(v);
        // expose id column to ExpressionAnalyzer.getReferencedColumns()
        ColumnExpression c = new ColumnExpression(field);
        v.down(); c.visit(v); v.up();
    }
}
