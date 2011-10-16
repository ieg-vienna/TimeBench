package timeBench.data.io.schema;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.bind.annotation.XmlTransient;

import prefuse.data.Tuple;
import timeBench.data.TemporalDataException;

/**
 * 
 * <b>Warning:</b> If prefuse has parsed the column as a date it will use the
 * runtime's locale to format the string. Either use {@link DateInstantEncoding}
 * or load the data without a DateParser.
 * 
 * @author Rind
 * 
 */
@SuppressWarnings("unused")
public class TwoStringInstantEncoding extends InstantEncoding {

    private String dateColumn;
    private String timeColumn;
    private String datePattern = "yyyy-MM-dd";
    private String timePattern = "HH:mm:ss";

    // allow specification of Locale via encoding (e.g. May vs. Mai)
    private String language = "en";

    @XmlTransient
    private SimpleDateFormat format = null;

    public TwoStringInstantEncoding(String key, String dateColumn,
            String timeColumn, String datePattern, String timePattern) {
        super(key);
        this.dateColumn = dateColumn;
        this.timeColumn = timeColumn;
        this.datePattern = datePattern;
        this.timePattern = timePattern;
    }

    // lazy
    private SimpleDateFormat prepareFormat() {
        if (format == null)
            format = new java.text.SimpleDateFormat(datePattern + "'T'"
                    + timePattern, new Locale(language));
        return format;
    }

    @Override
    protected Date buildDate(Tuple tuple) throws TemporalDataException {
        if (logger.isDebugEnabled())
            logger.debug("prepare temp.e. " + tuple.getString(dateColumn) + " "
                    + tuple.getString(timeColumn));

        String dateTime = tuple.getString(dateColumn) + "T"
                + tuple.getString(timeColumn);
        try {
            return prepareFormat().parse(dateTime);
        } catch (ParseException e) {
            throw new TemporalDataException(
                    "import temporal element failed could not parse \""
                            + dateTime + "\" using pattern \""
                            + format.toPattern() + "\"");
        }
    }
}
