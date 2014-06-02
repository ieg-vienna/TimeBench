package timeBench.calendar;

import timeBench.calendar.Calendar;
import timeBench.calendar.CalendarManager;
import timeBench.calendar.Granularity;
import timeBench.data.TemporalDataException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.TreeMap;

/**
 * A registry that holds Granularity instances and CalendarManager instances 
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
 * Modifications: 2014-03-15 / FU / changed from factory to registry<br>
 * Modifications: 2014-05-28 / TL / portation to new TimeBench calendar implementation<br>
 * </p>
 *
 * @author Tim Lammarsch, Frieder Ulm
 */

public class CalendarRegistry {
	private static CalendarRegistry instance = null;

	private TreeMap<Integer, TreeMap<Integer, CalendarManager>> calendarManagerMap = new TreeMap<Integer, TreeMap<Integer, CalendarManager>>();
	private TreeMap<Integer, TreeMap<Integer, Granularity>> granularityMap = new TreeMap<Integer, TreeMap<Integer, Granularity>>();
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
		CalendarManager result = null;

		int identifier = IdentifierConverter.getInstance().getCalendarManagerIdentifier(globalIdentifier);
		TreeMap<Integer, CalendarManager> calendarManagerVersions = calendarManagerMap.get(identifier);

		if (calendarManagerVersions == null) {
			calendarManagerVersions = new TreeMap<Integer, CalendarManager>();
			calendarManagerMap.put(identifier, calendarManagerVersions);
		}

		result = calendarManagerVersions.get(IdentifierConverter.getInstance().getVersionIdentifier(globalIdentifier));
		if (result == null && !enforceVersion)
			result = calendarManagerVersions.lastEntry().getValue();
		if (result == null) {
			if (identifier == JavaDateCalendarManager.getIdentifier()) {
				result = new JavaDateCalendarManager();
				calendarManagerVersions.put(IdentifierConverter.getInstance().getVersionIdentifier(globalIdentifier), result);
			}
		}
	
		return result;
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
	public Granularity getGranularity(int globalGranularityIdentifier, int globalIdentifier) {
		return getGranularity(globalGranularityIdentifier, globalIdentifier, false);
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
	public Granularity getGranularity(int globalGranularityIdentifier, int globalIdentifier, boolean enforceVersion) {
		Granularity result = null;

		int identifier = IdentifierConverter.getInstance().getGranularityIdentifier(globalIdentifier);
		TreeMap<Integer, Granularity> granularityVersions = granularityMap.get(identifier);

		if (granularityVersions == null) {
			granularityVersions = new TreeMap<Integer, Granularity>();
			granularityMap.put(identifier, granularityVersions);
		}

		result = granularityVersions.get(IdentifierConverter.getInstance().getVersionIdentifier(globalIdentifier));
		if (result == null && !enforceVersion)
			result = granularityVersions.lastEntry().getValue();
		if (result == null) {
			if (identifier == JavaDateCalendarManager.getIdentifier()) {				
				result = new Granularity(globalIdentifier);
				granularityVersions.put(IdentifierConverter.getInstance().getVersionIdentifier(globalIdentifier),result);
			}
		}
	
		return result;
	}

	public Granularity getGranularity(Calendar calendar, String granularityName, String contextGranularityName) {
		return calendar.getGranularity(granularityName, contextGranularityName);
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
}