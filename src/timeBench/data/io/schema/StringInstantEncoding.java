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
public class StringInstantEncoding extends InstantEncoding {

    private String temporalColumn;
    private String dateTimePattern = "yyyy-MM-dd";
    // allow specification of Locale via encoding (e.g. May vs. Mai)
    private String language = "en";

    @XmlTransient
    private DateFormat format = null;

    public StringInstantEncoding(String key, String temporalColumn,
            String dateTimePattern) {
        super(key);
        this.temporalColumn = temporalColumn;
        this.dateTimePattern = dateTimePattern;
    }

    // lazy
    private DateFormat prepareFormat() {
        if (format == null)
            format = new SimpleDateFormat(dateTimePattern, new Locale(language));
        return format;
    }

    @Override
    protected Date buildDate(Tuple tuple) throws TemporalDataException {
        if (logger.isDebugEnabled())
            logger.debug("prepare temp.e. " + tuple.getString(temporalColumn));

        try {
            return prepareFormat().parse(tuple.getString(temporalColumn));
        } catch (ParseException e) {
            throw new TemporalDataException(
                    "import temporal element failed could not parse \""
                            + tuple.getString(temporalColumn)
                            + "\" using pattern \"" + this.dateTimePattern
                            + "\"");
        }
    }
}
