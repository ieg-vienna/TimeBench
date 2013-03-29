package timeBench.action.layout.timescale;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import org.apache.log4j.Logger;

/**
 * <p>
 * The central class in the TimeVis API. It essentially implements the mapping
 * between pixels and dates by using a start date and the number of milliseconds
 * that should be displayed within one pixel.
 * </p>
 * <p>
 * BasicTimeScale uses {@link TimeUnitProvider#getBest(double, int)} to
 * determine the best fitting {@link TimeUnit} for the given milliseconds that
 * should be represented by one unit. {@link BasicTimeScale} can 'fit' the
 * pixel-to-date mapping (if desired; see
 * {@link BasicTimeScale#adjustDateToUnit(int, long)}) along the determined
 * {@link TimeUnit} to absorb rounding errors between dates that lie in one
 * {@link TimeUnit} and therefore have to be displayed exactly. This adjustement
 * is based on the desired pixels per millisecond and the minimum number of
 * pixels to be displayed within one {@link TimeUnit}.
 * </p>
 * 
 * @author peterw
 * 
 */
public class BasicTimeScale implements TimeScale {
	protected final Logger log = Logger.getLogger(getClass());
	private ChangeEvent ce = new ChangeEvent(this);
	private EventListenerList listenerList = new EventListenerList();

	protected long startDate;
	protected long millisPerPixel;

	protected TimeUnit timeUnit;
	protected TimeUnitProvider timeUnitProvider;
	protected int minimumPixelPerUnit = 28;

	/**
	 * Create a default {@link BasicTimeScale} with a default
	 * {@link TimeUnitProvider}, the current date as the start date and one
	 * hour representing one pixel.
	 */
	public BasicTimeScale() {
		this(TimeUnitProvider.createDefaultTimeUnitProvider());
	}

	/**
	 * Creates a {@link BasicTimeScale} with the current date as the start date
	 * and one hour representing one pixel.
	 * 
	 * @param timeUnitProvider
	 *            a {@link TimeUnitProvider} which will be use to determine the
	 *            best fitting {@link TimeUnit}
	 */
	public BasicTimeScale(TimeUnitProvider timeUnitProvider) {
		this(new Date().getTime(), GregorianTimeUnit.getMaxLength(Calendar.HOUR, 1), timeUnitProvider);
	}
	
	/**
	 * Create a {@link BasicTimeScale} with the start date, the
	 * milliseconds per pixel and {@link TimeUnitProvider} of the given {@link TimeScale}.
	 * 
	 * @param scale
	 *            the base {@link BasicTimeScale}
	 */
	public BasicTimeScale(TimeScale scale) {
		this(scale.getStartDate(), scale.getMillisPerPixel(), scale.getTimeUnitProvider());
	}

	/**
	 * Creates a {@link BasicTimeScale} with a default {@link TimeUnitProvider}.
	 * 
	 * @param startDate
	 *            a start date in milliseconds. The first pixel of this
	 *            {@link BasicTimeScale} will match to this date
	 * @param millisPerPixel
	 *            the number of millisecond each pixel represents
	 */
	public BasicTimeScale(long startDate, long millisPerPixel) {
		this(startDate, millisPerPixel, TimeUnitProvider.createDefaultTimeUnitProvider());
	}

	/**
	 * Creates a {@link BasicTimeScale} with a given start date in milliseconds,
	 * the number of milliseconds that should be represented by one pixel and a
	 * {@link TimeUnitProvider} which will be used to determine the best fitting
	 * {@link TimeUnit}.
	 * 
	 * @param startDate
	 *            a start date in milliseconds. The first pixel of this
	 *            {@link BasicTimeScale} will match to this date
	 * @param millisPerPixel
	 *            the number of millisecond each pixel represents
	 * @param timeUnitProvider
	 *            a {@link TimeUnitProvider} which will be use to determine the
	 *            best fitting {@link TimeUnit}
	 */
	public BasicTimeScale(long startDate, long millisPerPixel, TimeUnitProvider timeUnitProvider) {
		this.timeUnitProvider = timeUnitProvider;

		this.startDate = startDate;
		this.millisPerPixel = millisPerPixel;

		timeUnit = timeUnitProvider.getBest(millisPerPixel, minimumPixelPerUnit);
	}

