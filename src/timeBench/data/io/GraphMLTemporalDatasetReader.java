package timeBench.data.io;

import java.io.InputStream;
import java.util.Iterator;

import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;
import timeBench.data.relational.TemporalDataset;

/**
 * TemporalDatasetReader implementation that reads in temporal data formatted
 * using the GraphML file format. GraphML is an XML format supporting graph
 * structure and typed data schemas for both nodes and edges. For more
 * information about the format, please see the <a
 * href="http://graphml.graphdrawing.org/">GraphML home page</a>.
 * 
 * Based on {@link prefuse.data.io.GraphMLReader}.
 * 
 * @author Alexander Rind
 */
public class GraphMLTemporalDatasetReader extends AbstractTemporalDatasetReader {

    @Override
    public TemporalDataset readData(InputStream is) throws DataIOException {
        Graph rawGraph = new GraphMLReader().readGraph(is);
//        Node[] nodeCache = new Node[rawGraph.getNodeCount()];
        
//        TemporalDataset tmpds = new TemporalDataset();
        // TODO add data columns 
        
        // TODO prefuse does not 
        
        @SuppressWarnings("unchecked")
        Iterator<Node> nodes = (Iterator<Node>) rawGraph.nodes();
        while (nodes.hasNext()) {
            System.out.println(nodes.next());
            // GraphMLReader does not capture node ids
            // TODO decide element or object, build node, add to tmpds
        }
        
        // TODO for all edges: lookup nodes in array and insert correct edges
        
        return null;
    }

}
