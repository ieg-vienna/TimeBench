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

// TODO javadoc ????
/**
 * Implements an interval whose endpoints are real numbers.
 */

public class DefaultIntervalComparator implements IntervalComparator {

	public boolean match(long lo1, long hi1, long lo2, long hi2) {
    	return lo1 <= hi2 && lo2 <= hi1;
	}

	@Override
	public int compare(long lo1, long hi1, long lo2, long hi2) {
		if (lo1 < lo2)
			return -1;
		else if (lo1 == lo2)
		    // TODO sort by hi, if lo are equal?
			return 0;
		else
			return 1;
	}
}