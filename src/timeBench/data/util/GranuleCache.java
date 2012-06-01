package timeBench.data.util;

import java.util.ArrayList;

import prefuse.data.Table;
import prefuse.data.event.EventConstants;
import prefuse.data.event.TableListener;
import timeBench.calendar.Calendar;
import timeBench.calendar.CalendarManagerFactory;
import timeBench.calendar.CalendarManagers;
import timeBench.calendar.Granularity;
import timeBench.calendar.Granule;
import timeBench.data.GenericTemporalElement;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;

/**
 * Cache for first granules of temporal elements. The cached granules are
 * automatically removed on changes of the backing temporal element table.
 * 
 * @author Rind
 * @see timeBench.data.TemporalElement#getGranule()
 */
public class GranuleCache {

    private ArrayList<Granule> granules = new ArrayList<Granule>();

    private TemporalDataset tmpds;

    // TODO make configurable; move to TemporalDataset?
    private Calendar calendar = CalendarManagerFactory.getSingleton(
            CalendarManagers.JavaDate).getDefaultCalendar();

    public GranuleCache(TemporalDataset tmpds) {
        this.tmpds = tmpds;
        tmpds.getTemporalElements().getNodeTable()
                .addTableListener(new GranuleListener());
    }

    /**
     * <p>
     * Warning: reduced validity checks (to improve performance)
     * 
     * @param row
     * @return
     * @throws TemporalDataException
     */
    public Granule getGranule(int row) throws TemporalDataException {
        granules.ensureCapacity(row);
        if (granules.get(row) == null) {
            GenericTemporalElement elem = tmpds.getTemporalElementByRow(row);
            if (elem.isAnchored()) {
                // TODO reuse granularity objects --> calendar responsible?
                Granularity g = new Granularity(calendar,
                        elem.getGranularityId(), elem.getGranularityContextId());
                granules.set(row, new Granule(elem.getInf(), elem.getSup(),
                        Granule.MODE_INF_GRANULE, g));
            } else {
                // TODO distinguish unknown from unanchored
                granules.set(row, null);
            }
        }
        return granules.get(row);
    }

    public Granule getGranule(long id) throws TemporalDataException {
        GenericTemporalElement elem = tmpds.getTemporalElement(id);
        return (elem != null) ? this.getGranule(elem.getRow()) : null;
    }

    public void addGranule(int row, Granule granule) {
        granules.set(row, granule);
    }

    /**
     * Removes granules from the cache if the backing temporal element is
     * changed or deleted.
     * 
     * @author Rind
     */
    private class GranuleListener implements TableListener {

        @Override
        public void tableChanged(Table t, int start, int end, int col, int type) {
            // switch on the event type
            switch (type) {
            case EventConstants.UPDATE: {
                // do nothing if update on all columns, as this is only
                // used to indicate a non-measurable update.
                if (col == EventConstants.ALL_COLUMNS) {
                    break;
                } else {
                    for (int r = start; r <= end; ++r) {
                        GranuleCache.this.granules.set(r, null);
                    }
                }
                break;
            }
            case EventConstants.DELETE:
                if (col == EventConstants.ALL_COLUMNS) {
                    // entire rows deleted
                    for (int r = start; r <= end; ++r) {
                        GranuleCache.this.granules.set(r, null);
                    }
                }
                break;
            case EventConstants.INSERT:
                // nothing to do here
            } // switch
        }
    }
}
