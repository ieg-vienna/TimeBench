package timeBench.calendar;

import timeBench.data.TemporalDataException;
import timeBench.data.oo.Interval;

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
	
	public Granule(long inf,long sup,Granularity granularity) throws TemporalDataException {
		this(inf,sup,granularity,false);
	}

	public Granule(long inf,long sup,Granularity granularity, boolean forceNoCleaning) throws TemporalDataException {
		if(forceNoCleaning) {
			this.inf = inf;
			this.sup = sup;
			this.granularity = granularity;
		} else {
			Granule g2 = granularity.parseInfToGranule(inf);
			this.inf = g2.getInf();
			this.sup = g2.getInf();
			this.granularity = granularity;
		}
	}
	
	public Granule(Interval interval) throws TemporalDataException {
		this(interval.getInf(),interval.getSup(),interval.getGranularity());
	}
	
	public Granule(long count,Granularity granularity) {
		this.count = count;
		this.granularity = granularity;
		}
	
	public long getInf() throws TemporalDataException {
		if (inf == null)
			throw new TemporalDataException("Conversion of granule count to inf not implemented yet.");
		else
			return inf;
	}
	
	public long getSup() throws TemporalDataException {
		if (sup == null)
			throw new TemporalDataException("Conversion of granule count to sup not implemented yet.");
		else
			return sup;
	}

	public Granule mapToGranularityAsGranule(int targetGranularityIdentifier) throws TemporalDataException {
		return new Granule(inf,sup,new Granularity(granularity.getCalendar(), targetGranularityIdentifier));
	}

	public Granularity getGranularity() {
		return granularity;
	}
}
