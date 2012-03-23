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
	Double getMinValue(int level,int index);
	Double getMaxValue(int level,int index);
}
