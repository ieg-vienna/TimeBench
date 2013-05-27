package timeBench.action.analytical;

import ieg.prefuse.data.ParentChildNode;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;

import prefuse.data.tuple.TableTuple;

import timeBench.calendar.CalendarManager;
import timeBench.calendar.CalendarManagerFactory;
import timeBench.calendar.CalendarManagers;
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
public class TreeDebundlingAction extends prefuse.action.Action implements TemporalDatasetProvider {
    
	TemporalDataset sourceDataset;
	TemporalDataset workingDataset;

	public TreeDebundlingAction(TemporalDataset sourceDataset) {
		this.sourceDataset = sourceDataset;
	}
	

	/* (non-Javadoc)
	 * @see prefuse.action.Action#run(double)
	 */
	@Override
	public void run(double frac) {
		try {
			workingDataset = new TemporalDataset();
			workingDataset.addDataColumn("label", String.class, "");
			
			for(long iRoot : sourceDataset.getRoots()) {
				TemporalObject root = sourceDataset.getTemporalObject(iRoot);				
				ArrayList<TemporalObject> leafList = new ArrayList<TemporalObject>();
				ArrayList<String> stringsToLeaf = new ArrayList<String>();
				String currentStringState = root.getString("label");
				findLeaves(root,leafList,stringsToLeaf,currentStringState);
				for(int i=0; i<leafList.size(); i++) {
					TemporalObject iLeaf = leafList.get(i);
					TemporalObject path = workingDataset.addTemporalObject(
							workingDataset.addInterval(
									workingDataset.addInstant(root.getTemporalElement().getFirstInstant().getInf(),
											root.getTemporalElement().getFirstInstant().getSup(),
											root.getTemporalElement().getFirstInstant().getGranularityId(),
											root.getTemporalElement().getFirstInstant().getGranularityContextId()),
											workingDataset.addInstant(iLeaf.getTemporalElement().getLastInstant().getInf(),
													iLeaf.getTemporalElement().getLastInstant().getSup(),
													iLeaf.getTemporalElement().getLastInstant().getGranularityId(),
													iLeaf.getTemporalElement().getLastInstant().getGranularityContextId())));
					path.set("label", stringsToLeaf.get(i));
				}
			}
		} catch (TemporalDataException e) {
			throw new RuntimeException(e.getMessage());
		}		
	}

	private void findLeaves(TemporalObject temporalObject,ArrayList<TemporalObject> leafList,
			ArrayList<String> stringsToLeaf, String currentStringState) {
		if (temporalObject.getChildCount() == 0) {			
			leafList.add(temporalObject);
			stringsToLeaf.add(currentStringState);
		} else {
			for(TemporalObject iChild : temporalObject.childObjects()) {
				Iterator edges = iChild.outEdges();
				String addedStringState = currentStringState + "p" +
						((TableTuple)edges.next()).getLong(MultiPredicatePatternDiscovery.predicateColumn) + "e" + 
						iChild.getString("label");															
				findLeaves(iChild,leafList,stringsToLeaf,addedStringState);
			}
		}
	}


	/* (non-Javadoc)
	 * @see timeBench.data.TemporalDatasetProvider#getTemporalDataset()
	 */
	@Override
	public TemporalDataset getTemporalDataset() {
		return workingDataset;
	}
}
