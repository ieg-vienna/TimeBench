package timeBench.action.layout.timescale;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import timeBench.util.DateUtil;

class GregorianTimeUnit extends TimeUnit {

    private long maxLengthInMillis;
    
    private int field;
    private int factor;
    
    /**
     * Creates an TimeUnit with factor 1.
     * 
     * @param name
     *            the name of the TimeUnit
     * @param field
     *            the {@link Calendar} field used to determine the
     *            maxLengthInMillis
     * @param shortFormat
     *            the short {@link DateFormat}
     * @param longFormat
     *            the long {@link DateFormat}
     * @param fullFormat
     *            the full {@link DateFormat}
     */
    public GregorianTimeUnit(String name, int field, DateFormat shortFormat, DateFormat longFormat, DateFormat fullFormat) {
        this(name, field, 1, shortFormat, longFormat, fullFormat);
    }

    /**
     * <p>
     * Creates a new TimeUnit with a given name, a {@link Calendar} field, the
     * factor to multiply the milliseconds representing the {@link Calendar}
     * field with, and the various {@link DateFormat}s, {@link Date}s with
     * this TimeUnit should be formatted with.
     * </p>
     * <p>
     * The maxLengthInMillis of this TimeUnit it calculated upon the given
     * {@link Calendar} field and the factor. For example, if the
     * {@link Calendar} field is Calendar.SECOND and the factor is 5, the
     * maxLengthInMillis of the TimeUnit will be 5000. The name therefore should
     * be "5 Seconds", the shortFormat could be "ss", the longformat "HH:mm:ss"
     * and the fullFormat "dd.MM yyyy HH:mm:ss.SSS".
     * </p>
     * <p>
     * The field param is furthermore used to calculate dates fitting into this
     * TimeUnit, as this is not always as straightforward as finding a multiple
     * of maxLengthInMillis. As an accuracy like "Months" has different lengths,
     * depending on the date, it's not enough just to define a length. Thus you
     * have to provide field and a factor and the TimeUnit does the hard work
     * for you like finding dates 'fitting' into this TimeUnit (see
     * {@link TimeUnit#next(long)} and {@link TimeUnit#previous(long)}).
     * </p>
     * <p>
     * Dates either fit or don't fit into a TimeUnit. A date that fits into a
     * TimeUnit is like a number that has a common divider with another number.
     * For example a TimeUnit "5 Seconds" has the length 5000, which means that
     * every 5th second fits into this TimeUnit, like any day with time 7:05:05
     * but not 7:05:06. Another example is "Months", where 01-03-07 00:00:00
     * would fit into this TimeUnit while 01/03/07 00:01:00 would not. The next
     * fitting date would be 01/04/07 00:00:00.
     * </p>
     * 
     * @param name
     *            the name of the TimeUnit
     * @param field
     *            the {@link Calendar} field used to determine the
     *            maxLengthInMillis
     * @param factor
     *            the factor the milliseconds representing the {@link Calendar}
     *            field should be multiplied with to calculate the
     *            maxLengthInMillis
     * @param shortFormat
     *            the short {@link DateFormat}
     * @param longFormat
     *            the long {@link DateFormat}
     * @param fullFormat
     *            the full {@link DateFormat}
     */
    public GregorianTimeUnit(String name, int field, int factor, DateFormat shortFormat, DateFormat longFormat,
            DateFormat fullFormat) {
        super(name, shortFormat, longFormat, fullFormat);
        
        this.field = field;
        this.factor = factor;

        maxLengthInMillis = getMaxLength(field, factor);
    }

    @Override
    public long getMaxLengthInMillis() {
        return maxLengthInMillis;
    }

    @Override
    public long previous(long date) {
        return DateUtil.truncate(date, field, factor);
    }

    @Override
    public long next(long date) {
        long defaultNext = date + getMaxLengthInMillis();
        long adjusted = previous(defaultNext);
        if (adjusted > date) {
            return adjusted;
        } else {
            return defaultNext;
        }
    }

    /**
     * Returns the maximum length of a given {@link Calendar} field and a
     * multiplying factor in milliseconds.
     * 
     * @param field
     *            the {@link Calendar} field
     * @param factor
     *            the factor the milliseconds representing the {@link Calendar}
     *            field should be multiplied with to calculate the
     *            maxLengthInMillis
     * @return the maximum length in milliseconds
     */
    public static long getMaxLength(int field, int factor) {
        long maxLength = (long) factor;
        switch (field) {
        case Calendar.MONTH:
            maxLength *= 31;
        case Calendar.DAY_OF_MONTH:
            maxLength *= 25;
        case Calendar.HOUR:
            maxLength *= 60;
        case Calendar.MINUTE:
            maxLength *= 60;
        case Calendar.SECOND:
            maxLength *= 1000;
        case Calendar.MILLISECOND:
            break;
        case Calendar.WEEK_OF_YEAR:
            maxLength = (long) 1000l * 60 * 60 * 25 * 7 * factor;
            break;
        case Calendar.YEAR:
            maxLength = (long) 1000l * 60 * 60 * 24 * 366 * factor;
            break;
        default:
            throw new IllegalArgumentException("unsupported field");
        }
        return maxLength;
    }
}
