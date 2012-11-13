package timeBench.data.expression;

import prefuse.data.Schema;
import prefuse.data.Tuple;
import prefuse.data.expression.AbstractExpression;
import timeBench.data.GenericTemporalElement;
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
public class TemporalShiftExpression extends TemporalExpression {

	private TemporalElementExpression temporalElementExpression = null;
	private TemporalElementArrayExpression temporalElementArrayExpression = null;
	
	// did not add more possibilites because of "Lego brick" idea
	// do get chronon count from granules or temporal elements, perhaps add total chronon output there
	private long shiftChronons;
	
	public TemporalShiftExpression(TemporalElementExpression temporalElementExpression,long shiftChronons) {
		this.temporalElementExpression = temporalElementExpression;
		this.shiftChronons = shiftChronons;
	}

	public TemporalShiftExpression(TemporalElementArrayExpression temporalElementArrayExpression,long shiftChronons) {
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
			return shift(((TemporalElement)temporalElementExpression.get(t)).asGeneric(),shiftChronons);
		} else {
			TemporalElement[] unshifted = (TemporalElement[])temporalElementArrayExpression.get(t);
			TemporalElement[] result = new GenericTemporalElement[unshifted.length];
			for(int i=0; i<unshifted.length;i++)
				result[i] = shift(unshifted[i].asGeneric(),shiftChronons);						
			return result;
		}						
	}

	/**
	 * @param temporalElement
	 * @return
	 */
	private TemporalElement shift(GenericTemporalElement temporalElement,long shiftChronons) {			
		TemporalElement result = TemporalElement.createOnHeap(temporalElement.getInf()+shiftChronons, temporalElement.getSup()+shiftChronons,
				temporalElement.getGranularityId(), temporalElement.getGranularityContextId(), temporalElement.getKind());
		
		for (GenericTemporalElement iChild : temporalElement.childElements())
			result.linkWithChild(shift(iChild,shiftChronons));
		
		return result;
	}
}
