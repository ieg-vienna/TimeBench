package org.timebench.util;

import ieg.prefuse.data.DataHelper;

import java.io.PrintStream;

import org.timebench.calendar.Calendar;
import org.timebench.calendar.CalendarFactory;
import org.timebench.calendar.Granularity;
import org.timebench.calendar.Granule;
import org.timebench.calendar.JavaDateCalendarManager;
import org.timebench.data.GenericTemporalElement;
import org.timebench.data.Instant;
import org.timebench.data.Interval;
import org.timebench.data.Span;
import org.timebench.data.TemporalDataException;
import org.timebench.data.TemporalDataset;
import org.timebench.data.TemporalElement;
import org.timebench.data.TemporalObject;

import prefuse.data.Tuple;
import prefuse.data.tuple.TupleSet;

public class DebugHelper {
    
    public static void printTemporalObjectHierarchy(PrintStream out, TemporalDataset tmpds) {
        if (tmpds.getRootCount() == 0)
        	out.println("=== TemporalDataset is flat ===");
        else {
        	out.println("=== TemporalObject Hierarchy ===");
        	for (TemporalObject obj : tmpds.roots()) {
        		printTemporalObjectHierarchyStep(out, obj, 0);
        	}
        	out.println("================================");
        }
    }
    
    public static void printTemporalObjectHierarchy(PrintStream out, TemporalObject to) {
        out.println("=== TemporalObject Hierarchy ===");
        printTemporalObjectHierarchyStep(out, to, 0);
        out.println("================================");
    }
    
    private static void printTemporalObjectHierarchyStep(PrintStream out, TemporalObject to, int depth) {
        for (int i=0; i<depth; i++)
            out.print("   ");
        if (depth > 30) {
            out.println("--- maximum depth ---");
            return;
        }
        else
            out.println(to.getRow() + " " + to.getId() + " " + to.getTemporalElement().asPrimitive());
        
        for (TemporalObject tobj : to.childObjects()) {
            printTemporalObjectHierarchyStep(out, tobj, depth + 1);
        }
    }

    /**
     * generates tasks and random dependencies between tasks. Task names are
     * sorted by dependencies and each task has at least one predecessor and one
     * successor (except start and end).
     * <p>
     * Note: The tasks are not scheduled yet. All tasks start at <tt>epoch</tt>.
     * 
     * @param size
     * @param granularity
     * @return
     * @throws TemporalDataException
     */
    public static TemporalDataset generateProjectPlan(int size,
            Granularity granularity) throws TemporalDataException {
        TemporalDataset tmpds = new TemporalDataset();
        tmpds.addDataColumn("caption", String.class, "");

        // cache in arrays for comfortable access
        Interval[] elems = new Interval[size];
        TemporalObject[] objs = new TemporalObject[size];

        // add tasks with random length
        for (int i = 0; i < size; i++) {
            int days = (int) Math.floor(Math.random() * 10) + 1;

            Granule granule = new Granule(0l, 0l, granularity);
            Instant begin = tmpds.addInstant(granule);
            Span span = tmpds.addSpan(days, granularity.getIdentifier());
            elems[i] = tmpds.addInterval(begin, span);
            objs[i] = tmpds.addTemporalObject(elems[i]);
            objs[i].setString("caption", String.format("Task %2d", i));
        }

        // add a few random dependencies
        for (int i = 0; i < size / 2; i++) {
            int task1, task2;
            do {
                task1 = (int) Math.floor(Math.random() * size);
                task2 = (int) Math.floor(Math.random() * size);
            } while (task1 >= task2);

            tmpds.addEdge(objs[task1], objs[task2]);
        }

        // each task should have a successor (except last)
        for (int i = 0; i < size - 1; i++) {
            if (objs[i].getOutDegree() < 1) {
                int task2 = (int) Math.floor(Math.random() * (size - i - 1))
                        + i + 1; // i+1...size-1
                tmpds.addEdge(objs[i], objs[task2]);
            }
        }

        // each task should have a predecessor (except first)
        for (int i = 1; i < size; i++) {
            if (objs[i].getInDegree() < 1) {
                int task1 = (int) Math.floor(Math.random() * (i - 1)); // 0...i-1
                tmpds.addEdge(objs[task1], objs[i]);
            }
        }

        return tmpds;
    }
    
