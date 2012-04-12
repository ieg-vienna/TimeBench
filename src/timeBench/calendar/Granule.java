package timeBench.calendar;

import java.util.Date;

import timeBench.data.TemporalDataException;

/**
 * A granule of a {@link Granularity}. Use this for all methods on granules if possible.
 * For creation of multiple instances, factories are given in {@link Granularity}. 
 * <p>
 * Added:         2011-07-19 / TL<br>
 * Modifications: 2012-04-11 / TL / inf, sup absolute, identifier in context
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

	/**
	 * When creating a granule from inf and sup, just create the granule where inf is in.
	 */
	public final static int MODE_INF_GRANULE = 0;
	/**
	 * When creating a granule from inf and sup, just create the granule in the middle between inf and sup.
	 */
	public final static int MODE_MIDDLE_GRANULE = 1;
	/**
	 * When creating a granule from inf and sup, just create the granule where sup is in.
	 */
	public final static int MODE_SUP_GRANULE = 2;
	/**
	 * When creating a granule from inf and sup, just create it with exact parameters,
	 * even if it does not exist in this form in the granularity.
	 * Use with caution.
	 */
	public final static int MODE_FORCE = -1;

	/**
	 * Constructs a Granule containing inf in a given {@link Granularity}.
	 * @param inf the chronon that determines which granule is constructed
	 * @param sup currently ignored
	 * @param granularity the {@link Granularity} to which the granule belongs
	 * @throws TemporalDataException thrown when granularities are not fully implemented
	 */
	public Granule(long inf,long sup,Granularity granularity) throws TemporalDataException {
		this(inf,sup,MODE_INF_GRANULE,granularity);
	}

	/**
	 * Constructs a Granule from inf to sup using a given {@linkplain Granule#MODE_INF_GANULE mode} in a given {@link Granularity}.
	 * @param inf the chronon that determines the start of the granule constructed
	 * @param sup the chronon that determines the end of the granule constructed
	 * @param mode the {@linkplain Granule#MODE_INF_GANULE mode} used to construct the granule
	 * @param granularity granularity the {@link Granularity} to which the granule belongs
	 * @throws TemporalDataException TemporalDataException thrown when granularities are not fully implemented
	 */
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
		}
	}

	/**
	 * Constructs a Granule with a given identifier. Only when the context of the granularity is the size of the whole {@link Calendar},
	 * the result is a unique granule. Otherwise, the context of the granule at the {@link Calendar} origin, e.g. the birth of Christ or
	 * of Unix, is taken.
	 * @param identifier the identifier used to generate the granule
	 * @param granularity granularity the {@link Granularity} to which the granule belongs
	 */
	public Granule(long identifier,Granularity granularity) {
		this.identifier = identifier;
		this.granularity = granularity;
	}
	

	/**
	 * Constructs a Granule from a given {@link Date} in a given {@link Granularity}
	 * @param date the {@link Date} used to generate the granule
	 * @param granularity granularity the {@link Granularity} to which the granule belongs
	 */
	public Granule(Date date, Granularity granularity) throws TemporalDataException {
		Granule g2 = granularity.createGranule(date);
		this.inf = g2.getInf();
		this.sup = g2.getSup();
		this.granularity = granularity;
	}
	
	/**
	 * Return the inf of the granule.
	 * @return the inf
	 * @throws TemporalDataException thrown when granularities are not fully implemented
	 */
	public long getInf() throws TemporalDataException {
		if (inf == null)
			inf = granularity.createInf(this);
		return inf;
	}
	
	/**
	 * Return the sup of the granule.
	 * @return the sup
	 * @throws TemporalDataException thrown when granularities are not fully implemented
	 */
	public long getSup() throws TemporalDataException {
		if (sup == null)
			sup = granularity.createSup(this);
		return sup;
	}

	/**
	 * Return the {@link Granularity to which the granule belongs}.
	 * @return the {@link Granularity}
	 */
	public Granularity getGranularity() {
		return granularity;
	}

	/**
	 * Return the identifier of the granule. An identifier is a numeric label given in the context
	 * of the {@link Granularity}.
	 * @return the identifier
	 * @throws TemporalDataException thrown when granularities are not fully implemented
	 */
	public long getIdentifier() throws TemporalDataException {
		if (identifier == null)
			identifier = granularity.createGranuleIdentifier(this);
		return identifier;
	}

	/**
	 * Return the human readable label of the granule, given in the context
	 * of the {@link Granularity}.
	 * @return the label
	 * @throws TemporalDataException thrown when granularities are not fully implemented
	 */
	public String getLabel() throws TemporalDataException {
		if (label == null)
			label = granularity.createGranuleLabel(this);
		return label;
	}
}
