package timeBench.action.layout.timescale;

import ieg.util.QuarterDateFormat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import timeBench.calendar.Granularity;
import timeBench.calendar.JavaDateCalendarManager;
import timeBench.calendar.JavaDateCalendarManager.Granularities;

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
	private final SortedSet<TimeUnit> timeUnits = new TreeSet<TimeUnit>();

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
//	    return createGregorianTimeUnitProvider();
        return createGranularityTimeUnitProvider();
	}
	
	public static TimeUnitProvider createGregorianTimeUnitProvider() {
		TimeUnitProvider tup = new TimeUnitProvider();
		// TimeBench calendar currently works in UTC only
		TimeZone tz = TimeZone.getTimeZone("UTC");
		
		// handle two languages (e.g., for MouseTracker)
		String dataformat = (Locale.getDefault().getLanguage() == "de") ? "dd.MM yyyy" : "MMM d, yyyy";

		SimpleDateFormat milliFormat = new SimpleDateFormat("SSS");
		milliFormat.setTimeZone(tz);
		SimpleDateFormat milliFormatFull = new SimpleDateFormat(dataformat + " HH:mm:ss.SSS");
		milliFormatFull.setTimeZone(tz);
		tup.add(new GregorianTimeUnit("50 Milliseconds", Calendar.MILLISECOND, 50, milliFormat, milliFormat, milliFormatFull));

		SimpleDateFormat secondFormatShort = new SimpleDateFormat("ss");
		secondFormatShort.setTimeZone(tz);
		SimpleDateFormat secondFormatLong = new SimpleDateFormat("HH:mm:ss");
		secondFormatLong.setTimeZone(tz);
		tup.add(new GregorianTimeUnit("Seconds", Calendar.SECOND, secondFormatShort, secondFormatLong, milliFormatFull));
		tup.add(new GregorianTimeUnit("5 Seconds", Calendar.SECOND, 5, secondFormatShort, secondFormatLong, milliFormatFull));
		tup.add(new GregorianTimeUnit("15 Seconds", Calendar.SECOND, 15, secondFormatShort, secondFormatLong, milliFormatFull));

		SimpleDateFormat minuteFormatShort = new SimpleDateFormat("mm");
		minuteFormatShort.setTimeZone(tz);
		SimpleDateFormat minuteFormatLong = new SimpleDateFormat("HH:mm");
		minuteFormatLong.setTimeZone(tz);
		SimpleDateFormat minuteFormatFull = new SimpleDateFormat(dataformat + " HH:mm:ss");
		minuteFormatFull.setTimeZone(tz);
		tup.add(new GregorianTimeUnit("Minutes", Calendar.MINUTE, minuteFormatShort, minuteFormatLong, minuteFormatFull));
		tup.add(new GregorianTimeUnit("5 Minutes", Calendar.MINUTE, 5, minuteFormatShort, minuteFormatLong, minuteFormatFull));
		tup.add(new GregorianTimeUnit("15 Minutes", Calendar.MINUTE, 15, minuteFormatShort, minuteFormatLong, minuteFormatFull));

		SimpleDateFormat hourFormatShort = new SimpleDateFormat("HH");
		hourFormatShort.setTimeZone(tz);
		SimpleDateFormat hourFormatLong = new SimpleDateFormat("dd.MM HH:mm");
		hourFormatLong.setTimeZone(tz);
		tup.add(new GregorianTimeUnit("Hours", Calendar.HOUR, hourFormatShort, hourFormatLong, minuteFormatFull));
		tup.add(new GregorianTimeUnit("4 Hours", Calendar.HOUR, 4, hourFormatShort, hourFormatLong, minuteFormatFull));

		SimpleDateFormat dayFormatShort = new SimpleDateFormat("dd");
		dayFormatShort.setTimeZone(tz);
		DateFormat dayFormatLong = DateFormat.getDateInstance();
		dayFormatLong.setTimeZone(tz);
		SimpleDateFormat dayFormatFull = new SimpleDateFormat(dataformat + " HH:mm");
		dayFormatFull.setTimeZone(tz);
		tup.add(new GregorianTimeUnit("Days", Calendar.DAY_OF_MONTH, dayFormatShort, dayFormatLong, dayFormatFull));
		
		SimpleDateFormat weekFormatShort = new SimpleDateFormat("w");
		weekFormatShort.setTimeZone(tz);
		String weekPrefix = (Locale.getDefault().getLanguage().equals("de")) ? "Woche" : "Week";
		SimpleDateFormat weekFormatLong = new SimpleDateFormat("'" + weekPrefix + "' w");
		weekFormatLong.setTimeZone(tz);
		SimpleDateFormat weekFormatFull = new SimpleDateFormat(dataformat + " HH:mm");
		weekFormatFull.setTimeZone(tz);
		tup.weekUnit = new GregorianTimeUnit("Weeks", Calendar.WEEK_OF_YEAR, weekFormatShort, weekFormatLong, weekFormatFull);
		tup.add(tup.weekUnit);

		SimpleDateFormat monthFormatShort = new SimpleDateFormat("MMM");
		monthFormatShort.setTimeZone(tz);
		SimpleDateFormat monthFormatLong = new SimpleDateFormat("MMMMM yy");
		monthFormatLong.setTimeZone(tz);
		SimpleDateFormat monthFormatFull = new SimpleDateFormat(dataformat);
		monthFormatFull.setTimeZone(tz);
		tup.add(new GregorianTimeUnit("Months", Calendar.MONTH, monthFormatShort, monthFormatLong, monthFormatFull));

		String quarterPrefix = (Locale.getDefault().getLanguage().equals("de")) ? "Quartal " : "Quarter ";
		DateFormat quarterFormatShort = new QuarterDateFormat(false, "Q", false);
		quarterFormatShort.setTimeZone(tz);
		DateFormat quarterFormatLong = new QuarterDateFormat(true, quarterPrefix, false);
		quarterFormatLong.setTimeZone(tz);
		DateFormat quarterFormatFull = new QuarterDateFormat(true, quarterPrefix, false);
		quarterFormatFull.setTimeZone(tz);
		tup.add(new GregorianTimeUnit("Quarters", Calendar.MONTH, 3, quarterFormatShort, quarterFormatLong, quarterFormatFull));

		SimpleDateFormat yearFormatShort = new SimpleDateFormat("yy");
		yearFormatShort.setTimeZone(tz);
		SimpleDateFormat yearFormatLong = new SimpleDateFormat("yyyy");
		yearFormatLong.setTimeZone(tz);
		tup.add(new GregorianTimeUnit("Years", Calendar.YEAR, yearFormatShort, yearFormatLong, monthFormatFull));
        tup.add(new GregorianTimeUnit("Decades", Calendar.YEAR, 10, yearFormatShort, yearFormatLong, monthFormatFull));
        tup.add(new GregorianTimeUnit("Centuries", Calendar.YEAR, 100, yearFormatShort, yearFormatLong, monthFormatFull));
        tup.add(new GregorianTimeUnit("Millennia", Calendar.YEAR, 1000, yearFormatShort, yearFormatLong, yearFormatLong));
        // TODO better short format for centuries and millennia 

		return tup;
	}

    public static TimeUnitProvider createGranularityTimeUnitProvider() {
        TimeUnitProvider tup = new TimeUnitProvider();
        
        timeBench.calendar.Calendar cal = JavaDateCalendarManager.getSingleton().calendar();
        Granularity ms = new Granularity(cal, Granularities.Millisecond.toInt(), Granularities.Top.toInt());
        Granularity sec = new Granularity(cal, Granularities.Second.toInt(), Granularities.Top.toInt());
        Granularity min = new Granularity(cal, Granularities.Minute.toInt(), Granularities.Top.toInt());
        Granularity hour = new Granularity(cal, Granularities.Hour.toInt(), Granularities.Top.toInt());
        Granularity day = new Granularity(cal, Granularities.Day.toInt(), Granularities.Top.toInt());
        Granularity week = new Granularity(cal, Granularities.Week.toInt(), Granularities.Top.toInt());
        Granularity month = new Granularity(cal, Granularities.Month.toInt(), Granularities.Top.toInt());
        Granularity quarter = new Granularity(cal, Granularities.Quarter.toInt(), Granularities.Top.toInt());
        Granularity year = new Granularity(cal, Granularities.Year.toInt(), Granularities.Top.toInt());
        Granularity decade = new Granularity(cal, Granularities.Decade.toInt(), Granularities.Top.toInt());
        
        // handle two languages (e.g., for MouseTracker)
        String dataformat = (Locale.getDefault().getLanguage() == "de") ? "dd.MM yyyy" : "MMM d, yyyy";

        SimpleDateFormat milliFormat = new SimpleDateFormat("SSS");
        SimpleDateFormat milliFormatFull = new SimpleDateFormat(dataformat + " HH:mm:ss.SSS");
        tup.add(new GranularityTimeUnit("50 Milliseconds", ms, 50, milliFormat, milliFormat, milliFormatFull));

        SimpleDateFormat secondFormatShort = new SimpleDateFormat("ss");
        SimpleDateFormat secondFormatLong = new SimpleDateFormat("HH:mm:ss");
        tup.add(new GranularityTimeUnit("Seconds", sec, secondFormatShort, secondFormatLong, milliFormatFull));
        tup.add(new GranularityTimeUnit("5 Seconds", sec, 5, secondFormatShort, secondFormatLong, milliFormatFull));
        tup.add(new GranularityTimeUnit("15 Seconds", sec, 15, secondFormatShort, secondFormatLong, milliFormatFull));
	
        SimpleDateFormat minuteFormatShort = new SimpleDateFormat("mm");
        SimpleDateFormat minuteFormatLong = new SimpleDateFormat("HH:mm");
        SimpleDateFormat minuteFormatFull = new SimpleDateFormat(dataformat + " HH:mm:ss");
        tup.add(new GranularityTimeUnit("Minutes", min, minuteFormatShort, minuteFormatLong, minuteFormatFull));
        tup.add(new GranularityTimeUnit("5 Minutes", min, 5, minuteFormatShort, minuteFormatLong, minuteFormatFull));
        tup.add(new GranularityTimeUnit("15 Minutes", min, 15, minuteFormatShort, minuteFormatLong, minuteFormatFull));

        SimpleDateFormat hourFormatShort = new SimpleDateFormat("HH");
        SimpleDateFormat hourFormatLong = new SimpleDateFormat("dd.MM HH:mm");
        tup.add(new GranularityTimeUnit("Hours", hour, hourFormatShort, hourFormatLong, minuteFormatFull));
        tup.add(new GranularityTimeUnit("4 Hours", hour, 4, hourFormatShort, hourFormatLong, minuteFormatFull));

        SimpleDateFormat dayFormatShort = new SimpleDateFormat("dd");
        DateFormat dayFormatLong = DateFormat.getDateInstance();
        SimpleDateFormat dayFormatFull = new SimpleDateFormat(dataformat + " HH:mm");
        tup.add(new GranularityTimeUnit("Days", day, dayFormatShort, dayFormatLong, dayFormatFull));

        SimpleDateFormat weekFormatShort = new SimpleDateFormat("w");
        String weekPrefix = (Locale.getDefault().getLanguage().equals("de")) ? "Woche" : "Week";
        SimpleDateFormat weekFormatLong = new SimpleDateFormat("'" + weekPrefix + "' w");
        SimpleDateFormat weekFormatFull = new SimpleDateFormat(dataformat + " HH:mm");
        tup.weekUnit = new GranularityTimeUnit("Weeks", week, weekFormatShort, weekFormatLong, weekFormatFull);
        tup.add(tup.weekUnit);

        SimpleDateFormat monthFormatShort = new SimpleDateFormat("MMM");
        SimpleDateFormat monthFormatLong = new SimpleDateFormat("MMMMM yy");
        SimpleDateFormat monthFormatFull = new SimpleDateFormat(dataformat);
        tup.add(new GranularityTimeUnit("Months", month, monthFormatShort, monthFormatLong, monthFormatFull));

        String quarterPrefix = (Locale.getDefault().getLanguage().equals("de")) ? "Quartal " : "Quarter ";
        DateFormat quarterFormatShort = new QuarterDateFormat(false, "Q", false);
        DateFormat quarterFormatLong = new QuarterDateFormat(true, quarterPrefix, false);
        DateFormat quarterFormatFull = new QuarterDateFormat(true, quarterPrefix, false);
        tup.add(new GranularityTimeUnit("Quarters", quarter, 1, quarterFormatShort, quarterFormatLong, quarterFormatFull));
        
        SimpleDateFormat yearFormatShort = new SimpleDateFormat("yy");
        SimpleDateFormat yearFormatLong = new SimpleDateFormat("yyyy");
        tup.add(new GranularityTimeUnit("Years", year, yearFormatShort, yearFormatLong, monthFormatFull));
        tup.add(new GranularityTimeUnit("Decades", decade, yearFormatShort, yearFormatLong, monthFormatFull));
        tup.add(new GranularityTimeUnit("Centuries", decade, 10, yearFormatShort, yearFormatLong, monthFormatFull));
        tup.add(new GranularityTimeUnit("Millennia", decade, 100, yearFormatShort, yearFormatLong, yearFormatLong));
        
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
	    Iterator<TimeUnit>  i = timeUnits.tailSet(timeUnit).iterator();
	    if (i.hasNext()) {
	        i.next();
	        if (i.hasNext()) {
	            return i.next();
	        }
	    }
	    
		return null;
	}

	/**
	 * Returns the least accurate {@link TimeUnit}.
	 * 
	 * @return the least accurate {@link TimeUnit}
	 */
	public TimeUnit getLongest() {
		return timeUnits.last();
	}

	/**
	 * Returns the most accurate {@link TimeUnit}.
	 * 
	 * @return the most accurate {@link TimeUnit}
	 */
	public TimeUnit getShortest() {
		return timeUnits.first();
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
	}
}