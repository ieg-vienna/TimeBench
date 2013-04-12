package timeBench.data;

import ieg.prefuse.data.ParentChildNode;
import ieg.util.lang.CustomIterable;

import prefuse.data.Table;
import prefuse.data.tuple.TableNode;
import timeBench.calendar.Granule;

/**
 * Relational view of the temporal element. Following the <em>proxy tuple</em>
 * pattern [Heer & Agrawala, 2006] it provides an object oriented proxy for
 * accessing a row of the temporal elements table.
 * 
 * <p>
 * Note that this class provides only a thin layer over {@link TableNode} and
 * {@link TableTuple} for increased convenience. Methods such as
 * {@link #set(int, Object)} can still be called, which might put this object or
 * the whole temporal dataset in an inconsistent state.
 * 
 * @author Rind
 */
public abstract class TemporalElement extends ParentChildNode implements Comparable<TemporalElement> {

	protected static TemporalElementStore temporalDataHeap = new TemporalElementStore();
	
    // predefined column names for temporal elements (similar to VisualItem)
    /**
     * the identifier data field for temporal elements. Primary key of the
     * temporal element table.
     */
    public static final String ID = "id";

    /**
     * the &quot;inf&quot; data field for temporal elements.
     */
    public static final String INF = "inf";

    /**
     * the &quot;sup&quot; data field for temporal elements.
     */
    public static final String SUP = "sup";

    /**
     * the &quot;granularity id&quot; data field for temporal elements.
     */
    public static final String GRANULARITY_ID = "granularityID";

    /**
     * the &quot;granularity context id&quot; data field for temporal elements.
     */
    public static final String GRANULARITY_CONTEXT_ID = "granularityContextID";

    /**
     * the &quot;kind&quot; data field for temporal elements.
     */
    public static final String KIND = "kind";

    
    /**
     * creates an invalid TemporalElement. Use {@link TemporalDataset} as a
     * factory!
     */
    protected TemporalElement() {
    }
    
    /**
     * Initialize a new temporal element backed by a node table. This method is
     * used by the appropriate TupleManager instance, and should not be called
     * directly by client code, unless by a client-supplied custom TupleManager.
     * 
     * @param table
     *            the backing table
     * @param graph
     *            the backing graph
     * @param row
     *            the row in the node table to which this temporal element
     *            corresponds
     */
    protected void init(Table table, TemporalElementStore graph, int row) {
        super.init(table, graph, row);
    }
    
    
    /**
     * Creates a TemporalElement on the temporal data heap. Note that
     * currently, these TemporalElements have to be cleared manually
     * from there as there is no full support for weak references
     * in Java.
     * 
     * @param inf
     *            the lower end of the temporal element
     * @param sup
     *            the upper end of the temporal element
     * @param granularityId
     *            the granularityID of the temporal element
     * @param granularityContextId
     *            the granularityContextID of the temporal element
     * @param kind
     *            the kind of the temporal element
     * @return the created temporal element
     */
    public static TemporalElement createOnHeap(long inf, long sup, int granularityId,
    		int granularityContextId, int kind) {
    	return temporalDataHeap.addTemporalElement(inf, sup, granularityId, granularityContextId, kind);
    }
    
    /**
     * Destroys an instance of a (this) TemporalElement on the heap.
     * If it is not on the heap, nothing is done to prevent this, so prefuse will
     * throw an IllegalArgumentException. External references to
     * this TemporalElement should be cleared by the calling code.  
     */
    public void destroyFromHeap() {
    	temporalDataHeap.removeTemporalElement(this);
    }

    /**
     * Indicates if the temporal element is anchored in time.
     * 
     * <p>
     * Anchored temporal elements represent one or more granules on an
     * underlying granularity. They have an infimum and an supremum in the
     * discrete time domain.
     * 
     * @return true if the element is anchored
     */
    public abstract boolean isAnchored();

    /**
     * Gets the length of the temporal element.
     * 
     * <p>
     * This can be either
     * <li>the number of chronons in the bottom granularity for anchored
     * temporal elements or
     * <li>the number of granules in the current granularity for unanchored
     * temporal elements.
     * 
     * @return the length of the temporal element
     */
    // TODO does it make sense to have a common method for all temporal elements?
    public abstract long getLength();
    
