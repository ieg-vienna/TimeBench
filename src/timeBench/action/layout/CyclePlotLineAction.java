package timeBench.action.layout;

import java.util.Iterator;

import prefuse.data.expression.ColumnExpression;
import prefuse.data.expression.ComparisonPredicate;
import prefuse.data.expression.NumericLiteral;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualTable;
import timeBench.data.GranularityAggregationTree;
import ieg.prefuse.action.LinePlotAction;
import ieg.prefuse.data.ParentChildNode;

/**
 * Creates a group with line segments that connect the nodes at a given level
 * with their immediate siblings.
 * 
 * @author Rind
 */
public class CyclePlotLineAction extends LinePlotAction {

    private int depth;

    public CyclePlotLineAction(String group, String source, int depth) {
        super(group, source);
        this.depth = depth;
    }

    @Override
    public void run(double frac) {
        // get a visual table for line segments
        VisualTable lines = getTable();

        // get all nodes at depth-1
        VisualGraph graph = (VisualGraph) m_vis.getGroup(m_src);
        @SuppressWarnings("rawtypes")
        Iterator nodeIterator = graph.getNodeTable().tuples(
                new ComparisonPredicate(ComparisonPredicate.EQ,
                        new ColumnExpression(ParentChildNode.DEPTH),
                        new NumericLiteral(depth - 1)));

        // connect children of these nodes
        while (nodeIterator.hasNext()) {
            NodeItem node = (NodeItem) nodeIterator.next();
            // TODO assumption that children are ordered or reversely ordered
            super.connectPoints(lines, node.inNeighbors());
        }
    }

}
