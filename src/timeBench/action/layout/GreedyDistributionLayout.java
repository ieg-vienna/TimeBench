package timeBench.action.layout;

import java.util.ArrayList;
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

public class GreedyDistributionLayout extends Layout {

	protected String replacementGroup;
	int maxLanes = 7;
	protected boolean isReplacementGroup = false;

	public GreedyDistributionLayout(String group,String replacementGroup,int maxLanes) {
       super(group);
       this.maxLanes = maxLanes;
       this.replacementGroup = replacementGroup;
    }    

    @Override
    public void run(double frac) {
    	 TupleSet items = m_vis.getGroup(m_group);
         if (items instanceof VisualGraph) {
             items = ((VisualGraph) items).getNodes();
         }
         
         double ybase = m_vis.getDisplay(0).getBounds().getCenterY()-m_vis.getDisplay(0).getBounds().getY();
         double minx = m_vis.getDisplay(0).getBounds().getMinX();
         double maxx = m_vis.getDisplay(0).getBounds().getMaxX();
         
         ArrayList<Double> lastOnLane = new ArrayList<Double>();
         Iterator tuples = items.tuples();
         while (tuples.hasNext()) {
             VisualItem item = (VisualItem) tuples.next();
             if (item.getX() >= minx && item.getDouble(VisualItem.X2) <= maxx) {
            	 int takeLane = -1;

            	 for(int i=0; i<lastOnLane.size(); i++) {
            		 if(lastOnLane.get(i) < item.getX()) {
            			 takeLane = i;
            			 lastOnLane.set(i, item.getDouble(VisualItem.X2));
            			 break;
            		 }
            	 }
            	 if (takeLane == -1) {
            		 if (lastOnLane.size() >= maxLanes) {
            			 switchToReplacementGroup();
            			 break;
            		 }
            		 lastOnLane.add(item.getDouble(VisualItem.X2));
            		 takeLane = lastOnLane.size()-1;
            	 }
            	 if (takeLane % 2 == 0) {
                	 item.setY(ybase + (takeLane+1) * 8);          		 
            	 } else {
                	 item.setY(ybase - takeLane * 8);
            	 }
            	 item.setSize(1.2);
             }
         }
         if (lastOnLane.size() < maxLanes)
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
			if (items != null) {
				if (items instanceof VisualGraph) {
					items = ((VisualGraph) items).getNodes();
				}
				tuples = items.tuples();
				while (tuples.hasNext()) {
					VisualItem item = (VisualItem) tuples.next();
					item.setVisible(true);
				}
			}
			
			isReplacementGroup = true;
		}
	}
}
