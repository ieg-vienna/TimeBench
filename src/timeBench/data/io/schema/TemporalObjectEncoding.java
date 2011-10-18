package timeBench.data.io.schema;

import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;

import prefuse.data.Tuple;
import timeBench.calendar.Calendar;
import timeBench.calendar.Granularity;
import timeBench.data.TemporalDataException;
import timeBench.data.oo.TemporalElement;
import timeBench.data.relational.TemporalDataset;

@XmlAccessorType(XmlAccessType.NONE)
public abstract class TemporalObjectEncoding {

    protected static final Logger logger = Logger
            .getLogger(TemporalObjectEncoding.class);

    @XmlAttribute(required = true)
    private String key = null;

    @XmlElementWrapper(name = "data-element", required = false)
    @XmlElement(name = "column")
    private String[] dataColumns = new String[0];

    @XmlElement(name = "granularity-id", required = true)
    private int granularityId;

    @XmlTransient
    private Granularity granularity = null;

    // TODO declare optional encodings that can safely be skipped vs. required
    // encodings that are needed (e.g. for an interval)

    // same manager for all in a dataset
    // private CalendarManager manager;

    TemporalObjectEncoding() {
    }

    public TemporalObjectEncoding(String key, String[] dataColumns) {
        super();
        this.setKey(key);
        this.dataColumns = dataColumns;
    }

    void init(Calendar calendar) throws TemporalDataException {
        if (granularity == null)
            this.granularity = new Granularity(calendar, granularityId);
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
        this.granularityId = granularity.getIdentifier();
    }
}
