package timeBench.action.analytical;

import ieg.prefuse.data.ParentChildGraph;
import ieg.prefuse.data.ParentChildNode;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.tuple.TableTuple;
import prefuse.visual.tuple.TableEdgeItem;

import timeBench.calendar.CalendarManager;
import timeBench.calendar.CalendarManagerFactory;
import timeBench.calendar.CalendarManagers;
import timeBench.calendar.Granularity;
import timeBench.calendar.Granule;
import timeBench.data.GranularityAggregationTree;
import timeBench.data.GranularityAggregationTreeProvider;
import timeBench.data.Instant;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalDatasetProvider;
import timeBench.data.TemporalObject;

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
public class PatternInstanceCountAction extends prefuse.action.Action {
    
	TemporalDataset sourceDataset;
	ParentChildGraph workingDataset;

	public PatternInstanceCountAction(TemporalDataset sourceDataset) {
		this.sourceDataset = sourceDataset;
	}
	

	/* (non-Javadoc)
	 * @see prefuse.action.Action#run(double)
	 */
	@Override
	public void run(double frac) {
		workingDataset = new ParentChildGraph();
        workingDataset.initTupleManagers(ParentChildNode.class,
                TableEdgeItem.class);
		workingDataset.getNodes().addColumn("label", String.class, "");
		workingDataset.getNodes().addColumn("class", int.class, -1);
		workingDataset.getNodes().addColumn("count", int.class, 0);		
		
		workingDataset.getEdges().addColumn("count", int.class);
		
		Hashtable<Integer,Node> rootCount = new Hashtable<Integer, Node>();
		for(TemporalObject iO : sourceDataset.roots()) {
			int rootClass = iO.getInt("class");
			if (rootCount.contains(rootClass)) {
				doCount(iO,rootCount.get(rootClass));
			} else {
				Node node = doCount(iO,null);
				rootCount.put(rootClass,node);
			}
		}		
	}

	private Node doCount(Node sourceNode,Node newNode) {
		
		if(newNode == null) {
			newNode = workingDataset.addNode();
			newNode.set("label", sourceNode.get("label"));
			newNode.set("class", sourceNode.get("class"));
		}
		
		Hashtable<Integer,Node> count = new Hashtable<Integer,Node>();
		Iterator edges = sourceNode.inEdges();
		while (edges.hasNext()) {
			Edge edge = (Edge)edges.next();
			int classValue = (int)edge.getLong(MultiPredicatePatternDiscovery.predicateColumn);
			Node sourceNodeChild = edge.getSourceNode();
			int sourceClassValue = sourceNodeChild.getInt("class");
			int combined = sourceClassValue<<16+classValue;
			Node newSource = null;
			if(count.containsKey(combined)) {
				newSource = doCount(sourceNodeChild,count.get(combined));
				//System.out.println("existing: "+count.get(combined).getRow());
			}
			else {
				newSource = doCount(sourceNodeChild,null);
				count.put(combined, newSource);
				workingDataset.addEdge(newSource, newNode);
				//System.out.println("from "+newSource.getRow()+" to "+newNode.getRow());
				int x=0;x++;
			}
		}
		
		Enumeration<Integer> keys = count.keys();
		int result = 0;
		while(keys.hasMoreElements()) {
			result += (Integer)count.get(keys.nextElement()).get("count");
		}
		if(result == 0)
			result = 1;
		
		newNode.set("count", newNode.getInt("count")+result);
			
		return newNode;
	}


	public ParentChildGraph getResult() {
		return workingDataset;
	}
}
