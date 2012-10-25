package timeBench.action.analytical;

import java.sql.Timestamp;
import java.util.ArrayList;

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
	GranularityAggregationFunction[] aggFct;

	/**
	 * @param data
	 * @param temporalDataset
	 * @param columnsUsed 
	 */
	public GranularityAggregationAction(TemporalDataset sourceDataset, CalendarManagers calendarManager,GranularityAggregationSettings[] granularities, Double missingValueIdentifier) {
		this.sourceDataset = sourceDataset;
		this.calendarManager = CalendarManagerFactory.getSingleton(calendarManager);
		this.granularities = new Granularity[granularities.length];
		aggFct = new GranularityAggregationFunction[granularities.length];
		for(int i=0; i<granularities.length; i++) {
			this.granularities[i] = new Granularity(this.calendarManager.getDefaultCalendar(),granularities[i].getIdentifier(),granularities[i].getContextIdentifier());
			aggFct[i] = granularities[i].getAggregationFct();
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
			workingDataset = new GranularityAggregationTree(sourceDataset.getDataColumnSchema(),granularities.length+1);
			

			ArrayList<TemporalObject> currentBranches = new ArrayList<TemporalObject>();
			ArrayList<ArrayList<TemporalObject>> currentLeaves = new ArrayList<ArrayList<TemporalObject>>();
			
			for(TemporalObject iO : sourceDataset.temporalObjects()) {
				long inf = iO.getTemporalElement().asGeneric().getGranule().getInf();
				int i = 0;
				for(TemporalObject iB : currentBranches) {
					if (iB.getTemporalElement().asGeneric().getGranule().contains(inf)) {
		    			currentLeaves.get(i).add(iO);
		    			break;
					}
					i++;
				}
				if(i >= currentBranches.size()) {
			    	Granule newGranule = new Granule(inf,inf,granularities[0]); 
			    	Instant newTe = workingDataset.addInstant(newGranule);
			    	currentBranches.add(workingDataset.addTemporalObject(newTe));
			    	ArrayList<TemporalObject> leaves = new ArrayList<TemporalObject>();
			    	leaves.add(iO);
			    	currentLeaves.add(leaves);
				}
			}
			
			long[] roots = new long[currentBranches.size()];
			for(int i=0; i<currentBranches.size(); i++)
				roots[i] = currentBranches.get(i).getId();
						
			for(int i=1; i<granularities.length;i++) {
				ArrayList<ArrayList<TemporalObject>> futureLeaves = new ArrayList<ArrayList<TemporalObject>>();
				ArrayList<TemporalObject> futureBranches = new ArrayList<TemporalObject>(); 
				int whichChild = 0;
				for(int k=0; k<currentLeaves.size();k++) {
					ArrayList<TemporalObject> iCurrentLeaves = currentLeaves.get(k);
					while(iCurrentLeaves.size() > 0) {
						TemporalObject currentLeave = iCurrentLeaves.get(iCurrentLeaves.size()-1);
						iCurrentLeaves.remove(iCurrentLeaves.size()-1);
						TemporalObject targetBranch = null;
						long inf = currentLeave.getTemporalElement().asGeneric().getInf();
						long sup = currentLeave.getTemporalElement().asGeneric().getSup();
			    		whichChild = 0;
			    		for(int l=0; l<k; l++)
			    			whichChild += currentBranches.get(l).getChildObjectCount();
						for(TemporalObject potentialBranch : currentBranches.get(k).childObjects()) {
					    	if (potentialBranch.getTemporalElement().asGeneric().getGranule().contains(inf)) {
					    		targetBranch = potentialBranch;
					    	    break;
					    	}
				    	    whichChild++;
					    }
					    if (targetBranch == null) {
					    	Granule newGranule = new Granule(inf,sup,granularities[i]); 
					    	Instant newTe = workingDataset.addInstant(newGranule);
					    	targetBranch = workingDataset.addTemporalObject(newTe);
					    	futureBranches.add(targetBranch);
					    	futureLeaves.add(new ArrayList<TemporalObject>());					    	
					    	whichChild = futureLeaves.size() - 1;
					    	currentBranches.get(k).linkWithChild(targetBranch);
					    }
				    	futureLeaves.get(whichChild).add(currentLeave);
					}
				}
				if(i==granularities.length-1) {
					for(int j=0; j<futureBranches.size(); j++ ) {
						aggregate(futureBranches.get(j),futureLeaves.get(j),granularities.length-1);
					}
				} else {
					currentBranches = futureBranches;
					currentLeaves = futureLeaves;
				}
			}

            logger.debug("run -> after for loop over granularities");
            
            for(long iRoot : roots)
            	aggregate(workingDataset.getTemporalObject(iRoot),0);
			
			workingDataset.setRoots(roots);
			
			DebugHelper.printTemporalDatasetGraph(System.out, workingDataset.getTemporalObject(roots[0]));
		} catch (TemporalDataException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	
	

	private void aggregate(TemporalObject parent,int level) {
	    if (parent.getChildObjectCount() > 0) {
	    	for (TemporalObject child : parent.childObjects()) {
	    		aggregate(child,level+1);
	    	}
	    	aggregate(parent,parent.childObjects(),level);
	    }
	}
	private void aggregate(TemporalObject parent,Iterable<TemporalObject> childs,int level) {
		int[] dataColumnIndices = sourceDataset.getDataColumnIndices();
		
        // TODO skip columns where NOT canSetDouble() e.g. boolean, Object
		// already done: .kind
		double[] totalValue = aggFct[(aggFct.length-1)-level].aggregate(childs, dataColumnIndices, missingValueIdentifier);
		
		for(int i=0; i<dataColumnIndices.length; i++) {
			parent.setDouble(dataColumnIndices[i],totalValue[i]);
			parent.setInt(GranularityAggregationTree.DEPTH,level);
						
			int kind = 0;
			for(TemporalObject iO : childs) {
				kind &= iO.getKind(dataColumnIndices[i]);
			}
			parent.setKind(dataColumnIndices[i], kind);
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
