package timeBench.data.util;

import org.apache.log4j.Logger;

import prefuse.data.Table;
import prefuse.util.collections.IntIterator;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalElement;
import timeBench.data.TemporalObject;

/**
 * Checks a {@link TemporalDataset} for inconsistencies. The temporal dataset
 * does not automatically enforce consistency, in order to boost performance.
 * 
 * <p>
 * Possible inconsistencies (brainstorming):
 * <ul>
 * <li>Primary keys {@link TemporalObject#ID} and {@link TemporalElement#ID} are
 * unique.
 * <li>Temporal elements exist for all temporal objects (foreign key
 * {@link TemporalObject#TEMPORAL_ELEMENT_ID}).
 * <li>Temporal elements match with granules of the calendar.
 * <li>Temporal elements are structured according to their kind.
 * </ul>
 * 
 * @author Rind
 * 
 */
public class TemporalDatasetValidator {

    private static final Logger log = Logger
            .getLogger(TemporalDatasetValidator.class);

    public static boolean validate(TemporalDataset tmpds) {
        return false;
    }

    public static boolean validateIDs(TemporalDataset tmpds) {
        for (TemporalObject obj : tmpds.temporalObjects()) {
            TemporalElement elem = obj.getTemporalElement();
            if (elem == null || !elem.isValid()) {
                log.info("Missing temporal element ID "
                        + obj.getLong(TemporalObject.TEMPORAL_ELEMENT_ID)
                        + " for temporal object ID " + obj.getId());
                return false;
            }
        }

        return validateUniqueLongColumn(tmpds.getTemporalObjectTable(),
                TemporalObject.ID)
                && validateUniqueLongColumn(tmpds.getTemporalElements()
                        .getNodeTable(), TemporalElement.ID);
    }

    private static boolean validateUniqueLongColumn(Table table, String field) {
        int col = table.getColumnNumber(field);
        long prev = Long.MIN_VALUE;

        IntIterator i = table.index(field).allRows(
                prefuse.data.util.Index.TYPE_ASCENDING);
        while (i.hasNext()) {
            int row = i.nextInt();
            long value = table.getLong(row, col);
            if (log.isTraceEnabled()) {
                log.trace(row + " " + value);
            }
            if (prev == value) {
                log.info("Duplicate field value " + value + " at row " + row);
                return false;
            }
            prev = value;
        }

        return true;
    }

}