    /**
     * Sets the length of the temporal element.
     * 
     * <p>
     * This can be either
     * <li>the number of chronons in the bottom granularity for anchored
     * temporal elements or
     * <li>the number of granules in the current granularity for unanchored
     * temporal elements.
     * 
     * @value the length of the temporal element
     */
    // TODO does it make sense to have a common method for all temporal elements?
    public abstract void setLength(long value);

    /**
     * Get the temporal dataset of which this element is a member.
     * 
     * @return the backing temporal dataset
     */
    public TemporalElementStore getTemporalElementStore() {
        return (TemporalElementStore) this.m_graph;
    }

    /**
     * Get the temporal element id.
     * 
     * @return the id
     */
    public long getId() {
        return super.getLong(TemporalElement.ID);
    }
    
    /**
     * Get the granularity id.
     * 
     * @return the granularity id
     */
    public int getGranularityId() {
        return super.getInt(TemporalElement.GRANULARITY_ID);
    }
    
    /**
     * Get the granularity context id.
     * 
     * @return the granularity context id
     */
    public int getGranularityContextId() {
        return super.getInt(TemporalElement.GRANULARITY_CONTEXT_ID);
    }

    public static final int SPAN = 0x00;
    public static final int SET = 0x01;
    public static final int INSTANT = 0x02;
    public static final int INTERVAL = 0x03;

    public static final int RECURRING_INSTANT = 0x12;
    public static final int RECURRING_INTERVAL = 0x13;
    
    // Byte 1          Byte 2          Byte 3              Byte 4
    // 0x00 normal
    // 0x01 template  
    //                subkind
    //                         data primitive  template primitive
    //
    // checks agains non-template -> false
    // instant template granularities must be smaller or equal to instant data granularities
    // d chronons before -> recalculate, check for starts
    public static final int TEMPLATE_BEFORE_INSTANT_INSTANT                              = 0x01000202;
    public static final int TEMPLATE_AFTER_INSTANT_INSTANT                               = 0x01010202;    
    public static final int TEMPLATE_STARTS_INSTANT_INSTANT                              = 0x01020202;
    public static final int TEMPLATE_FINISHES_INSTANT_INSTANT                            = 0x01030202;
    public static final int TEMPLATE_DURING_INSTANT_INSTANT                              = 0x01040202;
    public static final int TEMPLATE_OUTSIDE_INSTANT_INSTANT                             = 0x01050202;
    public static final int TEMPLATE_OVERLAP_START_INSTANT_INSTANT                       = 0x01060202;
    public static final int TEMPLATE_OVERLAP_FINISH_INSTANT_INSTANT                      = 0x01070202;
    public static final int TEMPLATE_BEFORE_INTERVAL_INSTANT                             = 0x01000302;
    public static final int TEMPLATE_AFTER_INTERVAL_INSTANT                              = 0x01010302;    
    public static final int TEMPLATE_STARTS_INTERVAL_INSTANT                             = 0x01020302;
    public static final int TEMPLATE_FINISHES_INTERVAL_INSTANT                           = 0x01030302;
    public static final int TEMPLATE_OUTSIDE_INTERVAL_INSTANT                            = 0x01050302;
    public static final int TEMPLATE_OVERLAP_START_INTERVAL_INSTANT                      = 0x01060302;
    public static final int TEMPLATE_OVERLAP_FINISH_INTERVAL_INSTANT                     = 0x01070302;
    public static final int TEMPLATE_BEFORE_INTERVAL_INTERVAL                            = 0x01000303;
    public static final int TEMPLATE_AFTER_INTERVAL_INTERVAL                             = 0x01010303;    
    public static final int TEMPLATE_STARTS_INTERVAL_INTERVAL                            = 0x01020303;
    public static final int TEMPLATE_FINISHES_INTERVAL_INTERVAL                          = 0x01030303;
    public static final int TEMPLATE_DURING_INTERVAL_INTERVAL                            = 0x01040303;
    public static final int TEMPLATE_OUTSIDE_INTERVAL_INTERVAL                           = 0x01050303;
    public static final int TEMPLATE_OVERLAP_START_INTERVAL_INTERVAL                     = 0x01060303;
    public static final int TEMPLATE_OVERLAP_FINISH_INTERVAL_INTERVAL                    = 0x01070203;
    public static final int TEMPLATE_ASLONGAS_SPAN_SPAN                                  = 0x01100000;
    public static final int TEMPLATE_ASLONGAS_INTERVAL_SPAN     		                 = 0x01100300;
    public static final int TEMPLATE_STARTS_RECURRING_INSTANT_RECURRING_INSTANT          = 0x01021212;
    public static final int TEMPLATE_FINISHES_RECURRING_INSTANT_RECURRING_INSTANT        = 0x01031212;
    public static final int TEMPLATE_OUTSIDE_RECURRING_INSTANT_RECURRING_INSTANT         = 0x01041212;
    public static final int TEMPLATE_OVERLAP_START_RECURRING_INSTANT_RECURRING_INSTANT   = 0x01061212;
    public static final int TEMPLATE_OVERLAP_FINISH_RECURRING_INSTANT_RECURRING_INSTANT  = 0x01071212;
    public static final int TEMPLATE_STARTS_RECURRING_INTERVAL_RECURRING_INSTANT         = 0x01021312;
    public static final int TEMPLATE_FINISHES_RECURRING_INTERVAL_RECURRING_INSTANT       = 0x01031312;
    public static final int TEMPLATE_DURING_RECURRING_INTERVAL_RECURRING_INTERVAL        = 0x01041313;
    public static final int TEMPLATE_OUTSIDE_RECURRING_INTERVAL_RECURRING_INSTANT        = 0x01041312;
    public static final int TEMPLATE_OVERLAP_START_RECURRING_INTERVAL_RECURRING_INSTANT  = 0x01061312;
    public static final int TEMPLATE_OVERLAP_FINISH_RECURRING_INTERVAL_RECURRING_INSTANT = 0x01071312;
    public static final int TEMPLATE_ASLONGAS_RECURRING_INTERVAL_SPAN     		         = 0x01101300;
        
