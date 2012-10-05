package timeBench.data.expression;

import prefuse.data.expression.ComparisonPredicate;
import prefuse.util.collections.DefaultLiteralComparator;

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
public class TemporalComparisonPredicate extends ComparisonPredicate {
	
    public static final int BEFORE                              = 0x0101;
    public static final int AFTER                               = 0x0102;    
    public static final int STARTS                              = 0x0103;
    public static final int FINISHES                            = 0x0104;
    public static final int DURING                              = 0x0105;
    public static final int OUTSIDE                             = 0x0106;
    public static final int OVERLAPS                            = 0x0107;
    public static final int ASLONGAS                            = 0x0108;
    
    public TemporalComparisonPredicate(int operation, TemporalExpression left, TemporalExpression right) {
    	super(operation, left, right, DefaultLiteralComparator.getInstance());
    }
}
