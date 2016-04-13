package org.timebench.action.analytical;

import ieg.prefuse.data.ParentChildGraph;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.timebench.data.TemporalDataset;
import org.timebench.data.TemporalObject;

import prefuse.data.Edge;
import prefuse.data.Node;

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
		workingDataset.getNodes().addColumn("label", String.class, "");
		workingDataset.getNodes().addColumn("class", int.class, -1);
		workingDataset.getNodes().addColumn("count", int.class, 0);		
		workingDataset.getNodes().addColumn("depth", int.class, 0);		
		
		workingDataset.getEdges().addColumn("label", String.class, "");
		workingDataset.getEdges().addColumn("class", int.class, -1);
		
		Hashtable<Integer,Node> rootCount = new Hashtable<Integer, Node>();
		for(TemporalObject iO : sourceDataset.roots()) {
			int rootClass = iO.getInt("class");
			if (rootCount.containsKey(rootClass)) {
				doCount(iO,rootCount.get(rootClass),0);
			} else {
				Node node = doCount(iO,null,0);
				rootCount.put(rootClass,node);
			}
		}		
	}

	private Node doCount(Node sourceNode,Node newNode,int depth) {
		
		if(newNode == null) {
			newNode = workingDataset.addNode();
			newNode.set("label", sourceNode.get("label"));
			newNode.set("class", sourceNode.get("class"));		
			newNode.set("depth", depth);		
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
				newSource = doCount(sourceNodeChild,count.get(combined),depth+1);
			}
			else {
				newSource = doCount(sourceNodeChild,null,depth+1);
				count.put(combined, newSource);
				Edge newEdge = workingDataset.addEdge(newSource, newNode);
				newEdge.set("class",edge.get(MultiPredicatePatternDiscovery.predicateColumn));
				newSource.set("label","p"+newEdge.get("class")+newSource.get("label"));
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
