package org.timebench.controls;

import java.awt.event.MouseEvent;
import java.util.Iterator;

import prefuse.controls.ControlAdapter;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;


public class BranchHighlightControl extends ControlAdapter {

    private String activity = null;
    private boolean highlightWithInvisibleEdge = false;
    
    /**
     * Creates a new highlight control.
     */
    public BranchHighlightControl() {
        this(null);
    }
    
    /**
     * Creates a new highlight control that runs the given activity
     * whenever the neighbor highlight changes.
     * @param activity the update Activity to run
     */
    public BranchHighlightControl(String activity) {
        this.activity = activity;
    }
    
    /**
     * @see prefuse.controls.Control#itemEntered(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    public void itemEntered(VisualItem item, MouseEvent e) {
        if ( item instanceof EdgeItem )
            setBranchHighlight((EdgeItem)item, true, true, true);
    }
    
    /**
     * @see prefuse.controls.Control#itemExited(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    public void itemExited(VisualItem item, MouseEvent e) {
        if ( item instanceof EdgeItem )
        	setBranchHighlight((EdgeItem)item, false, true, true);
    }
    
    /**
     * Set the highlighted state of the neighbors of a node.
     * @param n the node under consideration
     * @param state the highlighting state to apply to neighbors
     */
    protected void setBranchHighlight(EdgeItem e, boolean state, boolean gop, boolean goc) {
    	if (gop) {
    		NodeItem p = (NodeItem)e.getTargetNode();
    		Iterator iter = p.outEdges();        
    		while ( iter.hasNext() )
    			setBranchHighlight((EdgeItem)iter.next(),state, true, false);
    	}
    	if (goc) {
    		NodeItem c = (NodeItem)e.getSourceNode();
    		Iterator iter = c.inEdges();
    		while ( iter.hasNext() )
    			setBranchHighlight((EdgeItem)iter.next(),state, false, true);
    	}
    	if (e.isVisible() || highlightWithInvisibleEdge)
    		e.setHighlighted(state);
        if ( activity != null )
            e.getVisualization().run(activity);
    }        
}
