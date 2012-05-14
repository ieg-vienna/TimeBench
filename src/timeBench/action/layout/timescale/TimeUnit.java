package timeBench.action.layout.timescale;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import timeBench.util.DateUtil;

//import at.ac.tuwien.cs.timevis.ui.StatusBar;
//import at.ac.tuwien.cs.timevis.ui.TimeScaleHeader;

/**
 * <p>
 * An TimeUnit represents a unit of time like Milliseconds, 50 Milliseconds,
 * Days, Weeks etc.
 * </p>
 * <p>
 * An TimeUnit is associated with a {@link BasicTimeScale} to represent it's
 * current accuracy.
 * </p>
 * <p>
 * An TimeUnit essentially consists of
 * <li>a name like "Month", "Quartal" or "50 Milliseconds"</li>
 * <li>a couple of {@link DateFormat}s which should be used to format
 * {@link Date}s at this TimeUnit.
 * <ul>
 * For example at TimeUnit "Days" a {@link DateFormat} should not include
 * anything below the accuracy of days such as hours or minutes, because it
 * would not make sense to display this highly inaccurate data. Another aspect
 * is that for example the provided short {@link DateFormat} should not include
 * years as this would make the the output too long. The long {@link DateFormat}
 * on the other hand should include years but not milliseconds or seconds, but
 * it should include hours to allow a little interpolation of dates.
 * </ul>
 * <ul>
 * Which {@link DateFormat}s to provide is not straightforward and must be
 * chosen carefully depending on the context of it's use.
 * {@link TimeUnitProvider} provides a set of predefined TimeUnits for the
 * {@link AdvancedTimeScale} class.
 * </ul>
 * </li>
 * <li> The maximum length of the TimeUnit in milliseconds. </li>
 * </p>
 * 
 * @author peterw
 * @see BasicTimeScale
 * @see TimeScaleHeader
 * @see DefaultAccuracyList
 */
public class TimeUnit implements Comparable<TimeUnit> {
	private Long maxLengthInMillis;
	private String name;
	private int field;
	private int factor;

	private DateFormat fullFormat;
	private DateFormat longFormat;
	private DateFormat shortFormat;

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
	public TimeUnit(String name, int field, DateFormat shortFormat, DateFormat longFormat, DateFormat fullFormat) {
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
	 * and the fullFormat "dd.MM yyyy HH:mm:ss.SSS". These are the values used
	 * in {@link DefaultAccuracyList}.
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
	public TimeUnit(String name, int field, int factor, DateFormat shortFormat, DateFormat longFormat,
			DateFormat fullFormat) {
		this.shortFormat = shortFormat;
		this.longFormat = longFormat;
		this.fullFormat = fullFormat;
		this.name = name;
		this.field = field;
		this.factor = factor;

		maxLengthInMillis = getMaxLength(field, factor);
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
			maxLength = (long) 1000 * 60 * 60 * 25 * 7;
			break;
		case Calendar.YEAR:
			maxLength = (long) 1000 * 60 * 60 * 24 * 366;
			break;
		default:
			throw new IllegalArgumentException("unsupported field");
		}
		return maxLength;
	}

	/**
	 * <p>
	 * Returns the maximum length in milliseconds.
	 * </p>
	 * <p>
	 * An TimeUnit can be of varying length. For example an TimeUnit
	 * representing one month may have 30 or 31 days. This method always returns
	 * the number of milliseconds representing 31 days as this is the maximum
	 * number of days one month can have. In the case of seconds this method
	 * would return 1000.
	 * </p>
	 * 
	 * @return the maximum length in milliseconds
	 */
	public long getMaxLengthInMillis() {
		return maxLengthInMillis;
	}

	/**
	 * <p>
	 * Returns the name of this TimeUnit.
	 * </p>
	 * <p>
	 * As TimeUnit objects are intended to be associated with
	 * {@link AdvancedTimeScale}s and are used to represent their current
	 * accuracy, the name should be named according to this meaning and
	 * therefore be in plural. It's used to tell the user: "The current accuracy
	 * is {name}" (eg. "Seconds", not "Second").
	 * </p>
	 * 
	 * @see AdvancedTimeScale
	 * @see StatusBar
	 * @return the name of this TimeUnit
	 */
	public String getName() {
		return name;
	}

	/**
	 * <p>
	 * Returns the {@link DateFormat} intended to be used, when there is not
	 * much space available.
	 * </p>
	 * 
	 * @return the short {@link DateFormat}
	 */
	public DateFormat getShortFormat() {
		return shortFormat;
	}

	/**
	 * <p>
	 * Returns the {@link DateFormat} intended to be used, when there is enough
	 * space available to display a date in a long format.
	 * </p>
	 * 
	 * @return the long {@link DateFormat}
	 */
	public DateFormat getLongFormat() {
		return longFormat;
	}

	/**
	 * <p>
	 * Returns the {@link DateFormat} intended to be used, when there is plenty
	 * of space available and the Date should be displayed in great detail.
	 * </p>
	 * 
	 * @return the long {@link DateFormat}
	 */
	public DateFormat getFullFormat() {
		return fullFormat;
	}

	/**
	 * <p>
	 * Returns a date in milliseconds, which fits into this TimeUnit and
	 * therefore can be accurately formatted using one of the provided
	 * {@link DateFormat}s and is the nearest date before the given date.
	 * </p>
	 * 
	 * @param date
	 *            in milliseconds
	 * @return the nearest date fitting into this TimeUnit before the given date
	 */
	public long previous(long date) {
		return DateUtil.truncate(date, field, factor);
	}

	/**
	 * <p>
	 * Returns a date in milliseconds, which fits into this TimeUnit and
	 * therefore can be accurately formatted using one of the provided
	 * {@link DateFormat}s and is the nearest date after the given date.
	 * </p>
	 * 
	 * @param date
	 *            in milliseconds
	 * @return the nearest date fitting into this TimeUnit after the given date
	 */
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
	 * Compares TimeUnits ONLY considering the maximum lengths.
	 */
	public int compareTo(TimeUnit o) {
		return maxLengthInMillis.compareTo(o.maxLengthInMillis);
	}

	/**
	 * Compares TimeUnits ONLY considering the maximum lengths.
	 */
	public boolean equals(Object obj) {
		return maxLengthInMillis.equals(((TimeUnit) obj).maxLengthInMillis);
	}
	
	public String toString() {
		return getName() + ": " + getMaxLengthInMillis();
	}

	public int hashCode() {
		return maxLengthInMillis.hashCode();
	}
}