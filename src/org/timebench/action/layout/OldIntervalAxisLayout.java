package org.timebench.action.layout;

import org.timebench.action.layout.timescale.BasicTimeScale;
import org.timebench.action.layout.timescale.TimeScale;
import org.timebench.data.AnchoredTemporalElement;
import org.timebench.data.TemporalElement;
import org.timebench.data.TemporalObject;

import prefuse.Constants;
import prefuse.data.expression.Predicate;
import prefuse.render.Renderer;
import prefuse.visual.VisualItem;

/**
 * Add's support for a max date data field and a max x data to
 * {@link TimeLayout}. Which means that the date in the {@link VisualItem}s
 * date field will be mapped to a pixel using a given {@link BasicTimeScale} and
 * which in turn will be written into the given max x field.
 * 
 * To use this functionality, you need a {@link Renderer} that incorporates the
 * max x field, like {@link DefaultIntervalRenderer}. Because prefuse does not
 * provide built in support for intervals, you have to explicitly choose a max x
 * field to be set by this {@link OldIntervalAxisLayout} and can then be read by a
 * chosen {@link Renderer}, like {@link DefaultIntervalRenderer} .
 * 
 * @author peterw
 * @see DefaultIntervalRenderer
 */
@Deprecated
public class OldIntervalAxisLayout extends TimeAxisLayout {
	private String maxXField;

	/**
	 * Create a {@link OldIntervalAxisLayout} without a {@link BasicTimeScale}.
	 * 
	 * @param group
	 *            the data group to layout
	 * @param maxXField
	 *            the x field, where the pixel representing the date contained
	 *            in the date field will be set
	 */
	public OldIntervalAxisLayout(String group, String maxXField) {
		this(group, maxXField, null);
	}

	/**
	 * Create a {@link OldIntervalAxisLayout} with the specified interval and max x
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
	public OldIntervalAxisLayout(String group, String maxXField,
			TimeScale timeScale) {
		super(group, timeScale);
		this.maxXField = maxXField;
	}
	
    /**
     * Create a {@link OldIntervalAxisLayout} with the specified interval and max x
	 * field.
     * 
     * @param group
     *            the data group to layout
   	 * @param maxXField
	 *            the x field, where the pixel representing the date contained
	 *            in the date field will be set
     * @param axis
     *            the axis type, either {@link prefuse.Constants#X_AXIS} or
     *            {@link prefuse.Constants#Y_AXIS}.
     * @param timeScale
     *            the {@link TimeScale} used to layout items
     * @param filter
     *            an optional predicate filter for limiting which items to
     *            layout.
     */
    public OldIntervalAxisLayout(String group, String maxXField, int axis, TimeScale timeScale, Placement placement,
            Predicate filter) {
        super(group,axis,timeScale,placement,filter);
		this.maxXField = maxXField;
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.ac.tuwien.cs.timevis.ui.TimeLayout#layoutItem(prefuse.visual.VisualItem)
	 */
    protected void layoutItem(VisualItem vi) {
        TemporalElement te = ((TemporalObject) vi.getSourceTuple())
                .getTemporalElement();
        
        if (te.isAnchored()) {
            AnchoredTemporalElement ate = (AnchoredTemporalElement) te
                    .asPrimitive();

            int pixelInf = timeScale.getPixelForDate(ate.getInf());
            int pixelSup = timeScale.getPixelForDate(ate.getSup());

            if (super.getAxis() == Constants.X_AXIS) {
                vi.setX(pixelInf);
                vi.setInt(maxXField, pixelSup);
            } else {
                // TODO test y axis layout direction
                vi.setY(pixelInf);
                vi.setInt(maxXField, pixelSup);
            }
        }
    }

	public String getMaxXField() {
		return maxXField;
	}

	public void setMaxXField(String maxXField) {
		this.maxXField = maxXField;
	}
}
