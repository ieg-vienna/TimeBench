package visual.sort;

import prefuse.Visualization;
import prefuse.data.Node;
import prefuse.visual.AggregateItem;
import prefuse.visual.DecoratorItem;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.sort.ItemSorter;

/**
 * 
 * 
 * <p>
 * Added:          / TL<br>
 * Modifications: 
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public class TreeItemSorter extends ItemSorter {
	@Override
    public int score(VisualItem item) {
        int type = ITEM;
        if ( item instanceof EdgeItem ) {
            type = EDGE;
        } else if ( item instanceof AggregateItem ) {
            type = AGGREGATE;
        } else if ( item instanceof DecoratorItem ) {
            type = DECORATOR;
        }
        
        int score = (1<<(26+type));
                
        if (item.getSourceTuple() instanceof Node) {
            for(Node node = (Node)item.getSourceTuple(); node.getParent() != null; node = node.getParent()) {
            	score++;
            }
        }

        return score;
    }
}
