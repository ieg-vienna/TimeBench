package timeBench.data;

import prefuse.data.DataTypeException;
import prefuse.data.column.LongColumn;

/**
 * Simple realization of a temporal column without caching.
 * @author Rind
 */
@Deprecated
public class TemporalColumn extends LongColumn {

    private TemporalElementStore tmpstr;

    public TemporalColumn(TemporalElementStore tmpstr) {
        super();
        this.tmpstr = tmpstr;
    }

    @Override
    public TemporalElement get(int row) {
        return tmpstr.getTemporalElement(super.getLong(row));
    }

    @Override
    public void set(Object val, int row) throws DataTypeException {
        if (val instanceof TemporalElement) {
            TemporalElement te = (TemporalElement) val;
            if (te.getTemporalElementStore() == tmpstr) {
                super.setLong(te.getId(), row);
            } else {
                 throw new DataTypeException(
                         "Cannot set temporal element from different data store.");
            }
        } else {
            super.set(val, row);
        }
    }

}
