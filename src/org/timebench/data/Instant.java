package org.timebench.data;

import java.util.Collections;

import org.timebench.calendar.Granule;

/**
 * Instant in the relational view. Following the
 * <em>proxy tuple</em> pattern [Heer & Agrawala, 2006] it provides an object
 * oriented proxy for accessing a row of the temporal elements table.
 * 
 * <p>
 * This class assumes that the underlying data tuple is an instant.
 * 
 * @author Rind
 */
public class Instant extends AnchoredTemporalElement {

    /**
     * creates an invalid TemporalElement. Use {@link TemporalDataset} as a
     * factory!
     */
    protected Instant() {
    }

    @Override
    public Iterable<GenericTemporalElement> childElements() {
        // an instant should not have children (speedup?)
        return Collections.<GenericTemporalElement> emptyList();
    }

    @Override
    public int getChildCount() {
        // an instant should not have children (speedup?)
        return 0;
    }
    
    public void set(Granule granule) throws TemporalDataException {
        ((TemporalElementStore) getGraph()).set(this, granule);
    }
}
