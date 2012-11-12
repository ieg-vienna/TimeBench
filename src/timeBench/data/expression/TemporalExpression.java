package timeBench.data.expression;

import prefuse.data.Schema;
import prefuse.data.expression.AbstractExpression;
import timeBench.data.TemporalElement;

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
public class TemporalExpression extends AbstractExpression {

	/* (non-Javadoc)
	 * @see prefuse.data.expression.Expression#getType(prefuse.data.Schema)
	 */
	@Override
	public Class getType(Schema s) {
		return TemporalElement.class;
	}
}
