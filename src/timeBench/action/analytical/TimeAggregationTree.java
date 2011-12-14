package timeBench.action.analytical;

import java.util.ArrayList;
import java.util.HashMap;

import timeBench.calendar.CalendarManager;
import timeBench.calendar.CalendarManagerFactory;
import timeBench.calendar.CalendarManagers;
import timeBench.calendar.Granularity;
import timeBench.calendar.Granule;
import timeBench.data.TemporalDataException;
import timeBench.data.oo.AnchoredTemporalElement;
import timeBench.data.oo.TemporalDataset;
import timeBench.data.oo.TemporalElement;
import timeBench.data.oo.TemporalObject;
import timeBench.data.relational.TemporalDatasetProvider;

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
public class TimeAggregationTree extends prefuse.action.Action implements TemporalDatasetProvider {
	timeBench.data.relational.TemporalDataset sourceDataset;
	timeBench.data.relational.TemporalDataset workingDataset;
	timeBench.data.oo.TemporalDataset temporalDataset;
	CalendarManager calendarManager;
	Granularity[] granularities;	
	double[] minValue;
	double[] maxValue;
	Double missingValueIdentifier;

	/**
	 * @param data
	 * @param temporalDataset
	 * @param columnsUsed 
	 */
	public TimeAggregationTree(timeBench.data.relational.TemporalDataset sourceDataset, CalendarManagers calendarManager,GranularityAggregationSettings[] granularities, Double missingValueIdentifier) {
		this.sourceDataset = sourceDataset;
		this.calendarManager = CalendarManagerFactory.getSingleton(calendarManager);
		this.granularities = new Granularity[granularities.length];
		for(int i=0; i<granularities.length; i++) {
			this.granularities[i] = new Granularity(this.calendarManager.getDefaultCalendar(),granularities[i].getIdentifier(),granularities[i].getContextIdentifier());
		}
		this.missingValueIdentifier = missingValueIdentifier;
	}

	/* (non-Javadoc)
	 * @see prefuse.action.Action#run(double)
	 */
	@Override
	public void run(double frac) {
		try {
			workingDataset = (timeBench.data.relational.TemporalDataset)sourceDataset.clone();
			temporalDataset = new TemporalDataset(workingDataset);
		} catch (TemporalDataException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Levels: Granularity / Granules of this Granularity / TemporalObjects in this Granule
		ArrayList<HashMap<Long,ArrayList<TemporalObject>>> bins = new ArrayList<HashMap<Long,ArrayList<TemporalObject>>>();		
		bins.add(new HashMap<Long,ArrayList<TemporalObject>>());
		bins.get(0).put(new Long(0),temporalDataset.getTemporalObjects());
		int lastBin = 0;

		minValue = new double[temporalDataset.getSourceData().getDataElements().getColumnCount()];
		maxValue = new double[minValue.length];
		for(int i=0; i<minValue.length;i++) {
			minValue[i] = Double.MAX_VALUE;
			maxValue[i] = Double.MIN_VALUE;
		}

		for(int i=granularities.length-1; i>=0;i--) {
			bins.add(new HashMap<Long,ArrayList<TemporalObject>>());
			for(ArrayList<TemporalObject> iGranuleBin : bins.get(lastBin).values()) {
				for(TemporalObject iTemporalObject : iGranuleBin) {
					TemporalElement temporalElement = iTemporalObject.getTemporalElement();
					if (temporalElement instanceof AnchoredTemporalElement) {
						Granule granule = null;
						try {
							granule = new Granule(((AnchoredTemporalElement)temporalElement).lifeSpan().getInf(),
									((AnchoredTemporalElement)temporalElement).lifeSpan().getSup(),granularities[i]);
							long granuleIdentifier = granule.getIdentifier();
							if(!bins.get(lastBin+1).containsKey(granuleIdentifier))
								bins.get(lastBin+1).put(granuleIdentifier,new ArrayList<TemporalObject>());
							bins.get(lastBin+1).get(granuleIdentifier).add(iTemporalObject);
						} catch (TemporalDataException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}				
			}
			lastBin++;
		}
		ArrayList<HashMap<Long,TemporalObject>> aggregatedObjects = new ArrayList<HashMap<Long,TemporalObject>>();
		int aggregatedObjectsLevel = 0;
		for(int i=bins.size()-1; i>=0;i--) {
			aggregatedObjects.add(new HashMap<Long,TemporalObject>());
			for(long iKey : bins.get(i).keySet()) {
				double[] numObjects = new double[temporalDataset.getTemporalObjects().get(0).getDataAspectsSize()];
				double[] totalValue = new double[temporalDataset.getTemporalObjects().get(0).getDataAspectsSize()];
				for(TemporalObject iObject : bins.get(i).get(iKey) ) {
					double[] values = new double[iObject.getDataAspectsSize()];
					for (int j=0; j<iObject.getDataAspectsSize(); j++)
					{
						Object value = iObject.getDataAspect(j);
						if(value instanceof Double)
							values[j] = (Double)value;
						else if(value instanceof Integer)
							values[j] = ((Integer)value).doubleValue();
						else if(value instanceof Long)
							values[j] = ((Long)value).doubleValue();
						else {							
							try {
								values[j] = Double.parseDouble(value.toString());
							} finally {
								values[j] = 0;
							}
						}
						if(missingValueIdentifier != null && missingValueIdentifier != values[j]) { 
							numObjects[j]++;
							totalValue[j]+=values[j];
						}
					}
				}
				for(int j=0;j<totalValue.length;j++) {
					if (numObjects[j] != 0) {
					totalValue[j] /= numObjects[j];
					} else {
						
					}
				}
				try {
					AggregationTreeTemporalObject newObject = null;
					if (aggregatedObjectsLevel == 0)
						newObject =	new AggregationTreeTemporalObject(iKey,granularities[bins.size()-i-1],totalValue,bins.get(i).get(iKey));
					else {
						ArrayList<TemporalObject> childObjects = new ArrayList<TemporalObject>();
						for(TemporalObject iObject : aggregatedObjects.get(aggregatedObjectsLevel-1).values()) {
							Granule granule = new Granule(((AnchoredTemporalElement)iObject.getTemporalElement()).lifeSpan().getInf(),
									((AnchoredTemporalElement)iObject.getTemporalElement()).lifeSpan().getSup(),granularities[bins.size()-i-1]);
							if (granule.getIdentifier() == iKey)
								childObjects.add(iObject);
						}
						newObject =	new AggregationTreeTemporalObject(iKey,granularities[bins.size()-i-1],totalValue, childObjects);
					}
					newObject.anchorRelational(datasetContainer.getWorkingDataset().getSourceData());
					aggregatedObjects.get(aggregatedObjectsLevel).put(iKey,newObject);
				} catch (TemporalDataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			aggregatedObjectsLevel++;
		}

		datasetContainer.setRoot(aggregatedObjects.get(aggregatedObjectsLevel).get(0).getRelationalTemporalObject());
	}

	/* (non-Javadoc)
	 * @see timeBench.data.relational.TemporalDatasetProvider#getTemporalDataset()
	 */
	@Override
	public timeBench.data.relational.TemporalDataset getTemporalDataset() {
		return temporalDataset;
	}
}
