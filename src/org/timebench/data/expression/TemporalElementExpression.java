package org.timebench.data.expression;

import org.timebench.data.TemporalElement;
import org.timebench.data.TemporalObject;

import prefuse.data.Schema;
import prefuse.data.Tuple;
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
public class TemporalElementExpression extends TemporalExpression {

	/* (non-Javadoc)
	 * @see prefuse.data.expression.Expression#getType(prefuse.data.Schema)
	 */
	@Override
	public Class getType(Schema s) {
		return TemporalElement.class;
	}
	
	public Object get(Tuple t) {
		if (t instanceof TemporalObject)
			return ((TemporalObject)t).getTemporalElement();
		else
			return null;
	}
}
