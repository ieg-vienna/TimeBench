package timeBench.calendar;

import timeBench.data.TemporalDataException;

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
public class Granule {
	private Long inf = null;
	private Long sup = null;
	private Long count = null;
	private String label = null;
	private Granularity granularity = null;
	
	Granule(long inf,long sup,Granularity granularity) {
		this.inf = inf;
		this.sup = sup;
		this.granularity = granularity;
	}
	
	Granule(long count,Granularity granularity) {
		this.count = count;
		this.granularity = granularity;
		}
	
	long getInf() throws TemporalDataException {
		if (inf == null)
			throw new TemporalDataException("Conversion of granule count to inf not implemented yet.");
		else
			return inf;
	}
	
	long getSup() throws TemporalDataException {
		if (sup == null)
			throw new TemporalDataException("Conversion of granule count to sup not implemented yet.");
		else
			return sup;
	}
}
