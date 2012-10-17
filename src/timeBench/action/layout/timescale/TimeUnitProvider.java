package timeBench.action.layout.timescale;

import ieg.util.QuarterDateFormat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Locale;

//TODO use/replace by calendar package

/**
 * <p>
 * Provides a set of {@link TimeUnit}s used by {@link BasicTimeScale} used
 * determine which levels accuracy to use for a given date range and display
 * size.
 * </p>
 * <p>
 * Override this class to provide more sophisticated implementations of
 * {@link TimeUnitProvider}, like one that can be configured via XML file.
 * </p>
 * 
 * @see BasicTimeScale
 * @author peterw
 */
public class TimeUnitProvider {
	private TimeUnit weekUnit;
	private final LinkedList<TimeUnit> timeUnits = new LinkedList<TimeUnit>();

	/**
	 * <p>
	 * Creates a default TimeUnitProvider with a set of predefined, hardcoded
	 * {@link TimeUnit}s.
	 * </p>
	 * 
	 * @see BasicTimeScale
	 * @author Peter Weishapl
	 */
	public static TimeUnitProvider createDefaultTimeUnitProvider() {
		TimeUnitProvider tup = new TimeUnitProvider();
		
		// handle two languages (e.g., for MouseTracker)
		String dataformat = (Locale.getDefault().getLanguage() == "de") ? "dd.MM yyyy" : "MMM d, yyyy";

		SimpleDateFormat milliFormat = new SimpleDateFormat("SSS");
		SimpleDateFormat milliFormatFull = new SimpleDateFormat(dataformat + " HH:mm:ss.SSS");
		tup.add(new GregorianTimeUnit("50 Milliseconds", Calendar.MILLISECOND, 50, milliFormat, milliFormat, milliFormatFull));

		SimpleDateFormat secondFormatShort = new SimpleDateFormat("ss");
		SimpleDateFormat secondFormatLong = new SimpleDateFormat("HH:mm:ss");
		tup.add(new GregorianTimeUnit("Seconds", Calendar.SECOND, secondFormatShort, secondFormatLong, milliFormatFull));
		tup.add(new GregorianTimeUnit("5 Seconds", Calendar.SECOND, 5, secondFormatShort, secondFormatLong, milliFormatFull));
		tup.add(new GregorianTimeUnit("15 Seconds", Calendar.SECOND, 15, secondFormatShort, secondFormatLong, milliFormatFull));

		SimpleDateFormat minuteFormatShort = new SimpleDateFormat("mm");
		SimpleDateFormat minuteFormatLong = new SimpleDateFormat("HH:mm");
		SimpleDateFormat minuteFormatFull = new SimpleDateFormat(dataformat + " HH:mm:ss");
		tup.add(new GregorianTimeUnit("Minutes", Calendar.MINUTE, minuteFormatShort, minuteFormatLong, minuteFormatFull));
		tup.add(new GregorianTimeUnit("5 Minutes", Calendar.MINUTE, 5, minuteFormatShort, minuteFormatLong, minuteFormatFull));
		tup.add(new GregorianTimeUnit("15 Minutes", Calendar.MINUTE, 15, minuteFormatShort, minuteFormatLong, minuteFormatFull));

		SimpleDateFormat hourFormatShort = new SimpleDateFormat("HH");
		SimpleDateFormat hourFormatLong = new SimpleDateFormat("dd.MM HH:mm");
		tup.add(new GregorianTimeUnit("Hours", Calendar.HOUR, hourFormatShort, hourFormatLong, minuteFormatFull));
		tup.add(new GregorianTimeUnit("4 Hours", Calendar.HOUR, 4, hourFormatShort, hourFormatLong, minuteFormatFull));

		SimpleDateFormat dayFormatShort = new SimpleDateFormat("dd");
		DateFormat dayFormatLong = DateFormat.getDateInstance();
		SimpleDateFormat dayFormatFull = new SimpleDateFormat(dataformat + " HH:mm");

		tup.add(new GregorianTimeUnit("Days", Calendar.DAY_OF_MONTH, dayFormatShort, dayFormatLong, dayFormatFull));
		SimpleDateFormat weekFormatShort = new SimpleDateFormat("w");

		String weekPrefix = (Locale.getDefault().getLanguage().equals("de")) ? "Woche" : "Week";
		SimpleDateFormat weekFormatLong = new SimpleDateFormat("'" + weekPrefix + "' w");
		SimpleDateFormat weekFormatFull = new SimpleDateFormat(dataformat + " HH:mm");
		tup.weekUnit = new GregorianTimeUnit("Weeks", Calendar.WEEK_OF_YEAR, weekFormatShort, weekFormatLong, weekFormatFull);
		tup.add(tup.weekUnit);

		SimpleDateFormat monthFormatShort = new SimpleDateFormat("MMM");
		SimpleDateFormat monthFormatLong = new SimpleDateFormat("MMMMM yy");
		SimpleDateFormat monthFormatFull = new SimpleDateFormat(dataformat);
		tup.add(new GregorianTimeUnit("Months", Calendar.MONTH, monthFormatShort, monthFormatLong, monthFormatFull));

		String quarterPrefix = (Locale.getDefault().getLanguage().equals("de")) ? "Quartal " : "Quarter ";
		DateFormat quarterFormatShort = new QuarterDateFormat(false, "Q", false);
		DateFormat quarterFormatLong = new QuarterDateFormat(true, quarterPrefix, false);
		DateFormat quarterFormatFull = new QuarterDateFormat(true, quarterPrefix, false);
		tup.add(new GregorianTimeUnit("Quartals", Calendar.MONTH, 3, quarterFormatShort, quarterFormatLong, quarterFormatFull));

		SimpleDateFormat yearFormatShort = new SimpleDateFormat("yy");
		SimpleDateFormat yearFormatLong = new SimpleDateFormat("yyyy");
		tup.add(new GregorianTimeUnit("Years", Calendar.YEAR, yearFormatShort, yearFormatLong, monthFormatFull));
        tup.add(new GregorianTimeUnit("Decades", Calendar.YEAR, 10, yearFormatShort, yearFormatLong, monthFormatFull));
        tup.add(new GregorianTimeUnit("Centuries", Calendar.YEAR, 100, yearFormatShort, yearFormatLong, monthFormatFull));
        tup.add(new GregorianTimeUnit("Millennia", Calendar.YEAR, 1000, yearFormatShort, yearFormatLong, yearFormatLong));
        // TODO better short format for centuries and millennia 

		return tup;
	}

