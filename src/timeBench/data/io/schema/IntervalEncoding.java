package timeBench.data.io.schema;

import java.util.Map;

import javax.xml.bind.annotation.XmlElement;

import prefuse.data.Tuple;
import timeBench.calendar.Calendar;
import timeBench.data.TemporalDataException;
import timeBench.data.oo.TemporalElement;
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
            Map<String, Integer> elements) throws TemporalDataException {
        if (beginKey != null && endKey != null) {
            timeBench.data.relational.GenericTemporalElement begin = tmpds
                    .getTemporalElement(elements.get(beginKey));
            timeBench.data.relational.GenericTemporalElement end = tmpds
                    .getTemporalElement(elements.get(endKey));

            // XXX make this more correct using Tim's classes (e.g., check &
            // handle different granularities)
            int row = tmpds.addTemporalElement(begin.getInf(), end.getSup(),
                    super.getGranularity().getIdentifier(),
                    TemporalDataset.PRIMITIVE_INTERVAL);

            // add edges to temporal element graph
            timeBench.data.relational.TemporalElement interval = tmpds
                    .getTemporalElement(row);
            tmpds.getTemporalElementsGraph().addEdge(begin, interval);
            tmpds.getTemporalElementsGraph().addEdge(end, interval);

            elements.put(super.getKey(), row);
        } else
            throw new TemporalDataException(
                    "import interval with span not supported yet");
    }

    @Override
    public void buildTemporalElement(Tuple tuple,
            Map<String, TemporalElement> elements) {
        // TODO Auto-generated method stub

    }
}
