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
    
    private double m_indent; // desired indentation on the "other" axis; ignored when bounds set
    
    public GranularityTreeLabelLayout(String group, GranularityTreeLayout layout) {
        this(group, layout, null, 0);
    }
    
    public GranularityTreeLabelLayout(String group, GranularityTreeLayout layout, Rectangle2D bounds) {
        this(group, layout, bounds, 0);
    }

    public GranularityTreeLabelLayout(String group, GranularityTreeLayout layout, Rectangle2D bounds,
            double indent)
    {
        super(group);
        if ( bounds != null )
            setLayoutBounds(bounds);
        m_layout = layout;
        m_indent = indent;
    }
    
    // ------------------------------------------------------------------------
       
    /**
     * Get the required minimum spacing between axis labels.
     * @return the axis label spacing
     */
    public double getIndent() {
        return m_indent;
    }

    /**
     * Set the required minimum spacing between axis labels.
     * @param spacing the axis label spacing to use
     */
    public void setIndent(double indent) {
    	m_indent = indent;
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
