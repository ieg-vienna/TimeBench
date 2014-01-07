package timeBench.action.analytical;

import ieg.prefuse.data.ParentChildNode;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import timeBench.calendar.Calendar;
import timeBench.calendar.CalendarFactory;
import timeBench.calendar.Granularity;
import timeBench.calendar.Granule;
import timeBench.data.GranularityAggregationTree;
import timeBench.data.GranularityAggregationTreeProvider;
import timeBench.data.Instant;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalDatasetProvider;
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
public class GranularityAggregationAction extends prefuse.action.Action implements GranularityAggregationTreeProvider,TemporalDatasetProvider {
    
    private final Logger logger = Logger.getLogger(this.getClass());
    
	TemporalDataset sourceDataset;
	GranularityAggregationTree workingDataset;
	Calendar calendar;
	Granularity[] granularities;	
	double missingValueIdentifier;
	GranularityAggregationFunction[] aggFct;


	public GranularityAggregationAction(TemporalDataset sourceDataset,Granularity[] granularities,
			Double missingValueIdentifier) {
		this(sourceDataset,granularities,null,missingValueIdentifier);
	}

	
	/**
	 * @param data
	 * @param temporalDataset
	 * @param columnsUsed 
	 */
	public GranularityAggregationAction(TemporalDataset sourceDataset,Granularity[] granularities,
			GranularityAggregationFunction[] aggFct,
			Double missingValueIdentifier) {
		this.sourceDataset = sourceDataset;
		this.granularities = new Granularity[granularities.length];
		aggFct = new GranularityAggregationFunction[granularities.length];
		if(granularities.length > 0)
			this.calendar = CalendarFactory.getInstance().getCalendar(
					CalendarFactory.getInstance().getCalendarIdentifierFromGranularityIdentifier(
					granularities[0].getGlobalGranularityIdentifier()));
		this.granularities = granularities;
		if (aggFct == null) {
			this.aggFct = new GranularityAggregationFunction[granularities.length];
			for (int i=0; i<granularities.length; i++)
				this.aggFct[i] = new GranularityAggregationMean();
		} else {
			this.aggFct = aggFct;
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
					
			if (granularities.length > 1) {			
				for(int i=1; i<granularities.length;i++) {
					ArrayList<ArrayList<TemporalObject>> futureLeaves = new ArrayList<ArrayList<TemporalObject>>();
					ArrayList<TemporalObject> futureBranches = new ArrayList<TemporalObject>(); 
					int whichChild = 0;
					for(int k=0; k<currentLeaves.size();k++) {
						ArrayList<TemporalObject> iCurrentLeaves = currentLeaves.get(k);
						while(iCurrentLeaves.size() > 0) {
							TemporalObject currentLeave = iCurrentLeaves.get(0);
							iCurrentLeaves.remove(0);
							TemporalObject targetBranch = null;
							long inf = currentLeave.getTemporalElement().asGeneric().getInf();
							long sup = currentLeave.getTemporalElement().asGeneric().getSup();
							whichChild = 0;
							for(int l=0; l<k; l++)
								whichChild += currentBranches.get(l).getChildCount();
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

				for(long iRoot : roots)
					aggregate(workingDataset.getTemporalObject(iRoot),0);
			} else {
				for(int i=0; i<roots.length; i++)
					aggregate(workingDataset.getTemporalObject(roots[i]),currentLeaves.get(i),0);
			}
			
			// TODO this could be cleaned
			for (long rootId : roots) {
			    workingDataset.getTemporalObject(rootId).setRoot(true);
			}
			
		} catch (TemporalDataException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void update() {
		for (TemporalObject aRoot : workingDataset.roots()) {
			boolean change = false;
			for (int i : sourceDataset.getDataColumnIndices()) {
				if (aRoot.getKind(i) > 0.0)
					change = true;
			}
			if (change)
				aggregate(aRoot, 0);
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
//		Vector<Integer> aggregateColumn = new Vector<Integer>();
		
		/*
		 * Aggregate Only the data Columns, do not aggregate the Metadata
		 * To have the Metadata in the leafes, we keep the Metadata Columns in th lowest level
		 */
//		if (level < (granularities.length - 1)) {
//			for (int i = 0 ; i < dataColumnIndices.length; i++) {
//				if (!(parent.getColumnName(dataColumnIndices[i]).endsWith(".kind") ||
//						parent.getColumnName(dataColumnIndices[i]).endsWith(".minValue") ||
//						parent.getColumnName(dataColumnIndices[i]).endsWith(".maxValue"))) {
//					aggregateColumn.add(Integer.valueOf(dataColumnIndices[i]));
//				}
//			}
//			
//			dataColumnIndices = new int[aggregateColumn.size()];
//			for (int i =0; i < dataColumnIndices.length; i++) {
//				dataColumnIndices[i] = aggregateColumn.get(i).intValue();
//			}
//		}
		
        // TODO skip columns where NOT canSetDouble() e.g. boolean, Object
		// already done: .kind
		double[] totalValue = aggFct[(aggFct.length-1)-level].aggregate(childs, dataColumnIndices, missingValueIdentifier);
		
		for(int i=0; i<dataColumnIndices.length; i++) {
			if (parent.canSetDouble(dataColumnIndices[i])) {
				parent.setDouble(dataColumnIndices[i],totalValue[i]);
			} else if (parent.canSetInt(dataColumnIndices[i])) {
				parent.setInt(dataColumnIndices[i],(int) totalValue[i]);
			}
			parent.setInt(ParentChildNode.DEPTH,level);
			
//			parent.setDouble(dataColumnIndices[i],totalValue[i]);
//			parent.setInt(ParentChildNode.DEPTH,level);
						
			int kind = 0;
			for(TemporalObject iO : childs) {
				kind &= iO.getKind(dataColumnIndices[i]);
			}
			parent.setKind(dataColumnIndices[i], kind);
		}
		
		workingDataset.setDepth(Math.max(level, workingDataset.getDepth()));		
	}

	/* (non-Javadoc)
	 * @see timeBench.data.relational.TemporalDatasetProvider#getTemporalDataset()
	 */
	@Override
	public GranularityAggregationTree getGranularityAggregationTree() {
		return workingDataset;
	}


	/* (non-Javadoc)
	 * @see timeBench.data.TemporalDatasetProvider#getTemporalDataset()
	 */
	@Override
	public TemporalDataset getTemporalDataset() {
		return workingDataset;
	}
}