    /**
     * Get the kind of temporal element (0 = span, 1 = set/temporal element, 2 =
     * instant, 3 = interval)
     * 
     * @return the kind of temporal element
     */
    public int getKind() {
        return super.getInt(TemporalElement.KIND);
    }
    
    /**
     * converts to a generic temporal element.
     * 
     * @return a generic temporal element of the same underlying data row.
     */
    public GenericTemporalElement asGeneric() {
        return ((TemporalElementStore) this.m_graph).getTemporalElementByRow(this.m_row);
    }

    /**
     * converts to a temporal primitive.
     * 
     * @return a temporal primitive of the same underlying data row.
     */
    public TemporalElement asPrimitive() {
        return ((TemporalElementStore) this.m_graph).getTemporalPrimitiveByRow(this.m_row);
    }
    
    /**
     * returns the first instant of temporal element.
     * for instants, return this.
     * for intervals, return begin.     
     * for others, does not interpret the semantics, just goes down to first child and tries again
     * 
     * @return the first instant
     * @throws TemporalDataException 
     */
    public Instant getFirstInstant() throws TemporalDataException {
    	TemporalElement thisElement = this.asPrimitive();
    	if (thisElement instanceof Instant) {
    		return (Instant)thisElement;
    	} else if (thisElement instanceof Interval) {
    		return ((Interval)thisElement).getBegin();	// TODO check if this is implemented yet
    	} else if (thisElement.getFirstChild() != null) {
    		return ((TemporalElement)thisElement.getFirstChild()).getFirstInstant();
    	}
    	
    	return null;
    }
    
