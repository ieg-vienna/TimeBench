package timeBench.action.analytical;

import java.util.ArrayList;

import prefuse.action.Action;
import prefuse.data.Schema;
import prefuse.data.expression.BinaryExpression;
import prefuse.data.expression.CompositePredicate;
import prefuse.data.expression.Expression;
import prefuse.data.expression.Predicate;
import timeBench.calendar.Granularity;
import timeBench.calendar.Granule;
import timeBench.calendar.JavaDateCalendarManager;
import timeBench.data.Instant;
import timeBench.data.Span;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalElement;
import timeBench.data.TemporalObject;
import timeBench.data.expression.TemporalElementArrayExpression;

/**
 * 
 * 
 * <p>
 * Added: / TL<br>
 * Modifications:
 * </p>
 * 
 * @author Tim Lammarsch
 * 
 */
public class IntervalEventFindingAction extends Action {

	TemporalDataset sourceDataset;
	Predicate[] templates;
	TemporalDataset eventDataset;
	int coherenceSettings;
	
	public static int ONLY_COHERENT 		  = 0;
	public static int SPACING_ALLOWED		  = 1;
	public static int OVERLAP_ALLOWED 		  = 2;
	public static int SPACING_OVERLAP_ALLOWED = 3; // 1&2

	Schema eventSchema = new Schema(new String[] { "class", "label" },
			new Class[] { Long.class, String.class });

	public IntervalEventFindingAction(TemporalDataset sourceDataset,Predicate[] templates) {
		this(sourceDataset,templates,ONLY_COHERENT);
	}
	
	public IntervalEventFindingAction(TemporalDataset sourceDataset,Predicate[] templates,int coherenceSettings) {
		this.sourceDataset = sourceDataset;
		this.templates= templates;
		this.coherenceSettings = coherenceSettings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see prefuse.action.Action#run(double)
	 */
	@Override
	public void run(double frac) {
		try {
			eventDataset = new TemporalDataset(eventSchema);

			ArrayList<Integer> potentialEvents = new ArrayList<Integer>();
			ArrayList<ArrayList<TemporalObject>> potentialObjectLists = new ArrayList<ArrayList<TemporalObject>>();

			for (TemporalObject iSource : sourceDataset.temporalObjects()) {				
				for (int j=0; j<templates.length; j++) {
					Predicate iTemplate = templates[j];
					for (int i = 0; i < potentialEvents.size(); i++) {
						/*if (satisfies(potentialObjectLists.get(i),templateDataset.getTemporalObject(potentialEvents.get(i)))) {
							potentialObjectLists.get(i).add(iSource);
						} else {
							potentialEvents.remove(i);
							potentialObjectLists.remove(i);
							i--;
						}*/
					}
					ArrayList<TemporalObject> newList = new ArrayList<TemporalObject>();
					newList.add(iSource);
					if (satisfies(newList, iTemplate)) {
						potentialEvents.add(j);
						potentialObjectLists.add(newList);
					}
				}
			}

			for (int i = 0; i < potentialEvents.size(); i++) {
				TemporalElement element;
				element = eventDataset.addInterval(
						eventDataset.addInstant(potentialObjectLists.get(i).get(0).getTemporalElement().getFirstInstant().getInf(),
								potentialObjectLists.get(i).get(0).getTemporalElement().getFirstInstant().getSup(),
								potentialObjectLists.get(i).get(0).getTemporalElement().getGranularityId(),
								potentialObjectLists.get(i).get(0).getTemporalElement().getGranularityContextId()),
								eventDataset.addInstant(potentialObjectLists.get(potentialObjectLists.size()-1).get(0).getTemporalElement().getFirstInstant().getInf(),
										potentialObjectLists.get(potentialObjectLists.size()-1).get(0).getTemporalElement().getFirstInstant().getSup(),
										potentialObjectLists.get(potentialObjectLists.size()-1).get(0).getTemporalElement().getGranularityId(),
										potentialObjectLists.get(potentialObjectLists.size()-1).get(0).getTemporalElement().getGranularityContextId()));
				TemporalObject object = eventDataset.addTemporalObject(element);
				object.setLong("class", potentialEvents.get(i));
				object.setString("label", "e" + i);
			}
		} catch (TemporalDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param iSource
	 * @param temporalObject
	 * @param instant
	 * @return
	 * @throws TemporalDataException
	 */
	private boolean satisfies(ArrayList<TemporalObject> checkedObjects, Predicate template)
			throws TemporalDataException {
		
		if (checkedObjects.size() > 1) {
			// check for coherence		
			if ( coherenceSettings != SPACING_OVERLAP_ALLOWED) {
				// check only last against others; assume this is called once for each new TemporalObject
				TemporalElement last = checkedObjects.get(checkedObjects.size()-1).getTemporalElement();
				if ( (coherenceSettings & SPACING_ALLOWED) != 0) {
					if (last.getFirstInstant().getInf() - checkedObjects.get(checkedObjects.size()-2).getTemporalElement().getLastInstant().getSup() > 1)
						return false;
				}
				if ( (coherenceSettings & OVERLAP_ALLOWED) != 0) {
					for(int i=0; i<checkedObjects.size()-1;i++) {
						if(checkedObjects.get(i).getTemporalElement().getLastInstant().getSup() >= last.getFirstInstant().getInf())
							return false;
					}
				}
			}
		}
		
		updateTemporalElementExpressions(template,checkedObjects);
		
 		return template.getBoolean(checkedObjects.get(checkedObjects.size()-1));
	}

	/**
	 * @param template
	 * @param checkedObjects
	 */
	private void updateTemporalElementExpressions(Expression template,
			ArrayList<TemporalObject> checkedObjects) {
		if(template instanceof TemporalElementArrayExpression)
			((TemporalElementArrayExpression)template).updateBuffer(checkedObjects);
		else if (template instanceof BinaryExpression) {
			updateTemporalElementExpressions(((BinaryExpression)template).getLeftExpression(), checkedObjects);
			updateTemporalElementExpressions(((BinaryExpression)template).getRightExpression(), checkedObjects);
		} else if(template instanceof CompositePredicate) {
			CompositePredicate composite = (CompositePredicate)template;
			for(int i=0; i<composite.size(); i++)
				updateTemporalElementExpressions(composite.get(i), checkedObjects);			
		}
	}

	/**
	 * @return the eventDataset
	 */
	public TemporalDataset getEventDataset() {
		return eventDataset;
	}
}
