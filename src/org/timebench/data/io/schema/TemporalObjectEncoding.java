package org.timebench.data.io.schema;

import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.timebench.calendar.Calendar;
import org.timebench.calendar.Granularity;
import org.timebench.data.GenericTemporalElement;
import org.timebench.data.TemporalDataException;
import org.timebench.data.TemporalDataset;
import org.timebench.data.TemporalObject;

import prefuse.data.Tuple;

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

    @XmlElement(name = "granularity-context-id", required = true)
    private int granularityContextId;
    
    @XmlTransient
    private Granularity granularity = null;
    
    @XmlTransient
    private boolean temporalObjectIdIncluded = false;

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
            this.granularity = new Granularity(calendar, granularityId, granularityContextId);
        
        this.temporalObjectIdIncluded = ArrayUtils.contains(this.dataColumns, 
                TemporalObject.ID);
    }

    public abstract void buildTemporalElement(TemporalDataset tmpds,
            Tuple tuple, Map<String, GenericTemporalElement> elements)
            throws TemporalDataException;

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

    public boolean isTemporalObjectIdIncluded() {
        return temporalObjectIdIncluded;
    }
}
