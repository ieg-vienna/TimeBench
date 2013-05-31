package timeBench.action.layout;

import java.util.Iterator;

import org.apache.log4j.Logger;

import prefuse.Constants;
import prefuse.action.layout.Layout;
import prefuse.data.expression.Predicate;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.VisiblePredicate;
import timeBench.action.layout.timescale.TimeScale;
import timeBench.data.AnchoredTemporalElement;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalElement;
import timeBench.data.TemporalObject;

public class PatternOverlayCheckLayout extends Layout {

	protected String replacementGroup;
	protected int maxOverlap = 0;
	protected boolean isReplacementGroup = false;
	
    public PatternOverlayCheckLayout(String checkGroup,String replacementGroup) {
       this(checkGroup,replacementGroup,0);
    }
    
    public PatternOverlayCheckLayout(String checkGroup,String replacementGroup,int maxOverlap) {
        super(checkGroup);
        this.replacementGroup = replacementGroup;
        this.maxOverlap = maxOverlap;
    }


    @Override
    public void run(double frac) {
    	 TupleSet items = m_vis.getGroup(m_group);
         if (items instanceof VisualGraph) {
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
            		 int overlapCounter = -1;	// Every item overlaps with itself
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
        	 switchToMainGroup();
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
