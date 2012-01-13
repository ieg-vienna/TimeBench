package timeBench.data.io.schema;

import ieg.prefuse.data.io.TextTableFormat;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import timeBench.calendar.Calendar;
import timeBench.data.TemporalDataException;

@XmlRootElement(name = "temporal-data-column-specifaction")
@XmlAccessorType(XmlAccessType.NONE)
public class TemporalDataColumnSpecification {
    
    // TODO handle different calendars
    @XmlElement(required = true)
    private Calendar calendar;

    @XmlElement(name = "fail-on-illegal-rows", required = false)
    private boolean failOnIllegalRows = false;

    // there might be some better solution with @XmlAnyElement(lax = true)
    // cp. http://jaxb.java.net/guide/Mapping_interfaces.html
    // but unmarshalling did not work (see TestXmlAny.java) 
    @XmlElementWrapper
    @XmlElements( {
        @XmlElement(name = "date-instant", type = DateInstantEncoding.class),
        @XmlElement(name = "string-instant", type = StringInstantEncoding.class),
        @XmlElement(name = "two-string-instant", type = TwoStringInstantEncoding.class),
        @XmlElement(name = "interval", type = IntervalEncoding.class) })
    private List<TemporalObjectEncoding> encodings = new LinkedList<TemporalObjectEncoding>();

    @XmlElement(name = "text-table")
    private TextTableFormat textTableSpec = new TextTableFormat();
    
    public void init() throws TemporalDataException {
        Set<String> keys = new TreeSet<String>();
        for (TemporalObjectEncoding enc : encodings) {
            // check for duplicate keys
            if (!keys.add(enc.getKey()))
                throw new TemporalDataException(
                        "Duplicate key in specification: " + enc.getKey());

            // initialize encodings with granularities and some validity checks
            enc.init(calendar);
        }
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setFailOnIllegalRows(boolean failOnIllegalRows) {
        this.failOnIllegalRows = failOnIllegalRows;
    }

    public boolean isFailOnIllegalRows() {
        return failOnIllegalRows;
    }

    public void addEncoding(TemporalObjectEncoding encoding) {
        this.encodings.add(encoding);
    }

    public Iterable<TemporalObjectEncoding> getEncodings() {
        return encodings;
    }
    
    public TextTableFormat getTableFormat() {
        return this.textTableSpec;
    }
    
    public void setTableFormat(TextTableFormat format) {
        this.textTableSpec = format;
    }
}