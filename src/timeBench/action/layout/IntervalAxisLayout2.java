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
 * field to be set by this {@link IntervalAxisLayout2} and can then be read by a
 * chosen {@link Renderer}, like {@link DefaultIntervalRenderer} .
 * 
 * @author peterw
 * @see DefaultIntervalRenderer
 */
public class IntervalAxisLayout2 extends TimeAxisLayout {
	int[] pathToInterval = null;

	/**
	 * Create a {@link IntervalAxisLayout2} without a {@link BasicTimeScale}.
	 * 
	 * @param group
	 *            the data group to layout
	 * @param maxXField
	 *            the x field, where the pixel representing the date contained
	 *            in the date field will be set
	 */
	public IntervalAxisLayout2(String group) {
		this(group, null);
	}

	/**
	 * Create a {@link IntervalAxisLayout2} with the specified interval and max x
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
	public IntervalAxisLayout2(String group,TimeScale timeScale) {
		this(group,timeScale,null);
	}
	
	/**
	 * Create a {@link IntervalAxisLayout2} with the specified interval and max x
	 * field.
	 * 
	 * @param group
	 *            the data group to layout
	 * @param maxXField
	 *            the x field, where the pixel representing the date contained
	 *            in the date field will be set
	 * @param timeScale
	 *            the {@link BasicTimeScale} to use for pixel-date mapping
	 * @param pathToInterval
	 *            null for primitive intervals or the position in the set where the interval is located
	 */
	public IntervalAxisLayout2(String group,TimeScale timeScale,int[] pathToInterval) {
		super(group, timeScale);
		this.pathToInterval = pathToInterval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.ac.tuwien.cs.timevis.ui.TimeLayout#layoutItem(prefuse.visual.VisualItem)
	 */
	protected void layoutItem(VisualItem vi) {		
        GenericTemporalElement te = ((TemporalObject) vi.getSourceTuple())
                .getTemporalElement();
        
        if(pathToInterval != null) {
        	for(int i=0; i<pathToInterval.length; i++)
        		te = (GenericTemporalElement)te.getChild(pathToInterval[i]);
        }
        
        int pixelInf = timeScale.getPixelForDate(te.getInf());
        int pixelSup = timeScale.getPixelForDate(te.getSup());
        double pixelWidth = (double)pixelSup-(double)pixelInf+1.0;
        double pixelMed = (double)pixelInf+pixelWidth/2.0;

        if (super.getAxis() == Constants.X_AXIS) {
            vi.setX(pixelMed);
            vi.setSizeX(pixelWidth);
        } else {
        	// TODO test y axis layout direction 
            vi.setY(pixelMed);
            vi.setSizeY(pixelWidth);
        }
	}
}
