package timeBench.data;

import java.util.ConcurrentModificationException;
import java.util.Iterator;

import ieg.prefuse.data.ParentChildNode;

import org.apache.log4j.Logger;
import org.timebench.util.lang.CustomIterable;

/**
 * Relational view of the temporal object. Following the <em>proxy tuple</em>
 * pattern [Heer & Agrawala, 2006] it provides an object oriented proxy for
 * accessing a row of the occurrences table in a {@link TemporalDataset}.
 * 
 * @author Rind
 *
 */
public class TemporalObject extends ParentChildNode {
    
    static Logger logger = Logger.getLogger(TemporalObject.class);
    
    // predefined column names for temporal objects (similar to VisualItem)
    
    /**
     * the identifier data field for temporal objects. Primary key of the
     * temporal object table.
     */
    public static final String ID = "_id";

    /**
     * the data field containing the identifier of the temporal element. Foreign
     * key to the temporal element table.
     */
    public static final String TEMPORAL_ELEMENT = "_temporal"; 
    
    /**
     * the data field containing the identifier of the temporal element. Foreign
     * key to the temporal element table.
     */
    public static final String TEMPORAL_ELEMENT_ID = TemporalTable.idColumnNameFor(TEMPORAL_ELEMENT); 
    
    /**
     * creates an invalid TemporalObject. Use {@link TemporalDataset} as a
     * factory!
     */
    // make constructor protected: TemporalObjectManager extends TupleManager
    public TemporalObject() {
    }

    /**
     * Get the temporal dataset of which this object is a member.
     * Actually this is identical with {@link #getGraph()}.
     * 
     * @return the backing temporal dataset
     */
    public TemporalDataset getTemporalDataset() {
        return (TemporalDataset) super.getGraph();
    }

    /**
     * Get the temporal element id.
     * 
     * @return the id
     */
    public long getId() {
        return super.getLong(TemporalObject.ID);
    }

    /**
     * @return the temporal element
     */
    public TemporalElement getTemporalElement() {
        // use typed primitive which is cached?
        return (TemporalElement) get(TEMPORAL_ELEMENT);
//        long teId = super.getLong(TemporalObject.TEMPORAL_ELEMENT_ID);
//        // the temporal object graph, is actually the temporal dataset 
//        return ((TemporalDataset) m_graph).getTemporalElement(teId);
    }

    /**
     * Get an iterator over all temporal objects that are parent of this
     * temporal object.
     * 
     * @return an object, which provides an iterator over parents
     */
    @SuppressWarnings("unchecked")
    public Iterable<TemporalObject> parentObjects() {
        return new CustomIterable(super.outNeighbors());
    }

    /**
     * Get the first or only parent temporal object.
     * 
     * @return a temporal object that is parent of this temporal object or
     *         <tt>null</tt>.
     */
    public TemporalObject getFirstParentObject() {
        return (TemporalObject) super.getFirstParent();
    }
    
    /**
     * Get an iterator over all temporal objects that are children of this
     * temporal object.
     * 
     * @return an object, which provides an iterator over children
     */
    @SuppressWarnings("unchecked")
    public Iterable<TemporalObject> childObjects() {
        return new CustomIterable(super.inNeighbors());
    }

    /**
     * Get the first or only child temporal object.
     * 
     * @return a temporal object that is child of this temporal object or
     *         <tt>null</tt>.
     */
    public TemporalObject getFirstChildObject() {
        return (TemporalObject) super.getFirstChild();
    }
    
    /**
     * creates a human-readable string from a {@link TemporalObject}.
     * <p>
     * Example: TemporalObject[id=5, temporal id=3]
     * 
     * @return a string representation
     */
    @Override
    public String toString() {
        return "TemporalObject[id=" + super.getLong(ID) + ", temporal id="
                + super.getLong(TemporalObject.TEMPORAL_ELEMENT_ID)
                + "]";
    }

	/**
	 * Returns the level of the TemporalObject if it belongs to a tree. Otherwise,
	 * and for root nodes, it returns 0.
	 * @return the tree level
	 */
	public int getTreeLevel() {
		@SuppressWarnings("rawtypes")
		Iterator parents = super.outNeighbors(); 
		if (parents.hasNext()) {
			return ((TemporalObject) parents.next()).getTreeLevel() + 1; 
		} else {
			return 0;
		}
	}
	
    public boolean isRoot() {
        return getTemporalDataset().isRoot(this);
    }

    /**
     * Note, that you cannot unset a root object while iterating over roots.
     * This would lead to a {@link ConcurrentModificationException}. Use
     * {@link Iterator#remove()} instead.
     * 
     * @param root
     */
    public void setRoot(boolean root) {
        getTemporalDataset().setRoot(this, root);
    }

	public Object getMin(int idx) {
		return getMin(this.getColumnName(idx));
	}

	public void setMin(int idx, Object value) {
		setMin(this.getColumnName(idx),value);
	}

	public Object getMax(int idx) {
		return getMax(this.getColumnName(idx));
	}

	public void setMax(int idx, Object value) {
		setMax(this.getColumnName(idx),value);
	}

	public int getKind(int idx) {
		return getKind(this.getColumnName(idx));
	}

	public void setKind(int idx, int value) {
		setKind(this.getColumnName(idx),value);
	}
	
	public Object getMin(String field) {
		String minField = field+".minValue";
		if (getColumnIndex(minField) >= 0)
			return this.get(minField);
		else
			return null;
	}
	
	public void setMin(String field, Object value) {
		String minField = field+".minValue";
		ensureFieldExistence(minField,m_table.getSchema().getColumnType(field));
		set(minField,value);
	}

	public Object getMax(String field) {
		String maxField = field+".maxValue";
		if (getColumnIndex(maxField) >= 0)
			return get(maxField);
		else
			return null;
	}

	public void setMax(String field, Object value) {
		String maxField = field+".maxValue";
		ensureFieldExistence(maxField,m_table.getSchema().getColumnType(field));
		set(maxField,value);
	}
	
	public static final String PREDICATES_COLUMN = "_predicates";
	
	public static final int INVALID = -1;
	public static final int MEASURED_DATA = 0x00;
	public static final int COMPUTER_GENERATED_DATA = 0x01;
	public static final int USER_GENERATED_DATA = 0x02;
	public static final int COMPUTER_GENERATED_AND_USER_GENERATED_PARTS = 0x03;
	
	public static final int TEMPLATE_ = 0x10;
		
	public int getKind(String field) {
		String kindField = field+".kind";
		if (getColumnIndex(kindField) >= 0)
			return getInt(kindField);
		else
			return INVALID;
	}
	
	public void setKind(String field, int value) {
		String kindField = field+".kind";
		ensureFieldExistence(kindField,m_table.getSchema().getColumnType(field));
		setInt(kindField,value);
	}
	
	@SuppressWarnings("rawtypes")
    private void ensureFieldExistence(String field,Class type) {
		
		Class existingType = m_table.getSchema().getColumnType(field);
		if (existingType == null)
			this.m_table.addColumn(field, type);
		else if (existingType != type)		
			throw new IllegalStateException("Field "+field+" exists but is of the wrong type");		
	}	
}
