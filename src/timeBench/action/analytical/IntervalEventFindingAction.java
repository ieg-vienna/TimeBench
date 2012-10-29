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
import timeBench.data.TemporalDatasetProvider;
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
public class IntervalEventFindingAction extends Action implements TemporalDatasetProvider {

	TemporalDataset sourceDataset;
	Predicate[] templates;
	TemporalDataset eventDataset;
	int coherenceSettings;
	
	public static int ONLY_COHERENT 		  = 0;
	public static int SPACING_ALLOWED		  = 1;
	public static int OVERLAP_ALLOWED 		  = 2;
	public static int SPACING_OVERLAP_ALLOWED = 3; // 1&2

	Schema eventSchema = new Schema(new String[] { "class", "label" },
			new Class[] { long.class, String.class });

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

			ArrayList<Integer> openEvents = new ArrayList<Integer>();
			ArrayList<ArrayList<TemporalObject>> openObjectLists = new ArrayList<ArrayList<TemporalObject>>();
			ArrayList<Integer> closedEvents = new ArrayList<Integer>();
			ArrayList<ArrayList<TemporalObject>> closedObjectLists = new ArrayList<ArrayList<TemporalObject>>();

			for (TemporalObject iSource : sourceDataset.temporalObjects()) {				
				for (int j=0; j<templates.length; j++) {
					Predicate iTemplate = templates[j];
					boolean satisfied = false;
					for (int i = 0; i < openEvents.size(); i++) {
						// if the next line belongs to event
						openObjectLists.get(i).add(iSource);		
						if (satisfies(openObjectLists.get(i),templates[openEvents.get(i)])) {
							satisfied = true;	// do not start new event of the same type when added
						} else {
							// otherwise, remove it again
							openObjectLists.get(i).remove(openObjectLists.get(i).size()-1);		
							// and close the event (save for later)
							closedEvents.add(openEvents.get(i));
							openEvents.remove(i);
							closedObjectLists.add(openObjectLists.get(i));
							openObjectLists.remove(i);
							i--;
						}
					}
					if (satisfied)
						break;
					ArrayList<TemporalObject> newList = new ArrayList<TemporalObject>();
					newList.add(iSource);
					// if line satisfies an event
					if (satisfies(newList, iTemplate)) {
						openEvents.add(j);
						openObjectLists.add(newList);
					}
				}
			}
			
			for (int i = 0; i < openEvents.size(); i++) {
				closedEvents.add(openEvents.get(i));
				closedObjectLists.add(openObjectLists.get(i));
			}
			
			for (int i = 0; i < closedEvents.size(); i++) {
				TemporalElement element;
				element = eventDataset.addInterval(
						eventDataset.addInstant(closedObjectLists.get(i).get(0).getTemporalElement().getFirstInstant().getInf(),
								closedObjectLists.get(i).get(0).getTemporalElement().getFirstInstant().getSup(),
								closedObjectLists.get(i).get(0).getTemporalElement().getGranularityId(),
								closedObjectLists.get(i).get(0).getTemporalElement().getGranularityContextId()),
								eventDataset.addInstant(closedObjectLists.get(i).get(closedObjectLists.get(i).size()-1).getTemporalElement().getLastInstant().getInf(),
										closedObjectLists.get(i).get(closedObjectLists.get(i).size()-1).getTemporalElement().getLastInstant().getSup(),
										closedObjectLists.get(i).get(closedObjectLists.get(i).size()-1).getTemporalElement().getGranularityId(),
										closedObjectLists.get(i).get(closedObjectLists.get(i).size()-1).getTemporalElement().getGranularityContextId()));
				TemporalObject object = eventDataset.addTemporalObject(element);
				object.setLong("class", closedEvents.get(i));
				object.setString("label", "e" + closedEvents.get(i));
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
				if ( (coherenceSettings & SPACING_ALLOWED) == 0) {
					if (last.getFirstInstant().getInf() - checkedObjects.get(checkedObjects.size()-2).getTemporalElement().getLastInstant().getSup() > 1)
						return false;
				}
				if ( (coherenceSettings & OVERLAP_ALLOWED) == 0) {
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

	/* (non-Javadoc)
	 * @see timeBench.data.TemporalDatasetProvider#getTemporalDataset()
	 */
	@Override
	public TemporalDataset getTemporalDataset() {
		return eventDataset;
	}
}
