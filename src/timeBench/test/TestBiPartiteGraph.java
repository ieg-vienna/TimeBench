package timeBench.test;

import prefuse.data.Table;
import prefuse.util.collections.IntIterator;
import timeBench.data.relational.BipartiteGraph;

public class TestBiPartiteGraph {
		/**
		 * test
		 * @param args system arguments
		 */
		public static void main(String[] args) {
			Table nodes1 = new Table(10, 3);
			nodes1.addColumn(BipartiteGraph.DEFAULT_NODE_KEY, int.class);
			for (int i = 0; i < 10; i++) {
				nodes1.set(i, 0, i);
			}
			Table nodes2 = new Table(10, 3);
			nodes2.addColumn(BipartiteGraph.DEFAULT_NODE_KEY, int.class);
			for (int i = 0; i < 10; i++) {
				nodes2.set(i, 0, i);
			}
	
			BipartiteGraph graph = new BipartiteGraph(nodes1, nodes2);
	//		Graph graph = new Graph(nodes1, false);
			graph.addEdge(0, 9);
			graph.addEdge(3, 2);
			graph.addEdge(5, 8);
			graph.addEdge(5, 6);
			graph.addEdge(3, 6);
			graph.addEdge(3, 6);
			
			nodes2.addRow();
			nodes2.set(10, 0, 10);
			graph.addEdge(3, 10);
	
			IntIterator edgeRows = graph.edgeRows1(3);
			while (edgeRows.hasNext()) {
				System.out.print(edgeRows.nextInt() + ", ");
			}
	
		}
	
	
}

