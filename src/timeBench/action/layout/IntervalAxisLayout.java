package timeBench.action.layout;

import prefuse.Constants;
import prefuse.render.Renderer;
import prefuse.visual.VisualItem;
import timeBench.action.layout.timescale.BasicTimeScale;
import timeBench.action.layout.timescale.TimeScale;
import timeBench.data.GenericTemporalElement;
import timeBench.data.TemporalObject;

/**
 * Add's support for a max date data field and a max x data to
 * {@link TimeLayout}. Which means that the date in the {@link VisualItem}s
 * date field will be mapped to a pixel using a given {@link BasicTimeScale} and
 * which in turn will be written into the given max x field.
 * 
 * To use this functionality, you need a {@link Renderer} that incorporates the
 * max x field, like {@link DefaultIntervalRenderer}. Because prefuse does not
 * provide built in support for intervals, you have to explicitly choose a max x
 * field to be set by this {@link IntervalAxisLayout} and can then be read by a
 * chosen {@link Renderer}, like {@link DefaultIntervalRenderer} .
 * 
 * @author peterw
 * @see DefaultIntervalRenderer
 */
public class IntervalAxisLayout extends TimeAxisLayout {
	private String maxXField;

	/**
	 * Create a {@link IntervalAxisLayout} without a {@link BasicTimeScale}.
	 * 
	 * @param group
	 *            the data group to layout
	 * @param maxXField
	 *            the x field, where the pixel representing the date contained
	 *            in the date field will be set
	 */
	public IntervalAxisLayout(String group, String maxXField) {
		this(group, maxXField, null);
	}

	/**
	 * Create a {@link IntervalAxisLayout} with the specified interval and max x
	 * field.
	 * 
	 * @param group
	 *            the data group to layout
	 * @param maxXField
	 *            the x field, where the pixel representing the date contained
	 *            in the date field will be set
	 * @param timeScale
	 *            the {@link BasicTimeScale} to use for pixel-date mapping
	 */
	public IntervalAxisLayout(String group, String maxXField,
			TimeScale timeScale) {
		super(group, timeScale);
		this.maxXField = maxXField;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.ac.tuwien.cs.timevis.ui.TimeLayout#layoutItem(prefuse.visual.VisualItem)
	 */
	protected void layoutItem(VisualItem vi) {
        GenericTemporalElement te = ((TemporalObject) vi.getSourceTuple())
                .getTemporalElement();
        int pixelInf = timeScale.getPixelForDate(te.getInf());
        int pixelSup = timeScale.getPixelForDate(te.getSup());

        if (super.getAxis() == Constants.X_AXIS) {
            vi.setX(pixelInf);
            vi.setInt(maxXField, pixelSup);
        } else {
        	// TODO test y axis layout direction 
            vi.setY(pixelInf);
            vi.setInt(maxXField, pixelSup);
        }
	}

	public String getMaxXField() {
		return maxXField;
	}

	public void setMaxXField(String maxXField) {
		this.maxXField = maxXField;
	}
}
