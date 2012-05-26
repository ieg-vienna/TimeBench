package timeBench.action.analytical;

import ieg.prefuse.data.DataHelper;

import java.util.ArrayList;

import timeBench.calendar.CalendarManager;
import timeBench.calendar.CalendarManagerFactory;
import timeBench.calendar.CalendarManagers;
import timeBench.calendar.Granularity;
import timeBench.calendar.Granule;
import timeBench.data.GenericTemporalElement;
import timeBench.data.Instant;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalDatasetProvider;
import timeBench.data.TemporalObject;
import timeBench.test.DebugHelper;

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
	TemporalDataset sourceDataset;
	TemporalDataset workingDataset;
	CalendarManager calendarManager;
	Granularity[] granularities;	
	double[][] minValues;
	double[][] maxValues;
	double missingValueIdentifier;

	/**
	 * @param data
	 * @param temporalDataset
	 * @param columnsUsed 
	 */
	public TimeAggregationTree(TemporalDataset sourceDataset, CalendarManagers calendarManager,GranularityAggregationSettings[] granularities, Double missingValueIdentifier) {
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
			workingDataset = new TemporalDataset(sourceDataset.getDataColumnSchema());
			
			workingDataset.addColumn("GranuleIdentifier", Granule.class); 
		
	        int[] dataColumnIndices = sourceDataset.getDataColumnIndices();
			int columnCount = dataColumnIndices.length;
			minValues = new double[columnCount][granularities.length+1];
			maxValues = new double[columnCount][granularities.length+1];
			for(int i=0; i<columnCount;	i++) {
				if (sourceDataset.getNodeTable().canGetDouble(dataColumnIndices[i])) {
					for(int j=0; j<=granularities.length; j++) {
						minValues[i][j] = Double.MAX_VALUE;
						maxValues[i][j] = Double.MIN_VALUE;
					}
				}
			}
			
			
			
			GenericTemporalElement te = workingDataset.addTemporalElement(sourceDataset.getInf(),sourceDataset.getSup(),
					0, 32767, TemporalDataset.PRIMITIVE_INTERVAL);
			TemporalObject root = workingDataset.addTemporalObject(te);

			ArrayList<ArrayList<TemporalObject>> currentLeaves = new ArrayList<ArrayList<TemporalObject>>();
			currentLeaves.add(new ArrayList<TemporalObject>());
			for(TemporalObject iO : sourceDataset.temporalObjects()) {
				currentLeaves.get(0).add(iO);
			}
			ArrayList<TemporalObject> currentBranches = new ArrayList<TemporalObject>();
			currentBranches.add(root);
			for(int i=granularities.length-1; i>=0;i--) {
				ArrayList<ArrayList<TemporalObject>> futureLeaves = new ArrayList<ArrayList<TemporalObject>>();
				ArrayList<TemporalObject> futureBranches = new ArrayList<TemporalObject>(); 
				for(int k=0; k<currentLeaves.size();k++) {
					ArrayList<TemporalObject> iCurrentLeaves = currentLeaves.get(k);
					while(iCurrentLeaves.size() > 0) {
						TemporalObject currentLeave = iCurrentLeaves.get(iCurrentLeaves.size()-1);
						iCurrentLeaves.remove(iCurrentLeaves.size()-1);
						long inf = currentLeave.getTemporalElement().asGeneric().getInf();
						long sup = currentLeave.getTemporalElement().asGeneric().getSup();
						TemporalObject targetBranch = null;
					    for(int j=futureBranches.size()-1;j>=0;j--) {
					    	TemporalObject potentialBranch = futureBranches.get(j);
					    	if (potentialBranch.getTemporalElement().asGeneric().getInf() <= inf &&
					    			potentialBranch.getTemporalElement().asGeneric().getSup() >= sup) {
					    		targetBranch = potentialBranch;
					    	    break;
					    	}
					    }
					    if (targetBranch == null) {
					    	Granule newGranule = new Granule(inf,sup,granularities[i]); 
					    	Instant newTe = workingDataset.addInstant(newGranule);
					    	targetBranch = workingDataset.addTemporalObject(newTe);
					    	targetBranch.set("GranuleIdentifier", newGranule);
					    	futureBranches.add(targetBranch);
					    	futureLeaves.add(new ArrayList<TemporalObject>());					    	
					    	currentBranches.get(k).linkWithChild(targetBranch);
					    }
				    	futureLeaves.get(futureLeaves.size()-1).add(currentLeave);
					}
				}
				if(i==0) {
					for(int j=0; j<futureBranches.size(); j++ ) {
						aggregate(futureBranches.get(j),futureLeaves.get(j),granularities.length);
					}
				} else {
					currentBranches = futureBranches;
					currentLeaves = futureLeaves;
				}
			}
							
			aggregate(root,0);
			//DataHelper.printTable(System.out, workingDataset.getNodeTable());
			DebugHelper.printTemporalDatasetGraph(System.out, root,"value");
		
			workingDataset.setRoots(new long[] { root.getId() } );
		} catch (TemporalDataException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	
	

	private void aggregate(TemporalObject parent,int level) {
	    if (parent.getChildCount() > 0) {
	    	for (TemporalObject child : parent.childObjects()) {
	    		aggregate(child,level+1);
	    	}
	    	aggregate(parent,parent.childObjects(),level);
	    }
	}
	private void aggregate(TemporalObject parent,Iterable<TemporalObject> childs,int level) {
		int[] dataColumnIndices = sourceDataset.getDataColumnIndices();
		double[] numObjects = new double[dataColumnIndices.length]; 
		double[] totalValue = new double[dataColumnIndices.length]; 
		for(int i=0; i<dataColumnIndices.length; i++) {
			numObjects[i] = 0;
			totalValue[i] = 0;
		}

        for (TemporalObject temporalObject : childs) {
			for(int j=0; j<dataColumnIndices.length; j++) {
				if(temporalObject.canGetDouble(dataColumnIndices[j])) {
					double value = temporalObject.getDouble(dataColumnIndices[j]);
					if (!Double.isNaN(value) && value != missingValueIdentifier) {
						totalValue[j] += value;
						numObjects[j]++;
						if (level <= granularities.length ) {
							minValues[j][level] = Math.min(minValues[j][level], value);
							maxValues[j][level] = Math.max(maxValues[j][level], value);
						}
					}
				}
			}
		}
		
		for(int i=0; i<dataColumnIndices.length; i++) {
			totalValue[i] /= numObjects[i];
			parent.setDouble(dataColumnIndices[i],totalValue[i]);
		}
	}

	/* (non-Javadoc)
	 * @see timeBench.data.relational.TemporalDatasetProvider#getTemporalDataset()
	 */
	@Override
	public TemporalDataset getTemporalDataset() {
		return workingDataset;
	}

	@Override
	public Double getMinValue(int level,int index) {
		if(minValues == null || minValues.length <= index)
			return null;
		else {
			if(minValues[index][level] == Double.MAX_VALUE)
				return Double.NaN;
			else
				return minValues[index][level];
		}
			
	}

	@Override
	public Double getMaxValue(int level,int index) {
		if(maxValues == null || maxValues.length <= index)
			return null;
		else {
			if(minValues[index][level] == Double.MAX_VALUE)
				return Double.NaN;
			else
				return maxValues[index][level];
		}
	}
}
