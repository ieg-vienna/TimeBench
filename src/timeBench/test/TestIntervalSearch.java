package timeBench.test;

import prefuse.data.Table;
import prefuse.util.collections.IntIterator;
import timeBench.data.util.DefaultIntervalComparator;
import timeBench.data.util.IntervalTreeIndex;

public class TestIntervalSearch {
	/**
	 * Test
	 * 
	 * @param args
	 *            system arguments
	 */
	public static void main(String[] args) {
		Table table = new Table(10, 2);
		table.addColumn("start", long.class);
		table.addColumn("end", long.class);
		table.set(0, 0, 10);table.set(0, 1, 20);
		table.set(1, 0, 14);table.set(1, 1, 25);
		table.set(2, 0, 10);table.set(2, 1, 16);
		table.set(3, 0, 16);table.set(3, 1, 18);
		table.set(4, 0, 21);table.set(4, 1, 26);
		table.set(5, 0, 16);table.set(5, 1, 26);
		table.set(6, 0, 8); table.set(6, 1, 12);
		table.set(7, 0, 18);table.set(7, 1, 22);
		table.set(8, 0, 22);table.set(8, 1, 26);
		table.set(9, 0, 10);table.set(9, 1, 12);

		IntervalTreeIndex intervalTree = new IntervalTreeIndex(table, table.rows(),
				table.getColumn(0), table.getColumn(1),
				new DefaultIntervalComparator());
		IntIterator result = intervalTree.rows(13, 19);
		while (result.hasNext()) {
			System.out.println(result.nextInt());
		}
	}


}
