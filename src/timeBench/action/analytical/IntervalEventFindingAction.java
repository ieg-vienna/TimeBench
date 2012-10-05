package timeBench.action.analytical;

import java.util.ArrayList;

import prefuse.action.Action;
import prefuse.data.Schema;
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
	TemporalDataset templateDataset;
	TemporalDataset eventDataset;

	Schema eventSchema = new Schema(new String[] { "class", "label" },
			new Class[] { Long.class, String.class });

	public IntervalEventFindingAction(TemporalDataset sourceDataset,
			TemporalDataset templateDataset) {
		this.sourceDataset = sourceDataset;
		this.templateDataset = templateDataset;
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

			ArrayList<Long> potentialEvents = new ArrayList<Long>();
			ArrayList<ArrayList<TemporalObject>> potentialObjectLists = new ArrayList<ArrayList<TemporalObject>>();

			for (TemporalObject iSource : sourceDataset.temporalObjects()) {
				for (TemporalObject iTemplate : templateDataset.temporalObjects()) {
					for (int i = 0; i < potentialEvents.size(); i++) {
						if (satisfies(potentialObjectLists.get(i),iSource,templateDataset.getTemporalObject(potentialEvents.get(i)))) {
							potentialObjectLists.get(i).add(iSource);
						} else {
							potentialEvents.remove(i);
							potentialObjectLists.remove(i);
							i--;
						}
					}
					ArrayList<TemporalObject> newList = new ArrayList<TemporalObject>();
					newList.add(iSource);
					if (satisfies(newList, iSource, iTemplate)) {
						potentialEvents.add(iTemplate.getId());
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
	private boolean satisfies(ArrayList<TemporalObject> checkedObjects,TemporalObject source, TemporalObject template)
			throws TemporalDataException {
		
		Object o = template.get(TemporalObject.PREDICATES_COLUMN);
		if (o instanceof Predicate) {
			if(!((Predicate)o).getBoolean(source))
				return false;
		}
		if (o instanceof Predicate[]) {
			for(Predicate iP : (Predicate[])o)
			if(!iP.getBoolean(source))
				return false;
		}
		
		TemporalElement teTemplate = template.getTemporalElement();
		TemporalElement teStart = checkedObjects.get(0).getTemporalElement();
		TemporalElement teSource = source.getTemporalElement();
		switch(teTemplate.getKind()) {
		case TemporalElement.TEMPLATE_AFTER_INSTANT_INSTANT:
		case TemporalElement.TEMPLATE_AFTER_INTERVAL_INSTANT:
		case TemporalElement.TEMPLATE_AFTER_INTERVAL_INTERVAL:
			if (teStart.getFirstInstant().getInf() <= teTemplate.getLastInstant().getSup())
				return false;
			break;
		case TemporalElement.TEMPLATE_ASLONGAS_INTERVAL_SPAN:
		case TemporalElement.TEMPLATE_ASLONGAS_RECURRING_INTERVAL_SPAN:
			if(!(teTemplate instanceof Span))
				return false;
			if (((Span)teTemplate).getLength() != teSource.asGeneric().getSup()-teStart.asGeneric().getInf()+1)
				return false;
			break;
		case TemporalElement.TEMPLATE_ASLONGAS_SPAN_SPAN:
			if(!(teTemplate instanceof Span))
				return false;
			if(!(teTemplate instanceof Span))
				return false;
			long totalLength = 0;
			for(TemporalObject iO : checkedObjects) {
				if (!(iO.getTemporalElement().asPrimitive() instanceof Span))
					return false;
				totalLength += ((Span)iO.getTemporalElement().asPrimitive()).getLength();				
			}				
			if (((Span)teTemplate).getLength() != totalLength)
				return false;
			break;
		case TemporalElement.TEMPLATE_BEFORE_INSTANT_INSTANT:
		case TemporalElement.TEMPLATE_BEFORE_INTERVAL_INSTANT:
		case TemporalElement.TEMPLATE_BEFORE_INTERVAL_INTERVAL:
			if(teSource.getLastInstant().getSup() >= teTemplate.getFirstInstant().getInf())
				return false;
			break;
		case TemporalElement.TEMPLATE_DURING_INSTANT_INSTANT:
		case TemporalElement.TEMPLATE_DURING_INTERVAL_INTERVAL:
			if(teStart.getFirstInstant().getInf() < teTemplate.getFirstInstant().getInf())
				return false;
			if(teSource.getLastInstant().getSup() > teTemplate.getLastInstant().getSup())
				return false;
			break;
		case TemporalElement.TEMPLATE_DURING_RECURRING_INTERVAL_RECURRING_INTERVAL:	{
			Granularity g = teTemplate.getGranule().getGranularity();
			for(TemporalObject iO : checkedObjects) {
				if (g.getIdentifier() != iO.getTemporalElement().getGranule().getGranularity().getIdentifier() ||
						g.getGranularityContextIdentifier() != iO.getTemporalElement().getGranule().getGranularity().getGranularityContextIdentifier())
					return false;
			}
			long inf = checkedObjects.get(0).getTemporalElement().getFirstInstant().getInf();
			long sup = checkedObjects.get(checkedObjects.size()-1).getTemporalElement().getLastInstant().getSup();
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
			break;
		}
		case TemporalElement.TEMPLATE_FINISHES_INSTANT_INSTANT:
		case TemporalElement.TEMPLATE_FINISHES_INTERVAL_INSTANT:
		case TemporalElement.TEMPLATE_FINISHES_INTERVAL_INTERVAL:
			if (teTemplate.getLastInstant().getSup() != teSource.getLastInstant().getSup())
				return false;
			break;
		case TemporalElement.TEMPLATE_FINISHES_RECURRING_INSTANT_RECURRING_INSTANT:
		case TemporalElement.TEMPLATE_FINISHES_RECURRING_INTERVAL_RECURRING_INSTANT:
			Granularity g = teTemplate.getGranule().getGranularity();
			for(TemporalObject iO : checkedObjects) {
				if (g.getIdentifier() != iO.getTemporalElement().getGranule().getGranularity().getIdentifier() ||
						g.getGranularityContextIdentifier() != iO.getTemporalElement().getGranule().getGranularity().getGranularityContextIdentifier())
					return false;
			}
			long sup = checkedObjects.get(checkedObjects.size()-1).getTemporalElement().getLastInstant().getSup();
			if (new Granule(sup,sup,Granule.MODE_SUP_GRANULE,g).getIdentifier() != teTemplate.getGranule().getIdentifier())
				return false;
			break;
		case TemporalElement.TEMPLATE_OUTSIDE_INSTANT_INSTANT:
			break;
		}		

		return true;
	}

	/**
	 * @return the eventDataset
	 */
	public TemporalDataset getEventDataset() {
		return eventDataset;
	}
}
