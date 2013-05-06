package timeBench.action.layout.timescale;

import java.util.Calendar;
import java.util.Date;

/**
 * Enhances {@link BasicTimeScale} by including an end date and display display
 * width. This enables the {@link AdvancedTimeScale} to automatically determine
 * the number milliseconds to be displayed per pixel, based on the given
 * interval and display width.
 * 
 * @author peterw
 * 
 */
public class AdvancedTimeScale extends BasicTimeScale {
	protected int displayWidth;
	protected boolean adjustDateRangeOnResize = true;
	protected long endDate;

	/**
	 * Create a {@link AdvancedTimeScale} with the current date as the start
	 * date and a length of 31 days and a default {@link TimeUnitProvider}.
	 * 
	 */
	public AdvancedTimeScale() {
		this(TimeUnitProvider.createDefaultTimeUnitProvider());
	}

	/**
	 * Create a {@link AdvancedTimeScale} with the current date as the start
	 * date and a length of 31 days.
	 * 
	 * @param timeUnitProvider
	 *            a {@link TimeUnitProvider} which will be use to determine the
	 *            best fitting {@link TimeUnit}
	 */
	public AdvancedTimeScale(TimeUnitProvider timeUnitProvider) {
		this(new Date().getTime(), new Date().getTime() + GregorianTimeUnit.getMaxLength(Calendar.DAY_OF_MONTH, 1), 1);
	}

	/**
	 * Create a {@link AdvancedTimeScale} with the interval, the
	 * display width and {@link TimeUnitProvider} of the given {@link AdvancedTimeScale}.
	 * 
	 * @param scale
	 *            the base {@link AdvancedTimeScale}
	 */
	public AdvancedTimeScale(AdvancedTimeScale scale) {
		this(scale.getStartDate(), scale.getEndDate(), scale.getDisplayWidth(), scale.getTimeUnitProvider());
	}

	/**
	 * Create a {@link AdvancedTimeScale} with a default
	 * {@link TimeUnitProvider}.
	 * 
	 * @param startDate
	 *            a start date in milliseconds. The first pixel of this
	 *            {@link AdvancedTimeScale} will match to this date
	 * @param endDate
	 *            a end date in millseconds. The last pixel of this
	 *            {@link AdvancedTimeScale} will match to this date (if
	 *            adjusted)
	 * @param displayWidth
	 *            the number of pixels used to display the given interval
	 */
	public AdvancedTimeScale(long startDate, long endDate, int displayWidth) {
		this(startDate, endDate, displayWidth, TimeUnitProvider.createDefaultTimeUnitProvider());
	}

	/**
	 * Create a {@link AdvancedTimeScale} with an interval, the number of pixel
	 * used to display this interval and a {@link TimeUnitProvider}.
	 * 
	 * @param startDate
	 *            a start date in milliseconds. The first pixel of this
	 *            {@link AdvancedTimeScale} will match to this date
	 * @param endDate
	 *            a end date in millseconds. The last pixel of this
	 *            {@link AdvancedTimeScale} will match to this date (if
	 *            adjusted)
	 * @param displayWidth
	 *            the number of pixels used to display the given interval
	 * @param timeUnitProvider
	 *            a {@link TimeUnitProvider} which will be use to determine the
	 *            best fitting {@link TimeUnit}
	 */
	public AdvancedTimeScale(long startDate, long endDate, int displayWidth, TimeUnitProvider timeUnitProvider) {
		super(timeUnitProvider);
		this.displayWidth = displayWidth;

		changeInterval(startDate, endDate);
	}

	/**
	 * Returns the end date of the interval to be displayed.
	 * 
	 * @return the end date in milliseconds
	 */
	public long getEndDate() {
		return endDate;
	}

	/**
	 * Returns the adjusted end date of the interval to be displayed.
	 * 
	 * @return the adjusted end date
	 * @see BasicTimeScale#adjustDateToUnit(int, long)
	 */
	public long getEndDateAdjusted() {
		return adjustDateToUnit(getDisplayWidth(), getEndDate());
	}

	/**
	 * Set the end date of the interval to be displayed.
	 * 
	 * @param endDate
	 */
	public void setEndDate(long endDate) {
		this.endDate = endDate;
		adjustTimeScale();
	}

	/**
	 * Get the duration of the current interval.
	 * 
	 * @return the duration of the interval
	 */
	public long getDuration() {
		return endDate - startDate;
	}

	/**
	 * Change the interval by setting a new start and end date.
	 * 
	 * @param startDate
	 *            the new start date
	 * @param endDate
	 *            the new end date
	 */
	public void changeInterval(long startDate, long endDate) {
		if (startDate == endDate) {
			log.debug("ignoring 0 scaling");
			return;
		}
		this.startDate = startDate;
		this.endDate = endDate;

		adjustTimeScale();
	}

	/**
	 * Returns the number of pixels used to display the given interval.
	 * 
	 * @return the number of pixels
	 */
	public int getDisplayWidth() {
		return displayWidth;
	}

	/**
	 * Set the number of pixels used to display the given interval.
	 * 
	 * @param widthInPixel
	 *            the number of pixels
	 */
	public void setDisplayWidth(int widthInPixel) {
		this.displayWidth = widthInPixel;
		if (isAdjustDateRangeOnResize()) {
			adjustTimeScale();
		} else {
			fireStateChanged();
		}
	}

	/**
	 * Overrides the pan() method, corrected by Stepan Hoffmann, 
	 */
	public void pan(int pixels) {

		endDate = (long) (endDate - getMillisPerPixel() * pixels);
		super.pan(pixels);
	}

	/**
	 * Center zooming.
	 * 
	 * @see at.ac.tuwien.cs.timevis.BasicTimeScale#zoom(double)
	 */
	public void zoom(double factor) {
		long oldDur = getDuration();
		long newDur = (long) (oldDur * factor);
		long diff = (newDur - oldDur) / 2;

		startDate = startDate - diff;
		endDate = endDate + diff;

		adjustTimeScale();
	}

	/**
	 * Calculates the number of milliseconds represented by one pixel and
	 * adjusts the end date and the current {@link TimeUnit}.
	 */
	protected void adjustTimeScale() {
		millisPerPixel = getDuration() / displayWidth;
		if (millisPerPixel == 0) {
			log.debug("Duration too short. Adjusting values.");
			millisPerPixel = 1;
			endDate = startDate + getDisplayWidth();
		}

		timeUnit = timeUnitProvider.getBest(millisPerPixel, minimumPixelPerUnit);

		if (timeUnit.getMaxLengthInMillis() < minimumPixelPerUnit * millisPerPixel) {
			log.debug("Duration too long. Adjusting values.");
			endDate = startDate + (timeUnit.getMaxLengthInMillis() / minimumPixelPerUnit) * displayWidth;
		}
		fireStateChanged();
	}

	/**
	 * Indicates if this {@link AdvancedTimeScale} is adjusted when the display
	 * width changes.
	 * 
	 * @return true if this {@link AdvancedTimeScale} is adjusted when the
	 *         display width changes
	 * @see AdvancedTimeScale#setDisplayWidth(int)
	 */
	public boolean isAdjustDateRangeOnResize() {
		return adjustDateRangeOnResize;
	}

	/**
	 * Determines if this {@link AdvancedTimeScale} is adjusted when the display
	 * width changes.
	 * 
	 * @param adjustOnResize
	 *            true to automatically adjust this {@link AdvancedTimeScale}
	 *            when the display width changes
	 */
	public void setAdjustDateRangeOnResize(boolean adjustOnResize) {
		this.adjustDateRangeOnResize = adjustOnResize;
	}
}
