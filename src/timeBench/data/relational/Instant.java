package timeBench.data.relational;

import java.util.Collections;
import java.util.Iterator;

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
     * relational temporal elements should only be created by the {@link TemporalElementManager}
     */
    protected Instant() {
    }

    @Override
    public Iterator<TemporalElement> childElements() {
        // an instant should not have children (speedup?)
        return Collections.<TemporalElement> emptyList().iterator();
    }

    @Override
    public int getChildElementCount() {
        // an instant should not have children (speedup?)
        return 0;
    }
}