    /**
     * generates tasks and random dependencies between tasks. Task names are
     * sorted by dependencies and each task has at least one predecessor and one
     * successor (except start and end). The intervals can be indeterminate.
     * <p>
     * Note: The tasks are not scheduled yet. All tasks start at <tt>epoch</tt>.
     * 
     * @param size
     * @param granularity
     * @return
     * @throws TemporalDataException
     */
    public static TemporalDataset generateIndeterminateProjectPlan(int size,
            Granularity granularity) throws TemporalDataException {
        TemporalDataset tmpds = new TemporalDataset();
        tmpds.addDataColumn("caption", String.class, "");

        // cache in arrays for comfortable access
        TemporalElement[] elems = new TemporalElement[size];
        TemporalObject[] objs = new TemporalObject[size];

        // add tasks with random length
        for (int i = 0; i < size; i++) {
        	
            long grBeginOfBegin = 0l;
            long grEndOfBegin   = grBeginOfBegin + (long) Math.floor(Math.random() * 4) + 1;
            
            long grBeginOfEnd   = grEndOfBegin   + (long) Math.floor(Math.random() * 10) + 4;
            long grEndOfEnd     = grBeginOfEnd   + (long) Math.floor(Math.random() * 4) + 1;
            
            Granule granule = new Granule(grBeginOfBegin, granularity, granularity.getCalendar().getTopGranule());
            Instant beginBegin = tmpds.addInstant(granule);
            Span span = tmpds.addSpan(grEndOfBegin - grBeginOfBegin + 1, granularity.getIdentifier());
            Interval begin = tmpds.addInterval(beginBegin, span);

            granule = new Granule(grBeginOfEnd, granularity, granularity.getCalendar().getTopGranule());
            Instant beginEnd = tmpds.addInstant(granule);
            span = tmpds.addSpan(grEndOfEnd - grBeginOfEnd + 1, granularity.getIdentifier());
            Interval end = tmpds.addInterval(beginEnd, span);
            
            long absMaxGranules = grEndOfEnd - grBeginOfBegin + 1;
            long absMinGranules = Math.max(1, grBeginOfEnd - grEndOfBegin + 1);
            long max = absMaxGranules - (long) Math.floor(Math.random() * (absMaxGranules - absMinGranules) * 0.6);
            long min = absMinGranules + (long) Math.floor(Math.random() * (max - absMinGranules) * 0.6);
            System.out.println("Spans: " + absMaxGranules + " " + max + " " + min + " " + absMinGranules + " bb:" + grBeginOfBegin + " eb:" + grEndOfBegin + " be:" + grBeginOfEnd + " ee:" + grEndOfEnd);
            
            Span maxDuration = tmpds.addSpan(max, granularity.getIdentifier());
            Span minduration = tmpds.addSpan(min, granularity.getIdentifier());
            
            elems[i] = tmpds.addIndeterminateInterval(begin,maxDuration, minduration, end);
            
            objs[i] = tmpds.addTemporalObject(elems[i]);
            objs[i].setString("caption", String.format("Task %2d", i));
        }

        // add a few random dependencies
        for (int i = 0; i < size / 2; i++) {
            int task1, task2;
            do {
                task1 = (int) Math.floor(Math.random() * size);
                task2 = (int) Math.floor(Math.random() * size);
            } while (task1 >= task2);

            tmpds.addEdge(objs[task1], objs[task2]);
        }

        // each task should have a successor (except last)
        for (int i = 0; i < size - 1; i++) {
            if (objs[i].getOutDegree() < 1) {
                int task2 = (int) Math.floor(Math.random() * (size - i - 1))
                        + i + 1; // i+1...size-1
                tmpds.addEdge(objs[i], objs[task2]);
            }
        }

        // each task should have a predecessor (except first)
        for (int i = 1; i < size; i++) {
            if (objs[i].getInDegree() < 1) {
                int task1 = (int) Math.floor(Math.random() * (i - 1)); // 0...i-1
                tmpds.addEdge(objs[task1], objs[i]);
            }
        }

        return tmpds;
    }
    
    public static TemporalDataset generateValidInstants(int size)
            throws TemporalDataException {
        TemporalDataset tmpds = new TemporalDataset();
        tmpds.addDataColumn("caption", String.class, "");
    
        for (int i = 0; i < size; i++) {
            long inf = Math.round(Math.random() * 31536000000.0); // 1 year
            int gId = (int) Math.floor(Math.random() * 6) + 1;
            gId += CalendarFactory.getSingleton().getGranularityIdentifierSummandFromCalendarIdentifier(
            		JavaDateCalendarManager.getSingleton().getDefaultCalendar().getIdentifier());
            Granularity granularity = CalendarFactory.getSingleton().getGranularity(gId,
            		JavaDateCalendarManager.getSingleton().getDefaultCalendar().getTopGranularity().getIdentifier());
            Granule granule = new Granule(inf,inf,granularity);
            // Granule granule = granularity.parseDateToGranule(new Date());
            TemporalElement te = tmpds.addInstant(granule);
            TemporalObject to = tmpds.addTemporalObject(te);
            to.setString("caption", "no " + i);
        }
    
        return tmpds;
    }

