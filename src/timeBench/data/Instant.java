package timeBench.data;

import java.util.Collections;

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

    @Deprecated
    private static final int[] SUPPORTED_KINDS = { TemporalDataset.PRIMITIVE_INSTANT };
    
    /**
     * creates an invalid TemporalElement. Use {@link TemporalDataset} as a
     * factory!
     */
    @SuppressWarnings("deprecation")
    protected Instant() {
        setSupportedKinds(SUPPORTED_KINDS);
    }

    @Override
    public Iterable<GenericTemporalElement> childElements() {
        // an instant should not have children (speedup?)
        return Collections.<GenericTemporalElement> emptyList();
    }

    @Override
    public int getChildElementCount() {
        // an instant should not have children (speedup?)
        return 0;
    }
}
