package timeBench.data.io;

import java.io.InputStream;

import prefuse.data.io.DataIOException;
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
        // TODO Auto-generated method stub
        return null;
    }

}
