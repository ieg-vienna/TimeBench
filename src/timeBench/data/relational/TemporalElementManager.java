package timeBench.data.relational;

import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.event.EventConstants;
import prefuse.data.event.TableListener;
import prefuse.data.tuple.TupleManager;
import prefuse.util.collections.IntIterator;

/**
 * Manager class for temporal elements. There is a unique {@link Tuple} for each
 * row of a {@link Table}. All data structures and tuples are created lazily, on
 * an as-needed basis. When a row is deleted from the Table, it's corresponding
 * Tuple (if created) is invalidated before being removed from this data
 * structure, ensuring that any other live references to the Tuple can't be used
 * to corrupt the Table. If the TemporalElementManager is used as an additional
 * TupleManager (i.e. not set by {@link Table#setTupleManager(TupleManager)}),
 * {@link #invalidateAutomatically()} must be called, so that removed tuples are
 * invalidated.
 * 
 * <p>
 * All tuples are instances of a subclass of {@link TemporalElement}. They are
 * either instances of {@link GenericTemporalElement} or temporal primitives
 * (e.g., {@link Instant}), depending on the state of {@link #generic}, when the
 * tuple is initialized.
 * 
 * <p>
 * <b>Warning:</b> prefuse's lazy initialization is not as lazy as expected.
 * {@link prefuse.data.Table#addRow()} fires a tuple event for which the tuple
 * is created. {@link #invalidate(int)} can be called to reinitialize the tuple.
 * 
 * <p>
 * Mostly reuses its superclass {@link TupleManager}.
 * 
 * @author Rind
 * 
 */
public class TemporalElementManager extends TupleManager {

    /**
     * the temporal dataset. It will be passed to new temporal element tuples
     */
    private TemporalDataset tmpds;

    // TODO undesirable to mix generic and primitive tuples in the dataset?
    /**
     * switch whether this manager creates new tuples as
     * {@link GenericTemporalElement} or temporal primitives (e.g.,
     * {@link Instant}).
     */
    private boolean generic;

    public TemporalElementManager(TemporalDataset tmpds, boolean generic) {
        super(tmpds.getTemporalElements(), tmpds.getTemporalElementsGraph(),
                TemporalElement.class);
        this.tmpds = tmpds;
        this.generic = generic;
    }

    @Override
    protected TemporalElement newTuple(int row) {
        int kind = m_table.getInt(row, TemporalDataset.KIND);
        TemporalElement t;
        if (this.existingElement != null) {
            t = this.existingElement;
            this.existingElement = null;
        }
        else if (generic || kind == -1)
            t = new GenericTemporalElement();
        else
            switch (kind) {
            case TemporalDataset.PRIMITIVE_INSTANT:
                t = new Instant();
                break;
            case TemporalDataset.PRIMITIVE_INTERVAL:
                t = new Interval();
                break;
            case TemporalDataset.PRIMITIVE_SPAN:
                t = new Span();
                break;
            default:
                if (TemporalElementManager.isAnchored(m_graph, row))
                    t = new AnchoredTemporalElement();
                else
                    t = new UnanchoredTemporalElement();
            }

        t.init(m_table, m_graph, tmpds, row);
        return t;
    }
    
    // temporarily store if an existing element should be added
    private TemporalElement existingElement = null;

    // Alternative 0: no constructor, use factory method instead
    // Alternative 1: change TupleManager and make array protected
    // Alternative 2: replicate array and many methods of TupleManager
    
    protected int addTemporalElement(TemporalElement element, int row) {
        Tuple result;

        // XXX check for multi thread issues
        synchronized (this) {
            // make sure there is no tuple for this row
            // (typically the table listener produces a tuple on row add)
            super.invalidate(row);

            // keep track of this temporal element object
            this.existingElement = element;
            // this will call TemporalElementManager.newTuple(int)
            result = super.getTuple(row);
        }
        
        // check if it worked as expected (e.g. multi thread issue)
        if (element != result)
            throw new RuntimeException("added temporal element not identical");
        
        return row;
    }

    /**
     * recursive helper method to find out if a temporal element is anchored.
     * 
     * <p>
     * A temporal element is anchored iff its kind is instant or interval or its
     * kind is set and at least one child is anchored.
     * 
     * @param g
     *            temporal elements graph.
     * @param row
     *            row number in the node table.
     * @return true if the temporal element is anchored.
     */
    protected static boolean isAnchored(Graph g, int row) {
        // use low level functions, otherwise a tuple would be created (circular
        // dependency)
        int kind = g.getNodeTable().getInt(row, TemporalDataset.KIND);
        if (kind == TemporalDataset.PRIMITIVE_INSTANT
                || kind == TemporalDataset.PRIMITIVE_INTERVAL)
            return true;
        else if (kind == TemporalDataset.PRIMITIVE_SET) {
            IntIterator iter = g.inEdgeRows(row);
            while (iter.hasNext())
                if (TemporalElementManager.isAnchored(g,
                        g.getSourceNode(iter.nextInt())))
                    return true;
        }
        return false;
    }

    /**
     * Get whether this manager creates new tuples as
     * {@link GenericTemporalElement} or temporal primitives (e.g.,
     * {@link Instant}).
     * 
     * @return true if new tuples are created as {@link GenericTemporalElement}.
     */
    public boolean isGeneric() {
        return generic;
    }

    /**
     * Set whether this manager creates new tuples as
     * {@link GenericTemporalElement} or temporal primitives (e.g.,
     * {@link Instant}).
     * 
     * @param generic
     *            true if new tuples are created as
     *            {@link GenericTemporalElement}.
     */
    public void setGeneric(boolean generic) {
        this.generic = generic;
    }

    /**
     * Adds a listener that invalidates tuples if necessary. It will invalidate
     * a tuple, if the underlying table row is removed or its primitive kind
     * changes. This is required if the object is used as an additional
     * TupleManager, so that it can not be set by
     * {@link Table#setTupleManager(TupleManager)}
     */
    public void invalidateAutomatically() {
        this.m_table.addTableListener(new TemporalListener());
    }

    // ------------------------------------------------------------------------
    // Listener Methods

    /**
     * Internal listener invalidating tuples on changes of the temp.el. table
     */
    private class TemporalListener implements TableListener {

        @Override
        public void tableChanged(Table t, int start, int end, int col, int type) {
            // must come from parent
            if (t != TemporalElementManager.this.m_table)
                return;

            // switch on the event type
            switch (type) {
            case EventConstants.UPDATE: {
                // do nothing if update on all columns, as this is only
                // used to indicate a non-measurable update.
                if (col == EventConstants.ALL_COLUMNS) {
                    break;
                }

                // if primitive has changed, invalidate primitives
                if (col == TemporalElementManager.this.m_table
                        .getColumnNumber(TemporalDataset.KIND))
                    for (int r = start; r <= end; ++r) {
                        TemporalElementManager.this.invalidate(r);
                    }
                break;
            }
            case EventConstants.DELETE:
                if (col == EventConstants.ALL_COLUMNS) {
                    // entire rows deleted
                    for (int r = start; r <= end; ++r) {
                        TemporalElementManager.this.invalidate(r);
                    }
                } else {
                    // relevant column deleted
                    if (col == t.getColumnNumber(TemporalDataset.KIND)
                            || col == t.getColumnNumber(TemporalDataset.INF)
                            || col == t.getColumnNumber(TemporalDataset.SUP)
                            || col == t
                                    .getColumnNumber(TemporalDataset.GRANULARITY_ID))
                        TemporalElementManager.this.invalidateAll();
                }
                break;
            case EventConstants.INSERT:
                // nothing to do here
            } // switch
        }
    }
}
