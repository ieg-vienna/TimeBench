package timeBench.data;

import java.util.Collections;

/**
 * Span in the relational view. Following the <em>proxy tuple</em> pattern [Heer
 * & Agrawala, 2006] it provides an object oriented proxy for accessing a row of
 * the temporal elements table.
 * 
 * <p>
 * This class assumes that the underlying data tuple is an span.
 * 
 * @author Rind
 */
public class Span extends UnanchoredTemporalElement {

    /**
     * relational temporal elements should only be created by the
     * {@link TemporalDataset}
     */
    protected Span() {
    }

    @Override
    public Iterable<GenericTemporalElement> childElements() {
        // a span should not have children (speedup?)
        return Collections.<GenericTemporalElement> emptyList();
    }

    @Override
    public int getChildCount() {
        // a span should not have children (speedup?)
        return 0;
    }
}
