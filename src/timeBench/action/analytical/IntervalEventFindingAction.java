package timeBench.action.analytical;

import java.util.ArrayList;

import prefuse.action.Action;
import prefuse.data.Schema;
import timeBench.data.Instant;
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
			ArrayList<Instant> potentialStartingPoints = new ArrayList<Instant>();
			ArrayList<Instant> potentialEndingPoints = new ArrayList<Instant>();

			for (TemporalObject iSource : sourceDataset.temporalObjects()) {
				for (TemporalObject iTemplate : templateDataset.temporalObjects()) {
					for (int i = 0; i < potentialEvents.size(); i++) {
						if (satisfies(iSource,templateDataset.getTemporalObject(potentialEvents.get(i)))) {
							potentialEndingPoints.set(i, iSource.getTemporalElement().getLastInstant());
						} else {
							potentialEvents.remove(i);
							potentialStartingPoints.remove(i);
							i--;
						}
					}
					if (satisfies(iSource, iTemplate)) {
						potentialEvents.add(iTemplate.getId());
						potentialStartingPoints.add(iSource.getTemporalElement().getFirstInstant());
						potentialEndingPoints.add(iSource.getTemporalElement().getLastInstant());
					}
				}
			}

			for (int i = 0; i < potentialEvents.size(); i++) {
				TemporalElement element;
				element = eventDataset.addInterval(
						eventDataset.addInstant(potentialStartingPoints.get(i).getInf(),potentialStartingPoints.get(i).getSup(),
								potentialStartingPoints.get(i).getGranularityId(),potentialStartingPoints.get(i).getGranularityContextId()),
						eventDataset.addInstant(potentialEndingPoints.get(i).getInf(), potentialEndingPoints.get(i).getSup(),
								potentialEndingPoints.get(i).getGranularityId(),potentialEndingPoints.get(i).getGranularityContextId()));
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
	private boolean satisfies(TemporalObject source, TemporalObject template)
			throws TemporalDataException {
		
		for (int i : templateDataset.getDataColumnIndices()) {
			if (template.getKind(i) == 0) {			
				Object value = template.get(i);
				if (value != null) {
					if (source.getColumnType(i) != template.getColumnType(i))
						return false;
					if (!source.get(i).equals(template.get(i)))
						return false;
				}
				if (template.getMin(i) != null) {
					if (source.canGetDouble(i) && source.getDouble(i) < (double)(Double)template.getMin(i))
						return false;
					if (source.canGetFloat(i) && source.getFloat(i) < (float)(Float)template.getMin(i))
						return false;
					if (source.canGetInt(i) && source.getInt(i) < (int)(Integer)template.getMin(i))
						return false;
					if (source.canGetLong(i) && source.getLong(i) < (long)(Long)template.getMin(i))
						return false;
				}
				if (template.getMax(i) != null) {
					if (source.canGetDouble(i) && source.getDouble(i) > (double)(Double)template.getMax(i))
						return false;
					if (source.canGetFloat(i) && source.getFloat(i) > (float)(Float)template.getMax(i))
						return false;
					if (source.canGetInt(i) && source.getInt(i) > (int)(Integer)template.getMax(i))
						return false;
					if (source.canGetLong(i) && source.getLong(i) > (long)(Long)template.getMax(i))
						return false;
				}
			/*TemporalElement teTemplate = template.getTemporalElement();
			TemporalElement teSource = source.getTemporalElement();
			if (teTemplate.getKind() >= 0x10) {
				if (teTemplate.getGranularityId() != teSource.getGranularityId())
					return false;
				if (teTemplate.getGranularityContextId() != teSource.getGranularityContextId())
					return false;
				if (teTemplate.getKind() == TemporalElement.INSTANT_TEMPLATE || teTemplate.getKind() == TemporalElement.INTERVAL_TEMPLATE) {
			if (teTemplate.getGranule().getIdentifier() != Long.MIN_VALUE
					&& teTemplate.getGranule().getIdentifier() != teSource.getGranule().getIdentifier())
				return false;
			if (teTemplate.getFirstInstant().getGranule().getInf() != Long.MIN_VALUE &&
					(teTemplate.getFirstInstant().getGranule().getInf() > teSource.getFirstInstant().getGranule().getInf() ||
					teTemplate.getLastInstant().getGranule().getSup() < teSource.getLastInstant().getGranule().getSup()))
				return false;*/				
			}
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
