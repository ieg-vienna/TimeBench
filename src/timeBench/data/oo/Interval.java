package timeBench.data.oo;

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
public class Interval extends AnchoredTemporalPrimitive {
	public Interval(Instant start,Instant stop) {
		parts.add(start);
		parts.add(stop);
	}
}
