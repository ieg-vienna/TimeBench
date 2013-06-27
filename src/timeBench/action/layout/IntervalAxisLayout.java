package timeBench.action.layout;

import ieg.prefuse.renderer.IntervalBarRenderer;
import prefuse.Constants;
import prefuse.data.expression.Predicate;
import prefuse.render.Renderer;
import prefuse.visual.VisualItem;
import timeBench.action.layout.TimeAxisLayout.Placement;
import timeBench.action.layout.timescale.BasicTimeScale;
import timeBench.action.layout.timescale.TimeScale;
import timeBench.calendar.JavaDateCalendarManager;
import timeBench.data.AnchoredTemporalElement;
import timeBench.data.Span;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalElement;
import timeBench.data.TemporalObject;
import timeBench.data.UnanchoredTemporalElement;

/**
 * Layout {@link prefuse.action.Action} that assigns position and length
 * along the x or y axis according to the {@link TemporalElement} linked to a
 * {@link VisualItem} created from a {@link TemporalDataset}. The position is
 * saved to the field {@link VisualItem#X} or {@link VisualItem#Y}. Depending on
 * the placement the position is either on INF, SUP, or middle respectively the
 * left, right, or center of the display space taken by the {@link VisualItem}.
 * The duration/length is saved to the field {@link VisualItem#SIZE} or
 * {@link VisualItem#SIZEY}.
 * 
 * <p>The default placement is {@link Placement#MIDDLE}, the center. The default
 * axis is {@link Constants#X_AXIS}.
 * 
 * <p>To use this functionality, you need a {@link Renderer} that correctly
 * interprets the {@link VisualItem#SIZE} or {@link VisualItem#SIZEY} field,
 * like {@link ShapeRenderer} for {@link Placement#MIDDLE} and
 * {@link IntervalBarRenderer} for {@link Placement#INF}.
 * 
 * <p>The item's of the given group are layed out using
 * {@link TimeScale#getPixelForDate(long)}.
 * 
 * <p>
 * Added: 2012-06-13 / AR (based on work by Peter Weishapl)<br>
 * Modifications: 2013-03-28 / TL / saving width in VisualItem.SIZE instead of custom maxXField<br>
 * 2013-06-12 / AR / Placement of anchor (X or Y) on INF, SUP, or middle
 * </p>
 * 
 * @author Rind, Lammarsch, peterw
 * @see DefaultIntervalRenderer
 */
public class IntervalAxisLayout extends TimeAxisLayout {

	/**
	 * Create a {@link IntervalAxisLayout} without a {@link BasicTimeScale}.
	 * 
	 * @param group
	 *            the data group to layout
	 */
	public IntervalAxisLayout(String group) {
		this(group, null);
	}

	/**
	 * Create a {@link IntervalAxisLayout} with the specified interval and max x
	 * field.
	 * 
	 * @param group
	 *            the data group to layout
	 * @param timeScale
	 *            the {@link BasicTimeScale} to use for pixel-date mapping
	 */
	public IntervalAxisLayout(String group, TimeScale timeScale) {
		super(group, timeScale);
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
     * @param pathToInterval
     *            null for primitive intervals or the position in the set where the interval is located
     */
    public IntervalAxisLayout(String group, TimeScale timeScale, int[] pathToInterval) {
        super(group, timeScale);
        super.setChildIndicesOnPathFromRoot(pathToInterval);
    }
    
    /**
     * Create a {@link IntervalAxisLayout} with the specified interval and max x
	 * field.
     * 
     * @param group
     *            the data group to layout
     * @param axis
     *            the axis type, either {@link prefuse.Constants#X_AXIS} or
     *            {@link prefuse.Constants#Y_AXIS}.
     * @param timeScale
     *            the {@link TimeScale} used to layout items
     * @param filter
     *            an optional predicate filter for limiting which items to
     *            layout.
     */
    public IntervalAxisLayout(String group, int axis, TimeScale timeScale,
            Placement placement, Predicate filter) {
        super(group,axis,timeScale,placement,filter);
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.ac.tuwien.cs.timevis.ui.TimeLayout#layoutItem(prefuse.visual.VisualItem)
	 */
    protected void layoutItem(VisualItem vi) {
        TemporalElement te = (TemporalElement) vi
                .get(TemporalObject.TEMPORAL_ELEMENT);
        TemporalElement te2 = super.getChildOnPath(te);

        if (te2.isAnchored()) {
        	try {
        		AnchoredTemporalElement ate = (AnchoredTemporalElement) te2
                    .asPrimitive();

            	int pixelInf = timeScale.getPixelForDate(te2.getFirstInstant().getInf());
            	int pixelSup = timeScale.getPixelForDate(te2.getLastInstant().getSup());
            	double pixelWidth = pixelSup - pixelInf + 1.0;

            	double coord = (placement == Placement.MIDDLE) 
                    ? pixelInf + pixelWidth / 2.0
                    : (placement == Placement.INF) ? pixelInf : pixelSup;
            
            	if (super.getAxis() == Constants.X_AXIS) {
                	vi.setX(coord);
                	vi.setSizeX(pixelWidth);
            	} else {
                	// TODO test y axis layout direction
                	vi.setY(coord);
                	vi.setSizeY(pixelWidth);
            	}
			} catch (TemporalDataException e) {
				throw new RuntimeException(e.getMessage());
			}
        } else {
        	try {
				long inf = te.getFirstInstant().getInf();
	        	long sup = te.getLastInstant().getSup();
	        	long duration = te2.getLength() *
	        			(te.getFirstInstant().getGranule().getSup()-te.getFirstInstant().getGranule().getInf()+1L);
	        	long before = ((sup-inf+1L)-duration)/2;
	        	int pixelInf = timeScale.getPixelForDate(inf+before);
	        	int pixelSup = timeScale.getPixelForDate(inf+before+duration-1L);
	        	System.out.println(te.getId()+": "+te2.getLength());
	        	
	            double pixelWidth = pixelSup - pixelInf + 1.0;

	            double coord = (placement == Placement.MIDDLE) 
	                    ? pixelInf + pixelWidth / 2.0
	                    : (placement == Placement.INF) ? pixelInf : pixelSup;
	            
	            if (super.getAxis() == Constants.X_AXIS) {
	                vi.setX(coord);
	                vi.setSizeX(pixelWidth);
	            } else {
	                // TODO test y axis layout direction
	                vi.setY(coord);
	                vi.setSizeY(pixelWidth);
	            }
			} catch (TemporalDataException e) {
				throw new RuntimeException(e.getMessage());
			}
        }
    }
}
