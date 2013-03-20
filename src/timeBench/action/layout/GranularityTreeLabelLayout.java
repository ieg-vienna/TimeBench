package timeBench.action.layout;

import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.logging.Logger;

import prefuse.Constants;
import prefuse.action.layout.Layout;
import prefuse.data.Schema;
import prefuse.data.query.ObjectRangeModel;
import prefuse.data.tuple.TupleSet;
import prefuse.data.util.Index;
import prefuse.util.MathLib;
import prefuse.util.PrefuseLib;
import prefuse.util.ui.ValuedRangeModel;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;


public class GranularityTreeLabelLayout extends Layout {

    public static final String FRAC = "frac";
    public static final String LABEL = "_label";
    public static final String VALUE = "_value";
    
    private GranularityTreeLayout m_layout; // pointer to matching layout, if any
    
    private int m_axis;
    private boolean m_asc = true;
    
    private double m_spacing; // desired spacing between axis labels
    
    /**
     * Create a new AxisLabelLayout layout.
     * @param group the data group of the axis lines and labels
     * @param axis the axis type, either {@link prefuse.Constants#X_AXIS}
     * or {@link prefuse.Constants#Y_AXIS}.
     */
    public GranularityTreeLabelLayout(String group, int axis)
    {
        this(group, axis, null);
    }
    
    /**
     * Create a new AxisLabelLayout layout.
     * @param group the data group of the axis lines and labels
     * @param axis the axis type, either {@link prefuse.Constants#X_AXIS}
     * or {@link prefuse.Constants#Y_AXIS}.
     * @param values the range model that defines the span of the axis
     * @param bounds the layout bounds within which to place the axis marks
     */
    public GranularityTreeLabelLayout(String group, int axis, Rectangle2D bounds)
    {
        super(group);
        if ( bounds != null )
            setLayoutBounds(bounds);
        m_axis = axis;
        m_spacing = 50;
    }
    
    /**
     * Create a new AxisLabelLayout layout.
     * @param group the data group of the axis lines and labels
     * @param layout an {@link AxisLayout} instance to model this layout after.
     * The axis type and range model of the provided instance will be used.
     */
    public GranularityTreeLabelLayout(String group, GranularityTreeLayout layout) {
        this(group, layout, null, 50);
    }
    
    /**
     * Create a new AxisLabelLayout layout.
     * @param group the data group of the axis lines and labels
     * @param layout an {@link AxisLayout} instance to model this layout after.
     * The axis type and range model of the provided instance will be used.
     * @param bounds the layout bounds within which to place the axis marks
     */
    public GranularityTreeLabelLayout(String group, GranularityTreeLayout layout, Rectangle2D bounds) {
        this(group, layout, bounds, 50);
    }

    /**
     * Create a new AxisLabelLayout layout.
     * @param group the data group of the axis lines and labels
     * @param layout an {@link AxisLayout} instance to model this layout after.
     * The axis type and range model of the provided instance will be used.
     * @param bounds the layout bounds within which to place the axis marks
     * @param spacing the minimum spacing between axis labels
     */
    public GranularityTreeLabelLayout(String group, GranularityTreeLayout layout, Rectangle2D bounds,
            double spacing)
    {
        super(group);
        if ( bounds != null )
            setLayoutBounds(bounds);
        m_layout = layout;
        m_spacing = spacing;
    }
    
    // ------------------------------------------------------------------------
       
    /**
     * Get the required minimum spacing between axis labels.
     * @return the axis label spacing
     */
    public double getSpacing() {
        return m_spacing;
    }

    /**
     * Set the required minimum spacing between axis labels.
     * @param spacing the axis label spacing to use
     */
    public void setSpacing(double spacing) {
        m_spacing = spacing;
    }
    
    /**
     * Indicates if the axis values should be presented in ascending order
     * along the axis.
     * @return true if data values increase as pixel coordinates increase,
     * false if data values decrease as pixel coordinates increase.
     */
    public boolean isAscending() {
        return m_asc;
    }
    
