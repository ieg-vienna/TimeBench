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

/**
 * Implements an interval whose endpoints are real numbers.
 */

public interface IntervalComparator {

    /**
     *
     * @param i The other interval.
     * @return <code>true</code> if this interval overlaps
     * <code>i</code>, <code>false</code> otherwise.
     */

	/**
	 * Returns whether two intervals overlap.
	 * @param lo1 the lower end of the 1st interval 
	 * @param hi1 the lower end of the 1st interval
	 * @param lo2 the upper end of the 2nd interval
	 * @param hi2 the upper end of the 2nd interval
	 */
	boolean match(long lo1, long hi1, long lo2, long hi2);

	int compare(long lo1, long hi1, long lo2, long hi2);
}