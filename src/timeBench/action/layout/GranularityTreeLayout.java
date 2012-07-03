package timeBench.action.layout;

import prefuse.action.GroupAction;
import timeBench.data.GranularityAggregationTree;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalObject;

public class GranularityTreeLayout extends GroupAction {

    // XXX assume depth is given via size of settings
    protected int depth;

    // TODO consider circumstances to invalidate these min max ident. arrays
    protected long[] minIdentifiers;
    protected long[] maxIdentifiers;

    GranularityGROOVELayoutSettings[] settings;

    public GranularityTreeLayout(String group,
            GranularityGROOVELayoutSettings[] settings) {
        super(group);
        this.settings = settings;
        this.depth = settings.length;
    }

    @Override
    public void run(double frac) {

        GranularityAggregationTree tree = (GranularityAggregationTree) m_vis
                .getSourceData(m_group);
        TemporalObject root = tree.getTemporalObject(tree.getRoots()[0]);

        try {
            calculateIdentifierRange(root);
        } catch (TemporalDataException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void calculateIdentifierRange(TemporalObject root)
            throws TemporalDataException {
        minIdentifiers = new long[depth];
        maxIdentifiers = new long[depth];

        TemporalObject node = root;
        for (int level = 0; level < depth; level++) {
            if (null == node) {
                throw new TemporalDataException(
                        "Aggregation Tree and Settings not matching at level "
                                + level);
            }

            if (settings[level].getFitting() == GROOVELayout.FITTING_FULL_AVAILABLE_SPACE) {
                minIdentifiers[level] = Long.MAX_VALUE;
                maxIdentifiers[level] = Long.MIN_VALUE;
            } else {
                minIdentifiers[level] = node.getTemporalElement().getGranule()
                        .getGranularity().getMinGranuleIdentifier();
                maxIdentifiers[level] = node.getTemporalElement().getGranule()
                        .getGranularity().getMaxGranuleIdentifier();
            }
            node = node.getFirstChildObject();
        }

        calculateIdentifierRangeByAvailableSpace(root, 0);

        System.out.println(java.util.Arrays.toString(minIdentifiers));
        System.out.println(java.util.Arrays.toString(maxIdentifiers));
    }

    private void calculateIdentifierRangeByAvailableSpace(TemporalObject node,
            int level) throws TemporalDataException {
        if (settings[level].getFitting() == GROOVELayout.FITTING_FULL_AVAILABLE_SPACE) {
            minIdentifiers[level] = Math.min(minIdentifiers[level], node
                    .getTemporalElement().getGranule().getIdentifier());
            maxIdentifiers[level] = Math.max(maxIdentifiers[level], node
                    .getTemporalElement().getGranule().getIdentifier());
        }
        if (level + 1 < depth) {
            for (TemporalObject o : node.childObjects()) {
                calculateIdentifierRangeByAvailableSpace(o, level + 1);
            }
        }
    }
}
