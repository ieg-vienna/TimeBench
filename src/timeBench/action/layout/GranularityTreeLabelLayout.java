package timeBench.action.layout;

import ieg.prefuse.data.LinkedTree;
import ieg.prefuse.data.LinkedTree.LinkedNode;

import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.logging.Logger;

import prefuse.Constants;
import prefuse.action.layout.Layout;
import prefuse.data.Schema;
import prefuse.data.Tuple;
import prefuse.data.query.ObjectRangeModel;
import prefuse.data.tuple.TupleSet;
import prefuse.data.util.Index;
import prefuse.util.MathLib;
import prefuse.util.PrefuseLib;
import prefuse.util.ui.ValuedRangeModel;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;
import timeBench.calendar.Granule;
import timeBench.data.TemporalDataException;


public class GranularityTreeLabelLayout extends Layout {

    public static final String FRAC = "frac";
    public static final String LABEL = "_label";
    public static final String VALUE = "_value";
    
    private GranularityTreeLayout m_layout; // pointer to matching layout, if any
    
    private int m_axis;
    
    public GranularityTreeLabelLayout(String group, GranularityTreeLayout layout, int axis, Rectangle2D bounds)
    {
        super(group);
        m_layout = layout;
        m_axis = axis;
        setLayoutBounds(bounds);
    }
            
    // ------------------------------------------------------------------------
    
    /**
     * @see prefuse.action.GroupAction#run(double)
     */
    public void run(double frac) {
        VisualTable labels = getTable();
                
        LinkedTree labelSource =  m_layout.getLabels()[m_axis];
        int depth = 0;
        int leaves = 0;
        Iterator iLeaf = labelSource.leaves();
        while (iLeaf.hasNext()) {
        	depth = Math.max(depth, ((Tuple)iLeaf.next()).getInt(LinkedTree.FIELD_DEPTH));
        	leaves++;
        }
        double indent = 0;
        String indentAxis;
        String distributeAxis;
        double indentedOrigin = 0;
        if (m_axis == Constants.X_AXIS) {
        	distributeAxis = VisualItem.X;
        	indentAxis = VisualItem.Y;
        	indent = m_bounds.getHeight() / (double)depth;
        	indentedOrigin = m_bounds.getY();
        } else {
        	distributeAxis = VisualItem.Y;
        	indentAxis = VisualItem.X;
        	indent = m_bounds.getWidth() / (double)depth;
        	indentedOrigin = m_bounds.getX();
        }        	
        
    	while(labels.getRowCount() < labelSource.getRowCount()) {
    		int newRow = labels.addRow();
    		labels.setVisible(newRow,false);
    	}

    	for(int i=0; i<labelSource.getRowCount(); i++) {
			try {
				Granule g = ((Granule)labelSource.get(i,GranularityTreeLayout.FIELD_GRANULE));
				if (g != null)
					labels.setString(i,VisualItem.LABEL,g.getLabel());
				PrefuseLib.updateBoolean((VisualItem)labels.getTuple(i), VisualItem.VISIBLE, g != null);        	
			} catch (TemporalDataException e) {
				labels.setString(i,VisualItem.LABEL,e.getMessage());
			}
			double indentedPosition = indentedOrigin + indent * (labelSource.getInt(i, LinkedTree.FIELD_DEPTH)-1) + indent/2;
			PrefuseLib.updateDouble((VisualItem)labels.getTuple(i), indentAxis, indentedPosition);
			PrefuseLib.updateDouble((VisualItem)labels.getTuple(i), distributeAxis, labelSource.getDouble(i, GranularityTreeLayout.FIELD_POSITION));
    	}    	
        
        for(int i=labelSource.getRowCount(); i < labels.getRowCount(); i++ ) {
        	PrefuseLib.updateBoolean((VisualItem)labels.getTuple(i), VisualItem.VISIBLE, false);
        }
        
        // get rid of any labels that are no longer being used
        garbageCollect(labels);
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
    
}