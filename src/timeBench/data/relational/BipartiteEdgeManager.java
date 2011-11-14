package timeBench.data.relational;

import org.apache.log4j.Logger;

import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.tuple.TupleManager;
import prefuse.util.StringLib;

/**
 * Manager class for edges in the bipartite graph. 
 * Mostly reuses its superclass {@link TupleManager}.
 * 
 * @author Rind
 *
 */
public class BipartiteEdgeManager extends TupleManager {
    
    private BipartiteGraph bGraph;

    @SuppressWarnings("rawtypes")
    public BipartiteEdgeManager(Table t, BipartiteGraph bGraph, Class tupleType) {
        super(t, null, tupleType);
        this.bGraph = bGraph;
    }

    @SuppressWarnings("rawtypes")
    public BipartiteEdgeManager(Table t, Graph graph, BipartiteGraph bGraph, Class tupleType) {
        super(t, graph, tupleType);
        this.bGraph = bGraph;
    }

    @Override
    protected BipartiteEdge newTuple(int row) {
        try {
            BipartiteEdge t = (BipartiteEdge) m_tupleType.newInstance();
            t.init(m_table, m_graph, bGraph, row);
            return t;
        } catch ( Exception e ) {
            Logger.getLogger(getClass().getName()).warn(
                e.getMessage()+"\n"+StringLib.getStackTrace(e));
            return null;
        }
    }

}
