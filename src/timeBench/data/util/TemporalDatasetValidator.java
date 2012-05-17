package timeBench.data.util;

import org.apache.log4j.Logger;

import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.util.collections.IntIterator;
import timeBench.data.GenericTemporalElement;
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
 * <li>Temporal elements are structured according to their kind.
 * <li>Lifespan of temporal elements match with granules of the calendar at the
 * right granularity.
 * <li>Lifespan of temporal elements contains lifespan of their children.
 * </ul>
 * 
 * TODO Should all inconsistencies be reported or return on first problem?
 * 
 * @author Rind
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

    /**
     * TODO untested/experimental
     * @param tmpds
     * @return
     */
    public static boolean validateTemporalElementContainment(
            TemporalDataset tmpds) {
        for (GenericTemporalElement elem : tmpds.temporalElements()) {
            if (elem.isAnchored()) {
                for (GenericTemporalElement child : elem.childElements()) {
                    if (child.isAnchored()) {
                        if (child.getInf() < elem.getInf()
                                || child.getSup() > elem.getSup()) {
                            log.info("Anchored temporal element with child "
                                    + "out of lifespan (parent ID: "
                                    + elem.getId() + ", child ID: "
                                    + child.getId() + ")");
                            return false;
                        }
                    }
                }
            } else {
                for (GenericTemporalElement child : elem.childElements()) {
                    if (child.getInf() > elem.getInf()) {
                        log.info("Unanchored temporal element with child "
                                + "length exceeding its length (parent ID: "
                                + elem.getId() + ", child ID: " + child.getId()
                                + ")");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * TODO untested/experimental
     * @param tmpds
     * @return
     */
    public static boolean validateTemporalElementStructure(TemporalDataset tmpds) {
        Graph graph = tmpds.getTemporalElements();
        for (GenericTemporalElement elem : tmpds.temporalElements()) {
            switch (elem.getKind()) {
            case TemporalDataset.PRIMITIVE_INSTANT:
                if (graph.getInDegree(elem) > 0) {
                    log.info("Instant with children at temporal element ID "
                            + elem.getId());
                    return false;
                }
                break;
            case TemporalDataset.PRIMITIVE_SPAN:
                if (graph.getInDegree(elem) > 0) {
                    log.info("Span with children at temporal element ID "
                            + elem.getId());
                    return false;
                }
                break;
            case TemporalDataset.PRIMITIVE_SET:
                if (graph.getInDegree(elem) == 0) {
                    log.info("Set without children at temporal element ID "
                            + elem.getId());
                    return false;
                }
                break;
            case TemporalDataset.PRIMITIVE_INTERVAL:
                // TODO check structure of interval
                break;
            default:
                log.info("Unexpected kind " + elem.getKind()
                        + " at temporal element ID " + elem.getId());
                return false;
            }

        }

        return true;
    }

    /**
     * TODO untested/experimental
     * @param tmpds
     * @return
     */
    public static boolean validateGranules(TemporalDataset tmpds) {
        for (GenericTemporalElement elem : tmpds.temporalElements()) {
            switch (elem.getKind()) {
            case TemporalDataset.PRIMITIVE_INSTANT:
                // TODO inf/sup of exactly one granule
                break;
            case TemporalDataset.PRIMITIVE_INTERVAL:
                // TODO inf<sup & inf/sup may be of different granules
                break;
            case TemporalDataset.PRIMITIVE_SPAN:
                if (elem.getInf() < 0) {
                    log.info("Span with negative length (ID: " + elem.getId()
                            + ")");
                    return false;
                }
                // TODO sanity check: large value in a "high" granularity
                // (misplaced timestamp)??
                break;
            case TemporalDataset.PRIMITIVE_SET:
                if (elem.isAnchored()) {
                    // TODO see interval
                } else {
                    // TODO see span, maybe more
                }
                break;
            default:
                log.info("Unexpected kind " + elem.getKind()
                        + " at temporal element ID " + elem.getId());
                return false;
            }

        }

        return true;
    }
}
