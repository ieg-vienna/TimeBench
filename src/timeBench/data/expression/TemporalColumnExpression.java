package timeBench.data.expression;

import prefuse.data.Schema;
import prefuse.data.Tuple;
import timeBench.data.TemporalElement;
import timeBench.data.TemporalElementStore;

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
}