    public static TemporalDataset generateFakeData(int size) {
        TemporalDataset tmpds = new TemporalDataset();
    
        for (int i = 0; i < size; i++) {
            long inf = Math.round(Math.random() * 315360000000.0); // 10 years
            long sup = inf + Math.round(Math.random() * 31536000000.0); // 1
                                                                        // year
            int gId = (int) Math.floor(Math.random() * 4) + 2;
            int kind = (int) Math.floor(Math.random() * 4);
            TemporalElement te = tmpds.addTemporalElement(inf, sup, gId, 32767, kind);
            tmpds.addTemporalObject(te);
        }
    
        return tmpds;
    }
    
    public static void main(String[] args) {
        System.out.println("Start Demo\n");
        TemporalDataset tmpds = generateFakeData(10);
        tmpds.getTemporalObject(1).setRoot(true);
        tmpds.getTemporalObject(1).linkWithChild(tmpds.getTemporalObject(2));
        printTemporalObjectHierarchy(System.out, tmpds);

        System.out.println("\nTemporal Objects");
        DataHelper.printTable(System.out, tmpds.getNodeTable());
        
        System.out.println("\nTemporal Elements");
        DataHelper.printTable(System.out, tmpds.getTemporalElements().getNodeTable());
    }
    
    public static void printTemporalDatasetGraph(PrintStream out,TemporalObject start) {
    	 DataHelper.printGraph(out, start, new TemporalElementInformation());
    }
    
    public static void printTemporalDatasetGraph(PrintStream out,TemporalObject start, String... cols) {
    	DataHelper.printGraph(out, start, new TemporalElementInformation(),cols);
    }
    
    public static void printTemporalDatasetTable(PrintStream out,TemporalDataset table) {
    	DataHelper.printTable(out, table.getNodeTable(), new TemporalElementInformation());
    }

    public static void printTemporalDatasetTable(PrintStream out,TemporalDataset table, String... cols) {
      	DataHelper.printTable(out, table.getNodeTable(), new TemporalElementInformation(), cols);
    }

    public static void printTemporalDatasetForest(PrintStream out,TemporalDataset table) {
    	DataHelper.printForest(out, table.getNodeTable(), table.roots(), table.getDepth(), TemporalObject.ID, new TemporalElementInformation());
    }

    public static void printTemporalDatasetForest(PrintStream out,TemporalDataset table,  String... cols) {
      	DataHelper.printForest(out, table.getNodeTable(), table.roots(), table.getDepth(), TemporalObject.ID, new TemporalElementInformation(), cols);
    }
    
    public static class TemporalElementInformation implements DataHelper.AdditionalNodeInformation {

		/* (non-Javadoc)
		 * @see ieg.prefuse.data.DataHelper.AdditionalNodeInformation#provideHeading(prefuse.data.Table)
		 */
		@Override
		public String provideHeading(TupleSet table) {
			return " Gr    Co Ident                                    Inf/Sup";
		}

		/* (non-Javadoc)
		 * @see ieg.prefuse.data.DataHelper.AdditionalNodeInformation#provideAdditionalInformation(prefuse.data.Node)
		 */
		@Override
		public String provideAdditionalInformation(Tuple node) {
			if (node instanceof TemporalObject) {
				TemporalObject to = (TemporalObject)node;
				GenericTemporalElement te = to.getTemporalElement().asGeneric();
				try {
					Granule granule = new Granule(te.getInf(),te.getSup(),
						new Granularity(JavaDateCalendarManager.getSingleton().getDefaultCalendar(),te.getGranularityId(),te.getGranularityContextId()));
					return String.format(" %2d %2d %5d %s/%s",te.getGranularityId(),te.getGranularityContextId(),granule.getIdentifier(),
							JavaDateCalendarManager.formatDebugString(te.asGeneric().getInf()),JavaDateCalendarManager.formatDebugString(te.asGeneric().getSup()));
				} catch (TemporalDataException e) {
					return ""; 
				}
			} else {
				return "";
			}
		}
    	
    }
}
