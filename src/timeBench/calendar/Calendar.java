package timeBench.calendar;

import org.apache.commons.lang3.builder.ToStringBuilder;
import timeBench.calendar.util.GranularityAssociation;
import timeBench.calendar.util.IdentifierConverter;
import timeBench.data.TemporalDataException;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Currently maps calendar functionality to CalendarManager.
 * In the future, will be able to map to more than
 * one calendar and/or calendric system.
 * <p/>
 * <p>
 * Added:         2011-07-19 / TL<br>
 * Modifications: 2012-04-11 / TL / inf, sup absolute, globalCalendarIdentifier in context, not JavaDate-only
 * </p>
 *
 * @author Tim Lammarsch
 */

@XmlRootElement(name = "calendar")
@XmlAccessorType(XmlAccessType.FIELD)
public class Calendar {
	@XmlTransient
	private CalendarManager calendarManager = null;

	@XmlTransient
	private int globalCalendarIdentifier;

	@XmlTransient
	private GranularityAssociation<Enum> association = new GranularityAssociation<>();

	@XmlElement
	private int localCalendarIdentifier;

	@XmlElement(name = "granularity")
	private List<Granularity> granularities;

	@XmlAttribute(required = true)
	private int localCalendarManagerIdentifier;

	@XmlAttribute
	private Integer localCalendarManagerVersionIdentifier = null;

	/**
	 * Empty constructor for JAXB
	 */
	public Calendar() {
	}

	/**
	 * Constructs a Calendar using a given {@link CalendarManager}.
	 *
	 * @param calendarManager the {@link CalendarManager} the new calendar maps to
	 */
	public Calendar(CalendarManager calendarManager, int globalIdentifier) {
		this.calendarManager = calendarManager;
		this.globalCalendarIdentifier = globalIdentifier;
	}

	/**
	 * Provide the bottom granularity of the this calendar as {@link Granularity}.
	 * For many calendars, it consists of the chronons of the discrete time domain.
	 *
	 * @return the bottom granularity as as {@link Granularity}
	 */
	public Granularity getBottomGranularity() {
		return calendarManager.getBottomGranularity(this);
	}

	/**
	 * Returns the top granularity of the this calendar as {@link Granularity}.
	 * This is the highest possible granularity of the calendar.
	 * Usually, this is a granularity where one granule is composed of all the time the calendar is defined for.
	 * Let all calendars that would normally have this be modified so they have one.
	 *
	 * @return the top granularity globalCalendarIdentifier
	 */
	public Granularity getTopGranularity() {
		return calendarManager.getTopGranularity(this);
	}

	/**
	 * Provide the granularity identifiers used by this calendar.
	 *
	 * @return granularity identifiers as array of integers
	 */
	public int[] getGranularityIdentifiers() {
		return calendarManager.getGlobalGranularityIdentifiers();
	}

	/**
	 * Constructs a {@link Granule} from a given {@link Date}. Consider using the adequate constructor of
	 * {@link Granule} instead.
	 *
	 * @param date        the {@link Date} used to generate the granule
	 * @param granularity granularity the {@link Granularity} to which the granule belongs
	 * @return the constructed {@link Granule}
	 * @throws TemporalDataException TemporalDataException thrown when granularities are not fully implemented
	 */
	protected Granule createGranule(Date date, Granularity granularity) throws TemporalDataException {
		return calendarManager.createGranule(date, granularity);
	}

	/**
	 * Constructs a {@link Granule} from inf to sup using a given {@linkplain Granule#MODE_INF_GRANULE mode} and
	 * for a given {@link Granularity}.
	 * Consider using the adequate constructor of {@link Granule} instead.
	 *
	 * @param inf         the chronon that determines the start of the granule constructed
	 * @param sup         the chronon that determines the end of the granule constructed
	 * @param mode        the {@linkplain Granule#MODE_INF_GRANULE mode} used to construct the granule
	 * @param granularity the {@link Granularity} to use
	 * @return the constructed {@link Granule}
	 * @throws TemporalDataException TemporalDataException thrown when granularities are not fully implemented
	 */
	protected Granule createGranule(long inf, long sup, int mode,
									Granularity granularity) throws TemporalDataException {
		return calendarManager.createGranule(inf, sup, mode, granularity);
	}

	/**
	 * Constructs several {@link Granule} objects from inf to sup that are at least partly in the given interval with
	 * a coverage of a least a given fraction and
	 * for a given {@link Granularity}. Consider using the adequate factory of {@link Granularity} instead.
	 *
	 * @param inf         the chronon that determines the start of the {@link Granule} range constructed
	 * @param sup         the chronon that determines the end of the {@link Granule} range constructed
	 * @param cover       the coverage fraction of a granule needed to be included in the result
	 * @param granularity the {@link Granularity} to use
	 * @return the constructed array of {@link Granule}
	 * @throws TemporalDataException TemporalDataException thrown when granularities are not fully implemented
	 */
	protected Granule[] createGranules(long inf, long sup, double cover,
									   Granularity granularity) throws TemporalDataException {
		return calendarManager.createGranules(inf, sup, cover, granularity);
	}