	/**
	 * Returns the number of milliseconds one pixel represents.
	 * 
	 * @return the number of milliseconds one pixel represents
	 */
	public long getMillisPerPixel() {
		return millisPerPixel;
	}

	/**
	 * Set the number of milliseconds one pixel represents.
	 * 
	 * @param millisPerPixel
	 *            the number of milliseconds one pixel represents
	 */
	public void setMillisPerPixel(long millisPerPixel) {
		this.millisPerPixel = millisPerPixel;
		adjustTimeScale();
	}

	/**
	 * Returns the current {@link TimeUnitProvider}.
	 * 
	 * @return the current {@link TimeUnitProvider}
	 */
	public TimeUnitProvider getTimeUnitProvider() {
		return timeUnitProvider;
	}

	/**
	 * Set the current {@link TimeUnitProvider}
	 * 
	 * @param timeUnitProvider
	 *            a {@link TimeUnitProvider}
	 */
	public void setTimeUnitProvider(TimeUnitProvider timeUnitProvider) {
		this.timeUnitProvider = timeUnitProvider;
		adjustTimeScale();
	}

	/**
	 * Returns the minimum number of pixels, that should be used to represent
	 * one {@link TimeUnit}.
	 * 
	 * @return the minimum number of pixels, that should be used to represent
	 *         one {@link TimeUnit}
	 * @see TimeUnitProvider#getBest(double, int)
	 */
	public int getMinimumPixelPerUnit() {
		return minimumPixelPerUnit;
	}

	/**
	 * Set the minimum number of pixels, that should be used to represent one
	 * {@link TimeUnit}.
	 * 
	 * @return the minimum number of pixels, that should be used to represent
	 *         one {@link TimeUnit}
	 * @see TimeUnitProvider#getBest(double, int)
	 */
	public void setMinimumPixelPerUnit(int minimumPixelPerUnit) {
		this.minimumPixelPerUnit = minimumPixelPerUnit;
		adjustTimeScale();
	}

	/**
	 * Returns the currently used {@link TimeUnit}
	 * 
	 * @return the currently used {@link TimeUnit}
	 */
	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	/**
	 * Returns the start date of this {@link BasicTimeScale}, which is
	 * represented by the first pixel.
	 * 
	 * @return the start date
	 */
	public long getStartDate() {
		return startDate;
	}

	/**
	 * Set the start date of this {@link BasicTimeScale}, which is represented
	 * by the first pixel.
	 * 
	 * @param startDate
	 *            the start date
	 */
	public void setStartDate(long startDate) {
		this.startDate = startDate;
		fireStateChanged();
	}

	/**
	 * Returns the start date adjusted to the current {@link TimeUnit}
	 * 
	 * @see BasicTimeScale#adjustDateToUnit(int, long)
	 * @return the adjusted start date
	 */
	public long getStartDateAdjusted() {
		return adjustDateToUnit(0, getStartDate());
	}

	/**
	 * Returns a date represented by the given pixel, adjusted to the current
	 * {@link TimeUnit}
	 * 
	 * @param pixel
	 *            a pixel
	 * @return an date represented by this pixel, adjusted to the current
	 *         {@link TimeUnit}
	 * @see BasicTimeScale#adjustDateToUnit(int, long)
	 */
	public long getDateAtPixel(int pixel) {
		return getDateAtPixel(pixel, true);
	}

	/**
	 * <p>
	 * Returns a date represented by the given pixel.
	 * </p>
	 * <p>
	 * This method uses {@link BasicTimeScale#getRawDate(int)} to retrieve that
	 * date and {@link BasicTimeScale#adjustDateToUnit(int, long)} to adjust
	 * this date, if given.
	 * </p>
	 * 
	 * @param pixel
	 *            a pixel
	 * @param adjustToUnit
	 *            return a date adjusted to the current {@link TimeUnit}
	 * @return a (adjusted) date represented by the given pixel
	 */
	public long getDateAtPixel(int pixel, boolean adjustToUnit) {
		long rawDate = getRawDate(pixel);

		if (adjustToUnit) {
			rawDate = adjustDateToUnit(pixel, rawDate);
		}

		return rawDate;
	}

