package timeBench.action.analytical;

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
public interface MinMaxValuesProvider {
	Double getMinValue(int index);
	Double getMaxValue(int index);
}
