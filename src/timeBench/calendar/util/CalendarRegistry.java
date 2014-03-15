package timeBench.calendar.util;

import timeBench.calendar.Calendar;
import timeBench.calendar.CalendarManager;
import timeBench.calendar.Granularity;
import timeBench.calendar.manager.GregorianCalendarManager;
import timeBench.calendar.manager.JavaDateCalendarManager;
import timeBench.data.TemporalDataException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.TreeMap;

/**
 * A factory class for all classes in the calendar package.
 * <p/>
 * The buildup of the granularity identifier is like this:
 * 31 30 29 28 27 26 25 24 23 22 21 20 19 18 17 16 15 14 13 12 11 10 09 08 07 06 05 04 03 02 01 00
 * M  M  M  M  M  V  V  V  V  V  V  V  V  C  C  C  C  C  C  C  T  T  T  T  T  G  G  G  G  G  G  G
 * <p/>
 * M: Calendar Manager
 * V: Version of the Calendar Manager
 * C: Calendar
 * T: Type Granularity (e.g., western modern (week), metric (kilosecond), ...)
 * G: Granularity (e.g., week, kilosecond, ...)
 * MMMMM VVVVVVVV CCCCCCC TTTTT GGGGGGG
 * <p/>
 * <p>
 * Added:         2013-07-23 / TL<br>
 * Modifications:
 * </p>
 *
 * @author Tim Lammarsch
 */

public class CalendarRegistry {
	private static CalendarRegistry instance = null;

	private TreeMap<Integer, TreeMap<Integer, CalendarManager>> calendarManagerMap;
	private JAXBContext jaxbContext = null;
	private Unmarshaller unmarshaller = null;

	/**
	 * Static method to get a instance of this class that always exists.
	 *
	 * @return the instance
	 */
	public static CalendarRegistry getInstance() {
		if (instance == null) {
			instance = new CalendarRegistry();
			instance.initialize();
		}
		return instance;
	}

	/**
	 * Creates if necessary and returns an instance of the calendar manager described by the identifier
	 * (ignoring the version).
	 *
	 * @param identifier the identifier to get the calendar manager for (MMMMM VVVVVVVV xxxxxxx xxxxx xxxxxxx)
	 * @return the calendar manager if possible, null otherwise
	 */
	public CalendarManager getCalendarManager(int identifier) {
		return getCalendarManager(identifier, false);
	}

	/**
	 * Creates if necessary and returns an instance of the calendar manager described by the globalIdentifier.
	 *
	 * @param globalIdentifier the globalIdentifier to get the calendar manager for (MMMMM VVVVVVVV xxxxxxx xxxxx xxxxxxx)
	 * @param enforceVersion   true means that a special version of a calendar manager is needed, otherwise that does not matter
	 * @return the calendar manager if possible, null otherwise
	 */
	public CalendarManager getCalendarManager(int globalIdentifier, boolean enforceVersion) {
		TreeMap<Integer, CalendarManager> calendarManagerVersions = calendarManagerMap.get(IdentifierConverter.getInstance().getManagerIdentifier(globalIdentifier));

		if (calendarManagerVersions == null || calendarManagerVersions.size() == 0) {
			return null;
		}

		if (enforceVersion) {
			return calendarManagerVersions.get(IdentifierConverter.getInstance().getVersionIdentifier(globalIdentifier));
		} else {
			int firstVersionIdentifier = calendarManagerVersions.firstKey();
			return calendarManagerVersions.get(firstVersionIdentifier);
		}
	}

	/**
	 * Creates if necessary and returns an instance of the calendar described by the globalIdentifier
	 * (ignoring the version).
	 * Can create a calendar manager on demand.
	 *
	 * @param globalIdentifier the globalIdentifier to get the calendar for (MMMMM VVVVVVVV CCCCCCC xxxxx xxxxxxx)
	 * @return the calendar manager if possible, null otherwise
	 */
	public Calendar getCalendar(int globalIdentifier) {
		return getCalendar(globalIdentifier, false);
	}

