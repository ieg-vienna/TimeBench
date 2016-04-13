package org.timebench.data.expression;

import org.timebench.data.TemporalElement;

import prefuse.data.Schema;
import prefuse.data.expression.AbstractExpression;

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
public abstract class TemporalExpression extends AbstractExpression {

	/* (non-Javadoc)
	 * @see prefuse.data.expression.Expression#getType(prefuse.data.Schema)
	 */
	@Override
	public Class getType(Schema s) {
		return TemporalElement.class;
	}
	
	public void destroyTemporaryTemporalElements() {		
	}
}
