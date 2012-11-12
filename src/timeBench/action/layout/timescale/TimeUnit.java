package timeBench.action.layout.timescale;

import java.text.DateFormat;
import java.util.Date;

import timeBench.ui.TimeScaleStatusBar;
import timeBench.ui.TimeScaleHeader;

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
 * <li>a name like "Month", "Quarter" or "50 Milliseconds"</li>
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
 */
public abstract class TimeUnit implements Comparable<TimeUnit> {
	private String name;

	private DateFormat fullFormat;
	private DateFormat longFormat;
	private DateFormat shortFormat;

	    /**
     * <p>
     * Creates a new TimeUnit with a given name and the various
     * {@link DateFormat}s, {@link Date}s with this TimeUnit should be formatted
     * with.
     * </p>
     * 
     * @param name
     *            the name of the TimeUnit
     * @param shortFormat
     *            the short {@link DateFormat}
     * @param longFormat
     *            the long {@link DateFormat}
     * @param fullFormat
     *            the full {@link DateFormat}
     */
	public TimeUnit(String name, DateFormat shortFormat, DateFormat longFormat,
			DateFormat fullFormat) {
		this.shortFormat = shortFormat;
		this.longFormat = longFormat;
		this.fullFormat = fullFormat;
		this.name = name;
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
	public abstract long getMaxLengthInMillis();

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
	 * @see TimeScaleStatusBar
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
	public abstract long previous(long date);

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
	public abstract long next(long date);

	/**
	 * Compares TimeUnits ONLY considering the maximum lengths.
	 */
	public int compareTo(TimeUnit o) {
	    return (getMaxLengthInMillis() < o.getMaxLengthInMillis()) ? -1 :
	        ((getMaxLengthInMillis() == o.getMaxLengthInMillis()) ? 0 : 1);
	}

	/**
	 * Compares TimeUnits ONLY considering the maximum lengths.
	 */
	public boolean equals(Object obj) {
		return getMaxLengthInMillis() == (((TimeUnit) obj).getMaxLengthInMillis());
	}
	
	public String toString() {
		return getName() + ": " + getMaxLengthInMillis();
	}
}