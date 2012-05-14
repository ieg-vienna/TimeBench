package timeBench.action.layout;

import java.util.Iterator;

import org.apache.log4j.Logger;

import prefuse.Constants;
import prefuse.action.layout.Layout;
import prefuse.data.expression.Predicate;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.VisualItem;
import timeBench.action.layout.timescale.TimeScale;
import timeBench.data.TemporalObject;

/**
 * Layout {@link prefuse.action.Action} that assigns positions along the x or y
 * Axis according to the {@link TemporalElement} linked to a {@link VisualItem}
 * created from a {@link TemporalDataset}.
 * 
 * The item's of the given group are layed out using the given {@link TimeScale}
 * s {@link TimeScale#getPixelForDate(long)}.
 * 
 * <p>
 * Added: 2012-05-14 / AR<br>
 * Modifications: 2012-XX-XX / YY / zzz
 * </p>
 * 
 * @author Rind, peterw
 * 
 */
public class TimeAxisLayout extends Layout {

    protected final Logger log = Logger.getLogger(getClass());

    protected TimeScale timeScale;

    private int m_axis = Constants.X_AXIS;

    protected Predicate m_filter = null;

    /**
     * Create a new TimeAxisLayout. Defaults to using the x-axis.
     * 
     * @param group
     *            the data group to layout
     */
    public TimeAxisLayout(String group) {
        super(group);
    }

    /**
     * Create a new TimeAxisLayout.
     * 
     * @param group
     *            the data group to layout
     * @param axis
     *            the axis type, either {@link prefuse.Constants#X_AXIS} or
     *            {@link prefuse.Constants#Y_AXIS}.
     */
    public TimeAxisLayout(String group, int axis) {
        this(group);
        setAxis(axis);
    }

    /**
     * Create a new TimeAxisLayout.
     * 
     * @param group
     *            the data group to layout
     * @param axis
     *            the axis type, either {@link prefuse.Constants#X_AXIS} or
     *            {@link prefuse.Constants#Y_AXIS}.
     * @param filter
     *            an optional predicate filter for limiting which items to
     *            layout.
     */
    public TimeAxisLayout(String group, int axis, Predicate filter) {
        this(group, axis);
        setFilter(filter);
    }

    // ------------------------------------------------------------------------

    @SuppressWarnings({ "rawtypes" })
    @Override
    public void run(double frac) {
        if (timeScale == null) {
            log.debug("cannot layout without timescale");
            return;
        }

        TupleSet items = m_vis.getGroup(m_group);
        if (items == null) {
            log.debug("nothing to layout");
            return;
        }

        // setMinMax(); TODO get layout bounds
        // get Inf / Sup of TemporalDataset (only temporal objects)

        Iterator tuples = items.tuples(m_filter);
        while (tuples.hasNext()) {
            VisualItem item = (VisualItem) tuples.next();
            // double v = item.getDouble(m_field);
            // double f = prefuse.util.MathLib.linearInterp(v, min, max);
            // set(item, f);

            layoutItem(item);
        }
    }

    /**
     * Layout a single item. Override to customize the layout routine.
     * 
     * @param vi
     *            the item to layout
     */
    protected void layoutItem(VisualItem vi) {
        TemporalObject to = (TemporalObject) vi.getSourceTuple();
        // TODO check inf/sup
        long time = to.getTemporalElement().getInf();

        if (m_axis == Constants.X_AXIS) {
            vi.setX(timeScale.getPixelForDate(time));
        } else {
            vi.setY(timeScale.getPixelForDate(time));
        }
    }

    // ------------------------------------------------------------------------

    public TimeScale getTimeScale() {
        return timeScale;
    }

    public void setTimeScale(TimeScale timeScale) {
        this.timeScale = timeScale;
    }

    /**
     * Return the axis type of this layout, either
     * {@link prefuse.Constants#X_AXIS} or {@link prefuse.Constants#Y_AXIS}.
     * 
     * @return the axis type of this layout.
     */
    public int getAxis() {
        return m_axis;
    }

    /**
     * Set the axis type of this layout.
     * 
     * @param axis
     *            the axis type to use for this layout, either
     *            {@link prefuse.Constants#X_AXIS} or
     *            {@link prefuse.Constants#Y_AXIS}.
     */
    public void setAxis(int axis) {
        if (axis < 0 || axis >= Constants.AXIS_COUNT)
            throw new IllegalArgumentException("Unrecognized axis value: "
                    + axis);
        m_axis = axis;
    }

    /**
     * Get the predicate filter to limit which items are considered for layout.
     * Only items for which the predicate returns a true value are included in
     * the layout computation.
     * 
     * @return the predicate filter used by this layout. If null, no filtering
     *         is performed.
     */
    public Predicate getFilter() {
        return m_filter;
    }

    /**
     * Set a predicate filter to limit which items are considered for layout.
     * Only items for which the predicate returns a true value are included in
     * the layout computation.
     * 
     * @param filter
     *            the predicate filter to use. If null, no filtering will be
     *            performed.
     */
    public void setFilter(Predicate filter) {
        m_filter = filter;
    }
}
