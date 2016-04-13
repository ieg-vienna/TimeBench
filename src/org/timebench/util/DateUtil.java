package org.timebench.util;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

import prefuse.data.Tuple;

// TODO replace by calendar package

/**
 * Provides methos to truncate {@link Date}s used by {@link TimeUnit} to 'fit'
 * dates and other utils.
 * 
 * @author peterw
 * @see TimeUnit
 */
public class DateUtil {
	/**
	 * Use this in prefuse {@link Tuple}s instead of null (using null throws a
	 * {@link NullPointerException}).
	 */
	public final static Date NULL_DATE = new Date(0);

	/**
	 * Check if a date is {@link DateUtil#NULL_DATE} or null.
	 * 
	 * @param date
	 *            the date to check
	 * @return true if the given date is {@link DateUtil#NULL_DATE} or null
	 */
	public static boolean isNull(Date date) {
		return NULL_DATE.equals(date) || date == null;
	}

	/**
	 * Truncate a date to fit into a {@link TimeUnit} with the given
	 * {@link Calendar} field and factor.
	 * 
	 * @param time
	 *            the date in milliseconds
	 * @param field
	 *            the {@link Calendar} field
	 * @param factor
	 *            the multiplying factors
	 * @return the truncated date in milliseconds
	 */
	public static long truncate(long time, int field, int factor) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);

		cal = truncate(cal, field, factor);
		return cal.getTimeInMillis();
	}

	/**
	 * Truncate a date to fit into a {@link TimeUnit} with the given
	 * {@link Calendar} field and factor.
	 * 
	 * @param cal
	 *            the date
	 * @param field
	 *            the {@link Calendar} field
	 * @param factor
	 *            the multiplying factors
	 * @return the truncated date in milliseconds
	 */
	public static Calendar truncate(Calendar cal, int field, int factor) {
		if (field == Calendar.WEEK_OF_YEAR) {
			cal.setFirstDayOfWeek(Calendar.MONDAY);
			cal = DateUtils.truncate(cal, Calendar.DAY_OF_MONTH);
			cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		} else {
			cal = DateUtils.truncate(cal, field);
		}

		cal.set(field, cal.get(field) - (cal.get(field) % factor));

		return cal;
	}

	/**
	 * Returns if a given date is on a weekend or not
	 * 
	 * @param cal
	 *            the date
	 * @return true if the date is on a weekend, false if not
	 */
	public static boolean isWeekend(Calendar cal) {
		int day = cal.get(Calendar.DAY_OF_WEEK);
		if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
			return true;
		}
		return false;
	}
}
