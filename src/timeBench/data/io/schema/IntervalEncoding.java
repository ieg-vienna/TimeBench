package timeBench.data.io.schema;

import java.util.Map;

import javax.xml.bind.annotation.XmlElement;

import prefuse.data.Tuple;
import timeBench.calendar.Calendar;
import timeBench.data.TemporalDataException;
import timeBench.data.relational.GenericTemporalElement;
import timeBench.data.relational.TemporalDataset;

public class IntervalEncoding extends TemporalObjectEncoding {

    @XmlElement(name = "begin", required = false)
    private String beginKey = null;

    @XmlElement(name = "end", required = false)
    private String endKey = null;

    @XmlElement(name = "span", required = false)
    private String spanKey = null;

    IntervalEncoding() {
        super();
    }

    public IntervalEncoding(String key, String beginKey, String endKey) {
        super.setKey(key);
        this.beginKey = beginKey;
        this.endKey = endKey;
    }

    @Override
    void init(Calendar calendar) throws TemporalDataException {
        super.init(calendar);
        int count = (beginKey != null) ? 1 : 0;
        count += (endKey != null) ? 1 : 0;
        count += (spanKey != null) ? 1 : 0;
        if (count < 2)
            throw new TemporalDataException("Interval \"" + super.getKey()
                    + "\" underspecified");
        if (count > 2)
            throw new TemporalDataException("Interval \"" + super.getKey()
                    + "\" overspecified");
    }

    @Override
    public void buildTemporalElement(TemporalDataset tmpds, Tuple tuple,
            Map<String, GenericTemporalElement> elements)
            throws TemporalDataException {
        if (beginKey != null && endKey != null) {
            GenericTemporalElement begin = elements.get(beginKey);
            GenericTemporalElement end = elements.get(endKey);

            // check & test
            GenericTemporalElement interval = tmpds.addInterval(begin, end)
                    .asGeneric();

            elements.put(this.getKey(), interval);
        } else
            throw new TemporalDataException(
                    "import interval with span not supported yet");
    }
}
