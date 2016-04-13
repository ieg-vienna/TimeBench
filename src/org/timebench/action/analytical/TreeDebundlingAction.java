package org.timebench.action.analytical;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;

import org.timebench.data.TemporalDataException;
import org.timebench.data.TemporalDataset;
import org.timebench.data.TemporalDatasetProvider;
import org.timebench.data.TemporalObject;

import prefuse.data.tuple.TableTuple;

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
	Hashtable<String,Integer> classes = null;
	ArrayList<String> workingClasses = null;

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
			workingDataset.addDataColumn("class", int.class, -1);
			
			for(TemporalObject root : sourceDataset.roots()) {
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
			
			workingClasses = new ArrayList<String>();
			for(TemporalObject iTo : workingDataset.temporalObjects()) {
				String label = iTo.getString("label");
				int thisclass = workingClasses.indexOf(label);
				if (thisclass == -1) {
					workingClasses.add(label);
					thisclass = workingClasses.size()-1;
				}
				iTo.setInt("class", thisclass);
			}
			
			classes = new Hashtable<String, Integer>();
			String[] sortedClasses = (String[])workingClasses.toArray(new String[workingClasses.size()]);
			Arrays.sort(sortedClasses);
			double addPerStep = 12.0/(double)sortedClasses.length;
			double val=0;
			for(String iStr : sortedClasses) {
				classes.put(iStr, (int)Math.round(val));
				val += addPerStep;
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
						((TableTuple)edges.next()).getLong(MultiPredicatePatternDiscovery.predicateColumn) + 
						iChild.getString("label");															
				findLeaves(iChild,leafList,stringsToLeaf,addedStringState);
			}
		}
	}
	
	public Hashtable<String,Integer> getClasses() {
		return classes;
	}


	/* (non-Javadoc)
	 * @see timeBench.data.TemporalDatasetProvider#getTemporalDataset()
	 */
	@Override
	public TemporalDataset getTemporalDataset() {
		return workingDataset;
	}
}
