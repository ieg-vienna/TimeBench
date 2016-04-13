package org.timebench.action.layout;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.timebench.action.layout.timescale.TimeScale;
import org.timebench.data.AnchoredTemporalElement;
import org.timebench.data.TemporalDataset;
import org.timebench.data.TemporalElement;
import org.timebench.data.TemporalObject;

import prefuse.Constants;
import prefuse.action.layout.Layout;
import prefuse.data.expression.Predicate;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.VisiblePredicate;

public class PatternOverlayCheckLayout extends Layout {

	protected String replacementGroup;
	protected String secondPrimaryGroup;
	protected int maxOverlap = 0;
	protected boolean isReplacementGroup = false;
	
    public PatternOverlayCheckLayout(String checkGroup,String secondPrimaryGroup,String replacementGroup) {
       this(checkGroup,secondPrimaryGroup,replacementGroup,0);
    }
    
    public PatternOverlayCheckLayout(String checkGroup,String secondPrimaryGroup,String replacementGroup,int maxOverlap) {
        super(checkGroup);
        this.replacementGroup = replacementGroup;
        this.secondPrimaryGroup = secondPrimaryGroup;
        this.maxOverlap = maxOverlap;
        
        switchToMainGroup();
    }


    @Override
    public void run(double frac) {
    	 TupleSet items = m_vis.getGroup(m_group);    	     	 

    	 boolean leave = false;
         Iterator tuples = items.tuples();
         while (tuples.hasNext()) {
        	 VisualItem item = (VisualItem)tuples.next();
        	 if (m_vis.getDisplay(0).getBounds().intersects(item.getBounds().getBounds())) {
    		 Iterator tuples2 = items.tuples();
    		 int overlapCounter = 0;
    		 while (tuples2.hasNext()) {
    			 VisualItem item2 = (VisualItem)tuples2.next();
    			 if (item != item2) {
    				 if (item.getBounds().intersects(item2.getBounds().getBounds()))
    					 overlapCounter++;
            		 if (overlapCounter > maxOverlap) {
            			 switchToReplacementGroup();
            			 leave = true;
            			 break;
            		 }
    			 }
    		 }
             if (leave)
            	 break;
        	 }
         }
         if (leave == false)
        	 switchToMainGroup();
    	 
         /*if (items instanceof VisualGraph) {
             items = ((VisualGraph) items).getNodes();
         }                
         
         boolean leave = false;
         Iterator tuples = items.tuples();
         while (tuples.hasNext()) {
             NodeItem item = (NodeItem) tuples.next();
             Iterator childTuples = item.children();
             while (childTuples.hasNext()) {
            	 VisualItem parent = (VisualItem)item;
            	 VisualItem child = (VisualItem) childTuples.next();
            	 double start = parent.getX();
            	 double stop = child.getDouble(VisualItem.X2);            	 
            	 if (start <= m_vis.getDisplay(0).getBounds().getMaxX() && stop >= m_vis.getDisplay(0).getBounds().getMinX()) {
            		 int overlapCounter = 0;
            		 Iterator tuples2 = items.tuples();
            		 while (tuples2.hasNext()) {
            			 double start2 = ((VisualItem)tuples2.next()).getX();
            			 if (start2 > start && start2 < stop)
            				 overlapCounter++;
            		 }
            		 if (overlapCounter > maxOverlap) {
            			 switchToReplacementGroup();
            			 leave = true;
            			 break;
            		 }
            	 } else {
            		 int x = 0;
            		 x++;
            	 }
             }
             if (leave)
            	 break;
         }
         if (leave == false)
        	 switchToMainGroup();*/
    }

	private void switchToMainGroup() {
		if (isReplacementGroup) {
			TupleSet items = m_vis.getGroup(m_group);
			if (items instanceof VisualGraph) {
				items = ((VisualGraph) items).getNodes();
			}
			Iterator tuples = items.tuples();
			while (tuples.hasNext()) {
				VisualItem item = (VisualItem) tuples.next();
				item.setVisible(true);
			}
			
			items = m_vis.getGroup(secondPrimaryGroup);
			if (items instanceof VisualGraph) {
				items = ((VisualGraph) items).getNodes();
			}
			tuples = items.tuples();
			while (tuples.hasNext()) {
				VisualItem item = (VisualItem) tuples.next();
				item.setVisible(true);
			}

			items = m_vis.getGroup(replacementGroup);
			if (items instanceof VisualGraph) {
				items = ((VisualGraph) items).getNodes();
			}
			tuples = items.tuples();
			while (tuples.hasNext()) {
				VisualItem item = (VisualItem) tuples.next();
				item.setVisible(false);
			}
        isReplacementGroup = false;
		}
	}

	private void switchToReplacementGroup() {
		if (!isReplacementGroup) {
			TupleSet items = m_vis.getGroup(m_group);
			if (items instanceof VisualGraph) {
				items = ((VisualGraph) items).getNodes();
			}
			Iterator tuples = items.tuples();
			while (tuples.hasNext()) {
				VisualItem item = (VisualItem) tuples.next();
				item.setVisible(false);
			}
			
			items = m_vis.getGroup(secondPrimaryGroup);
			if (items instanceof VisualGraph) {
				items = ((VisualGraph) items).getNodes();
			}
			tuples = items.tuples();
			while (tuples.hasNext()) {
				VisualItem item = (VisualItem) tuples.next();
				item.setVisible(false);
			}

			items = m_vis.getGroup(replacementGroup);
			if (items instanceof VisualGraph) {
				items = ((VisualGraph) items).getNodes();
			}
			tuples = items.tuples();
			while (tuples.hasNext()) {
				VisualItem item = (VisualItem) tuples.next();
				item.setVisible(true);
			}
			
			isReplacementGroup = true;
		}
	}
}
