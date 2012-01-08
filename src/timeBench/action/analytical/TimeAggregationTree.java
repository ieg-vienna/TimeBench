package timeBench.action.analytical;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import timeBench.calendar.CalendarManager;
import timeBench.calendar.CalendarManagerFactory;
import timeBench.calendar.CalendarManagers;
import timeBench.calendar.Granularity;
import timeBench.calendar.Granule;
import timeBench.data.TemporalDataException;
import timeBench.data.oo.AnchoredTemporalElement;
import timeBench.data.oo.Interval;
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
public class TimeAggregationTree extends prefuse.action.Action implements TemporalDatasetProvider, MinMaxValuesProvider {
	timeBench.data.relational.TemporalDataset sourceDataset;
	timeBench.data.relational.TemporalDataset workingDataset;
	timeBench.data.oo.TemporalDataset temporalDataset;
	CalendarManager calendarManager;
	Granularity[] granularities;	
	double[] minValues;
	double[] maxValues;
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
			workingDataset = sourceDataset;
			temporalDataset = new TemporalDataset(workingDataset);
		
			int columnCount = sourceDataset.getDataElements().getColumnCount();
			minValues = new double[columnCount];
			maxValues = new double[columnCount];
			for(int i=0; i<columnCount; i++) {
				minValues[i] = Double.MAX_VALUE;
				maxValues[i] = Double.MIN_VALUE;
			}
			
			AnchoredTemporalElement te = (AnchoredTemporalElement)temporalDataset.getTemporalElement();
			TemporalObject root = new TemporalObject(new Interval(te.getInf(),te.getSup(),te.getGranularity()),new ArrayList<Object>());
			root.getSubObjects().addAll(temporalDataset.getSubObjects());	// only works because we are not relationally anchored yet

			ArrayList<TemporalObject> workingList = new ArrayList<TemporalObject>();
			workingList.add(root);
			for(int i=granularities.length-1; i>=0;i--) {
				ArrayList<TemporalObject> futureWorkingList = new ArrayList<TemporalObject>();
				for(TemporalObject iWorkingObject : workingList) {
					Hashtable<Long,ArrayList<TemporalObject>> hashtable = new Hashtable<Long,ArrayList<TemporalObject>>(); 
					while(iWorkingObject.getSubObjects().size() > 0) {
						TemporalElement temporalElement = iWorkingObject.getSubObjects().get(0).getTemporalElement();
						if (temporalElement instanceof AnchoredTemporalElement) {
							Granule granule = null;
							try {
								granule = new Granule(((AnchoredTemporalElement)temporalElement).lifeSpan().getInf(),
										((AnchoredTemporalElement)temporalElement).lifeSpan().getSup(),granularities[i]);
								long granuleIdentifier = granule.getIdentifier();
								if(!hashtable.containsKey(granuleIdentifier))
									hashtable.put(granuleIdentifier,new ArrayList<TemporalObject>());
								hashtable.get(granuleIdentifier).add(iWorkingObject.getSubObjects().get(0));
							} catch (TemporalDataException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							iWorkingObject.getSubObjects().remove(0);
						}
					}
					for(long iKey : hashtable.keySet()) {
						Granule g = new Granule(iKey,granularities[i]);
						TemporalObject newObject = new TemporalObject(new Interval(g.getInf(),g.getSup(),granularities[i]),new ArrayList<Object>());
						newObject.getSubObjects().addAll(hashtable.get(iKey));
						futureWorkingList.add(newObject);
						iWorkingObject.addSubObject(newObject);
					}
				}
				workingList = futureWorkingList;
				//System.err.print(i+": " );
				//for(int j=0; j<workingList.size();j++)
					//System.err.print(workingList.get(j).getSubObjects().size()+"/");
				//System.err.println();
			}
			
			aggregate(root);
	
			//System.err.println();
			
			int rootIndex = root.anchorRelational(workingDataset);
			workingDataset.setRoots(new int[]{rootIndex});
			
			//System.err.println();
			
		} catch (TemporalDataException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private void aggregate(TemporalObject fromHere)
	{		
		double[] numObjects = null; 
		double[] totalValue = null; 

		//if(fromHere.getTemporalElement().getGranularity().getIdentifier() > 4)
//			System.err.print(fromHere.getTemporalElement().getGranularity().getIdentifier() + "-" + 
					//fromHere.getSubObjects().size() + " / ");
		
		if(fromHere.getSubObjects().size() > 0) {
			for(TemporalObject iObject : fromHere.getSubObjects()) {
				aggregate(iObject);
				double[] values = new double[iObject.getDataAspectsSize()];
				if (numObjects == null)
					numObjects = new double[iObject.getDataAspectsSize()];
				if (totalValue == null)
					totalValue = new double[iObject.getDataAspectsSize()];
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
						minValues[j] = Math.min(minValues[j], values[j]);
						maxValues[j] = Math.max(maxValues[j], values[j]);
						numObjects[j]++;
						totalValue[j]+=values[j];
					}					
				}
			}

			for(int j=0;j<totalValue.length;j++) {
				if (numObjects[j] != 0) {
					totalValue[j]/=numObjects[j];
				} else {
					if (missingValueIdentifier != null )
						totalValue[j] = missingValueIdentifier;
					else
						totalValue[j] = Double.NaN;
				}
				fromHere.setDataAspect(j, totalValue[j]);
			}		
		}
	}

	/* (non-Javadoc)
	 * @see timeBench.data.relational.TemporalDatasetProvider#getTemporalDataset()
	 */
	@Override
	public timeBench.data.relational.TemporalDataset getTemporalDataset() {
		return workingDataset;
	}

	@Override
	public Double getMinValue(int index) {
		if(minValues == null || minValues.length <= index)
			return null;
		else
			return minValues[index];
	}

	@Override
	public Double getMaxValue(int index) {
		if(maxValues == null || maxValues.length <= index)
			return null;
		else
			return maxValues[index];	}
}
