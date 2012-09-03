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

package timeBench.data.util;

import java.util.List;

import prefuse.data.Table;
import prefuse.data.column.Column;

/**
 * Implements an interval tree as described in Section 14.3 of <i>Introduction
 * to Algorithms</i>, Second edition.
 */

public class IntervalTree  extends RedBlackTree {

	/**
	 * Inner class for an interval tree node, extending a red-black tree node
	 * with an additional max field. <code>IntervalTree.Node</code> contains an
	 * <code>Interval</code> as its data and has as auxiliary data a
	 * <code>double</code>, <code>max</code>, which is the maximum value of
	 * right endpoints for the subtree of which a particular node is the root.
	 */
	protected class Node extends RedBlackTree.Node {
		/** Maximum value in the subtree rooted at this node. */
		protected long max;

		/**
		 * Initializes a new node in an interval tree.
		 * 
		 * @param i
		 *            The interval stored in the node.
		 */
		public Node(int row) {
			super(row);
			if (row >= 0)
				max = getHigh();
		}

		/**
		 * Returns the <code>String</code> representation of this node.
		 */
		public String toString() {
			return super.toString() + ", max = " + max;
		}

		long getLow() {
			return colLo.getLong(row);
		}

		long getHigh() {
			return colHi.getLong(row);
		}

		Node getLeft() {
			return (Node) left;
		}

		Node getRight() {
			return (Node) right;
		}

		Node getParent() {
			return (Node) parent;
		}

		@Override
		public int compareTo(timeBench.data.util.BinarySearchTree.Node o) {
			Node n = (Node) o;
			return comparator.compare(this.getLow(), this.getHigh(),
					n.getLow(), n.getHigh());
		}

	}

	protected Column colLo;
	protected Column colHi;
	protected IntervalComparator comparator;

	public IntervalTree(Table table, Column colLo,
			Column colHi, IntervalComparator comparator) {
		setNil(new Node(-1));
		root = nil;
		this.colLo = colLo;
		this.colHi = colHi;
		this.comparator = comparator;
	}

	/**
	 * Calls {@link RedBlackTree}'s {@link RedBlackTree#leftRotate} and then
	 * fixes the <code>max</code> fields.
	 * 
	 * @param handle
	 *            Handle to the node being left rotated.
	 */
	protected void leftRotate(RedBlackTree.Node handle) {
		Node  x = (Node) handle;
		Node y = x.getRight();

		super.leftRotate(x);

		y.max = x.max;
		x.max = Math.max(x.getHigh(),
				Math.max((x.getLeft()).max, (x.getRight()).max));
	}

	/**
	 * Calls <code>RedBlackTree</code>'s {@link RedBlackTree#rightRotate} and
	 * then fixes the <code>max</code> fields.
	 * 
	 * @param handle
	 *            Handle to the node being right rotated.
	 */
	protected void rightRotate(RedBlackTree.Node handle) {
		Node x = (Node) handle;
		Node y = x.getLeft();

		super.rightRotate(x);

		y.max = x.max;
		x.max = Math.max(x.getHigh(),
				Math.max(x.getLeft().max, x.getRight().max));
		// x.max = Math.max(((Node) x.left).max, ((Node) x.right).max);
	}

	/**
	 * Inserts an interval into the tree, creating a new node for this interval.
	 * 
	 * @param data
	 *            The interval being inserted.
	 * @return A handle to the <code>Node</code> that is created.
	 *         <code>Node</code> is opaque to methods outside this class.
	 * @throws ClassCastException
	 *             if <code>data</code> is not an <code>Interval</code> object.
	 */
	public Node insert(int row) {
		Node z = new Node(row);
		treeInsert(z);
		return z;
	}


	/**
	 * Inserts a node, updating the <code>max</code> fields of its ancestors
	 * before the superclass's <code>insertNode</code> is called.
	 * 
	 * @param x
	 *            The node to insert.
	 */
	protected void treeInsert(Node x) {
		// Update the max fields of the path down to where the node
		// will be inserted in the tree.
		for (Node i = (Node) root; i != nil; i = (Node) ((i.compareTo(x) >= 0) ? i.left
				: i.right))
			i.max = Math.max(x.max, i.max);

		super.treeInsert(x);
	}

	/**
	 * Deletes a node from the tree.
	 * 
	 * @param handle
	 *            Handle to the node being deleted.
	 * @throws ClassCastException
	 *             if <code>handle</code> is not a <code>Node</code> object.
	 */
	public void delete(Object handle) {
		// Walk up the tree by following parent pointers while
		// updating the max value of each node along the path.
		Node x = (Node) handle;

		x.max = Long.MIN_VALUE;

		for (Node i = x.getParent(); i != nil; i = i.getParent())
			i.max = Math.max(i.getLeft().max, i.getRight().max);

		// Now actually remove the node.
		super.delete(handle);
	}

	public void search(long low, long high, Node node, List<Node> result) {
		if (node != nil && low < node.max) {
			search(low, high, node.getLeft(), result);
			if (comparator.match(low, high, node.getLow(), node.getHigh())) {
				result.add(node);
			}
			search(low, high, node.getRight(), result);
		}
	}

	public void search(long value, Node node, List<Node> result) {
		if (node != nil && value < node.max) {
			search(value, node.getLeft(), result);
			if (node.getLow() <= value && value <= node.getHigh()) {
				result.add(node);
			}
			search(value, node.getRight(), result);
		}
	}
}