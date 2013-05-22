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
public class PatternCountAction extends prefuse.action.Action {
    
	TemporalDataset sourceDataset;
	Table workingDataset;

	public PatternCountAction(TemporalDataset sourceDataset) {
		this.sourceDataset = sourceDataset;
	}
	

	/* (non-Javadoc)
	 * @see prefuse.action.Action#run(double)
	 */
	@Override
	public void run(double frac) {
		workingDataset = new Table();
		workingDataset.addColumn("pattern", String.class);
		workingDataset.addColumn("changeChronons", ArrayList.class);
		workingDataset.addColumn("count", ArrayList.class);
			
		Iterator temporalObjectIterator = sourceDataset.nodes();
		Hashtable<String,Long> patterns = new Hashtable<String, Long>();
		while(temporalObjectIterator.hasNext()) {				
			TemporalObject to = (TemporalObject)temporalObjectIterator.next();				
		}
	
	}

	public Table getTemporalDataset() {
		return workingDataset;
	}
}
