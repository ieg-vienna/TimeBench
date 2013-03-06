package visual.sort;

import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.sort.ItemSorter;

public class SizeItemSorter extends ItemSorter {
	public int score(VisualItem item) {
		int result = super.score(item);
		
		double x = 0;
		double x2 = 0;
		
		if(item instanceof NodeItem) {
			x = item.getX();
			x2 = item.getDouble(VisualItem.X2);
		} else if (item instanceof EdgeItem) {
			EdgeItem edge = ((EdgeItem)item);
			x = edge.getTargetItem().getX();
			x2 = edge.getSourceItem().getDouble(VisualItem.X2);
		}
		
		result += 4096-(int)Math.round(Math.abs(x2-x));
		
		return result;
	}
}
