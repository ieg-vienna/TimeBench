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

/**
 * Implements the {@link Dictionary} interface as a binary search tree
 * from Chapter 12 of <i>Introduction to Algorithms</i>, Second
 * edition.  Objects inserted into a binary search tree must implement
 * the <code>Comparable</code> interface.
 *
 * <p>
 *
 * When extending this class, you must instantiate a new
 * <code>nil</code> of the proper type and override constructors along
 * with the other methods.  See {@link RedBlackTree} for an example.
 */

abstract class BinarySearchTree
{
    /** Root of the binary search tree. */
    protected Node root;

    /** Sentinel, replaces NIL in the textbook's code. */
    protected Node nil;

    /**
     * Interface for when we visit a node during a walk.
     */
    public interface Visitor
    {
	/**
	 * Perform some action upon visiting the node.
	 *
	 * @param handle Handle that identifies the node being visited.
	 */
	public Object visit(Object handle);
    }

    /**
     * Inner class for a node of a binary search tree.  May be
     * extended in subclasses of <code>BinarySearchTree</code>.
     */
    protected class Node implements Comparable<Node>
    {
	/** The data stored in the node. */
	protected int row;

	/** The node's parent. */
	protected Node parent;

	/** The node's left child. */
	protected Node left;

	/** The node's right child. */
	protected Node right;

	/**
	 * Initializes a node with the data and makes other pointers
	 * nil.
	 *
	 * @param data Data to save in the node.
	 */
	public Node(int row)
	{
	    this.row = row;
	    parent = nil;
	    left = nil;
	    right = nil;
	}

	/**
	 * Compares this node to another node.  The comparison is
	 * based on the <code>data</code> instance variables of the
	 * two nodes.
	 *
	 * @param o The other node.
	 * @return A negative integer if this node is less than
	 * <code>o</code>; 0 if this node equals <code>o</code>; a
	 * positive integer if this node is greater than
	 * <code>o</code>.
	 * @throws ClassCastException if <code>o</code> is not a
	 * <code>Node</code>.
	 */
	// Compare this node to another node.
	public int compareTo(Node o)
	{
		throw new UnsupportedOperationException("compareTo");
//	    return data.compareTo(((Node) o).data);
	}

	/**
	 * Returns the <code>data</code> instance variable of this node
	 * as a <code>String</code>.
	 */
	public String toString()
	{
	    if (this == nil)
		return "nil";
	    else
		return row + "";
	}

	/**
	 * Returns a multiline <code>String</code> representation of
	 * the subtree rooted at this node, representing the depth of
	 * each node by two spaces per depth preceding the
	 * <code>String</code> representation of the node.
	 *
	 * @param depth Depth of this node.
	 */
	public String toString(int depth)
	{
	    String result = "";

	    if (left != nil)
		result += left.toString(depth + 1);

	    for (int i = 0; i < depth; i++)
		result += "  ";

	    result += toString() + "\n";

	    if (right != nil)
		result += right.toString(depth + 1);

	    return result;
	}
    }

    /**
     * Sets the sentinel <code>nil</code> to a given node.
     *
     * @param node The node that <code>nil</code> is set to.
     */
    protected void setNil(Node node)
    {
	nil = node;
	nil.parent = nil;
	nil.left = nil;
	nil.right = nil;
    }

    /**
     * Creates a binary search tree with just a <code>nil</code>,
     * which is the root.
     */
    public BinarySearchTree()
    {
	setNil(new Node(-1));
	root = nil;
    }

    /**
     * Returns <code>true</code> if the given node is the sentinel
     * <code>nil</code>, <code>false</code> otherwise.
     *
     * @param node The node that is being asked about.
     */
    public boolean isNil(Object node)
    {
	return node == nil;
    }

    /**
     * Returns a multiline <code>String</code> representation of the
     * tree, representing the depth of each node by two spaces per
     * depth preceding the <code>String</code> representation of the
     * node.
     */
    public String toString()
    {
	return root.toString(0);
    }

    /**
     * Returns the node with the minimum key in the tree.
     *
     * @return A <code>Node</code> object with the minimum key in the
     * tree, or the sentinel <code>nil</code> if the tree is empty.
     */
    public int minimum()
    {
	return treeMinimum(root);
    }

    /**
     * Returns the node with the minimum key in the subtree rooted at
     * a node.
     *
     * @param x Root of the subtree.
     * @return A <code>Node</code> object with the minimum key in the
     * tree, or the sentinel <code>nil</code> if the tree is empty.
     */
    protected int treeMinimum(Node x)
    {
	while (x.left != nil)
	    x = x.left;

	return x.row;
    }

    /**
     * Returns the row with the "maximum" interval in the index according to the {@link IntervalComparator#compare(long, long, long, long) method}.
     *
     * @return A <code>Node</code> object with the maximum key in the
     * tree, or the sentinel <code>nil</code> if the tree is empty.
     */
    public int maximum()
    {
	return treeMaximum(root);
    }

    /**
     * Returns the row node with the maximum key in the subtree rooted at
     * a node.
     *
     * @param x Root of the subtree.
     * @return A <code>Node</code> object with the maximum key in the
     * tree, or the sentinel <code>nil</code> if the tree is empty.
     */
    protected int treeMaximum(Node x)
    {
	while (x.right != nil)
	    x = x.right;

	return x.row;
    }


    /**
     * Returns the successor of a given node in an inorder walk of the
     * tree.
     *
     * @param node The node whose successor is returned.
     * @return If <code>node</code> has a successor, it is returned.
     * Otherwise, return the sentinel <code>nil</code>.
     */
    public Object successor(Object node)
    {
	Node x = (Node) node;
	
	if (x.right != nil)
	    return treeMinimum(x.right);

	Node y = x.parent;
	while (y != nil && x == y.right) {
	    x = y;
	    y = y.parent;
	}

	return y;
    }

    /**
     * Returns the predecessor of a given node in an inorder walk of
     * the tree.
     *
     * @param node The node whose predecessor is returned.
     * @return If <code>node</code> has a predecessor, it is returned.
     * Otherwise, return the sentinel <code>nil</code>.
     */
    public Object predecessor(Object node)
    {
	Node x = (Node) node;

	if (x.left != nil)
	    return treeMaximum(x.left);

	Node y = x.parent;
	while (y != nil && x == y.left) {
	    x = y;
	    y = y.parent;
	}

	return y;
    }
    
    /**
     * Inserts data into the tree, creating a new node for this data.
     *
     * @param data Data to be inserted into the tree.
     * @return A reference to the <code>Node</code> object created.
     * The <code>Node</code> class is opaque to methods outside this
     * class.
     */
    public Object insert(int row)
    {
	Node z = new Node(row);
	treeInsert(z);

	return z;
    }

    /**
     * Inserts a node into the tree.
     *
     * @param z The node to insert.
     */
    protected void treeInsert(Node z)
    {
	Node y = nil;
	Node x = root;

	while (x != nil) {
	    y = x;
	    if (z.compareTo(x) <= 0)
		x = x.left;
	    else
		x = x.right;
	}

	z.parent = y;
	if (y == nil)
	    root = z;		// the tree had been empty
	else {
	    if (z.compareTo(y) <= 0)
		y.left = z;
	    else
		y.right = z;
	}
    }
}

// $Id: BinarySearchTree.java,v 1.1 2003/10/14 16:56:20 thc Exp $
// $Log: BinarySearchTree.java,v $
// Revision 1.1  2003/10/14 16:56:20  thc
// Initial revision.
//
