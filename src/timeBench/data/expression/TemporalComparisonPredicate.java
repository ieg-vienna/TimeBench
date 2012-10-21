package timeBench.data.expression;

import java.util.ArrayList;

import prefuse.data.Schema;
import prefuse.data.Tuple;
import prefuse.data.event.ExpressionListener;
import prefuse.data.expression.BinaryExpression;
import prefuse.data.expression.ComparisonPredicate;
import prefuse.data.expression.ExpressionVisitor;
import prefuse.data.expression.Predicate;
import prefuse.util.collections.DefaultLiteralComparator;
import timeBench.calendar.Granularity;
import timeBench.calendar.Granule;
import timeBench.data.Span;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalElement;
import timeBench.data.TemporalObject;

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
public class TemporalComparisonPredicate extends BinaryExpression implements Predicate {
	
    public static final int BEFORE                              = 0x0101;
    public static final int AFTER                               = 0x0102;    
    public static final int STARTS                              = 0x0103;
    public static final int FINISHES                            = 0x0104;
    public static final int DURING                              = 0x0105;
    public static final int OUTSIDE                             = 0x0106;
    public static final int OVERLAPS                            = 0x0107;
    public static final int ASLONGAS                            = 0x0108;
       
    public TemporalComparisonPredicate(int operation, TemporalExpression left, TemporalExpression right) {
    	super(operation,Integer.MIN_VALUE,Integer.MAX_VALUE,left,right);
    }

	/* (non-Javadoc)
	 * @see prefuse.data.expression.Expression#getType(prefuse.data.Schema)
	 */
	@Override
	public Class getType(Schema s) {
		return boolean.class;
	}
	
    @SuppressWarnings("unchecked")
	public boolean getBoolean(Tuple t) {    	    
    	
    	try {
    	
    	if (TemporalElement.class.isAssignableFrom(m_left.getClass()))
    		throw new IllegalArgumentException("Operation only permitted on TemporalElement,TemporalElement or TemporalElement,ArrayList<TemporalElement>");   	
		TemporalElement teTemplate = (TemporalElement)m_left.get(t);
		
        ArrayList<TemporalElement> history = null;
    	if (ArrayList.class.isAssignableFrom(m_right.getClass())) {
    		history = (ArrayList<TemporalElement>)m_right.get(t);
    	} else if(TemporalElement.class.isAssignableFrom(m_right.getClass())) {
    		history = new ArrayList<TemporalElement>();
    		history.add((TemporalElement)m_right.get(t));
    	} else
    		throw new IllegalArgumentException("Operation only permitted on TemporalElement,TemporalElement or TemporalElement,ArrayList<TemporalElement>");
    	TemporalElement teStart = history.get(0);
    	TemporalElement teEnd = history.get(history.size()-1);
				
		switch(m_op) {
		case BEFORE:
			if(teEnd.getLastInstant().getSup() >= teTemplate.getFirstInstant().getInf())
				return false;
			break;
		case AFTER:
			if (teStart.getFirstInstant().getInf() <= teTemplate.getLastInstant().getSup())
				return false;
			break;
		case STARTS:
			if (teTemplate.getKind() == TemporalElement.RECURRING_INSTANT || teTemplate.getKind() == TemporalElement.RECURRING_INTERVAL) {
			  if(teTemplate.getGranules()[0].getIdentifier() != teStart.getGranules()[0].getIdentifier())
				 return false;
			  if (teEnd.getGranules()[teEnd.getGranules().length-1].getIdentifier()
					  > teTemplate.getGranules()[teTemplate.getGranules().length-1].getIdentifier())
						return false;
			} else {			
				if (teTemplate.getFirstInstant().getInf() != teStart.getFirstInstant().getInf() ||
						teEnd.getLastInstant().getSup() > teTemplate.getLastInstant().getSup())
					return false;
			}
			break;
		case FINISHES:
			if (teTemplate.getKind() == TemporalElement.RECURRING_INSTANT || teTemplate.getKind() == TemporalElement.RECURRING_INTERVAL) {
				if(teTemplate.getGranules()[teTemplate.getGranules().length-1].getIdentifier() != 
						teEnd.getGranules()[teEnd.getGranules().length-1].getIdentifier())
					return false;
				if (teStart.getGranules()[0].getIdentifier() < teTemplate.getGranules()[0].getIdentifier())
					return false;
			} else {			
				if (teTemplate.getLastInstant().getSup() != teEnd.getLastInstant().getSup() ||
						teStart.getFirstInstant().getInf() < teTemplate.getFirstInstant().getInf())
					return false;
			}
			break;
		case DURING:
			if (teTemplate.getKind() == TemporalElement.RECURRING_INSTANT || teTemplate.getKind() == TemporalElement.RECURRING_INTERVAL) {
				Granularity g = teTemplate.getGranule().getGranularity();
				for(TemporalElement iTe : history) {
					if (g.getIdentifier() != iTe.getGranule().getGranularity().getIdentifier() ||
							g.getGranularityContextIdentifier() != iTe.getGranule().getGranularity().getGranularityContextIdentifier())
						return false;
				}
				long inf = history.get(0).getFirstInstant().getInf();
				long sup = history.get(history.size()-1).getLastInstant().getSup();
				Granule[] possible = g.createGranules(teTemplate.getFirstInstant().getInf(), teTemplate.getLastInstant().getSup());
				for(Granule iG : g.createGranules(inf, sup)) {
					boolean found = false;
					for(Granule iG2 : possible) {
						if ( iG.getIdentifier() == iG2.getIdentifier()) {
							found = true;
							break;
						}					
					}
					if (!found)
						return false;
				}				
			} else {
				if(teStart.getFirstInstant().getInf() < teTemplate.getFirstInstant().getInf())
					return false;
				if(teEnd.getLastInstant().getSup() > teTemplate.getLastInstant().getSup())
					return false;
			}
			break;
		case OUTSIDE:
			if (teTemplate.getKind() == TemporalElement.RECURRING_INSTANT || teTemplate.getKind() == TemporalElement.RECURRING_INTERVAL) {
				Granule g = teTemplate.getGranule();
				Granularity gy = g.getGranularity();
				for(TemporalElement iTe : history) {
					if (gy.getIdentifier() != iTe.getGranule().getGranularity().getIdentifier() ||
							gy.getGranularityContextIdentifier() != iTe.getGranule().getGranularity().getGranularityContextIdentifier() ||
						g.getIdentifier() == iTe.getGranule().getIdentifier())
						return false;					
				}
			} else {
				if (!(history.get(0).getFirstInstant().getInf() >= teTemplate.getLastInstant().getSup() ||
						history.get(history.size()-1).getLastInstant().getSup() <= teTemplate.getFirstInstant().getInf()))
					return false;
			}
			break;
		case OVERLAPS:
			if (teTemplate.getKind() == TemporalElement.RECURRING_INSTANT || teTemplate.getKind() == TemporalElement.RECURRING_INTERVAL) {
			} else {
				
			}
			break;
		case ASLONGAS:
			if(!(teTemplate instanceof Span))
				return false;
			long total = 0;
			for(TemporalElement iTe : history) {
				total += (iTe instanceof Span) ? ((Span)iTe).getLength() : iTe.getLastInstant().getSup()-iTe.getFirstInstant().getInf() + 1;
			}
			if (((Span)teTemplate).getLength() != total)
				return false;
			break;
		}		

    	} catch (TemporalDataException e) {
    		throw new UnsupportedOperationException("Illegal handling of time-oriented data: "+e.getMessage());
    	}
    	
		return true;    	
    }
}
