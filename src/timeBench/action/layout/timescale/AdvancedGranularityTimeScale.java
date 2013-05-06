package timeBench.action.layout.timescale;

public class AdvancedGranularityTimeScale extends AdvancedTimeScale {
	public AdvancedGranularityTimeScale(long l, long m, int i) {
		super(l,m,i);
	}

	public AdvancedGranularityTimeScale(AdvancedGranularityTimeScale timeScale) {
		super(timeScale);
	}

	/**
	 * Calculates the number of milliseconds represented by one pixel and
	 * adjusts the end date and the current {@link TimeUnit}.
	 */
	@Override
	protected void adjustTimeScale() {
		millisPerPixel = getDuration() / displayWidth;
		if (millisPerPixel == 0) {
			log.debug("Duration too short. Adjusting values.");
			millisPerPixel = 1;
			endDate = startDate + getDisplayWidth();
		}

//		if (millisPerPixel < 75877594) {
//			millisPerPixel = 75877594;
//		}

		timeUnit = timeUnitProvider.getBest(millisPerPixel, minimumPixelPerUnit);
		//8370000000

//		if (timeUnit.getMaxLengthInMillis() < minimumPixelPerUnit * millisPerPixel) {
//			log.debug("Duration too long. Adjusting values.");
//			endDate = startDate + (timeUnit.getMaxLengthInMillis() / minimumPixelPerUnit) * displayWidth;
//		}
		fireStateChanged();
	}

}
