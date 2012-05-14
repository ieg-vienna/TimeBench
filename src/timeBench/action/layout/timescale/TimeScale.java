package timeBench.action.layout.timescale;

import javax.swing.event.ChangeListener;

/**
 * <p>
 * The central interface in the TimeVis API. It essentially implements the
 * mapping between pixels and dates.
 * </p>
 * 
 * This interface is only used for consumers of the time scale.
 * It allows reading and registering listeners but no manipulation.
 * 
 * It can be used for TimeLayout, MouseTracker, TimeScaleHeader, and TimeScalePainter. 
 * 
 * @author Alexander Rind
 */
public interface TimeScale {

	/**
	 * Returns the number of milliseconds one pixel represents.
	 * 
	 * @return the number of milliseconds one pixel represents
	 */
	public abstract long getMillisPerPixel();

	/**
	 * Returns the current {@link TimeUnitProvider}.
	 * 
	 * @return the current {@link TimeUnitProvider}
	 */
	public abstract TimeUnitProvider getTimeUnitProvider();

	/**
	 * Returns the minimum number of pixels, that should be used to represent
	 * one {@link TimeUnit}.
	 * 
	 * @return the minimum number of pixels, that should be used to represent
	 *         one {@link TimeUnit}
	 * @see TimeUnitProvider#getBest(double, int)
	 */
	public abstract int getMinimumPixelPerUnit();

	/**
	 * Returns the currently used {@link TimeUnit}
	 * 
	 * @return the currently used {@link TimeUnit}
	 */
	public abstract TimeUnit getTimeUnit();

	/**
	 * Returns the start date of this {@link BasicTimeScale}, which is
	 * represented by the first pixel.
	 * 
	 * @return the start date
	 */
	public abstract long getStartDate();

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
	public abstract long getDateAtPixel(int pixel);

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
	public abstract long getDateAtPixel(int pixel, boolean adjustToUnit);

	/**
	 * Returns the pixel representing the given date.
	 * 
	 * @param date
	 *            a date
	 * @return the pixel representing the given date
	 */
	public abstract int getPixelForDate(long date);

	public void addChangeListener(ChangeListener cl);

	public void removeChangeListener(ChangeListener cl);

}
