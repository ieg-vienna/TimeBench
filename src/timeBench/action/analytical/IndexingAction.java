package timeBench.action.analytical;

import java.util.Map;
import java.util.TreeMap;

import prefuse.action.ItemAction;
import prefuse.visual.VisualItem;

public abstract class IndexingAction extends ItemAction {

    protected Map<Object, Double> factors = new TreeMap<Object, Double>();

    protected String absoluteValueField = null;
    protected String indexedValueField = null;
    protected String categoryField = null;

    protected VisualItem indexedPoint = null;

    public IndexingAction(String group, String absoluteValueField,
            String indexedValueField) {
        super(group);
        this.absoluteValueField = absoluteValueField;
        this.indexedValueField = indexedValueField;
    }

    public IndexingAction(String group, String absoluteValueField,
            String indexedValueField, String categoryField) {
        super(group);
        this.absoluteValueField = absoluteValueField;
        this.indexedValueField = indexedValueField;
        this.categoryField = categoryField;
    }

    @Override
    public void run(double frac) {
        if (factors.isEmpty()) {
            recalculateFactors();
        }
        super.run(frac);
    }

    protected abstract void recalculateFactors();

    @Override
    public void process(VisualItem item, double frac) {
        Object code = categoryField != null ? item.get(categoryField)
                : "default";
        Double factor = factors.get(code);
        factor = factor != null ? factor : 1.0;
        double value = item.getDouble(absoluteValueField) * factor * 100 - 100;
        item.set(indexedValueField, value);
    }

    public String getAbsoluteValueField() {
        return absoluteValueField;
    }

    public void setAbsoluteValueField(String absoluteValueField) {
        this.absoluteValueField = absoluteValueField;
    }

    public String getIndexedValueField() {
        return indexedValueField;
    }

    public void setIndexedValueField(String indexedValueField) {
        this.indexedValueField = indexedValueField;
    }

    public String getCategoryField() {
        return categoryField;
    }

    public void setCategoryField(String categoryField) {
        this.categoryField = categoryField;
    }

    public VisualItem getIndexedPoint() {
        return indexedPoint;
    }

    public void setIndexedPoint(VisualItem indexedPoint) {
        this.indexedPoint = indexedPoint;
        this.factors.clear();
    }

//    public void setIndexedPoint(VisualItem indexedItem) {
//        if (indexedItem != null
//                && indexedItem.getSourceTuple() instanceof TemporalObject) {
//            setIndexedPoint(((TemporalObject) indexedItem.getSourceTuple())
//                    .getTemporalElement());
//        } else {
//            throw new IllegalArgumentException(
//                    "Indexing visual item is not a temporal object");
//        }
//    }
}
