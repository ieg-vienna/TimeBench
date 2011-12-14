package timeBench.data.relational;

/**
 * Any class can have this interface in order to provide the getTemporalDataset()
 * method to get a relational temporal dataset. It is designed with an action in
 * mind that created a new temporal dataset instance. Subsequent action can
 * ask a class that implements this interface for a current version of the dataset.
 * 
 * <p>
 * Added:         12-12-2011 / TL<br>
 * Modifications: 
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public interface TemporalDatasetProvider {
	public TemporalDataset getTemporalDataset();
}
