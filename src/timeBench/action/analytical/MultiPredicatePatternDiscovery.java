package timeBench.action.analytical;

import prefuse.action.Action;
import prefuse.data.expression.Predicate;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalDatasetProvider;

/**
 * 
 * 
 * <p>
 * Added:          / TL<br>
 * Modifications: 
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public class MultiPredicatePatternDiscovery extends Action implements TemporalDatasetProvider {

	TemporalDataset eventDataset;
	TemporalDataset patternDataset;
	Predicate[] predicates;	// Bertone: Intervals
	
	public MultiPredicatePatternDiscovery(TemporalDataset eventDataset,Predicate[] predicates) {
		this.eventDataset = eventDataset;
		this.predicates = predicates;
	}
	
	/* (non-Javadoc)
	 * @see prefuse.action.Action#run(double)
	 */
	@Override
	public void run(double frac) {
		
	}

	/* (non-Javadoc)
	 * @see timeBench.data.TemporalDatasetProvider#getTemporalDataset()
	 */
	@Override
	public TemporalDataset getTemporalDataset() {		
		return patternDataset;
	}	
}
