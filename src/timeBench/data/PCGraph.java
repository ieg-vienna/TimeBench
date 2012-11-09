package timeBench.data;

import prefuse.data.Graph;

public class PCGraph extends Graph {

    // TODO make sure tuples are created as PCNode (only if used outside TemporalDataset)
    /**
     * Instances are always directed.
     */
    public PCGraph() {
        super(true);
    }

    /**
     * Get the number of children of the given node id.
     * 
     * @param node
     *            a node id (node table row number)
     * @return the number of child nodes for the given node
     */
    public int getChildCount(int node) {
        return getInDegree(node);
    }

    /**
     * Get the child node id at the given index.
     * 
     * @param node
     *            the parent node id (node table row number)
     * @param idx
     *            the child index
     * @return the child node id (node table row number)
     */
    public int getChildRow(int node, int idx) {
        int cc = getChildCount(node);
        if (idx < 0 || idx >= cc)
            return -1;
        int[] links = (int[]) m_links.get(node, INLINKS);
        return getSourceNode(links[idx]);
    }

    /**
     * Get the number of parents of the given node id.
     * 
     * @param node
     *            a node id (node table row number)
     * @return the number of parent nodes for the given node
     */
    public int getParentCount(int node) {
        return getOutDegree(node);
    }

    /**
     * Get the parent node id at the given index.
     * 
     * @param node
     *            the child node id (node table row number)
     * @param idx
     *            the parent index
     * @return the parent node id (node table row number)
     */
    public int getParentRow(int node, int idx) {
        int cc = getParentCount(node);
        if (idx < 0 || idx >= cc)
            return -1;
        int[] links = (int[]) m_links.get(node, OUTLINKS);
        return getTargetNode(links[idx]);
    }

    // TODO implement depth using m_links as cache for such metadata

}
