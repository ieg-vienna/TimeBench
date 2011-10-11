package timeBench.test;

import java.util.Iterator;

import prefuse.Visualization;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.util.collections.IntIterator;
import timeBench.data.TemporalDataException;
import prefuse.visual.VisualGraph;
import timeBench.data.relational.TemporalDataset;
import timeBench.data.relational.TemporalElement;
import timeBench.data.relational.TemporalObject;
import timeBench.data.util.DefaultIntervalComparator;
import timeBench.data.util.IntervalIndex;

public class TestTemporalDataSet {
	/**
	 * Test
	 * 
	 * TODO convert to a junit test
	 * 
	 * @param args
	 *            system arguments
	 * @throws TemporalDataException 
	 */
	public static void main(String[] args) throws TemporalDataException {
		TemporalDataset dataset = new TemporalDataset();
		dataset.addTemporalElement(1, 10, 1, 1);
		dataset.addTemporalElement(4, 12, 1, 1);
		dataset.addTemporalElement(6, 8, 1, 1);
		dataset.addTemporalElement(3, 14, 1, 1);
        int begin = dataset.addTemporalElement(2, 6, 1, 1);
        int end = dataset.addTemporalElement(5, 13, 1, 1);
        int interval = dataset.addTemporalElement(2, 13, 1, 3);
        dataset.getTemporalElementsGraph().addEdge(begin, interval);
        dataset.getTemporalElementsGraph().addEdge(end, interval);
		
		Table dataElements = dataset.getDataElements();
		dataElements.addColumn("ID", String.class);
		dataElements.addColumn("Age", int.class);
		dataElements.addRows(6);
		dataElements.set(0, "ID", "Morg"); dataElements.set(0, "Age", 28);
		dataElements.set(1, "ID", "Ben"); dataElements.set(1, "Age", 26);
		dataElements.set(2, "ID", "Xu"); dataElements.set(2, "Age", 29);
		dataElements.set(3, "ID", "Fab"); dataElements.set(3, "Age", 21);
		dataElements.set(4, "ID", "Sha"); dataElements.set(4, "Age", 25);
		dataElements.set(5, "ID", "Tra"); dataElements.set(5, "Age", 29);
		
		dataset.addOccurrence(0, 0);
		dataset.addOccurrence(1, 1);
		dataset.addOccurrence(2, 2);
		dataset.addOccurrence(3, 3);
		dataset.addOccurrence(4, 4);
        dataset.addOccurrence(5, interval);

        System.out.println(dataset);

        System.out.println("Test interval index");
		IntervalIndex index = dataset.createTemporalIndex(new DefaultIntervalComparator());
		
		IntIterator rows5 = index.rows(5);
		while (rows5.hasNext()) {
		    int row = rows5.nextInt(); 
			System.out.println(row + " " + dataset.getTemporalElement(row));
		}
		
        System.out.println("Test iterator & tuplemanager");
        System.out.println(dataset.getTemporalElement(0));
        // Iterator<TemporalElement> teIterator = dataset.temporalElements();
        // while (teIterator.hasNext()) {
        // TemporalElement te = teIterator.next();
        for (TemporalElement te : dataset.temporalElementsIterable()) {
            System.out.println(te);
        }

        System.out.println("Test temporal elements graph");
        TemporalElement te = dataset.getTemporalElement(interval);
        System.out.println("Children of interval   count="
                + te.getChildElementCount());
        Iterator<TemporalElement> teIterator = te.childElements();
        while (teIterator.hasNext()) {
            System.out.println(teIterator.next());
        }
        te = dataset.getTemporalElement(begin);
        System.out.println("Children of instant   count="
                + te.getChildElementCount());
        teIterator = te.childElements();
        while (teIterator.hasNext()) {
            System.out.println(teIterator.next());
        }

        te = dataset.getTemporalElement(begin);
        System.out.println("Parents of begin instant   count="
                + te.getParentElementCount());
        teIterator = te.parentElements();
        while (teIterator.hasNext()) {
            System.out.println(teIterator.next());
        }
        
        System.out.println("\nTest tuplemanager for TemporalObject");

        Iterator<TemporalObject> toIterator = dataset.temporalObjects();
        while (toIterator.hasNext()) {
            TemporalObject to = toIterator.next(); 
            System.out.println(to);
            System.out.println(" " + to.getDataElement());
            System.out.println(" " + to.getTemporalElement());
            System.out.println("  anchored=" + to.getTemporalElement().isAnchored() + ", length=" + to.getTemporalElement().getLength());
        }
/*        
        Iterator iter = dataset.getTemporalElement(interval).inEdges();
        while (iter.hasNext()) {
            prefuse.data.Edge edge = (prefuse.data.Edge) iter.next();
            System.out.println("c" + edge.getColumnName(2));
        }
*/
        // check that adding a tuple set to a visualization does not affect existing tuples 
        TemporalElement t2 = dataset.getTemporalElement(2);
        System.out.println(t2);
        Visualization viz = new Visualization();
        viz.addTable("aex", dataElements);
        System.out.println(t2);
        VisualGraph vg = viz.addGraph("bex", dataset.getTemporalElementsGraph());
        Node v2 = vg.getNode(2); 
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(t2 + " " + v2);
    }
}
