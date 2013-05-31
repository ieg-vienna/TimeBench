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

	protected Predicate m_filter = VisiblePredicate.TRUE;
	protected String replacementGroup;
	int maxLanes = 7;

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
         
         double ybase = m_vis.getDisplay(0).getBounds().getCenterY();
         
         ArrayList<Double> lastOnLane = new ArrayList<Double>();
         Iterator tuples = items.tuples(m_filter);
         while (tuples.hasNext()) {
             VisualItem item = (VisualItem) tuples.next();
             int takeLane = -1;

             for(int i=0; i<lastOnLane.size(); i++) {
            	 if(lastOnLane.get(i) < item.getX()) {
            		 takeLane = i;
                	 lastOnLane.set(i, item.getDouble(VisualItem.X2));
            		 break;
            	 }
            	 if (takeLane >= maxLanes) {
            		 switchToReplacementGroup();
            		 break;
            	 }
             }
           	 if (takeLane == -1) {
           		 lastOnLane.add(item.getDouble(VisualItem.X2));
           		 takeLane = lastOnLane.size()-1;
           	 }
           	 item.setY(ybase + ((takeLane % 2) == 0 ? -1 : 1) * takeLane * 10);
           	 item.setSizeY(6);
         }
    }
    
	private void switchToReplacementGroup() {
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
	}
}
