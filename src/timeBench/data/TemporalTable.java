package timeBench.data;

import prefuse.data.DataTypeException;
import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.column.Column;
import prefuse.data.column.ExpressionColumn;
import prefuse.data.expression.ColumnExpression;
import prefuse.data.expression.ExpressionVisitor;
import timeBench.data.expression.TemporalExpression;

/**
 * Minimal prefuse data structure for time-oriented data. This is internally
 * used by {@link TemporalDataset} and can be applied to build arbitrary data
 * structures. E.g., {@link Graph#Graph(Table, Table, boolean)}
 * 
 * @author Rind
 */
public class TemporalTable extends Table {
    
    public static final String ID_POSTFIX = "_id";

    public void addTemporalColumn(String name, TemporalElementStore store) {
        String idColumn = TemporalTable.idColumnNameFor(name);
        this.addColumn(idColumn, long.class, -1l);
        this.addColumn(name, new TemporalColumn(this, idColumn, store));
        store.register(this, idColumn);
        // index idColumn -> done by store.register(...)
    }

    @Override
    protected Column removeColumn(int idx) {
        Column col = this.getColumn(idx);
        if (col instanceof TemporalColumn) {
            String idColumn = TemporalTable.idColumnNameFor(super.getColumnName(idx));
            ((TemporalColumn)col).store.unregister(this, idColumn);
            super.removeColumn(idColumn);
        }
        return super.removeColumn(idx);
    }
    
    /**
     * Yields the name of the column storing the {@link TemporalElement#ID} for
     * a given TemporalColumn.
     * 
     * @param name
     *            column name for {@link TemporalElement}
     * @return column name for id
     */
    public static String idColumnNameFor(String name) {
        return name + ID_POSTFIX;
    }

    /**
     * Specialized column that allows temporal elements to be retrieved by
     * {@link TemporalPrimitveByIdColumnExpression} and in addition be saved.
     * 
     * @author Rind
     */
    static class TemporalColumn extends ExpressionColumn {
        
        // Alternatively it would be possible to extend AbstractColumn directly
        // but in that case the caching would need to be replicated
        
        private Table table;
        private TemporalElementStore store;
        private String idColumn;

        public TemporalColumn(Table table, String idColumn, TemporalElementStore store) {
            super(table, new TemporalPrimitveByIdColumnExpression(idColumn, store));
            this.table = table;
            this.store = store;
            this.idColumn = idColumn;
        }
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public boolean canSet(Class type) {
            return type.isAssignableFrom(TemporalElement.class);
        }

        @Override
        public void set(Object val, int row) throws DataTypeException {
            if (val instanceof TemporalElement) {
                TemporalElement te = (TemporalElement) val;
                if (te.getTemporalElementStore() == store) {
                    table.setLong(row, idColumn, te.getId());
                } else {
                     throw new DataTypeException(
                             "Cannot set temporal element from different data store.");
                }
            } else {
                throw new DataTypeException(
                        "Cannot set temporal element from type " + val.getClass() + ".");
            }
        }

        // XXX problem: TE in cache; TE invalidated & replaced; how update cache? (a) check on get (b) invalidate in Store (c) no cache
        @Override
        public TemporalElement get(int row) {
            // workaround to empty cache if temp. el. was removed
            TemporalElement te = (TemporalElement) super.get(row);
            if (! te.isValid()) {
                super.invalidateCache(row, row);
                te = (TemporalElement) super.get(row);
            }
            return te;
        }

        // XXX why?
//        @Override
//        public String getString(int row) throws DataTypeException {
//            // workaround to empty cache if temp. el. was removed
//            TemporalElement te = (TemporalElement) super.get(row);
//            if (! te.isValid()) {
//                super.invalidateCache(row, row);
//            }
//            return super.getString(row);
//        }
    }
    
    /**
     * Yields a temporal primitive based on its {@link TemporalElement#ID} from
     * another column.
     * 
     * @author Rind
     */
    static class TemporalPrimitveByIdColumnExpression extends TemporalExpression {

        // Alternatively it could accept id as expression (e.g., [field]+1)?

        protected final String field;
        protected final TemporalElementStore store;

        public TemporalPrimitveByIdColumnExpression(String field,
                TemporalElementStore store) {
            this.field = field;
            this.store = store;
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
            return store.getTemporalPrimitive(id);
            // store.getTemporalElement(id);
        }

        public String getField() {
            return field;
        }

        public TemporalElementStore getTemporalElementStore() {
            return store;
        }

        @Override
        public void visit(ExpressionVisitor v) {
            super.visit(v);
            // expose id column to ExpressionAnalyzer.getReferencedColumns()
            ColumnExpression c = new ColumnExpression(field);
            v.down(); c.visit(v); v.up();
        }
    }
}