	/**
	 * Constructs several {@link Granule} objects from other {@link Granule} objects for a given {@link Granularity}
	 * that can (and most likely
	 * will) be in a different {@link Granularity}. All {@link Granule} with
	 * a coverage of a least a given fraction are returned.
	 * Consider using the adequate factory of {@link Granularity} instead.
	 *
	 * @param granules    the array of {@link Granule} used as source
	 * @param cover       the coverage fraction of a granule needed to be included in the result
	 * @param granularity the {@link Granularity} to use
	 * @return the constructed array of {@link Granule}
	 * @throws TemporalDataException TemporalDataException thrown when granularities are not fully implemented
	 */
	protected Granule[] createGranules(Granule[] granules, double cover,
									   Granularity granularity) throws TemporalDataException {
		return calendarManager.createGranules(granules, cover, granularity);
	}

	/**
	 * Calculate and return the globalCalendarIdentifier of a {@link Granule}. An globalCalendarIdentifier is a numeric label given in the context
	 * of the {@link Granularity}. Consider using the adequate method of
	 * {@link Granule} instead.
	 *
	 * @return the globalCalendarIdentifier
	 * @throws TemporalDataException thrown when granularities are not fully implemented
	 */
	protected long createGranuleIdentifier(Granule granule) throws TemporalDataException {
		return calendarManager.createGranuleIdentifier(granule);
	}

	/**
	 * Calculate and return the human readable label of a {@link Granule}.
	 * Consider using the adequate method of
	 * {@link Granule} instead.
	 *
	 * @return the label
	 * @throws TemporalDataException thrown when granularities are not fully implemented
	 */
	protected String createGranuleLabel(Granule granule) throws TemporalDataException {
		return calendarManager.createGranuleLabel(granule);
	}

	/**
	 * Calculate and return the inf of a {@link Granule}.
	 *
	 * @return the inf
	 * @throws TemporalDataException thrown when granularities are not fully implemented
	 */
	protected long createInf(Granule granule) throws TemporalDataException {
		return calendarManager.createInf(granule);
	}

	/**
	 * Calculate and return the sup of a {@link Granule}.
	 *
	 * @return the sup
	 * @throws TemporalDataException thrown when granularities are not fully implemented
	 */
	protected long createSup(Granule granule) throws TemporalDataException {
		return calendarManager.createSup(granule);
	}

	/**
	 * Provide the minimum globalCalendarIdentifier value that granules of a granularity can assume.
	 *
	 * @param granularity the granularity
	 * @return the minimum granule globalCalendarIdentifier value
	 * @throws TemporalDataException thrown when granularity has illegal identifiers
	 */
	public long getMinGranuleIdentifier(Granularity granularity) throws TemporalDataException {
		return calendarManager.getMinGranuleIdentifier(granularity);
	}

	/**
	 * Provide the maximum globalCalendarIdentifier value that granules of a granularity can assume.
	 *
	 * @param granularity the granularity
	 * @return the maximum granule globalCalendarIdentifier value
	 * @throws TemporalDataException thrown when granularity has illegal identifiers
	 */
	public long getMaxGranuleIdentifier(Granularity granularity) throws TemporalDataException {
		return calendarManager.getMaxGranuleIdentifier(granularity);
	}

