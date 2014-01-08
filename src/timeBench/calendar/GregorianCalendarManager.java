package timeBench.calendar;

import timeBench.calendar.util.IdentifierConverter;
import timeBench.data.TemporalDataException;

import java.util.*;

public class GregorianCalendarManager implements CalendarManager {
	protected static final int LOCAL_CALENDAR_MANAGER = 2;
	private static final int LOCAL_CALENDAR_MANAGER_VERSION = 1;
	private static final int GLOBAL_CALENDAR_MANAGER_VERSION;

	private TreeMap<Integer, Calendar> calendarMap = new TreeMap<>();

	private enum GregorianGranularity{
		Millisecond,
		Second,
		Minute,
		Hour,
		Day,
		Week,
		Month,
		Quarter,
		Year,
		Decade,
		Top
	}

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
//		switch (mode) {
//			case Granule.MODE_FORCE:
//				return new Granule(inf, sup, mode, granularity);
//			case Granule.MODE_INF_GRANULE:
//				return createGranule(inf, granularity);
//			case Granule.MODE_MIDDLE_GRANULE:
//				return createGranule(inf + (sup - inf) / 2, granularity);
//			case Granule.MODE_SUP_GRANULE:
//				return createGranule(sup, granularity);
//			default:
//				throw new TemporalDataException("Illegal mode in createGranule");
//		}
		return null;
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
		associateGranularities(calendar);
	}

	private void associateGranularities(Calendar calendar) throws TemporalDataException {
		enumLoop: for (GregorianGranularity currentEnumGranularity : GregorianGranularity.values()){
			for (Granularity currentGranularity : calendar.getGranularities()){
				if (currentEnumGranularity.toString().equalsIgnoreCase(currentGranularity.getGranularityLabel())){
					calendar.getAssociation().associateGranularities(currentEnumGranularity, currentGranularity);
					continue enumLoop;
				}
			}
		}
		if (calendar.getAssociation().getAssociationCount() != GregorianGranularity.values().length){
			throw new TemporalDataException("Failed to map all internal granularities defined in " + GregorianGranularity.class.getSimpleName());
		}
	}