	/**
	 * Creates if necessary and returns an instance of the calendar described by the globalIdentifier.
	 * Can create a calendar manager on demand.
	 *
	 * @param globalIdentifier the globalIdentifier to get the calendar for (MMMMM VVVVVVVV CCCCCCC xxxxx xxxxxxx)
	 * @param enforceVersion   true means that a special version of a calendar manager is needed, otherwise that does not matter
	 * @return the calendar manager if possible, null otherwise
	 */
	public Calendar getCalendar(int globalIdentifier, boolean enforceVersion) {
		try {
			int calendarManagerIdentifier = IdentifierConverter.getInstance().buildCalendarManagerVersionIdentifier(
					IdentifierConverter.getInstance().getManagerIdentifier(globalIdentifier),
					IdentifierConverter.getInstance().getVersionIdentifier(globalIdentifier));
			CalendarManager calendarManager = getCalendarManager(calendarManagerIdentifier, enforceVersion);
			if (calendarManager == null)
				return null;
			else {
				return calendarManager.getCalendar(IdentifierConverter.getInstance().getCalendarIdentifier(globalIdentifier));
			}
		}
		catch (TemporalDataException e) {
			return null;
		}
	}

	/**
	 * Creates and returns an instance of a granularity described by the identifier and context identifier
	 * (ignoring the version).
	 * Can create a calendar manager and calendar on demand.
	 *
	 * @param globalGranularityIdentifier the identifier of the granularity (MMMMM VVVVVVVV CCCCCCC TTTTT GGGGGGG)
	 * @param globalContextGranularityIdentifier
	 *                                    the identifier of the granularity's context granularity (MMMMM VVVVVVVV CCCCCCC TTTTT GGGGGGG)
	 * @return the granularity if possible, null otherwise
	 */
	public Granularity getGranularity(int globalGranularityIdentifier, int globalContextGranularityIdentifier) {
		return getGranularity(globalGranularityIdentifier, globalContextGranularityIdentifier, false);
	}

	/**
	 * Creates and returns an instance of a granularity described by the identifier and context identifier.
	 * Can create a calendar manager and calendar on demand.
	 *
	 * @param globalGranularityIdentifier the identifier of the granularity (MMMMM VVVVVVVV CCCCCCC TTTTT GGGGGGG)
	 * @param globalContextGranularityIdentifier
	 *                                    the identifier of the granularity's context granularity (MMMMM VVVVVVVV CCCCCCC TTTTT GGGGGGG)
	 * @param enforceVersion              true means that a special version of a calendar manager is needed, otherwise that does not matter
	 * @return the granularity if possible, null otherwise. The globalGranularityIdentifier and globalContextGranularityIdentifier must be of
	 *         the same calendarManager and calendar
	 */
	public Granularity getGranularity(int globalGranularityIdentifier, int globalContextGranularityIdentifier, boolean enforceVersion) {
		if (IdentifierConverter.getInstance().getManagerIdentifier(globalGranularityIdentifier) != IdentifierConverter.getInstance().getManagerIdentifier(globalContextGranularityIdentifier) ||
				IdentifierConverter.getInstance().getCalendarIdentifier(globalGranularityIdentifier) != IdentifierConverter.getInstance().getCalendarIdentifier(globalContextGranularityIdentifier)) {
			return null;
		}

		Calendar calendar = getCalendar(IdentifierConverter.getInstance().getCalendarIdentifier(globalGranularityIdentifier), enforceVersion);
		if (calendar == null)
			return null;
		else {
			try {
				return calendar.getGranularity(globalGranularityIdentifier).setIntoContext(calendar.getGranularity(globalContextGranularityIdentifier));
			}
			catch (TemporalDataException e) {
				return null;
			}
		}
	}

	public Granularity getGranularity(Calendar calendar, String granularityName, String contextGranularityName) {
		return calendar.getGranularity(granularityName, contextGranularityName);
	}

