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
	private Long identifier = null;
	private String label = null;
	private Granularity granularity = null;

	public final static int MODE_INF_GRANULE = 0;
	public final static int MODE_MIDDLE_GRANULE = 1;
	public final static int MODE_SUP_GRANULE = 2;
	public final static int MODE_FORCE = -1;

	public Granule(long inf,long sup,Granularity granularity) throws TemporalDataException {
		this(inf,sup,MODE_INF_GRANULE,granularity);
	}
	
	public Granule(long inf,long sup,int mode,Granularity granularity) throws TemporalDataException {
		if(mode == MODE_FORCE) {
			this.inf = inf;
			this.sup = sup;
			this.granularity = granularity;
		} else {
			Granule g2 = granularity.createGranule(inf,sup,mode);
			this.inf = g2.getInf();
			this.sup = g2.getInf();
			this.granularity = granularity;
			// TODO identifier setzen? label setzen?		
		}
	}
	
	public Granule(long identifier,Granularity granularity) {
		this.identifier = identifier;
		this.granularity = granularity;
	}
	
	public long getInf() throws TemporalDataException {
		if (inf == null)
			inf = granularity.createInf(this);
		return inf;
	}
	
	public long getSup() throws TemporalDataException {
		if (sup == null)
			sup = granularity.createSup(this);
		return sup;
	}

	public Granularity getGranularity() {
		return granularity;
	}

	public long getIdentifier() throws TemporalDataException {
		if (identifier == null)
			identifier = granularity.createGranuleIdentifier(this);
		return identifier;
	}

	public String getLabel() throws TemporalDataException {
		if (label == null)
			label = granularity.createGranuleLabel(this);
		return label;
	}
}