//	private Granule createGranule(GregorianCalendar calInf, GregorianCalendar calSup, Granularity granularity) throws TemporalDataException {
//		for (int field : buildGranularityListForCreateGranule(granularity)) {
//			calInf.set(field, calInf.getActualMinimum(field));
//			calSup.set(field, calSup.getActualMaximum(field));
//		}
//
//		if (granularity.getGlobalGranularityIdentifier() == GRANULARITY_DECADE) {
//			calInf.set(GregorianCalendar.YEAR,
//					(calInf.get(GregorianCalendar.YEAR) - 1) / 10 * 10 + 1);
//			calSup.set(GregorianCalendar.YEAR,
//					(calSup.get(GregorianCalendar.YEAR) - 1) / 10 * 10 + 10);
//		} else if (granularity.getGlobalGranularityIdentifier() == GRANULARITY_QUARTER) {
//			// calInf DAY_OF_MONTH is already at 1
//			// calSup DAY_OF_MONTH needs to be set to 1 first
//			// because last day may change (e.g., 31 March --June--> 1 July)
//			switch (calInf.get(GregorianCalendar.MONTH)) {
//				case GregorianCalendar.JANUARY:
//				case GregorianCalendar.FEBRUARY:
//				case GregorianCalendar.MARCH:
//					calInf.set(GregorianCalendar.MONTH, GregorianCalendar.JANUARY);
//					calSup.set(GregorianCalendar.DAY_OF_MONTH, 1);
//					calSup.set(GregorianCalendar.MONTH, GregorianCalendar.MARCH);
//					calSup.set(GregorianCalendar.DAY_OF_MONTH, 31);
//					break;
//				case GregorianCalendar.APRIL:
//				case GregorianCalendar.MAY:
//				case GregorianCalendar.JUNE:
//					calInf.set(GregorianCalendar.MONTH, GregorianCalendar.APRIL);
//					calSup.set(GregorianCalendar.DAY_OF_MONTH, 1);
//					calSup.set(GregorianCalendar.MONTH, GregorianCalendar.JUNE);
//					calSup.set(GregorianCalendar.DAY_OF_MONTH, 30);
//					break;
//				case GregorianCalendar.JULY:
//				case GregorianCalendar.AUGUST:
//				case GregorianCalendar.SEPTEMBER:
//					calInf.set(GregorianCalendar.MONTH, GregorianCalendar.JULY);
//					calSup.set(GregorianCalendar.DAY_OF_MONTH, 1);
//					calSup.set(GregorianCalendar.MONTH, GregorianCalendar.SEPTEMBER);
//					calSup.set(GregorianCalendar.DAY_OF_MONTH, 30);
//					break;
//				case GregorianCalendar.OCTOBER:
//				case GregorianCalendar.NOVEMBER:
//				case GregorianCalendar.DECEMBER:
//					calInf.set(GregorianCalendar.MONTH, GregorianCalendar.OCTOBER);
//					calSup.set(GregorianCalendar.DAY_OF_MONTH, 1);
//					calSup.set(GregorianCalendar.MONTH, GregorianCalendar.DECEMBER);
//					calSup.set(GregorianCalendar.DAY_OF_MONTH, 31);
//					break;
//			}
//		} else if (granularity.getGlobalGranularityIdentifier() == GRANULARITY_WEEK) {
//			long dow = (calInf.get(GregorianCalendar.DAY_OF_WEEK) + 5) % 7;
//			long oldInf = calInf.getTimeInMillis();
//			calInf.setTimeInMillis(oldInf - dow * 86400000L);
//			long oldSup = calSup.getTimeInMillis();
//			calSup.setTimeInMillis(oldSup + (6 - dow) * 86400000L);
//			if ((granularity.getGranularityContextIdentifier() == GRANULARITY_MONTH &&
//					calInf.get(GregorianCalendar.MONTH) != calSup.get(GregorianCalendar.MONTH)) ||
//					(granularity.getGranularityContextIdentifier() == GRANULARITY_QUARTER &&
//							calInf.get(GregorianCalendar.MONTH) / 3 != calSup.get(GregorianCalendar.MONTH) / 3) ||
//					(granularity.getGranularityContextIdentifier() == GRANULARITY_YEAR) &&
//							calInf.get(GregorianCalendar.YEAR) != calSup.get(GregorianCalendar.YEAR)) {
//				GregorianCalendar calBorder = new GregorianCalendar();
//				calBorder.setTimeZone(TimeZone.getTimeZone("UTC"));
//				calBorder.setTimeInMillis(calInf.getTimeInMillis());
//				calBorder.set(GregorianCalendar.DAY_OF_MONTH, 1);
//				calBorder.add(GregorianCalendar.MONTH, 1);
//				boolean front = oldSup < calBorder.getTimeInMillis();
//				if (!front)
//					front = calBorder.getTimeInMillis() - oldInf > oldSup - calBorder.getTimeInMillis();
//				if (front) {
//					calSup.set(GregorianCalendar.DAY_OF_MONTH, 1);
//					calSup.set(GregorianCalendar.MONTH, calInf.get(GregorianCalendar.MONTH));
//					calSup.set(GregorianCalendar.YEAR, calInf.get(GregorianCalendar.YEAR));
//					calSup.set(GregorianCalendar.DAY_OF_MONTH, calSup.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
//				} else {
//					calInf.set(GregorianCalendar.DAY_OF_MONTH, 1);
//					calInf.set(GregorianCalendar.MONTH, calSup.get(GregorianCalendar.MONTH));
//					calInf.set(GregorianCalendar.YEAR, calSup.get(GregorianCalendar.YEAR));
//				}
//			}
//		}
//
//		return new Granule(calInf.getTimeInMillis(), calSup.getTimeInMillis(), Granule.MODE_FORCE, granularity);
//	}
}
