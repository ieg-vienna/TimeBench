package org.timebench.data.expression;

import java.util.ArrayList;

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
public class TemporalElementArrayExpression extends AbstractExpression {

	private TemporalElement[] arrayBuffer;
	
	public TemporalElementArrayExpression(ArrayList<TemporalObject> source) {
		updateBuffer(source);
	}
	
	/**
	 * @param source
	 */
	public void updateBuffer(ArrayList<TemporalObject> source) {
		arrayBuffer = new TemporalElement[source.size()];
		for(int i=0; i<arrayBuffer.length;i++)
			arrayBuffer[i] = source.get(i).getTemporalElement();
	}

	/* (non-Javadoc)
	 * @see prefuse.data.expression.Expression#getType(prefuse.data.Schema)
	 */
	@Override
	public Class getType(Schema s) {
		return TemporalElement[].class;				
	}

	public Object get(Tuple t) {
		return arrayBuffer;
	}
}
