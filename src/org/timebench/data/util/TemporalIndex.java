/************************************************************************
 *
 * 1. This software is for the purpose of demonstrating one of many
 * ways to implement the algorithms in Introduction to Algorithms,
 * Second edition, by Thomas H. Cormen, Charles E. Leiserson, Ronald
 * L. Rivest, and Clifford Stein.  This software has been tested on a
 * limited set of test cases, but it has not been exhaustively tested.
 * It should not be used for mission-critical applications without
 * further testing.
 *
 * 2. McGraw-Hill licenses and authorizes you to use this software
 * only on a microcomputer located within your own facilities.
 *
 * 3. You will abide by the Copyright Law of the United Sates.
 *
 * 4. You may prepare a derivative version of this software provided
 * that your source code indicates that it based on this software and
 * also that you have made changes to it.
 *
 * 5. If you believe that you have found an error in this software,
 * please send email to clrs-java-bugs@mhhe.com.  If you have a
 * suggestion for an improvement, please send email to
 * clrs-java-suggestions@mhhe.com.
 *
 ***********************************************************************/

package org.timebench.data.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.timebench.data.GenericTemporalElement;
import org.timebench.data.TemporalDataset;
import org.timebench.data.TemporalElement;
import org.timebench.data.TemporalObject;
import org.timebench.data.util.IntervalTree.Node;

import prefuse.data.Table;
import prefuse.data.column.Column;
import prefuse.data.event.EventConstants;
import prefuse.data.event.TableListener;
import prefuse.data.expression.Predicate;
import prefuse.util.collections.CompositeIterator;
import prefuse.util.collections.IntIterator;

/**
 * Implements an interval tree as described in Section 14.3 of <i>Introduction
 * to Algorithms</i>, Second edition.
 */

public class TemporalIndex implements TableListener,  IntervalIndex {

	private IntervalTree intervalTree;

	HashMap<Integer, Node> treeNodes;

	private IntervalComparator comparator;

	private Table table;
	
	private int colLoInd; 
	
	private int colHiInd; 
	
	private Predicate rowsPredicate;
	
	private TemporalDataset dataset;
	
	public TemporalIndex(TemporalDataset dataset, Predicate rowsPredicate,
			IntervalComparator comparator) {
		this.dataset = dataset;
		table = dataset.getTemporalElements().getNodeTable();
		table.addTableListener(this);
        Column colLo = table.getColumn(TemporalElement.INF);
        Column colHi = table.getColumn(TemporalElement.SUP);
        colLoInd = table.getColumnNumber(colLo);
        colHiInd = table.getColumnNumber(colHi);
        intervalTree = new IntervalTree(table, colLo, colHi, comparator);
        this.rowsPredicate = rowsPredicate;
        index();
	}

	@Override
	public void tableChanged(Table t, int start, int end, int col, int type) {
		switch (type) {
		case EventConstants.DELETE:
			for (int i = start; i <= end; i++) {
				Node node = treeNodes.get(i);
				if (node != null) {
					intervalTree.delete(node);
					treeNodes.remove(i);
				}
			}
			return;
		case EventConstants.INSERT:
			for (int i = start; i <= end; i++) {
				Node node = intervalTree.insert(i);
				treeNodes.put(i, node);
			}
			return;
		case EventConstants.UPDATE:
			if (col == colHiInd || col == colLoInd || col == EventConstants.ALL_COLUMNS) {
				for (int i = start; i <= end; i++) {
					Node node = treeNodes.get(i);
					if (node != null) {
						intervalTree.delete(node);
						intervalTree.treeInsert(node);
					}
				}
			}
			return;
		}
	}

	@Override
	public IntervalComparator getComparator() {
		return comparator;
	}

	@Override
	public int size() {
		return treeNodes.size();
	}
	
	

	/**
	 * Finds the intervals that overlap with a given value. The intervals are
	 * returned in a sorted order (using the provided {@link IntervalComparator}
	 * ).
	 * 
	 * @param value
	 *            The value to search with.
	 */
	@Override
	public IntIterator rows(long value) {
		final List<Node> result = new ArrayList<Node>();
		intervalTree.search(value, (Node)intervalTree.root, result);
		Collections.sort(result);
		return new NodesIntIterator(result);
	}


	/**
	 * Finds the intervals that overlaps with a given interval. The intervals
	 * are returned in a sorted order (using the provided
	 * {@link IntervalComparator}).
	 * 
	 * @param interval
	 *            The interval to overlap with.
	 */
	@Override
	public IntIterator rows(long low, long high) {
		final List<Node> result = new ArrayList<Node>();
		intervalTree.search(low, high,  (Node)intervalTree.root, result);
		Collections.sort(result);
		return new NodesIntIterator(result);
	}


	/**
	 * Finds the temporal objects that overlap with a given temporal value. 
	 * @param value
	 *            The value to search with.
	 */
	@Override
	public Iterator<TemporalObject> temporalObjects(long value) {
		final List<Node> nodes = new ArrayList<Node>();
		intervalTree.search(value,  (Node)intervalTree.root, nodes);
		return getTemporalObjects(nodes);
	}
	
	@Override
	public Iterator<TemporalObject> temporalObjects(long low, long high) {
		final List<Node> nodes = new ArrayList<Node>();
		intervalTree.search(low, high,  (Node)intervalTree.root, nodes);
		return getTemporalObjects(nodes);
	}

	/**
	 * Finds the intervals that overlaps with a given interval. The intervals
	 * are returned in a sorted order (using the provided
	 * {@link IntervalComparator}).
	 * 
	 * @param interval
	 *            The interval to overlap with.
	 */

	private Iterator<TemporalObject> getTemporalObjects(List<Node> temporalElements) {
		Iterator[] result = new Iterator[temporalElements.size()];
		int i = 0;
		for (Node node : temporalElements) {
			GenericTemporalElement tE = dataset.getTemporalElementByRow(node.row);
			result[i++] = dataset.getTemporalObjectsByElementId(tE.getId()).iterator();
		}
		return new CompositeIterator(result);
	}

	private static class NodesIntIterator extends IntIterator {
		int index = 0;
		final List<Node> result;

		NodesIntIterator(List<Node> result) {
			this.result = result;
		}

		@Override
		public void remove() {
		}

		@Override
		public boolean hasNext() {
			return index < result.size();
		}

		@Override
		public int nextInt() {
			return result.get(index++).row;
		}

	}

	/**
	 * @see prefuse.data.util.Index#index()
	 */
	private void index() {
		if (treeNodes == null) {
			treeNodes = new HashMap<Integer, IntervalTree.Node>();
		}
		else {
			treeNodes.clear();
		}
		// TODO: clear intervaTree (no appropriate method for this yet).
		
        IntIterator rows = table.rows(rowsPredicate);
		// iterate over all valid values, adding them to the index
		while (rows.hasNext()) {
			int r = rows.nextInt();
			Node node = intervalTree.insert(table.getColumnRow(r,  colLoInd));
			treeNodes.put(r, node);
		}
	}

	@Override
	public int minimum() {
		// TODO Auto-generated method stub
		return intervalTree.minimum();
	}

	@Override
	public int maximum() {
		// problem: underlying data structure is sorted by INF
		throw new UnsupportedOperationException("contact Bilal oder Alex :)");
	}

}