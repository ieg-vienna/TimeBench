package timeBench.action.layout.timescale;

/**
 * Enhances {@link AdvancedTimeScale} by adding fisheye distortion support.
 * 
 * @author peterw
 * 
 */
public class FisheyeTimeScale extends AdvancedTimeScale {
	private double fisheyeIntensity;

	/**
	 * Creates a default {@link FisheyeTimeScale} with the data of a newly
	 * created {@link AdvancedTimeScale} and a fisheye distortion intensity of
	 * 0.
	 */
	public FisheyeTimeScale() {
		this(new AdvancedTimeScale());
	}

	/**
	 * Create a {@link FisheyeTimeScale} based with the interval and the display
	 * width of the given {@link AdvancedTimeScale} and a fisheye distortion
	 * intensity of 0.
	 * 
	 * @param scale
	 *            the base {@link AdvancedTimeScale}
	 * @see AdvancedTimeScale#AdvancedTimeScale(AdvancedTimeScale)
	 */
	public FisheyeTimeScale(AdvancedTimeScale scale) {
		this(scale, 0);
	}

	/**
	 * Create a {@link FisheyeTimeScale} with the interval, the display width
	 * and {@link TimeUnitProvider} of the given {@link AdvancedTimeScale} and
	 * the given fisheye distortion intensity.
	 * 
	 * @param scale
	 *            the base {@link AdvancedTimeScale}
	 * @param fisheyeIntensity
	 *            the intensity of the fisheye distortion
	 * @see AdvancedTimeScale#AdvancedTimeScale(AdvancedTimeScale)
	 */
	public FisheyeTimeScale(AdvancedTimeScale scale, double fisheyeIntensity) {
		super(scale);
		this.fisheyeIntensity = fisheyeIntensity;
	}

	/**
	 * Create a {@link FisheyeTimeScale} with a default {@link TimeUnitProvider}
	 * and a fisheye distortion intensity of 0.
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
	public FisheyeTimeScale(long startDate, long endDate, int widthInPixel) {
		super(startDate, endDate, widthInPixel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.ac.tuwien.cs.timevis.BasicTimeScale#getRawDate(int)
	 */
	protected long getRawDate(int x) {
		if (isFisheyeEnabled()) {
			x = (int) inverseFisheye(x, getDisplayWidth() / 2, 0, getDisplayWidth());
		}
		return super.getRawDate(x);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.ac.tuwien.cs.timevis.BasicTimeScale#getPixelForDate(long)
	 */
	public int getPixelForDate(long date) {
		int x = super.getPixelForDate(date);
		if (isFisheyeEnabled()) {
			x = (int) fisheye(x, getDisplayWidth() / 2, 0, getDisplayWidth());
		}
		return x;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.ac.tuwien.cs.timevis.BasicTimeScale#adjustDateToUnit(int, long)
	 */
	public long adjustDateToUnit(int x, long rawDate) {
		if (isFisheyeEnabled()) {
			long prevUnit = getTimeUnit().previous(rawDate);
			if (prevUnit == rawDate) {
				return rawDate;
			}
			long nextUnit = getTimeUnit().next(rawDate);
			int prevX = (int) getPixelForDate(prevUnit);
			int nextX = (int) getPixelForDate(nextUnit);

			int millisPerPixel = (int) ((nextUnit - prevUnit) / (nextX - prevX));
			rawDate = nextUnit - (nextX - x) * millisPerPixel;
			return rawDate;
		} else {
			return super.adjustDateToUnit(x, rawDate);
		}
	}

	/**
	 * <p>
	 * Indicates if fisheye distortion is enabled.
	 * </p>
	 * <p>
	 * Fisheye distortion is enabled if the fisheye distortion intensity if
	 * greater 0.
	 * </p>
	 * 
	 * @return true if fisheye distortion is enabled
	 */
	public boolean isFisheyeEnabled() {
		return fisheyeIntensity > 0;
	}

	/**
	 * Sets the fisheye distortion to 2 if fisheye is true or 0 if false.
	 * 
	 * @param fisheye
	 *            true to enable fisheye distortion, false to disable
	 */
	public void setFisheyeEnabled(boolean fisheye) {
		if (fisheye) {
			setFisheyeIntensity(2);
		} else {
			setFisheyeIntensity(0);
		}
	}

	/**
	 * Returns the current fisheye distortion intensity. 0 means fisheye
	 * distortion is disabled.
	 * 
	 * @return the fisheye distortion intensity
	 */
	public double getFisheyeIntensity() {
		return fisheyeIntensity;
	}

	/**
	 * Set the current fisheye distortion intensity. 0 means fisheye distortion
	 * is disabled.
	 * 
	 * @param fishFactor
	 *            the fisheye distortion intensity
	 */
	public void setFisheyeIntensity(double fishFactor) {
		this.fisheyeIntensity = fishFactor;
		fireStateChanged();
	}

	// fisheye distortion implementation following
	private double fisheye(double x, double a, double min, double max) {
		return fisheye(x, a, fisheyeIntensity, min, max, false);
	}

	private double inverseFisheye(double x, double a, double min, double max) {
		return fisheye(x, a, fisheyeIntensity, min, max, true);
	}

	private static double fisheye(double x, double a, double d, double min, double max, boolean inverse) {
		if (d != 0) {
			boolean left = x < a;
			double v, m = (left ? a - min : max - a);
			if (m == 0)
				m = max - min;
			v = Math.abs(x - a) / m;

			if (inverse) {
				v = v / (d * (1 - v) + 1);
			} else {
				v = (d + 1) / (d + (1 / v));
			}

			return (left ? -1 : 1) * m * v + a;
		} else {
			return x;
		}
	}
}
