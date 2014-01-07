package timeBench.calendar;

import timeBench.calendar.util.IdentifierConverter;
import timeBench.data.TemporalDataException;

import java.text.DateFormat;
import java.util.*;

public class GregorianCalendarManager implements CalendarManager {
	protected static final int LOCAL_CALENDAR_MANAGER = 2;
	private static final int LOCAL_CALENDAR_MANAGER_VERSION = 1;
	private static final int GLOBAL_CALENDAR_MANAGER_VERSION;

	private TreeMap<Integer, Calendar> calendarMap = new TreeMap<>();

	static {
		try {
			GLOBAL_CALENDAR_MANAGER_VERSION = IdentifierConverter.getInstance().buildCalendarManagerVersionIdentifier(LOCAL_CALENDAR_MANAGER, LOCAL_CALENDAR_MANAGER_VERSION);
			CalendarFactory.getInstance().registerCalendarManager(GLOBAL_CALENDAR_MANAGER_VERSION, new GregorianCalendarManager());
		}
		catch (TemporalDataException e) {
			throw new RuntimeException("Failed to initialize GregorianCalendarManager", e);
		}
	}

	@Override
	public Calendar getDefaultCalendar() {
		int firstCalendarIdentifier = calendarMap.firstKey();
		return calendarMap.get(firstCalendarIdentifier);
	}

	@Override
	public Calendar getCalendar(int localIdentifier) {
		return calendarMap.get(localIdentifier);
	}

	@Override
	public int[] getGlobalGranularityIdentifiers() {
		ArrayList<Integer> identifierList = new ArrayList<>();
		for (Calendar currentCalendar : calendarMap.values()) {
			for (Granularity currentGranularity : currentCalendar.getGranularities()) {
				identifierList.add(currentGranularity.getGlobalGranularityIdentifier());
			}
		}

		int[] identifiers = new int[identifierList.size()];
		for (int i = 0; i < identifierList.size(); i++) {
			identifiers[i] = identifierList.get(i);
		}

		return identifiers;
	}

	@Override
	public Granule createGranule(Date input, Granularity granularity) throws TemporalDataException {
		return null;  //TODO: auto-generated body, implement me!
	}

	@Override
	public Granule createGranule(long inf, long sup, int mode, Granularity granularity) throws TemporalDataException {
		return null;  //TODO: auto-generated body, implement me!
	}

	@Override
	public Granule[] createGranules(long inf, long sup, double cover, Granularity granularity) throws TemporalDataException {
		return new Granule[0];  //TODO: auto-generated body, implement me!
	}

	@Override
	public Granule[] createGranules(Granule[] granules, double cover, Granularity granularity) throws TemporalDataException {
		return new Granule[0];  //TODO: auto-generated body, implement me!
	}

	@Override
	public long createGranuleIdentifier(Granule granule) throws TemporalDataException {
		return 0;  //TODO: auto-generated body, implement me!
	}

	@Override
	public String createGranuleLabel(Granule granule) throws TemporalDataException {
		return null;  //TODO: auto-generated body, implement me!
	}

	@Override
	public long createInf(Granule granule) throws TemporalDataException {
		return 0;  //TODO: auto-generated body, implement me!
	}

	@Override
	public long createSup(Granule granule) throws TemporalDataException {
		return 0;  //TODO: auto-generated body, implement me!
	}

	@Override
	public long getMinGranuleIdentifier(Granularity granularity) throws TemporalDataException {
		return 0;  //TODO: auto-generated body, implement me!
	}

	@Override
	public long getMaxGranuleIdentifier(Granularity granularity) throws TemporalDataException {
		return 0;  //TODO: auto-generated body, implement me!
	}

	@Override
	public long getMaxLengthInIdentifiers(Granularity granularity) throws TemporalDataException {
		return 0;  //TODO: auto-generated body, implement me!
	}

	@Override
	public boolean contains(Granule granule, long chronon) throws TemporalDataException {
		return false;  //TODO: auto-generated body, implement me!
	}

	@Override
	public long getStartOfTime() {
		return Long.MIN_VALUE;
	}

	@Override
	public long getEndOfTime() {
		return Long.MAX_VALUE;
	}

	@Override
	public Granularity getBottomGranularity(Calendar calendar) {
		return null;  //TODO: need to find a good way to implement this
			// each calendar needs to have one and only one bottom designated granularity
	}

	@Override
	public Granularity getTopGranularity(Calendar calendar) {
		return null;  //TODO: need to find a good way to implement this
		// each calendar needs to have one and only one top designated granularity
	}

	@Override
	public int getGlobalCalendarManagerVersionIdentifier() {
		return GLOBAL_CALENDAR_MANAGER_VERSION;
	}

	@Override
	public int getLocalCalendarManagerIdentifier() {
		return LOCAL_CALENDAR_MANAGER;
	}

	@Override
	public int getLocalCalendarManagerVersionIdentifier() {
		return LOCAL_CALENDAR_MANAGER_VERSION;
	}

	@Override
	public Granularity getGranularity(Calendar calendar, String granularityName, String contextGranularityName) {
		return null;  //TODO: auto-generated body, implement me!
	}

	@Override
	public void registerCalendar(int localIdentifier, Calendar calendar) throws TemporalDataException {
		if (calendarMap.containsKey(localIdentifier)) {
			throw new TemporalDataException("Calendar: " + getClass().getSimpleName() + " already contains a calendar with identifier: " + localIdentifier);
		}
		calendarMap.put(localIdentifier, calendar);
	}


}
