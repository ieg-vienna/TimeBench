package timeBench.calendar;

import org.eclipse.persistence.oxm.annotations.XmlInverseReference;
import timeBench.data.TemporalDataException;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

/**
 * A granularity of a {@link Calendar}. Contains factories for multiple {@link Granule} instances.
 * <p>
 * Added:         2011-07-19 / TL<br>
 * Modifications: 2012-04-11 / TL / inf, sup absolute, globalGranularityIdentifier in context
 * </p>
 *
 * @author Tim Lammarsch
 */
@XmlRootElement(name = "granularity")
@XmlAccessorType(XmlAccessType.FIELD)
public class Granularity {
	@XmlElement(required = true)
	private int identifier;

	@XmlElement(required = true)
	private int contextIdentifier;
	
	@XmlElement(required = true)
	private String granularityLabel;

	@XmlElement(required = true)
	private Hashtable<Locale,String> granularityLabels;
	
	@XmlAttribute
	private Boolean isBottomGranularity = false;



	/**
	 * Empty constructor for JAXB
	 */
	public Granularity() {
	}

	/**
	 * Constructs a Granularity using a given {@link Calendar}, with identifiers for granularity
	 * and context given as integers from the {@link CalendarManager}.
	 *
	 * @param calendar                    The {@link Calendar} the granularity belongs to.
	 * @param globalGranularityIdentifier The globalGranularityIdentifier of the granularity whose meaning depends on the {@link CalendarManager}.
	 * @param localGranularityContextIdentifier           The context globalGranularityIdentifier of the granularity whose meaning depends on the {@link CalendarManager}.
	 */
//	public Granularity(Calendar calendar, int globalGranularityIdentifier, int localGranularityContextIdentifier) {
//		this.calendar = calendar;
//		this.globalGranularityIdentifier = globalGranularityIdentifier;
////		this.contextLocalIdentifier = localGranularityContextIdentifier;
//		//TODO
//	}

	/**
	 * Return {@link Calendar} this granularity belongs to.
	 *
	 * @return the {@link Calendar} this granularity belongs to
	 */
	public Calendar getCalendar() {
		return calendar;
	}

	/**
	 * Returns globalGranularityIdentifier of this granularity whose meaning depends on the {@link CalendarManager}.
	 *
	 * @return globalGranularityIdentifier of this granularity whose meaning depends on the {@link CalendarManager}
	 */
	public int getGlobalGranularityIdentifier() {
		return globalGranularityIdentifier;
	}

	public boolean isInTopContext() {
		return this.equals(calendar.getTopGranularity());
//		return contextLocalIdentifier == calendar.getTopGranularity().getGlobalGranularityIdentifier();
	}

	/**
	 * Constructs a {@link Granule} from a given {@link Date}. Consider using the adequate constructor of
	 * {@link Granule} instead.
	 *
	 * @param date the {@link Date} used to generate the granule
	 * @return the constructed {@link Granule}
	 * @throws TemporalDataException TemporalDataException thrown when granularities are not fully implemented
	 */
	protected Granule createGranule(Date date) throws TemporalDataException {
		return calendar.createGranule(date, this);
	}

	/**
	 * Constructs a {@link Granule} from inf to sup using a given {@linkplain Granule#MODE_INF_GRANULE mode}.
	 * Consider using the adequate constructor of
	 * {@link Granule} instead.
	 *
	 * @param inf  the chronon that determines the start of the granule constructed
	 * @param sup  the chronon that determines the end of the granule constructed
	 * @param mode the {@linkplain Granule#MODE_INF_GRANULE mode} used to construct the granule
	 * @return the constructed {@link Granule}
	 * @throws TemporalDataException TemporalDataException thrown when granularities are not fully implemented
	 */
	protected Granule createGranule(long inf, long sup, int mode) throws TemporalDataException {
		return calendar.createGranule(inf, sup, mode, this);
	}

	/**
	 * Constructs several {@link Granule} objects from inf to sup that are at least partly in the given interval.
	 *
	 * @param inf the chronon that determines the start of the {@link Granule} range constructed
	 * @param sup the chronon that determines the end of the {@link Granule} range constructed
	 * @return the constructed array of {@link Granule}
	 * @throws TemporalDataException TemporalDataException thrown when granularities are not fully implemented
	 */
	public Granule[] createGranules(long inf, long sup) throws TemporalDataException {
		return this.createGranules(inf, sup, 0.0);
	}

