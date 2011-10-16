package timeBench.data.io.schema;

import java.util.Map;

import org.apache.log4j.Logger;

import prefuse.data.Tuple;
import timeBench.calendar.Granularity;
import timeBench.data.TemporalDataException;
import timeBench.data.oo.TemporalElement;
import timeBench.data.relational.TemporalDataset;

public abstract class TemporalObjectEncoding {

    protected static final Logger logger = Logger
            .getLogger(TemporalObjectEncoding.class);

    private String key;
    private String[] dataColumns;

    private Granularity granularity;

    // TODO declare optional encodings that can safely be skipped vs. required
    // encodings that are needed (e.g. for an interval)

    // same manager for all in a dataset
    // private CalendarManager manager;

    public TemporalObjectEncoding(String key) {
        this.key = key;
        this.dataColumns = new String[0];
    }

    public TemporalObjectEncoding(String key, String[] dataColumns) {
        super();
        this.setKey(key);
        this.dataColumns = dataColumns;
    }

    public abstract void buildTemporalElement(TemporalDataset tmpds,
            Tuple tuple, Map<String, Integer> elements)
            throws TemporalDataException;

    @Deprecated
    public abstract void buildTemporalElement(Tuple tuple,
            Map<String, TemporalElement> elements);

    public String[] getDataColumns() {
        return dataColumns;
    }

    public void setDataColumns(String[] dataColumns) {
        this.dataColumns = dataColumns;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public Granularity getGranularity() {
        return granularity;
    }

    public void setGranularity(Granularity granularity) {
        this.granularity = granularity;
    }
}
