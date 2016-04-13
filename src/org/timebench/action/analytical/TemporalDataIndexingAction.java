package org.timebench.action.analytical;

import org.timebench.data.TemporalObject;

/**
 * Indexing based on temporal objects occurring at the same temporal element as
 * the selected indexing point.
 * 
 * @author Rind
 */
public class TemporalDataIndexingAction extends IndexingAction {

    public TemporalDataIndexingAction(String group, String absoluteValueField,
            String indexedValueField) {
        super(group, absoluteValueField, indexedValueField);
    }

    public TemporalDataIndexingAction(String group, String absoluteValueField,
            String indexedValueField, String categoryField) {
        super(group, absoluteValueField, indexedValueField, categoryField);
    }

    @Override
    protected void recalculateFactors() {
        factors.clear();
        if (indexedPoint != null
                && indexedPoint.getSourceTuple() instanceof TemporalObject) {
            TemporalObject obj = (TemporalObject) indexedPoint.getSourceTuple();
            for (TemporalObject o : obj.getTemporalElement().temporalObjects(
                    obj.getTemporalDataset())) {
                factors.put(o.get(categoryField),
                        1.0d / o.getDouble(absoluteValueField));
            }
        }
    }

}
