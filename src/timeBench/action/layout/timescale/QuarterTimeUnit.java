package timeBench.action.layout.timescale;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import timeBench.calendar.Granularity;

public class QuarterTimeUnit extends GranularityTimeUnit {
    
    private static final String[] NUMERALS = {"I", "II", "III", "IV"}; 

    public QuarterTimeUnit(String name, Granularity granularity) {
        super(name, granularity, DateFormat.getDateInstance(), DateFormat
                .getDateInstance(), DateFormat.getDateInstance());
    }

    @Override
    public String formatShort(Date date) {
        return  format(date, false, "Q");
    }

    @Override
    public String formatLong(Date date) {
        return  format(date, true, " Q ");
    }

    @Override
    public String formatFull(Date date) {
        return format(date, true,
                "de".equals(Locale.getDefault().getLanguage()) ? " Quartal "
                        : " Quarter ");
    }

    private String format(Date date, boolean useYear, String quarterPrefix) {
        // http://stackoverflow.com/a/302688
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        /* Consider whether you need to set the calendar's timezone. */
        cal.setTime(date);
        int month = cal.get(Calendar.MONTH); /* 0 through 11 */
        int quarter = month / 3; /* 0 through 3 */

        StringBuilder sb = new StringBuilder();
        if (useYear) {
            sb.append(cal.get(Calendar.YEAR));
        }
        sb.append(quarterPrefix);
        sb.append(NUMERALS[quarter]);
        return sb.toString();
    }
    
}
