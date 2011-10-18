package timeBench.data.io.schema;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import prefuse.data.Tuple;
import timeBench.data.TemporalDataException;

@XmlRootElement(name = "date-instant")
public class DateInstantEncoding extends InstantEncoding {

    @XmlElement(name = "temporal-column", required = true)
    private String temporalColumn;

    DateInstantEncoding() {
    }

    public DateInstantEncoding(String key, String temporalColumn) {
        super.setKey(key);
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