    /**
     * returns the last instant of temporal element.
     * for instants, return this.
     * for intervals, return end.     
     * for others, does not interpret the semantics, just goes down to first child and tries again
     * 
     * @return the first instant
     * @throws TemporalDataException 
     */
    public Instant getLastInstant() throws TemporalDataException {
    	TemporalElement thisElement = this.asPrimitive();
    	if (thisElement instanceof Instant) {
    		return (Instant)thisElement;
    	} else if (thisElement instanceof Interval) {
    		return ((Interval)thisElement).getEnd();	// TODO check if this is implemented yet
    	} else if (thisElement.getLastChild() != null) {
    		return ((TemporalElement)thisElement.getLastChild()).getLastInstant();
    	}
    	
    	return null;
    }
    
    /**
     * Get an iterator over all temporal objects occurring with this temporal 
     * element.
     * 
     * @return temporal objects occurring with the temporal element
     */
    public Iterable<TemporalObject> temporalObjects() {
        return ((TemporalElementStore) this.m_graph).getTemporalObjectsByElementId(getId());
    }
    
    public Iterable<TemporalObject> temporalObjects(TemporalDataset tmpds) {
        return tmpds.getTemporalObjectsByElementId(getId());
    }
    
    /**
     * Get an iterator over all temporal elements that are parents of this
     * temporal element.
     * 
     * @return an iterator over parents
     */
    @SuppressWarnings("unchecked")
    public Iterable<GenericTemporalElement> parentElements() {
        return new CustomIterable(super.parents());
    }

    /**
     * Get an iterator over all temporal elements that are children of this
     * temporal element.
     * 
     * @return an iterator over children
     */
    @SuppressWarnings("unchecked")
    public Iterable<GenericTemporalElement> childElements() {
        return new CustomIterable(super.children());
    }

    /**
     * Get the first or only child temporal element as a primitive.
     * 
     * @return a temporal element that is child of this temporal element or
     *         <tt>null</tt>.
     */
    public TemporalElement getFirstChildPrimitive() {
        int child = getGraph().getChildRow(m_row, 0);
        if (child > -1) {
            return ((TemporalElementStore) this.m_graph).getTemporalPrimitiveByRow(child);
        } else {
            return null;
        }
    }
    
    /**
     * Get the last or only child temporal element as a primitive.
     * 
     * @return a temporal element that is child of this temporal element or
     *         <tt>null</tt>.
     */
    public TemporalElement getLastChildPrimitive() {
        int child = getGraph().getChildRow(m_row, getChildCount() - 1);
        if (child > -1) {
            return ((TemporalElementStore) this.m_graph).getTemporalPrimitiveByRow(child);
        } else {
            return null;
        }
    }
    
    /**
     * Gets the first granule of an anchored temporal element. For an
     * {@link Instant}, the granule represents the time of the instant. If it is
     * unanchored, <tt>null</tt> is returned. Granules are cached.
     * 
     * @return the first granule
     * @throws TemporalDataException
     * @see timeBench.data.util.GranuleCache
     */
    public Granule getGranule() throws TemporalDataException {
        return getGranules()[0];
    }
    
    /**
     * Gets a list of granules of an anchored temporal element. If it is
     * unanchored, <tt>null</tt> is returned. Granules are cached.
     * 
     * @return the first granule
     * @throws TemporalDataException
     * @see timeBench.data.util.GranuleCache
     */
    public Granule[] getGranules() throws TemporalDataException {
        return ((TemporalElementStore) this.m_graph).getGranulesByRow(m_row);
    }

    /**
     * creates a human-readable string from a {@link TemporalElement}.
     * <p>
     * Example: GenericTemporalElement[id=2, inf=3, sup=14, granularityId=1, granularityContextId=1, kind=1]
     * 
     * @return a string representation
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[id=" + getId() + ", inf="
                + super.getLong(TemporalElement.INF) + ", sup="
                + super.getLong(TemporalElement.SUP) + ", granularityId="
                + getGranularityId() + ", granularityContextId="
                + getGranularityContextId() + ", kind=" + getKind() + "]";
    }

    @Override
    public int compareTo(TemporalElement o) {
        return Long.valueOf(this.getId()).compareTo(o.getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TemporalElement) {
            TemporalElement el = (TemporalElement) obj;
            return this.getId() == el.getId() && this.getGraph() == el.getGraph();
        } else {
            return false;
        }
    }
}
