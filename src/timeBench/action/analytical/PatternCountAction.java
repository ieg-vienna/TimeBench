package timeBench.action.analytical;

import ieg.prefuse.data.ParentChildNode;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;

import prefuse.data.Table;
import prefuse.data.tuple.TableTuple;

import timeBench.calendar.CalendarManager;
import timeBench.calendar.CalendarManagerFactory;
import timeBench.calendar.CalendarManagers;
import timeBench.calendar.Granularity;
import timeBench.calendar.Granule;
import timeBench.calendar.JavaDateCalendarManager;
import timeBench.data.GranularityAggregationTree;
import timeBench.data.GranularityAggregationTreeProvider;
import timeBench.data.Instant;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalDatasetProvider;
import timeBench.data.TemporalElement;
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
public class PatternCountAction extends prefuse.action.Action implements TemporalDatasetProvider {
    
	TemporalDataset sourceDataset;
	TemporalDataset workingDataset;

	public PatternCountAction(TemporalDataset sourceDataset) {
		this.sourceDataset = sourceDataset;
	}
	

	/* (non-Javadoc)
	 * @see prefuse.action.Action#run(double)
	 */
	@Override
	public void run(double frac) {
		try {
			workingDataset = new TemporalDataset();
			
			Hashtable<String,Long> patterns = new Hashtable<String, Long>();
			Iterator temporalObjectIterator = sourceDataset.nodes();
			while(temporalObjectIterator.hasNext()) {				
				TemporalObject to = (TemporalObject)temporalObjectIterator.next();
				String pattern = to.getString("pattern");			
				if (workingDataset.getDataColumnSchema().getColumnIndex(pattern) == -1) {
					workingDataset.addDataColumn(pattern, int.class, 0);
				}
				long inf = to.getTemporalElement().asGeneric().getInf();
				TemporalObject lastTO = getLastTemporalObjectBefore(inf);
				TemporalObject newTO = null;
				TemporalElement newTE = null;
				if (lastTO == null) {
					newTE = workingDataset.addTemporalElement(inf, inf, JavaDateCalendarManager.Granularities.Millisecond.toInt(),
							JavaDateCalendarManager.Granularities.Millisecond.toInt(),TemporalElement.INSTANT);
					newTO = workingDataset.addTemporalObject(newTE);					
				} else {
					newTE = workingDataset.addTemporalElement(inf, inf, JavaDateCalendarManager.Granularities.Millisecond.toInt(),
							JavaDateCalendarManager.Granularities.Millisecond.toInt(),TemporalElement.INSTANT);
					newTO = workingDataset.addTemporalObject(newTE);
					for(int i : workingDataset.getDataColumnIndices()) {
						newTO.set(i,lastTO.get(i));
					}
				}
				newTO.setInt(pattern,lastTO.getInt(pattern)+1);
				long sup = to.getTemporalElement().asGeneric().getSup();
				lastTO = getLastTemporalObjectBefore(sup);
				newTE = workingDataset.addTemporalElement(sup, sup, JavaDateCalendarManager.Granularities.Millisecond.toInt(),
						JavaDateCalendarManager.Granularities.Millisecond.toInt(),TemporalElement.INSTANT);
				newTO = workingDataset.addTemporalObject(newTE);
				for(int i : workingDataset.getDataColumnIndices()) {
					newTO.set(i,lastTO.get(i));
				}
				newTO.setInt(pattern,lastTO.getInt(pattern)-1);
			}
		} catch (TemporalDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	private TemporalObject getLastTemporalObjectBefore(long inf) {
		TemporalObject result = null;
		
		int lastInf = 0;
		Iterator temporalObjectIterator = workingDataset.nodes();
		while(temporalObjectIterator.hasNext()) {
			TemporalObject to = (TemporalObject)temporalObjectIterator.next();
			if(to.getTemporalElement().asGeneric().getInf() > lastInf &&
					to.getTemporalElement().asGeneric().getInf() < inf) {
				result = to;
			}
		}
		
		return result;
	}


	public TemporalDataset getTemporalDataset() {
		return workingDataset;
	}
}