	/**
	 * <p>
	 * Returns a date represented by the given pixel.
	 * </p>
	 * <p>
	 * The date is determined by multiplying given pixel with the milliseconds
	 * per pixel and adding the start date.
	 * </p>
	 * 
	 * @param pixel
	 * @return a date represented by the given pixel
	 */
	protected long getRawDate(int pixel) {
		return startDate + (long) (pixel * getMillisPerPixel());
	}

	/**
	 * Returns the pixel representing the given date.
	 * 
	 * @param date
	 *            a date
	 * @return the pixel representing the given date
	 */
	public int getPixelForDate(long date) {
		long startDifference = date - startDate;
		int x = (int) (startDifference / getMillisPerPixel());

		return x;
	}

	/**
	 * <p>
	 * Adjusts a given date at a particular pixel to the current
	 * {@link TimeUnit}.
	 * </p>
	 * <p>
	 * One pixel represents not a single date, but an interval defined by the
	 * number of milliseconds per pixel. The given pixel and date must match for
	 * this method to return correct results.
	 * </p>
	 * <p>
	 * Given, parameter pixel represents a range of dates, where one of these
	 * date 'fit' into the current {@link TimeUnit} and parameter date is one
	 * date represented by this pixel, but not 'fitting' into the
	 * {@link TimeUnit}. Adjusting this date to the current {@link TimeUnit}
	 * just means to convert the given date to the fitting date. Note that the
	 * pixel-date mapping remains correct as one pixel maps to both dates, the
	 * given and the returned.
	 * </p>
	 * <p>
	 * If none of the dates represented by the given pixel 'fits' into the
	 * current {@link TimeUnit}, the adjusted date will be interpolated based
	 * on the next 'fitting' date and the number of milliseconds per pixel.
	 * </p>
	 * 
	 * @param pixel
	 *            the pixel representing rawDate
	 * @param rawDate
	 *            a date represented by pixel
	 * @return a date adjusted to the {@link TimeUnit} and represented by the
	 *         given pixel
	 */
	public long adjustDateToUnit(int pixel, long rawDate) {
		if (timeUnit.previous(rawDate) == rawDate) {
			return rawDate;
		}

		long nextUnit = timeUnit.next(rawDate);
		int unitX = (int) getPixelForDate(nextUnit);

		rawDate = nextUnit - (unitX - pixel) * millisPerPixel;
		return rawDate;
	}

	/**
	 * Zoom this {@link BasicTimeScale}.
	 * 
	 * @param factor
	 *            the zoom factor
	 */
	public void zoom(double factor) {
		millisPerPixel *= factor;
		adjustTimeScale();
	}

	/**
	 * Pan this {@link BasicTimeScale}.
	 * 
	 * @param pixels
	 *            the number of pixels to pan
	 */
	public void pan(int pixels) {
		startDate = (long) (startDate - getMillisPerPixel() * pixels);
		fireStateChanged();
	}

	/**
	 * Adjusts the {@link TimeUnit} and fires a {@link ChangeEvent} when
	 * properties like millisPerSecond, startDate, etc. have changed.
	 */
	protected void adjustTimeScale() {
		timeUnit = timeUnitProvider.getBest(millisPerPixel, minimumPixelPerUnit);
		fireStateChanged();
	}

	protected void fireStateChanged() {
		for (ChangeListener l : listenerList.getListeners(ChangeListener.class)) {
			l.stateChanged(ce);
		}
	}

	public void addChangeListener(ChangeListener cl) {
		listenerList.add(ChangeListener.class, cl);
	}

	public void removeChangeListener(ChangeListener cl) {
		listenerList.remove(ChangeListener.class, cl);
	}

	public String toString() {
		DateFormat df = DateFormat.getDateTimeInstance();
		StringBuilder sb = new StringBuilder();
		sb.append(df.format(startDate));
		sb.append(", Unit: ").append(timeUnit);

		return sb.toString();
	}
}
