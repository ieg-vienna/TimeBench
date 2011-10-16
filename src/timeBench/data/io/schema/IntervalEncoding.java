package timeBench.data.io.schema;

import java.util.Map;

import prefuse.data.Tuple;
import timeBench.data.TemporalDataException;
import timeBench.data.oo.TemporalElement;
import timeBench.data.relational.TemporalDataset;

public class IntervalEncoding extends TemporalObjectEncoding {

    private String beginKey = null;
    private String endKey = null;
    @SuppressWarnings("unused")
    private String spanKey = null;

    public IntervalEncoding(String key, String beginKey, String endKey) {
        super(key);
        this.beginKey = beginKey;
        this.endKey = endKey;
    }

    // TODO does not work
    // public IntervalEncoding(String key, String beginKey, String spanKey) {
    // super(key);
    // this.beginKey = beginKey;
    // this.spanKey = spanKey;
    // }

    @Override
    public void buildTemporalElement(TemporalDataset tmpds, Tuple tuple,
            Map<String, Integer> elements) throws TemporalDataException {
        if (beginKey != null && endKey != null) {
            timeBench.data.relational.TemporalElement begin = tmpds
                    .getTemporalElement(elements.get(beginKey));
            timeBench.data.relational.TemporalElement end = tmpds
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