	/**
	 * Constructs several {@link Granule} objects from inf to sup that are at least partly in the given interval with
	 * a coverage of a least a given fraction.
	 *
	 * @param inf   the chronon that determines the start of the {@link Granule} range constructed
	 * @param sup   the chronon that determines the end of the {@link Granule} range constructed
	 * @param cover the coverage fraction of a granule needed to be included in the result
	 * @return the constructed array of {@link Granule}
	 * @throws TemporalDataException TemporalDataException thrown when granularities are not fully implemented
	 */
	public Granule[] createGranules(long inf, long sup, double cover) throws TemporalDataException {
		return calendar.createGranules(inf, sup, cover, this);
	}

	/**
	 * Constructs several {@link Granule} objects from other {@link Granule} objects that can (and most likely
	 * will) be in a different granularity. All {@link Granule} that are at least partly covered are
	 * returned.
	 *
	 * @param granules the array of {@link Granule} used as source
	 * @return the constructed array of {@link Granule}
	 * @throws TemporalDataException TemporalDataException thrown when granularities are not fully implemented
	 */
	public Granule[] createGranules(Granule[] granules) throws TemporalDataException {
		return this.createGranules(granules, 0.0);
	}

	/**
	 * Constructs several {@link Granule} objects from other {@link Granule} objects that can (and most likely
	 * will) be in a different granularity. All {@link Granule} with
	 * a coverage of a least a given fraction are returned.
	 *
	 * @param granules the array of {@link Granule} used as source
	 * @param cover    the coverage fraction of a granule needed to be included in the result
	 * @return the constructed array of {@link Granule}
	 * @throws TemporalDataException TemporalDataException thrown when granularities are not fully implemented
	 */
	public Granule[] createGranules(Granule[] granules, double cover) throws TemporalDataException {
		return calendar.createGranules(granules, cover, this);
	}

	/**
	 * Calculate and return the globalGranularityIdentifier of a {@link Granule}. An globalGranularityIdentifier is a numeric granularityLabel given in the context
	 * of the granularity. Consider using the adequate method of
	 * {@link Granule} instead.
	 *
	 * @return the globalGranularityIdentifier
	 * @throws TemporalDataException thrown when granularities are not fully implemented
	 */
	protected long createGranuleIdentifier(Granule granule) throws TemporalDataException {
		return calendar.createGranuleIdentifier(granule);
	}

	/**
	 * Calculate and return the human readable granularityLabel of a {@link Granule}.
	 * Consider using the adequate method of
	 * {@link Granule} instead.
	 *
	 * @return the granularityLabel
	 * @throws TemporalDataException thrown when granularities are not fully implemented
	 */
	protected String createGranuleLabel(Granule granule) throws TemporalDataException {
		return calendar.createGranuleLabel(granule);
	}

	/**
	 * Calculate and return the inf of a {@link Granule}.
	 *
	 * @return the inf
	 * @throws TemporalDataException thrown when granularities are not fully implemented
	 */
	public Long createInf(Granule granule) throws TemporalDataException {
		return calendar.createInf(granule);
	}

	/**
	 * Calculate and return the sup of a {@link Granule}.
	 *
	 * @return the sup
	 * @throws TemporalDataException thrown when granularities are not fully implemented
	 */
	public Long createSup(Granule granule) throws TemporalDataException {
		return calendar.createSup(granule);
	}

	/**
	 * Provide the minimum globalGranularityIdentifier value that granules of this granularity can assume.
	 *
	 * @return the minimum granule globalGranularityIdentifier value
	 * @throws TemporalDataException thrown when granularity has illegal identifiers
	 */
	public long getMinGranuleIdentifier() throws TemporalDataException {
		return calendar.getMinGranuleIdentifier(this);
	}

	/**
	 * Provide the maximum globalGranularityIdentifier value that granules of this granularity can assume.
	 *
	 * @return the maximum granule globalGranularityIdentifier value
	 * @throws TemporalDataException thrown when granularity has illegal identifiers
	 */
	public long getMaxGranuleIdentifier() throws TemporalDataException {
		return calendar.getMaxGranuleIdentifier(this);
	}

	public long getMaxLengthInIdentifiers() throws TemporalDataException {
		return calendar.getMaxLengthInIdentifiers(this);
	}