    /**
     * Sets if the axis values should be presented in ascending order
     * along the axis.
     * @param asc true if data values should increase as pixel coordinates
     * increase, false if data values should decrease as pixel coordinates
     * increase.
     */
    public void setAscending(boolean asc) {
        m_asc = asc;
    }
    
    // ------------------------------------------------------------------------
    
    /**
     * @see prefuse.action.GroupAction#run(double)
     */
    public void run(double frac) {      
        VisualTable labels = getTable();

        for(int i=0; i<m_layout.getSettings().length; i++) {
        	GranularityTreeLayoutSettings settings = m_layout.getSettings()[i];
        	if(settings.getTargetAxis() == m_axis) {
        		 //m_layout.getMinIdentifiers()[i];
        	}
        }
        
        Iterator iter = labels.tuples();
        while ( iter.hasNext() ) {
        }
        
        // get rid of any labels that are no longer being used
        garbageCollect(labels);
    }
    
    // ------------------------------------------------------------------------
    // Quantitative Axis Layout
        
    /**
     * Get the "breadth" of a rectangle, based on the axis type.
     */
    protected double getBreadth(Rectangle2D b) {
        switch ( m_axis ) {
        case Constants.X_AXIS:
            return b.getWidth();
        default:
            return b.getHeight();
        }
    }    
    
    /**
     * Set the layout values for an axis label item.
     */
    protected void set(VisualItem item, double xOrY, Rectangle2D b) {
        switch ( m_axis ) {
        case Constants.X_AXIS:
            xOrY = m_asc ? xOrY + b.getMinX() : b.getMaxX() - xOrY;
            PrefuseLib.updateDouble(item, VisualItem.X,  xOrY);
            PrefuseLib.updateDouble(item, VisualItem.Y,  b.getMinY());
            PrefuseLib.updateDouble(item, VisualItem.X2, xOrY);
            PrefuseLib.updateDouble(item, VisualItem.Y2, b.getMaxY());
            break;
        case Constants.Y_AXIS:
            xOrY = m_asc ? b.getMaxY() - xOrY - 1 : xOrY + b.getMinY();
            PrefuseLib.updateDouble(item, VisualItem.X,  b.getMinX());
            PrefuseLib.updateDouble(item, VisualItem.Y,  xOrY);
            PrefuseLib.updateDouble(item, VisualItem.X2, b.getMaxX());
            PrefuseLib.updateDouble(item, VisualItem.Y2, xOrY);
        }
    }
    
    /**
     * Reset an axis label VisualItem
     */
    protected void reset(VisualItem item) {
        item.setVisible(false);
        item.setEndVisible(false);
        item.setStartStrokeColor(item.getStrokeColor());
        item.revertToDefault(VisualItem.STROKECOLOR);
        item.revertToDefault(VisualItem.ENDSTROKECOLOR);
        item.setStartTextColor(item.getTextColor());
        item.revertToDefault(VisualItem.TEXTCOLOR);
        item.revertToDefault(VisualItem.ENDTEXTCOLOR);
        item.setStartFillColor(item.getFillColor());
        item.revertToDefault(VisualItem.FILLCOLOR);
        item.revertToDefault(VisualItem.ENDFILLCOLOR);
    }
    
    /**
     * Remove axis labels no longer being used.
     */
    protected void garbageCollect(VisualTable labels) {
        Iterator iter = labels.tuples();
        while ( iter.hasNext() ) {
            VisualItem item = (VisualItem)iter.next();
            if ( !item.isStartVisible() && !item.isEndVisible() ) {
                labels.removeTuple(item);
            }
        }
    }
    
    /**
     * Create a new table for representing axis labels.
     */
    protected VisualTable getTable() {
        TupleSet ts = m_vis.getGroup(m_group);
        if ( ts == null ) {
            Schema s = PrefuseLib.getAxisLabelSchema();
            VisualTable vt = m_vis.addTable(m_group, s);
            vt.index(VALUE);
            return vt;
        } else if ( ts instanceof VisualTable ) {
            return (VisualTable)ts;
        } else {
            throw new IllegalStateException(
                "Group already exists, not being used for labels");
        }
    }
    
} // end of class AxisLabels
