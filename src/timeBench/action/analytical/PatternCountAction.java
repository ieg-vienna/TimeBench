package timeBench.action.analytical;

import ieg.prefuse.data.ParentChildNode;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;

import prefuse.data.Table;
import prefuse.data.tuple.TableTuple;

import timeBench.calendar.Calendar;
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
	Hashtable<String,Integer> patterns = new Hashtable<String, Integer>();

	public PatternCountAction(TemporalDataset sourceDataset) {
		this.sourceDataset = sourceDataset;
	}
	

	/* (non-Javadoc)
	 * @see prefuse.action.Action#run(double)
	 */
	@Override
	public void run(double frac) {
		Calendar calendar = JavaDateCalendarManager.getSingleton().getDefaultCalendar();				
		
		try {
			workingDataset = new TemporalDataset();
			
			patterns =  new Hashtable<String, Integer>();
			Iterator temporalObjectIterator = sourceDataset.nodes();
			while(temporalObjectIterator.hasNext()) {				
				
				TemporalObject to = (TemporalObject)temporalObjectIterator.next();
				String pattern = to.getString("label");
				
				if(pattern.contains("e3p2e1p1e1p0e0")) {
					int x= 0;
					x++;
				}
				
				if (workingDataset.getDataColumnSchema().getColumnIndex(pattern) == -1) {
					workingDataset.addDataColumn(pattern, int.class, 0);
					patterns.put(pattern, 1);
				} else
					patterns.put(pattern,patterns.get(pattern)+1);
				long inf = to.getTemporalElement().asGeneric().getInf();
				
				TemporalObject lastTO = getLastTemporalObjectBefore(inf);
				TemporalObject newTO = null;
				TemporalElement newTE = null;
				if (lastTO == null) {
					newTE = workingDataset.addTemporalElement(inf, inf, calendar.getBottomGranularity(),TemporalElement.INSTANT);
					newTO = workingDataset.addTemporalObject(newTE);
					newTO.setInt(pattern, 1);
				} else if(lastTO.getTemporalElement().asGeneric().getInf() < inf) {
					newTE = workingDataset.addTemporalElement(inf, inf, calendar.getBottomGranularity(),TemporalElement.INSTANT);
					newTO = workingDataset.addTemporalObject(newTE);
					for(int i : workingDataset.getDataColumnIndices()) {
						newTO.set(i,lastTO.get(i));
					}
					newTO.setInt(pattern,lastTO.getInt(pattern)+1);
//					System.out.print("I: ");
//					for(int i : workingDataset.getDataColumnIndices()) {
//						System.out.print(workingDataset.getNodeTable().getSchema().getColumnName(i)+":"+newTO.getInt(i)+", ");
//					}
//					System.out.println();
				} else {
					newTE  = lastTO.getTemporalElement();
					newTO = lastTO;
					newTO.setInt(pattern,lastTO.getInt(pattern)+1);
//					System.out.print("M: ");
//					for(int i : workingDataset.getDataColumnIndices()) {
//						System.out.print(workingDataset.getNodeTable().getSchema().getColumnName(i)+":"+newTO.getInt(i)+", ");
//					}
//					System.out.println(JavaDateCalendarManager.formatDebugString(newTE.asGeneric().getInf()));
				}
				
				long sup = to.getTemporalElement().asGeneric().getSup()+1;
				Iterator temporalObjectIterator2 = workingDataset.nodes();
				boolean foundEnd = false;
				while(temporalObjectIterator2.hasNext()) {
					TemporalObject to2 = (TemporalObject)temporalObjectIterator2.next();
					if (to2.getTemporalElement().asGeneric().getInf() > inf &&
							to2.getTemporalElement().asGeneric().getSup() < sup) {
						to2.setInt(pattern,to2.getInt(pattern)+1);
					}
					if (to2.getTemporalElement().asGeneric().getSup() == sup) {
						foundEnd = true;
//						System.out.print("E: ");
//						for(int i : workingDataset.getDataColumnIndices()) {
//							System.out.print(workingDataset.getNodeTable().getSchema().getColumnName(i)+":"+to2.getInt(i)+", ");
//						}
//						System.out.println();
					}
				}
				
				if (!foundEnd) {
					TemporalObject lastTO2 = getLastTemporalObjectBefore(sup);
					newTE = workingDataset.addTemporalElement(sup, sup, calendar.getBottomGranularity(),TemporalElement.INSTANT);
					newTO = workingDataset.addTemporalObject(newTE);
					for(int i : workingDataset.getDataColumnIndices()) {
						newTO.set(i,lastTO2.get(i));						
					}
					newTO.setInt(pattern,newTO.getInt(pattern)-1);
//					System.out.print("S: ");
//					for(int i : workingDataset.getDataColumnIndices()) {
//						System.out.print(workingDataset.getNodeTable().getSchema().getColumnName(i)+":"+newTO.getInt(i)+", ");
//					}
//					System.out.println();
				}
			}
		} catch (TemporalDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	private TemporalObject getLastTemporalObjectBefore(long inf) {
		TemporalObject result = null;
		
		long lastInf = 0;
		Iterator temporalObjectIterator = workingDataset.nodes();
		while(temporalObjectIterator.hasNext()) {
			TemporalObject to = (TemporalObject)temporalObjectIterator.next();
			if(to.getTemporalElement().asGeneric().getInf() >= lastInf &&
					to.getTemporalElement().asGeneric().getInf() <= inf) {
				result = to;
				lastInf = to.getTemporalElement().asGeneric().getInf();
			}
		}
		
		return result;
	}

	public TemporalDataset getTemporalDataset() {
		return workingDataset;
	}
	
	public Hashtable<String,Integer> getPatterns() {
		return patterns;
	}
}