	/**
	 * This method is automatically invoked by the JAXB unmarshaller after unmarshalling.
	 */
	void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
		try {
			registerWithCalendarManager();
			verifyGranularities();
		}
		catch (TemporalDataException e) {
			throw new RuntimeException("Failed to initialize calendar with local identifier: " + localCalendarIdentifier, e);
		}
	}

	/**
	 * Initializes unmarshalled granularities by setting their fully qualified global granularity identifier. Furthermore,
	 * it checks whether this calendar has granularities with the same identifier, and if there is exactly one bottom and
	 * top granularity.
	 *
	 * @throws TemporalDataException Thrown if a duplicate granularity identifier is found, if the defined bottom granularity
	 *                               is the same granularity as the top granularity, if the top or bottom granularity is not defined, or defined more than once.
	 *                               Also thrown if a field used to assemble the global granularity identifier is invalid.
	 */
	private void verifyGranularities() throws TemporalDataException {
		int bottomGranularityCount = 0;
		int topGranularityCount = 0;
		ArrayList<Integer> localGranularityIdentifiers = new ArrayList<>();

		for (Granularity currentGranularity : granularities) {
			setGlobalGranularityIdentifier(currentGranularity);

			if (!localGranularityIdentifiers.contains(currentGranularity.getLocalGranularityIdentifier())) {
				localGranularityIdentifiers.add(currentGranularity.getLocalGranularityIdentifier());
			} else {
				throw new TemporalDataException("Duplicate granularity identifier found: " + currentGranularity.getLocalGranularityIdentifier());
			}

			if (currentGranularity.isBottomGranularity()) {
				if (currentGranularity.isTopGranularity() && granularities.size() > 1) {
					throw new TemporalDataException(
							"Top granularity with identifier: " + currentGranularity.getLocalGranularityIdentifier() + " is the same as bottom granularity.");
				}
				bottomGranularityCount++;
			} else if (currentGranularity.isTopGranularity()) {
				topGranularityCount++;
			}
		}
		if (bottomGranularityCount != 1 && topGranularityCount != 1) {
			throw new TemporalDataException(
					"Top and/or bottom granularity not set.");
		}
	}

	/**
	 * Attempts to register this calendar with its defined CalendarManager instance.
	 *
	 * @throws TemporalDataException Thrown if the CalendarManager with the defined identifier could not be retrieved.
	 */
	private void registerWithCalendarManager() throws TemporalDataException {
		CalendarManager manager = CalendarFactory.getInstance().getCalendarManager(
				IdentifierConverter.getInstance().buildCalendarManagerVersionIdentifier(
						localCalendarManagerIdentifier,
						localCalendarManagerVersionIdentifier == null ? 0 : localCalendarManagerVersionIdentifier),
				localCalendarManagerVersionIdentifier != null);

		if (manager == null) {
			throw new TemporalDataException(
					"Could not find CalendarManager with identifier: " + localCalendarManagerIdentifier);
		}
		setCalendarManager(manager);
		manager.registerCalendar(localCalendarIdentifier, this);
	}

	/**
	 * Sets the fully qualified global granularity identifier.
	 *
	 * @param currentGranularity The granularity to initialize the global identifier for.
	 * @throws TemporalDataException Thrown if the fields to assemble the global granularity identifier are invalid.
	 */
	private void setGlobalGranularityIdentifier(Granularity currentGranularity) throws TemporalDataException {
		currentGranularity.setGlobalGranularityIdentifier(IdentifierConverter.getInstance().buildGlobalIdentifier(
				calendarManager.getLocalCalendarManagerIdentifier(),
				calendarManager.getLocalCalendarManagerVersionIdentifier(),
				localCalendarIdentifier,
				currentGranularity.getLocalGranularityTypeIdentifier(),
				currentGranularity.getLocalGranularityIdentifier()));
	}

	public long getMaxLengthInIdentifiers(Granularity granularity) throws TemporalDataException {
		return calendarManager.getMaxLengthInIdentifiers(granularity);
	}

	public boolean contains(Granule granule, long chronon) throws TemporalDataException {
		return calendarManager.contains(granule, chronon);
	}

	public Granularity getGranularity(String granularityName, String contextGranularityName) {
		return calendarManager.getGranularity(this, granularityName, contextGranularityName);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).
				append("manager", calendarManager.getClass().getSimpleName()).
				toString();
	}

	public Granule getTopGranule() throws TemporalDataException {
		return new Granule(Long.MIN_VALUE, Long.MAX_VALUE, getTopGranularity());
	}

	public int getGlobalCalendarIdentifier() {
		return globalCalendarIdentifier;
	}

	public int getLocalCalendarIdentifier() {
		return localCalendarIdentifier;
	}

	public void setLocalCalendarIdentifier(int localCalendarIdentifier) {
		this.localCalendarIdentifier = localCalendarIdentifier;
	}

	public List<Granularity> getGranularities() {
		return granularities;
	}

	public void setGranularities(List<Granularity> granularities) {
		this.granularities = granularities;
	}

	public CalendarManager getCalendarManager() {
		return calendarManager;
	}

	public void setCalendarManager(CalendarManager calendarManager) {
		this.calendarManager = calendarManager;
	}

	public void setGlobalCalendarIdentifier(int globalCalendarIdentifier) {
		this.globalCalendarIdentifier = globalCalendarIdentifier;
	}

	public int getLocalCalendarManagerIdentifier() {
		return localCalendarManagerIdentifier;
	}

	public void setLocalCalendarManagerIdentifier(int localCalendarManagerIdentifier) {
		this.localCalendarManagerIdentifier = localCalendarManagerIdentifier;
	}

	public Integer getLocalCalendarManagerVersionIdentifier() {
		return localCalendarManagerVersionIdentifier;
	}

	public void setLocalCalendarManagerVersionIdentifier(Integer localCalendarManagerVersionIdentifier) {
		this.localCalendarManagerVersionIdentifier = localCalendarManagerVersionIdentifier;
	}

	public GranularityAssociation<Enum> getAssociation() {
		return association;
	}
}