	/**
	 * Returns the next less accurate {@link TimeUnit} based on the given
	 * {@link TimeUnit}.
	 * 
	 * @param timeUnit
	 *            the next more accurate {@link TimeUnit} than the returned one
	 * @return the next less accurate {@link TimeUnit} than the given one
	 */
	public TimeUnit getLonger(TimeUnit timeUnit) {
		int index = timeUnits.indexOf(timeUnit) + 1;
		if (index < timeUnits.size()) {
			return timeUnits.get(index);
		}
		return null;
	}

	/**
	 * Returns the least accurate {@link TimeUnit}.
	 * 
	 * @return the least accurate {@link TimeUnit}
	 */
	public TimeUnit getLongest() {
		return timeUnits.getLast();
	}

	/**
	 * Returns the most accurate {@link TimeUnit}.
	 * 
	 * @return the most accurate {@link TimeUnit}
	 */
	public TimeUnit getShortest() {
		return timeUnits.getFirst();
	}

	/**
	 * <p>
	 * Returns the best {@link TimeUnit}, based on the maximum number of
	 * milliseconds that can be displayed in one pixel and the minimum number of
	 * pixels that can be used to display one {@link TimeUnit}.
	 * </p>
	 * <p>
	 * The best {@link TimeUnit} is determined through calculating the minimal
	 * milliseconds per {@link TimeUnit} by multiplying the millisPerPixel with
	 * minimumPixelPerUnit.
	 * </p>
	 * <p>
	 * The shortest {@link TimeUnit} whose maximum length is larger than the
	 * calculated minimal milliseconds per {@link TimeUnit} is chosen as the
	 * best fitting {@link TimeUnit}.
	 * </p>
	 * <p>
	 * For example if the desired milliseconds per pixel would be set to 105 and
	 * the minimum number of pixels per unit would be 9, the minimum number of
	 * pixels per {@link TimeUnit} would be 9*105=945. The chosen
	 * {@link TimeUnit} would therefore be "Seconds" (if provided), which has a
	 * (maximum) length of 1000 milliseconds.
	 * </p>
	 * 
	 * @param millisPerPixel
	 *            the maximum number of milliseconds that can be displayed in
	 *            one pixel
	 * @param minimumPixelPerUnit
	 *            the minimum number of milliseconds that can used to display
	 *            one {@link TimeUnit}
	 * @return the {@link TimeUnit} that best fits the given criteria
	 */
	public TimeUnit getBest(double millisPerPixel, int minimumPixelPerUnit) {
		double millisPerUnit = millisPerPixel * minimumPixelPerUnit;

		for (TimeUnit timeUnit : timeUnits) {
			if (timeUnit.getMaxLengthInMillis() > millisPerUnit) {
				return timeUnit;
			}
		}
		return getLongest();
	}

	/**
	 * <p>
	 * Returns the {@link TimeUnit} representing one week.
	 * </p>
	 * <p>
	 * This information is necessary to distinguish between {@link TimeUnit}s
	 * longer and the ones shorter than one week so that for {@link TimeUnit}s
	 * shorter than one week, weekends can be highlighted in the user interface.
	 * </p>
	 * 
	 * @return the {@link TimeUnit} representing one week
	 */
	public TimeUnit getWeekUnit() {
		return weekUnit;
	}

	/**
	 * <p>
	 * Set the {@link TimeUnit} representing one week.
	 * </p>
	 * <p>
	 * This information is necessary to distinguish between {@link TimeUnit}s
	 * longer and the ones shorter than one week so that for {@link TimeUnit}s
	 * shorter than one week, weekends can be highlighted in the user interface.
	 * </p>
	 * 
	 * @param weekUnit
	 *            the {@link TimeUnit} that is one week long
	 */
	public void setWeekUnit(TimeUnit weekUnit) {
		this.weekUnit = weekUnit;
	}

	protected void add(TimeUnit timeUnit) {
		timeUnits.add(timeUnit);
		Collections.sort(timeUnits);
	}
}