	/**
	 * This method serves to initialize the registry data structure with instances of calendar managers. The structure
	 * is built in a two-tier hashtable:
	 * <p/>
	 * <CalendarManager identifier: <CalendarManager Version identifier, CalendarManager>>
	 * <p/>
	 * In practice, this would look something like this:
	 * <p/>
	 * 1 (JavaDateCalendarManager)
	 * | version 1 --> object instance
	 * 2 (GregorianCalendarManager)
	 * | version 1 --> object instance
	 * | version 2 --> object instance
	 * 3 (SolarHijriCalendarManager)
	 * | version 1 --> object instance
	 */
	private void initialize() {
		calendarManagerMap = new TreeMap<>();

		//register versions of JavaDateCalendarManager
		TreeMap<Integer, CalendarManager> javaDateCalendarManagerVersions = new TreeMap<>();
		javaDateCalendarManagerVersions.put(
				JavaDateCalendarManager.getSingleton().getLocalCalendarManagerVersionIdentifier(),
				JavaDateCalendarManager.getSingleton());
		calendarManagerMap.put(
				JavaDateCalendarManager.getSingleton().getLocalCalendarManagerIdentifier(),
				javaDateCalendarManagerVersions);


		//register versions of GregorianCalendarManager
		TreeMap<Integer, CalendarManager> gregorianCalendarManagerVersions = new TreeMap<>();
		gregorianCalendarManagerVersions.put(
				GregorianCalendarManager.getInstance().getLocalCalendarManagerVersionIdentifier(),
				GregorianCalendarManager.getInstance());
		calendarManagerMap.put(GregorianCalendarManager.getInstance().getLocalCalendarManagerIdentifier(),
				gregorianCalendarManagerVersions);
	}

	public void loadCalendar(File file) throws TemporalDataException {
		Calendar calendar = null;
		try {
			if (jaxbContext == null) {
				jaxbContext = JAXBContext.newInstance(Calendar.class);
			}
			if (unmarshaller == null) {
				unmarshaller = jaxbContext.createUnmarshaller();
			}

			Object unmarshalledObject = unmarshaller.unmarshal(file);

			if (unmarshalledObject != null && unmarshalledObject instanceof Calendar) {
				calendar = (Calendar) unmarshalledObject;
			} else {
				throw new TemporalDataException("Failed to unmarshal XML file from: " + file);
			}
		}
		catch (JAXBException e) {
			throw new TemporalDataException("Failed to initialize JAXB unmarshaller.", e);
		}

		calendar.initializeCalendar();
		calendar.getCalendarManager().registerCalendar(calendar.getLocalCalendarIdentifier(), calendar);
	}

	/**
	 * Calculates the calendar manager, version, and calendar parts of a granularity identifier from
	 * a calendar identifier.
	 *
	 * @param identifier
	 * @return the resulting identifier
	 * @deprecated port this method to {@link timeBench.calendar.util.IdentifierConverter}
	 */
	@Deprecated
	public int getGranularityIdentifierSummandFromCalendarIdentifier(
			int identifier) {
		return identifier << 12;
	}

	/**
	 * Calculates the calendar manager identifier from
	 * a calendar identifier.
	 *
	 * @param identifier
	 * @return the resulting identifier
	 * @deprecated use {@link timeBench.calendar.util.IdentifierConverter#getManagerIdentifier(int)}
	 */
	@Deprecated
	public int getCalendarManagerIdentifierFromCalendarIdentifier(
			int identifier) {
		return identifier >> 7;
	}

	/**
	 * Calculates the calendar identifier from
	 * a granularity identifier.
	 *
	 * @param identifier
	 * @return the resulting identifier
	 * @deprecated use {@link timeBench.calendar.util.IdentifierConverter#getCalendarIdentifier(int)}
	 */
	@Deprecated
	public int getCalendarIdentifierFromGranularityIdentifier(
			int identifier) {
		return identifier >> 12;
	}
}
