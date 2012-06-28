package timeBench.action.analytical;

import ieg.prefuse.data.DataHelper;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import timeBench.calendar.CalendarManager;
import timeBench.calendar.CalendarManagerFactory;
import timeBench.calendar.CalendarManagers;
import timeBench.calendar.Granularity;
import timeBench.calendar.Granule;
import timeBench.data.GenericTemporalElement;
import timeBench.data.GranularityAggregationTree;
import timeBench.data.GranularityAggregationTreeProvider;
import timeBench.data.Instant;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
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
public class GranularityAggregationAction extends prefuse.action.Action implements GranularityAggregationTreeProvider {
    
    private final Logger logger = Logger.getLogger(this.getClass());
    
	TemporalDataset sourceDataset;
	GranularityAggregationTree workingDataset;
	CalendarManager calendarManager;
	Granularity[] granularities;	
	double missingValueIdentifier;

	/**
	 * @param data
	 * @param temporalDataset
	 * @param columnsUsed 
	 */
	public GranularityAggregationAction(TemporalDataset sourceDataset, CalendarManagers calendarManager,GranularityAggregationSettings[] granularities, Double missingValueIdentifier) {
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
        logger.trace("run -> begin of method");
		try {
			workingDataset = new GranularityAggregationTree(sourceDataset.getDataColumnSchema(),sourceDataset.getDataColumnIndices().length,granularities.length+1);
			
			logger.debug("run -> after for loop with min max values");
			
			GenericTemporalElement te = workingDataset.addTemporalElement(sourceDataset.getInf(),sourceDataset.getSup(),
					0, 32767, TemporalDataset.PRIMITIVE_INTERVAL);
			TemporalObject root = workingDataset.addTemporalObject(te);

			ArrayList<ArrayList<TemporalObject>> currentLeaves = new ArrayList<ArrayList<TemporalObject>>();
			currentLeaves.add(new ArrayList<TemporalObject>());
			for(TemporalObject iO : sourceDataset.temporalObjects()) {
				currentLeaves.get(0).add(iO);
			}
            logger.debug("run -> after for loop to add all source objects");
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
						TemporalObject targetBranch = null;
						long inf = currentLeave.getTemporalElement().asGeneric().getInf();
						long sup = currentLeave.getTemporalElement().asGeneric().getSup();
						for(TemporalObject potentialBranch : currentBranches.get(k).childObjects()) {
					    	if (potentialBranch.getTemporalElement().asGeneric().getGranule().contains(inf)) {
					    		targetBranch = potentialBranch;
					    	    break;
					    	}
					    }
					    if (targetBranch == null) {
					    	Granule newGranule = new Granule(inf,sup,granularities[i]); 
					    	Instant newTe = workingDataset.addInstant(newGranule);
					    	targetBranch = workingDataset.addTemporalObject(newTe);
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
            logger.debug("run -> after for loop over granularities");
							
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
					}
				}
			}
		}
		
        // TODO skip columns where NOT canSetDouble() e.g. boolean, Object
		for(int i=0; i<dataColumnIndices.length; i++) {
			totalValue[i] /= numObjects[i];
			parent.setDouble(dataColumnIndices[i],totalValue[i]);
		}
		
	}

	/* (non-Javadoc)
	 * @see timeBench.data.relational.TemporalDatasetProvider#getTemporalDataset()
	 */
	@Override
	public GranularityAggregationTree getGranularityAggregationTree() {
		return workingDataset;
	}
}
