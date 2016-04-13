package org.timebench.data.io.schema;

import java.util.Map;

import javax.xml.bind.annotation.XmlElement;

import org.timebench.calendar.Calendar;
import org.timebench.data.GenericTemporalElement;
import org.timebench.data.Instant;
import org.timebench.data.TemporalDataException;
import org.timebench.data.TemporalDataset;

import prefuse.data.Tuple;

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
            GenericTemporalElement gBegin = elements.get(beginKey);
            GenericTemporalElement gEnd = elements.get(endKey);
            // TODO check & test
            Instant begin = (Instant) gBegin.asPrimitive();
            Instant end = (Instant) gEnd.asPrimitive();

            GenericTemporalElement interval = tmpds.addInterval(begin, end)
                    .asGeneric();

            elements.put(this.getKey(), interval);
        } else
            throw new TemporalDataException(
                    "import interval with span not supported yet");
    }
}
