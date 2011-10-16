package timeBench.data.io.schema;

import java.util.Date;

import prefuse.data.Tuple;
import timeBench.data.TemporalDataException;

public class DateInstantEncoding extends InstantEncoding {

    private String temporalColumn;

    public DateInstantEncoding(String key, String temporalColumn) {
        super(key);
        this.temporalColumn = temporalColumn;
    }

    @Override
    protected Date buildDate(Tuple tuple) throws TemporalDataException {
        if (logger.isDebugEnabled())
            logger.debug("prepare temp.e. " + tuple.getString(temporalColumn));

        if (tuple.canGetDate(temporalColumn)) {
            return tuple.getDate(temporalColumn);
        } else {
            // the column is not a DateColumn
            throw new TemporalDataException(
                    "import temporal element failed column " + temporalColumn
                            + " is not of type date.");
        }
    }
}
