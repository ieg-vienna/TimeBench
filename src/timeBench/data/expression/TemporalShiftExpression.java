package timeBench.data.expression;

import prefuse.data.Schema;
import prefuse.data.Tuple;
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
public class TemporalShiftExpression extends AbstractExpression {

	private TemporalElementExpression temporalElementExpression = null;
	private TemporalElementArrayExpression temporalElementArrayExpression = null;
	
	public TemporalShiftExpression(TemporalElementExpression temporalElementExpression) {
		this.temporalElementExpression = temporalElementExpression;
	}

	public TemporalShiftExpression(TemporalElementArrayExpression temporalElementArrayExpression) {
		this.temporalElementArrayExpression = temporalElementArrayExpression;
	}
	
	/* (non-Javadoc)
	 * @see prefuse.data.expression.Expression#getType(prefuse.data.Schema)
	 */
	@Override
	public Class getType(Schema s) {
		if (temporalElementExpression != null)
			return TemporalElement.class;
		else
			return TemporalElement[].class;
	}

	public Object get(Tuple t) {
		if (temporalElementExpression != null) {
			TemporalElement result = (TemporalElement)temporalElementExpression.get(t);
			return result;
		} else {
			TemporalElement[] result = (TemporalElement[])temporalElementArrayExpression.get(t);
			return result;
		}						
	}
}
