package timeBench.action.analytical;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import prefuse.data.Tuple;
import prefuse.data.util.Index;
import prefuse.util.collections.IntIterator;
import prefuse.visual.VisualItem;
import timeBench.data.AnchoredTemporalElement;
import timeBench.data.GenericTemporalElement;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalElement;
import timeBench.data.TemporalElementStore;
import timeBench.data.TemporalObject;

public class InterpolationIndexingAction extends IndexingAction {

    private long indexTime;

    private long windowSize = 32 * 24 * 60 * 60 * 1000l;

    public InterpolationIndexingAction(String group, String absoluteValueField,
            String indexedValueField, String categoryField) {
        super(group, absoluteValueField, indexedValueField, categoryField);
    }

    public InterpolationIndexingAction(String group, String absoluteValueField,
            String indexedValueField) {
        super(group, absoluteValueField, indexedValueField);
    }

    @Override
    protected void recalculateFactors() {
        // TODO Auto-generated method stub
        factors.clear();

        // find TemporalDataset in both cases added as Graph or Table
        @SuppressWarnings("rawtypes")
        Iterator items = m_vis.items(m_group);
        if (! items.hasNext()) {
            // empty tuple set -> nothing to do 
//            System.out.println("empty tuple set -> nothing to do");
            return;
        }
        Tuple tuple = ((VisualItem) items.next()).getSourceTuple();
        if (! (tuple instanceof TemporalObject)) {
            throw new UnsupportedOperationException("Indexing only runs on TemporalObjects.");
        }
        TemporalDataset tmpds = ((TemporalObject) tuple).getTemporalDataset();
        TemporalElementStore store = tmpds.getTemporalElements();

        Map<Object, TemporalObject> center = new TreeMap<Object, TemporalObject>();
        Map<Object, TemporalObject> left = new TreeMap<Object, TemporalObject>();
        Map<Object, TemporalObject> right = new TreeMap<Object, TemporalObject>();

        Index index = store.getNodeTable().index(TemporalElement.INF);
        IntIterator ii = index.rows(indexTime - windowSize, indexTime
                + windowSize, Index.TYPE_AII);

        
//        System.out.printf("searching window: %tF - %tF %n", (indexTime-windowSize), (indexTime+windowSize));
        
        while (ii.hasNext()) {
            GenericTemporalElement el = store.getTemporalElementByRow(ii.nextInt());
//            System.out.println("in window: " + el);
            if (el.isAnchored()) {
                for (TemporalObject obj : el.temporalObjects(tmpds)) {
//                    System.out.println("     with: " + obj + " value=" + obj.getDouble(absoluteValueField));
                    if (el.getSup() < indexTime) {
                        TemporalObject prev = left.get(obj.get(categoryField));
                        if (prev == null
                                || el.getSup() > ((AnchoredTemporalElement) prev
                                        .getTemporalElement()).getSup()) {
                            left.put(obj.get(categoryField), obj);
                        }
                    } else if (el.getInf() > indexTime) {
                        TemporalObject prev = right.get(obj.get(categoryField));
                        if (prev == null
                                || el.getInf() < ((AnchoredTemporalElement) prev
                                        .getTemporalElement()).getInf()) {
                            right.put(obj.get(categoryField), obj);
                        }
                    } else {
                        // if (el.getInf() <= indexTime && el.getSup() >= indexTime) {
                        center.put(obj.get(categoryField), obj);
                    }
                }
            }
        }
//        System.out.println("structures center: " + center.size() + " left: " + left.size() + " right: " + right.size());
        
        for (Map.Entry<Object, TemporalObject> e : center.entrySet()) {
            factors.put(e.getKey(), 1.0d / e.getValue().getDouble(absoluteValueField));
//            System.out.println("center key=" + e.getKey() + " value=" + e.getValue().getDouble(absoluteValueField));
            left.remove(e.getKey());
            right.remove(e.getKey());
        }
        
        for (Map.Entry<Object, TemporalObject> e : left.entrySet()) {
            TemporalObject l = e.getValue();
            TemporalObject r = right.get(e.getKey());
            
            if (r != null) {
                long leftTime = ((AnchoredTemporalElement) l.getTemporalElement()).getSup();
                long rightTime = ((AnchoredTemporalElement) r.getTemporalElement()).getInf();

                double value = l.getDouble(absoluteValueField)
                        + (indexTime - leftTime)
                        * (r.getDouble(absoluteValueField) - l
                                .getDouble(absoluteValueField))
                        / (rightTime - leftTime);
//                System.out.println("interp key=" + e.getKey() + " value=" + value);
                factors.put(e.getKey(), 1.0d / value);
            } else {
                factors.put(e.getKey(), 1.0d / l.getDouble(absoluteValueField));
//                System.out.println("left## key=" + e.getKey() + " value=" + l.getDouble(absoluteValueField));
            }
            right.remove(e.getKey());
        }

        for (Map.Entry<Object, TemporalObject> e : right.entrySet()) {
            TemporalObject r = e.getValue();
            factors.put(e.getKey(), 1.0d / r.getDouble(absoluteValueField));
//            System.out.println("right# key=" + e.getKey() + " value=" + r.getDouble(absoluteValueField));
        }
    }

    public long getIndexTime() {
        return indexTime;
    }

    public void setIndexTime(long indexTime) {
        this.indexTime = indexTime;
        this.factors.clear();
    }
}