	public boolean contains(Granule granule, long chronon) throws TemporalDataException {
		return calendar.contains(granule, chronon);
	}

	public void setGlobalGranularityIdentifier(int globalGranularityIdentifier) {
		this.globalGranularityIdentifier = globalGranularityIdentifier;
	}

	public String getGranularityLabel() {
		return granularityLabel;
	}

	public void setCalendar(Calendar calendar) {
		this.calendar = calendar;
	}

	public Granularity getContextGranularity() {
		return contextGranularity;
	}

	public List<GranularityIdentifier> getPermittedContextIdentifiers() {
		return permittedContextIdentifiers;
	}

	public Boolean getTopGranularity() {
		return isTopGranularity;
	}

	public Boolean getBottomGranularity() {
		return isBottomGranularity;
	}

	public GranularityIdentifier getIdentifier() {
		return identifier;
	}

	public Hashtable<GranularityIdentifier, Granularity> getPermittedContextGranularities(){
		return permittedContextGranularities;
	}

	public void setPermittedContextGranularities(Hashtable<GranularityIdentifier, Granularity> permittedContextGranularities) {
		this.permittedContextGranularities = permittedContextGranularities;
	}

	public void setGranularityLabel(String granularityLabel) {
		this.granularityLabel = granularityLabel;
	}

	public void setIdentifier(GranularityIdentifier identifier) {
		this.identifier = identifier;
	}

	public void setTopGranularity(Boolean topGranularity) {
		isTopGranularity = topGranularity;
	}

	public void setBottomGranularity(Boolean bottomGranularity) {
		isBottomGranularity = bottomGranularity;
	}

	public void setPermittedContextIdentifiers(List<GranularityIdentifier> permittedContextIdentifiers) {
		this.permittedContextIdentifiers = permittedContextIdentifiers;
	}

	public Granularity setIntoContext(Granularity granularity) throws TemporalDataException{
		if (!calendar.containsGranularity(granularity)){
			throw new TemporalDataException("Failed to set granularity: " + this + " into context of: " + granularity +
					" , granularities are not of the same calendar.");
		}

		if (!permittedContextGranularities.containsValue(granularity)){
			throw new TemporalDataException("Failed to set granularity: " + this + " into context of: " + granularity +
					" , context granularity is not permitted.");
		}

		Granularity newGranularity = new Granularity();
		newGranularity.granularityLabel = this.granularityLabel;
		newGranularity.calendar = this.calendar;
		newGranularity.globalGranularityIdentifier = this.globalGranularityIdentifier;
		newGranularity.identifier = this.identifier;
		newGranularity.isBottomGranularity = this.isBottomGranularity;
		newGranularity.isTopGranularity = this.isTopGranularity;
		newGranularity.permittedContextIdentifiers = this.permittedContextIdentifiers;
		newGranularity.permittedContextGranularities = this.permittedContextGranularities;
		newGranularity.contextGranularity = granularity;

		return newGranularity;
	}

	@Override
	public String toString() {
		return "Granularity{" +
				"calendar=" + calendar +
				", globalGranularityIdentifier=" + globalGranularityIdentifier +
				", identifier=" + identifier +
				", granularityLabel='" + granularityLabel + '\'' +
				", isTopGranularity=" + isTopGranularity +
				", isBottomGranularity=" + isBottomGranularity +
				", permittedContextIdentifiers=" + permittedContextIdentifiers +
				'}';
	}

	@Override
	public int hashCode() {
		int result = globalGranularityIdentifier;
		result = 31 * result + (identifier != null ? identifier.hashCode() : 0);
		result = 31 * result + (granularityLabel != null ? granularityLabel.hashCode() : 0);
		result = 31 * result + (isTopGranularity != null ? isTopGranularity.hashCode() : 0);
		result = 31 * result + (isBottomGranularity != null ? isBottomGranularity.hashCode() : 0);
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Granularity that = (Granularity) o;

		return globalGranularityIdentifier == that.globalGranularityIdentifier;
	}
}
Neue Ghostery-Version. Weitere Informationen finden Sie hier.

Machen Sie einen Rundgang durch die Ghostery-Konfiguration,
um die wichtigsten Funktionen von Ghostery kennenzulernen.

Ghostery einrichten

Verwerfen