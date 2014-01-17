package timeBench.calendar;

import timeBench.calendar.util.GranularityAssociation;
import timeBench.calendar.util.IdentifierConverter;
import timeBench.data.TemporalDataException;

import java.util.*;

public class GregorianCalendarManager extends CalendarManager {
	protected static final int LOCAL_CALENDAR_MANAGER = 2;
	private static final int LOCAL_CALENDAR_MANAGER_VERSION = 1;
	private static GregorianCalendarManager instance = null;

	private TreeMap<Integer, GranularityAssociation<GregorianGranularity>> calendarAssociationMap = new TreeMap<>();

	private enum GregorianGranularity {
		Millisecond(),
		Second(GregorianCalendar.MILLISECOND),
		Minute(GregorianCalendar.SECOND, GregorianCalendar.MILLISECOND),
		Hour(GregorianCalendar.MINUTE, GregorianCalendar.SECOND, GregorianCalendar.MILLISECOND),
		Day(GregorianCalendar.AM_PM, GregorianCalendar.HOUR, GregorianCalendar.HOUR_OF_DAY, GregorianCalendar.MINUTE, GregorianCalendar.SECOND, GregorianCalendar.MILLISECOND),
		Week(Day.getGregorianCalendarFieldIdentifiers()), //only works manually
		Month(GregorianCalendar.DAY_OF_MONTH, GregorianCalendar.AM_PM, GregorianCalendar.HOUR, GregorianCalendar.HOUR_OF_DAY, GregorianCalendar.MINUTE, GregorianCalendar.SECOND, GregorianCalendar.MILLISECOND),
		Quarter(Month.getGregorianCalendarFieldIdentifiers()),
		Year(GregorianCalendar.DAY_OF_YEAR, GregorianCalendar.AM_PM, GregorianCalendar.HOUR, GregorianCalendar.HOUR_OF_DAY, GregorianCalendar.MINUTE, GregorianCalendar.SECOND, GregorianCalendar.MILLISECOND),
		Decade(Year.getGregorianCalendarFieldIdentifiers()),
		Top(GregorianCalendar.DAY_OF_YEAR, GregorianCalendar.AM_PM, GregorianCalendar.HOUR, GregorianCalendar.HOUR_OF_DAY, GregorianCalendar.MINUTE, GregorianCalendar.SECOND, GregorianCalendar.MILLISECOND, GregorianCalendar.YEAR);

		private int[] gregorianCalendarFieldIdentifiers;

		private GregorianGranularity(int... gregorianCalendarFieldIdentifier){
			this.gregorianCalendarFieldIdentifiers = gregorianCalendarFieldIdentifier;
		}

		public int[] getGregorianCalendarFieldIdentifiers(){
			return gregorianCalendarFieldIdentifiers;
		}
	}

	private GregorianCalendarManager(){
	}

	public static GregorianCalendarManager getInstance(){
		if (instance == null){
			instance = new GregorianCalendarManager();
		}
		return instance;
	}

	@Override
	public Granule createGranule(Date input, Granularity granularity) throws TemporalDataException {
		return createGranuleFromChronon(input.getTime(), granularity);
	}

	@Override
	public Granule createGranule(long inf, long sup, int mode, Granularity granularity) throws TemporalDataException {
		switch (mode) {
			case Granule.MODE_FORCE:
				return new Granule(inf, sup, mode, granularity);
			case Granule.MODE_INF_GRANULE:
				return createGranuleFromChronon(inf, granularity);
			case Granule.MODE_MIDDLE_GRANULE:
				return createGranuleFromChronon(inf + (sup - inf) / 2, granularity);
			case Granule.MODE_SUP_GRANULE:
				return createGranuleFromChronon(sup, granularity);
			default:
				throw new TemporalDataException("Illegal mode in createGranule");
		}
	}

	private Granule createGranuleFromChronon(long chronon, Granularity granularity) throws TemporalDataException {
		GregorianCalendar calInf = new GregorianCalendar();
		GregorianCalendar calSup = new GregorianCalendar();
		calInf.setTimeZone(TimeZone.getTimeZone("UTC"));
		calSup.setTimeZone(TimeZone.getTimeZone("UTC"));
		calInf.setTimeInMillis(chronon);
		calSup.setTimeInMillis(chronon);

		return createGranule(calInf, calSup, granularity);
	}

	@Override
	public Granule[] createGranules(long inf, long sup, double cover, Granularity granularity) throws TemporalDataException {
		Granule first = createGranule(inf, granularity);
		Granule last = createGranule(sup, granularity);

		long firstIdentifier = first.getIdentifier();
		long lastIdentifier = last.getIdentifier();

		if ((double) (inf - first.getInf()) / (double) (first.getSup() - first.getInf()) < cover) {
			firstIdentifier++;
		}
		if ((double) (last.getSup() - sup) / (double) (last.getSup() - last.getInf()) < cover) {
			lastIdentifier--;
		}

		Granule[] result;
		if (firstIdentifier > lastIdentifier)
			result = new Granule[0];
		else {
			result = new Granule[(int) (lastIdentifier - firstIdentifier + 1)];
			result[0] = first;
			for (int i = 1; i < (int) (lastIdentifier - firstIdentifier); i++) {
				result[i] = createGranule(result[i - 1].getSup() + 1, granularity);
			}
			result[(int) (lastIdentifier - firstIdentifier)] = last;    // when first=last set 0 again
		}

		return result;
	}

	@Override
	public Granule[] createGranules(Granule[] granules, double cover, Granularity granularity) throws TemporalDataException {
		ArrayList<Granule> result = new ArrayList<>();
		for (Granule iGran : granules) {
			for (Granule iGran2 : createGranules(iGran.getInf(), iGran.getSup(), cover, granularity)) {
				if (result.get(result.size() - 1).getIdentifier() < iGran2.getIdentifier())
					result.add(iGran2);
			}
		}

		return (Granule[]) result.toArray();
	}

	@Override
	public String createGranuleLabel(Granule granule) throws TemporalDataException {
		String result = null;

		int calendarIdentifier = IdentifierConverter.getInstance().getCalendarIdentifier(granule.getGranularity().getGlobalGranularityIdentifier());
		Calendar calendar = calendarMap.get(calendarIdentifier);
		if (calendar == null) {
			throw new TemporalDataException("Failed to create granule label: Failed to fetch calendar: " + calendarIdentifier);
		}

		GranularityAssociation<GregorianGranularity> association = calendarAssociationMap.get(calendar.getLocalCalendarIdentifier());
		GregorianGranularity granularityEnum = association.getAssociation(granule.getGranularity());
		GregorianGranularity granularityContextEnum = association.getAssociation(granule.getGranularity().getContextGranularity());

		GregorianCalendar timeStamp = new GregorianCalendar();
		timeStamp.setTimeZone(TimeZone.getTimeZone("UTC"));
		timeStamp.setTimeInMillis(granule.getInf());

		switch (granularityEnum) {
			case Day:
				if (granularityContextEnum == GregorianGranularity.Week) {
					result = timeStamp.getDisplayName(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.SHORT, Locale.getDefault());
				} else
					result = String.format("%d", granule.getIdentifier() + 1);
				break;
			case Week:
				if (granularityContextEnum == GregorianGranularity.Month || granularityContextEnum == GregorianGranularity.Quarter) {
					result = String.format("%d", granule.getIdentifier());
				} else
					result = String.format("%d", granule.getIdentifier() + 1);
				break;
			case Month:
				if (granularityContextEnum == GregorianGranularity.Year) {
					result = timeStamp.getDisplayName(GregorianCalendar.MONTH, GregorianCalendar.LONG, Locale.getDefault());
				} else
					result = String.format("M%d", granule.getIdentifier() + 1);
				break;
			case Quarter:
				result = String.format("Q%d", granule.getIdentifier() + 1);
				break;
			case Year:
				if (granularityContextEnum == GregorianGranularity.Decade) {
					result = String.format("%d", granule.getIdentifier() + 1);
				} else {
					result = String.format("%d", granule.getIdentifier() + 1970);
				}
				break;
			case Decade:
				result = String.format("%ds", granule.getIdentifier() * 10);
				break;
			default:
				result = String.format("%d", granule.getIdentifier() + 1);
		}
		return result;
	}

	@Override
	public long createInf(Granule granule) throws TemporalDataException {
		if (granule.getContextGranule() == null)
			throw new TemporalDataException("Cannot generate inf without context granule");

		if (granule.getGranularity().getGlobalGranularityIdentifier() < granule.getContextGranule().getGranularity().getGlobalGranularityIdentifier())
			return createInfLocal(granule, granule.getContextGranule());
		else
			return createInfLocal(granule.getContextGranule(), granule);
	}

	@Override
	public long createSup(Granule granule) throws TemporalDataException {
		if (granule.getContextGranule() == null)
			throw new TemporalDataException("Cannot generate sup without context granule");

		if (granule.getGranularity().getGlobalGranularityIdentifier() < granule.getContextGranule().getGranularity().getGlobalGranularityIdentifier())
			return createSupLocal(granule, granule.getContextGranule());
		else
			return createSupLocal(granule.getContextGranule(), granule);
	}

	@Override
	public long getMinGranuleIdentifier(Granularity granularity) throws TemporalDataException {
		GranularityAssociation<GregorianGranularity> association = calendarAssociationMap.get(granularity.getCalendar().getLocalCalendarIdentifier());
		GregorianGranularity granularityEnum = association.getAssociation(granularity);
		GregorianGranularity granularityContextEnum = association.getAssociation(granularity.getContextGranularity());

		switch (granularityEnum) {
			case Millisecond:
				switch (granularityContextEnum) {
					case Top:
						return Long.MIN_VALUE;
					default:
						return 0;
				}
			case Second:
				switch (granularityContextEnum) {
					case Top:
						return Long.MIN_VALUE;
					default:
						return 0;
				}
			case Minute:
				switch (granularityContextEnum) {
					case Top:
						return Long.MIN_VALUE;
					default:
						return 0;
				}
			case Hour:
				switch (granularityContextEnum) {
					case Top:
						return Long.MIN_VALUE;
					default:
						return 0;
				}
			case Day:
				switch (granularityContextEnum) {
					case Top:
						return Long.MIN_VALUE;
					default:
						return 0;
				}
			case Week:
				switch (granularityContextEnum) {
					case Top:
						return Long.MIN_VALUE;
					default:
						return 0;
				}
			case Month:
				switch (granularityContextEnum) {
					case Top:
						return Long.MIN_VALUE;
					default:
						return 0;
				}
			case Quarter:
				switch (granularityContextEnum) {
					case Top:
						return Long.MIN_VALUE;
					default:
						return 0;
				}
			case Year:
			case Decade:
				switch (granularityContextEnum) {
					case Top:
						return Long.MIN_VALUE;
					default:
						return 0;
				}
		}

		return 0;
	}

	@Override
	public long getMaxGranuleIdentifier(Granularity granularity) throws TemporalDataException {
		GranularityAssociation<GregorianGranularity> association = calendarAssociationMap.get(granularity.getCalendar().getLocalCalendarIdentifier());
		GregorianGranularity granularityEnum = association.getAssociation(granularity);
		GregorianGranularity granularityContextEnum = association.getAssociation(granularity.getContextGranularity());

		switch (granularityEnum) {
			case Millisecond:
				switch (granularityContextEnum) {
					case Millisecond:
						return 0L;
					case Second:
						return 999L;
					case Minute:
						return 59999L;
					case Hour:
						return 3599999L;
					case Day:
						return 86399999L;
					case Week:
						return 604799999L;
					case Month:
						return 2678399999L;
					case Quarter:
						return 7948799999L;
					case Year:
						return 31622399999L;
					case Decade:
						return 315619199999L;
					case Top:
						return Long.MAX_VALUE;
					default:
						throw new UnsupportedOperationException();
						//						return 1000L;
				}
			case Second:
				switch (granularityContextEnum) {
					case Minute:
						return 59L;
					case Hour:
						return 3599L;
					case Day:
						return 86399L;
					case Week:
						return 604799L;
					case Month:
						return 2678399L;
					case Quarter:
						return 7948799L;
					case Year:
						return 31622399L;
					case Decade:
						return 315619199L;
					case Top:
						return Long.MAX_VALUE;
					default:
						throw new UnsupportedOperationException();
				}
			case Minute:
				switch (granularityContextEnum) {
					case Hour:
						return 59L;
					case Day:
						return 1439L;
					case Week:
						return 10079L;
					case Month:
						return 44639L;
					case Quarter:
						return 132479L;
					case Year:
						return 527039L;
					case Decade:
						return 5260319L;
					case Top:
						return Long.MAX_VALUE;
					default:
						throw new UnsupportedOperationException();
				}
			case Hour:
				switch (granularityContextEnum) {
					case Day:
						return 23L;
					case Week:
						return 167L;
					case Month:
						return 743L;
					case Quarter:
						return 2207L;
					case Year:
						return 8783L;
					case Decade:
						return 87671L;
					case Top:
						return Long.MAX_VALUE;
					default:
						throw new UnsupportedOperationException();
				}
			case Day:
				switch (granularityContextEnum) {
					case Week:
						return 6L;
					case Month:
						return 30L;
					case Quarter:
						return 91L;
					case Year:
						return 365L;
					case Decade:
						return 3652L;
					case Top:
						return Long.MAX_VALUE;
					default:
						throw new UnsupportedOperationException();
				}
			case Week:
				switch (granularityContextEnum) {
					case Month:
						return 5L;
					case Quarter:
						return 13L;
					case Year:
						return 52L;
					case Decade:
						return 521L;
					case Top:
						return Long.MAX_VALUE;
					default:
						throw new UnsupportedOperationException();
				}
			case Month:
				switch (granularityContextEnum) {
					case Quarter:
						return 2L;
					case Year:
						return 11L;
					case Decade:
						return 119; // = 10 * 12 - 1
					case Top:
						return Long.MAX_VALUE;
					default:
						throw new UnsupportedOperationException();
				}
			case Quarter:
				switch (granularityContextEnum) {
					case Year:
						return 3L;
					case Decade:
						return 39; // = 10 * 4 - 1
					case Top:
						return Long.MAX_VALUE;
					default:
						throw new UnsupportedOperationException();
				}
			case Year:
				switch (granularityContextEnum) {
					case Decade:
						return 9;
					case Top:
						return Long.MAX_VALUE;
					default:
						throw new UnsupportedOperationException();
				}
			case Decade:
				switch (granularityContextEnum) {
					case Top:
						return Long.MAX_VALUE;
					default:
						throw new UnsupportedOperationException();
				}
		}

		return 0;
	}

	@Override
	public boolean contains(Granule granule, long chronon) throws TemporalDataException {
		GranularityAssociation<GregorianGranularity> association = calendarAssociationMap.get(granule.getGranularity().getCalendar().getLocalCalendarIdentifier());
		if (association.getAssociation(granule.getGranularity()) ==
				GregorianGranularity.Top) {
			return chronon >= granule.getInf() && chronon <= granule.getSup();
		} else {
			Granule g2 = new Granule(chronon, chronon, granule.getGranularity());
			return granule.getIdentifier() == g2.getIdentifier();
		}
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
	public int getLocalCalendarManagerIdentifier() {
		return LOCAL_CALENDAR_MANAGER;
	}

	@Override
	public int getLocalCalendarManagerVersionIdentifier() {
		return LOCAL_CALENDAR_MANAGER_VERSION;
	}

	@Override
	public Granularity getGranularity(Calendar calendar, String granularityName, String contextGranularityName) {
		Granularity granularity = null;
		Granularity contextGranularity = null;
		for (Granularity currentGranularity : calendar.getGranularities()) {
			if (granularity != null && contextGranularity != null){
				break;
			}
			if (currentGranularity.getGranularityLabel().equalsIgnoreCase(granularityName)){
				granularity = currentGranularity;
				continue;
			}
			if (currentGranularity.getGranularityLabel().equalsIgnoreCase(contextGranularityName)){
				contextGranularity = currentGranularity;
			}
		}

		if (granularity != null && contextGranularity != null){
			try {
				return granularity.setIntoContext(contextGranularity);
			}
			catch (TemporalDataException e) {
				return null;
			}
		}
		return null;
	}

	@Override
	public void registerCalendar(int localIdentifier, Calendar calendar) throws TemporalDataException {
		if (calendarMap.containsKey(localIdentifier)) {
			throw new TemporalDataException("Calendar: " + getClass().getSimpleName() + " already contains a calendar with identifier: " + localIdentifier);
		}
		calendarMap.put(localIdentifier, calendar);
		associateGranularities(localIdentifier, calendar);
	}

	private void associateGranularities(Integer localIdentifier, Calendar calendar) throws TemporalDataException {
		GranularityAssociation<GregorianGranularity> association = new GranularityAssociation<>();
		calendarAssociationMap.put(localIdentifier, association);

		enumLoop:
		for (GregorianGranularity currentEnumGranularity : GregorianGranularity.values()) {
			for (Granularity currentGranularity : calendar.getGranularities()) {
				if (currentEnumGranularity.toString().equalsIgnoreCase(currentGranularity.getGranularityLabel())) {
					association.associateGranularity(currentEnumGranularity, currentGranularity);
					continue enumLoop;
				}
			}
		}
		if (association.getAssociationCount() != GregorianGranularity.values().length) {
			throw new TemporalDataException("Failed to map all internal granularities defined in " + GregorianGranularity.class.getSimpleName());
		}
	}

	@Override
	public long getMaxLengthInIdentifiers(Granularity granularity) throws TemporalDataException {
		GranularityAssociation<GregorianGranularity> association = calendarAssociationMap.get(granularity.getCalendar().getLocalCalendarIdentifier());
		GregorianGranularity granularityEnum = association.getAssociation(granularity);
		GregorianGranularity granularityContextEnum = association.getAssociation(granularity.getContextGranularity());

		switch (granularityEnum) {
			case Millisecond:
				switch (granularityContextEnum) {
					case Millisecond:
						return 1L;
					case Second:
						return 1000L;
					case Minute:
						return 60000L;
					case Hour:
						return 3600000L;
					case Day:
						return 86400000L;
					case Week:
						return 604800000L;
					case Month:
						return 2678400000L;
					case Quarter:
						return 7948800000L;
					case Year:
						return 31622400000L;
					case Decade:
						return 315619200000L;
					case Top:
						return Long.MAX_VALUE;
					default:
						throw new UnsupportedOperationException();
						//						return 1000L;
				}
			case Second:
				switch (granularityContextEnum) {
					case Minute:
						return 60L;
					case Hour:
						return 3600L;
					case Day:
						return 86400L;
					case Week:
						return 604800L;
					case Month:
						return 267840L;
					case Quarter:
						return 7948800L;
					case Year:
						return 31622400L;
					case Decade:
						return 315619200L;
					case Top:
						return Long.MAX_VALUE;
					default:
						throw new UnsupportedOperationException();
				}
			case Minute:
				switch (granularityContextEnum) {
					case Hour:
						return 60L;
					case Day:
						return 1440L;
					case Week:
						return 10080L;
					case Month:
						return 44640L;
					case Quarter:
						return 132480L;
					case Year:
						return 527040L;
					case Decade:
						return 5260320L;
					case Top:
						return Long.MAX_VALUE;
					default:
						throw new UnsupportedOperationException();
				}
			case Hour:
				switch (granularityContextEnum) {
					case Day:
						return 24L;
					case Week:
						return 168L;
					case Month:
						return 744L;
					case Quarter:
						return 2208L;
					case Year:
						return 8784L;
					case Decade:
						return 87672L;
					case Top:
						return Long.MAX_VALUE;
					default:
						throw new UnsupportedOperationException();
				}
			case Day:
				switch (granularityContextEnum) {
					case Week:
						return 7L;
					case Month:
						return 31L;
					case Quarter:
						return 92L;
					case Year:
						return 366L;
					case Decade:
						return 3653L;
					case Top:
						return Long.MAX_VALUE;
					default:
						throw new UnsupportedOperationException();
				}
			case Week:
				switch (granularityContextEnum) {
					case Month:
						return 6L;
					case Quarter:
						return 14L;
					case Year:
						return 53L;
					case Decade:
						return 521L;
					case Top:
						return Long.MAX_VALUE;
					default:
						throw new UnsupportedOperationException();
				}
			case Month:
				switch (granularityContextEnum) {
					case Quarter:
						return 3L;
					case Year:
						return 12L;
					case Decade:
						return 120L;
					case Top:
						return Long.MAX_VALUE;
					default:
						throw new UnsupportedOperationException();
				}
			case Quarter:
				switch (granularityContextEnum) {
					case Year:
						return 4L;
					case Decade:
						return 40L;
					case Top:
						return Long.MAX_VALUE;
					default:
						throw new UnsupportedOperationException();
				}
			case Year:
				switch (granularityContextEnum) {
					case Decade:
						return 10L;
					case Top:
						return Long.MAX_VALUE;
					default:
						throw new UnsupportedOperationException();
				}
			case Decade:
				switch (granularityContextEnum) {
					case Top:
						return Long.MAX_VALUE;
					default:
						throw new UnsupportedOperationException();
				}
		}

		return 0;
	}

	@Override
	public long createGranuleIdentifier(Granule granule) throws TemporalDataException {
		long result = 0;

		GranularityAssociation<GregorianGranularity> association = calendarAssociationMap.get(granule.getGranularity().getCalendar().getLocalCalendarIdentifier());
		GregorianGranularity granularityEnum = association.getAssociation(granule.getGranularity());
		GregorianGranularity granularityContextEnum = association.getAssociation(granule.getGranularity().getContextGranularity());

		GregorianCalendar timeStamp = new GregorianCalendar();
		timeStamp.setTimeZone(TimeZone.getTimeZone("UTC"));
		timeStamp.setTimeInMillis(granule.getInf());

		switch (granularityEnum) {
			case Millisecond:
				switch (granularityContextEnum) {
					case Second:
						result = granule.getInf() % 1000L;
						break;
					case Minute:
						result = granule.getInf() % 60000L;
						break;
					case Hour:
						result = granule.getInf() % 3600000L;
						break;
					case Day:
						result = granule.getInf() % 86400000L;
						break;
					case Week: {
						result = ((timeStamp.get(GregorianCalendar.DAY_OF_WEEK) + 5) % 7) * 86400000L + granule.getInf() % 86400000L;
						break;
					}
					case Month: {
						result = (timeStamp.get(GregorianCalendar.DAY_OF_MONTH) - 1) * 86400000L + granule.getInf() % 86400000L;
						break;
					}
					case Quarter:
						result = getDayInQuarter(timeStamp) * 86400000L + granule.getInf() % 86400000L;
						break;
					case Year: {
						result = (timeStamp.get(GregorianCalendar.DAY_OF_YEAR) - 1) * 86400000L + granule.getInf() % 86400000L;
						break;
					}
					case Decade:
						throw new UnsupportedOperationException("Not implemented yet.");
					default:
						result = granule.getInf();
				}
				break;
			case Second:
				result = granule.getInf() / 1000L;
				switch (granularityContextEnum) {
					case Minute:
						result %= 60L;
						break;
					case Hour:
						result %= 3600L;
						break;
					case Day:
						result %= 86400L;
						break;
					case Week: {
						result = ((timeStamp.get(GregorianCalendar.DAY_OF_WEEK) + 5) % 7) * 86400L + result % 86400L;
						break;
					}
					case Month: {
						result = (timeStamp.get(GregorianCalendar.DAY_OF_MONTH) - 1) * 86400L + result % 86400L;
						break;
					}
					case Quarter:
						result = getDayInQuarter(timeStamp) * 86400L + result % 86400L;
						break;
					case Year: {
						result = (timeStamp.get(GregorianCalendar.DAY_OF_YEAR) - 1) * 86400L + result % 86400L;
						break;
					}
					case Decade:
						throw new UnsupportedOperationException("Not implemented yet.");
				}
				break;
			case Minute:
				result = granule.getInf() / 60000L;
				switch (granularityContextEnum) {
					case Hour:
						result %= 60;
						break;
					case Day:
						result %= 1440;
						break;
					case Week: {
						result = ((timeStamp.get(GregorianCalendar.DAY_OF_WEEK) + 5) % 7) * 1440L + result % 1440L;
						break;
					}
					case Month: {
						result = (timeStamp.get(GregorianCalendar.DAY_OF_MONTH) - 1) * 1440L + result % 1440L;
						break;
					}
					case Quarter:
						result = getDayInQuarter(timeStamp) * 1440L + result % 1440L;
						break;
					case Year: {
						result = (timeStamp.get(GregorianCalendar.DAY_OF_YEAR) - 1) * 1440L + result % 1440L;
						break;
					}
					case Decade:
						throw new UnsupportedOperationException("Not implemented yet.");
				}
				break;
			case Hour:
				result = granule.getInf() / 3600000L;
				switch (granularityContextEnum) {
					case Day:
						result %= 24;
						break;
					case Week: {
						result = ((timeStamp.get(GregorianCalendar.DAY_OF_WEEK) + 5) % 7) * 24 + result % 24;
						break;
					}
					case Month: {
						result = (timeStamp.get(GregorianCalendar.DAY_OF_MONTH) - 1) * 24 + result % 24;
						break;
					}
					case Quarter:
						result = getDayInQuarter(timeStamp) * 24 + result % 24;
						break;
					case Year: {
						result = (timeStamp.get(GregorianCalendar.DAY_OF_YEAR) - 1) * 24 + result % 24;
						break;
					}
					case Decade:
						throw new UnsupportedOperationException("Not implemented yet.");
				}
				break;
			case Day:
				switch (granularityContextEnum) {
					case Week: {
						result = (timeStamp.get(GregorianCalendar.DAY_OF_WEEK) + 5) % 7;
						break;
					}
					case Month: {
						result = timeStamp.get(GregorianCalendar.DAY_OF_MONTH) - 1;
						break;
					}
					case Quarter:
						result = getDayInQuarter(timeStamp);
						break;
					case Year: {
						result = timeStamp.get(GregorianCalendar.DAY_OF_YEAR) - 1;
						break;
					}
					case Decade:
						throw new UnsupportedOperationException("Not implemented yet.");
					default:
						result = granule.getInf() / 86400000L;
				}
				break;
			case Week:
				// TODO add context granule to make this "clean"
				switch (granularityContextEnum) {
					case Month: {
						result = timeStamp.get(GregorianCalendar.WEEK_OF_MONTH) - 1;
						break;
					}
					case Quarter: {
						int weekCounter = 0;
						if (timeStamp.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.MONDAY)
							weekCounter--;
						int originalQuarter = timeStamp.get(GregorianCalendar.MONTH) / 3;
						long chronon = granule.getInf();
						while (timeStamp.get(GregorianCalendar.MONTH) / 3 == originalQuarter) {
							weekCounter++;
							chronon -= 604800000L;
							timeStamp.setTimeInMillis(chronon);
						}
						timeStamp.set(GregorianCalendar.DAY_OF_MONTH, 1);
						timeStamp.add(GregorianCalendar.MONTH, 1);
						switch (timeStamp.get(GregorianCalendar.DAY_OF_WEEK)) {
							case GregorianCalendar.MONDAY:
							case GregorianCalendar.TUESDAY:
							case GregorianCalendar.WEDNESDAY:
							case GregorianCalendar.THURSDAY:
								weekCounter++;
						}

						result = weekCounter;
						break;
					}
					case Year: {
						result = timeStamp.get(GregorianCalendar.WEEK_OF_YEAR) - 1;
						break;
					}
					case Decade:
						throw new UnsupportedOperationException("Not implemented yet.");
					default:
						result = (granule.getInf() + 259200000L) / 604800000L; // Add the 3 days of week zero that were not in 1970
						break;
				}
				break;
			case Month: {
				switch (granularityContextEnum) {
					case Quarter:
						result = timeStamp.get(GregorianCalendar.MONTH) % 3;
						break;
					case Year:
						result = timeStamp.get(GregorianCalendar.MONTH);
						break;
					case Decade:
						result = timeStamp.get(GregorianCalendar.MONTH) +
								((timeStamp.get(GregorianCalendar.YEAR) - 1) % 10) * 12;
						break;
					default:
						result = (timeStamp.get(GregorianCalendar.YEAR) - 1970) * 12 +
								timeStamp.get(GregorianCalendar.MONTH);
						break;
				}
				break;
			}
			case Quarter: {
				switch (granularityContextEnum) {
					case Year:
						result = timeStamp.get(GregorianCalendar.MONTH) / 3;
						break;
					case Decade:
						result = timeStamp.get(GregorianCalendar.MONTH) / 3 +
								((timeStamp.get(GregorianCalendar.YEAR) - 1) % 10) * 4;
						break;
					default:
						result = (timeStamp.get(GregorianCalendar.YEAR) - 1970) * 4 +
								timeStamp.get(GregorianCalendar.MONTH) / 3;
						break;
				}
				break;
			}
			case Year: {
				switch (granularityContextEnum) {
					case Decade:
						result = (timeStamp.get(GregorianCalendar.YEAR) - 1) % 10;
						break;
					default:
						result = timeStamp.get(GregorianCalendar.YEAR) - 1970;
				}
				break;
			}
			case Decade: {
				result = (timeStamp.get(GregorianCalendar.YEAR) - 1) / 10;
				//				System.out.println(timeStamp.get(GregorianCalendar.YEAR) + "  " + result);
				break;
			}
		}

		return result;
	}

	private long getDayInQuarter(GregorianCalendar timeStamp) {
		switch (timeStamp.get(GregorianCalendar.MONTH)) {
			case GregorianCalendar.JANUARY:
				return timeStamp.get(GregorianCalendar.DAY_OF_MONTH) - 1;
			case GregorianCalendar.FEBRUARY:
				return 31 + timeStamp.get(GregorianCalendar.DAY_OF_MONTH) - 1;
			case GregorianCalendar.MARCH:
				return 31 +
						(timeStamp.isLeapYear(timeStamp.get(GregorianCalendar.YEAR)) ? 29 : 28) +
						timeStamp.get(GregorianCalendar.DAY_OF_MONTH) - 1;
			case GregorianCalendar.APRIL:
				return timeStamp.get(GregorianCalendar.DAY_OF_MONTH) - 1;
			case GregorianCalendar.MAY:
				return 30 + timeStamp.get(GregorianCalendar.DAY_OF_MONTH) - 1;
			case GregorianCalendar.JUNE:
				return 61 + timeStamp.get(GregorianCalendar.DAY_OF_MONTH) - 1;
			case GregorianCalendar.JULY:
				return timeStamp.get(GregorianCalendar.DAY_OF_MONTH) - 1;
			case GregorianCalendar.AUGUST:
				return 31 + timeStamp.get(GregorianCalendar.DAY_OF_MONTH) - 1;
			case GregorianCalendar.SEPTEMBER:
				return 62 + timeStamp.get(GregorianCalendar.DAY_OF_MONTH) - 1;
			case GregorianCalendar.OCTOBER:
				return timeStamp.get(GregorianCalendar.DAY_OF_MONTH) - 1;
			case GregorianCalendar.NOVEMBER:
				return 31 + timeStamp.get(GregorianCalendar.DAY_OF_MONTH) - 1;
			case GregorianCalendar.DECEMBER:
				return 61 + timeStamp.get(GregorianCalendar.DAY_OF_MONTH) - 1;
			default:
				assert false;
				return 0l;
		}
	}

	private long createInfLocal(Granule granule, Granule contextGranule) throws TemporalDataException {
		GranularityAssociation<GregorianGranularity> association = calendarAssociationMap.get(granule.getGranularity().getCalendar().getLocalCalendarIdentifier());
		GregorianGranularity granularityEnum = association.getAssociation(granule.getGranularity());
		GregorianGranularity granularityContextEnum = association.getAssociation(contextGranule.getGranularity());

		GregorianCalendar timeStamp = new GregorianCalendar();
		timeStamp.setTimeZone(TimeZone.getTimeZone("UTC"));

		long result = 0;

		switch (granularityEnum) {
			case Millisecond:
				result = granule.getIdentifier();
				if (granularityContextEnum != GregorianGranularity.Top)
					result += contextGranule.getInf();
				break;
			case Second:
				result = granule.getIdentifier() * 1000L;
				if (granularityContextEnum != GregorianGranularity.Top)
					result += contextGranule.getInf();
				break;
			case Minute:
				result = granule.getIdentifier() * 60000L;
				if (granularityContextEnum != GregorianGranularity.Top)
					result += contextGranule.getInf();
				break;
			case Hour:
				result = granule.getIdentifier() * 3600000L;
				if (granularityContextEnum != GregorianGranularity.Top)
					result += contextGranule.getInf();
				break;
			case Day:
				// Warning does not handle day light saving time
				result = granule.getIdentifier() * 86400000L;
				if (granularityContextEnum != GregorianGranularity.Top)
					result += contextGranule.getInf();
				break;
			case Week:
				switch (granularityContextEnum) {
					case Month: {
						timeStamp.setTimeInMillis(contextGranule.getInf());
						if (timeStamp.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.FRIDAY ||
								timeStamp.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SATURDAY ||
								timeStamp.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SUNDAY)
							result = timeStamp.getTimeInMillis() + granule.getIdentifier() * 604800000L;
						else
							result = timeStamp.getTimeInMillis() + (granule.getIdentifier() - 1) * 604800000L;
						break;
					}
					case Year:
					case Decade: {
						timeStamp.setTimeInMillis(contextGranule.getInf());
						if (timeStamp.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.FRIDAY ||
								timeStamp.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SATURDAY ||
								timeStamp.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SUNDAY)
							// roll forward to begin of new week in new year
							result = timeStamp.getTimeInMillis() + (7 - ((timeStamp.get(GregorianCalendar.DAY_OF_WEEK) + 5) % 7)) * 86400000L;
						else
							// roll back to begin of week in last year
							result = timeStamp.getTimeInMillis() - ((timeStamp.get(GregorianCalendar.DAY_OF_WEEK) + 5) % 7) * 86400000L;
						result += granule.getIdentifier() * 604800000L;
						break;
					}
					case Top:
						// 1 Jan 1970 is a Thursday
						result = granule.getIdentifier() * 604800000L - 259200000; // = 3*24*60*60*1000
						break;
					default:
						throw new TemporalDataException("Unknown context granularity");
				}
				break;
			case Month: {
				timeStamp.setTimeInMillis(contextGranule.getInf());
				switch (granularityContextEnum) {
					case Quarter:
						timeStamp.set(GregorianCalendar.MONTH, (int) (granule.getIdentifier() * 3));
						break;
					case Year:
						timeStamp.set(GregorianCalendar.MONTH, (int) (granule.getIdentifier()));
						break;
					case Decade:
						timeStamp.set(GregorianCalendar.MONTH, (int) (granule.getIdentifier() % 12));
						break;
					default:
						timeStamp.setTimeInMillis(0);
						int year = ((int) granule.getIdentifier()) / 12;
						if (granularityContextEnum == GregorianGranularity.Decade) {
							timeStamp.set(GregorianCalendar.YEAR, year + 1970 + 1);
						} else {
							timeStamp.set(GregorianCalendar.YEAR, year + 1970);
						}
						timeStamp.set(GregorianCalendar.MONTH, (int) (granule.getIdentifier() % 12));
						result = timeStamp.getTimeInMillis();
				}
				break;
			}
			case Quarter: {
				timeStamp.setTimeInMillis(contextGranule.getInf());
				switch (granularityContextEnum) {
					case Year:
						timeStamp.set(GregorianCalendar.MONTH, (int) (granule.getIdentifier()) % 4 * 3);
						break;
					case Decade:
						timeStamp.set(GregorianCalendar.MONTH, (int) (granule.getIdentifier()) % 12 * 3);
						break;
					default:
						timeStamp.setTimeInMillis(0);
						int year = ((int) granule.getIdentifier()) / 4;
						if (granularityContextEnum == GregorianGranularity.Decade) {
							timeStamp.set(GregorianCalendar.YEAR, year + 1970 + 1);
						} else {
							timeStamp.set(GregorianCalendar.YEAR, year + 1970);
						}
						//					System.out.println(timeStamp.get(GregorianCalendar.YEAR) + " " + year + " " + granule.getGranularity().getContextLocalIdentifier());
						timeStamp.set(GregorianCalendar.MONTH, (int) (granule.getIdentifier() % 4 * 3));
						result = timeStamp.getTimeInMillis();
				}
				break;
			}
			case Year: {
				timeStamp.setTimeInMillis(0);
				int year = ((int) granule.getIdentifier());
				if (granularityContextEnum == GregorianGranularity.Decade) {
					timeStamp.set(GregorianCalendar.YEAR, year + 1971);
				} else {
					timeStamp.set(GregorianCalendar.YEAR, year + 1970);
				}
				result = timeStamp.getTimeInMillis();
				break;
			}
			case Decade: {
				timeStamp.setTimeInMillis(0);
				timeStamp.set(GregorianCalendar.YEAR, (int) (granule.getIdentifier() * 10 + 1));
				result = timeStamp.getTimeInMillis();
				break;
			}
		}

		return result;
	}

	public long createSupLocal(Granule granule, Granule contextGranule) throws TemporalDataException {
		GranularityAssociation<GregorianGranularity> association = calendarAssociationMap.get(granule.getGranularity().getCalendar().getLocalCalendarIdentifier());
		GregorianGranularity granularityEnum = association.getAssociation(granule.getGranularity());

		GregorianCalendar timeStamp = new GregorianCalendar();
		timeStamp.setTimeZone(TimeZone.getTimeZone("UTC"));
		long result = 0;

		// invalid granule ids for context granularities are not defined (insert context granule)

		switch (granularityEnum) {
			case Millisecond:
				result = granule.getInf();
				break;
			case Second:
				result = granule.getInf() + 999L;
				break;
			case Minute:
				result = granule.getInf() + 59999L;
				break;
			case Hour:
				result = granule.getInf() + 3599999L;
				break;
			case Day:
				result = granule.getInf() + 86399999L;
				break;
			case Week:
				result = granule.getInf() + 604799999;
				break;
			case Month:
				result = granule.getInf();
				int monthId = (int) granule.getIdentifier() % 12;
				if (monthId < 0)
					monthId += 12;
				switch (monthId) {
					case 0:
					case 2:
					case 4:
					case 6:
					case 7:
					case 9:
					case 11:
						result += 2678399999L;
						break;
					case 1:
						timeStamp.setTimeInMillis(result);
						if (timeStamp.isLeapYear(timeStamp.get(GregorianCalendar.YEAR)))
							result += 2505599999L;
						else
							result += 2419199999L;
						break;
					default:
						result += 2591999999L;
				}
				break;
			case Quarter:
				result = granule.getInf();
				int quarterId = (int) granule.getIdentifier() % 4;
				if (quarterId < 0)
					quarterId += 4;
				switch (quarterId) {
					case 0:
						// 86400000
						timeStamp.setTimeInMillis(result);
						if (timeStamp.isLeapYear(timeStamp.get(GregorianCalendar.YEAR)))
							result += 7862399999L;
						else
							result += 7775999999L;
						break;
					case 1:
						result += 7862399999L;
						break;
					default:
						result += 7948799999L;
				}
				break;
			case Year: {
				result = granule.getInf();
				timeStamp.setTimeInMillis(result);
				if (timeStamp.isLeapYear((int) granule.getIdentifier() + 1970))
					result += 31622399999L;
				else
					result += 31535999999L;
				break;
			}
			case Decade: {
				result = granule.getInf();
				timeStamp.setTimeInMillis(result);
				for (int i = (int) granule.getIdentifier() * 10 + 1; i < (int) granule.getIdentifier() * 10 + 1 + 10; i++) {
					if (timeStamp.isLeapYear(i))
						result += 31622400000L;
					else
						result += 31536000000L;
				}
				result--;
				break;
			}
		}

		return result;
	}

	private Granule createGranule(GregorianCalendar calInf, GregorianCalendar calSup, Granularity granularity) throws TemporalDataException {
		GranularityAssociation<GregorianGranularity> association = calendarAssociationMap.get(granularity.getCalendar().getLocalCalendarIdentifier());
		GregorianGranularity granularityEnum = association.getAssociation(granularity);
		GregorianGranularity contextGranularityEnum;

		for (int i = 0; i < granularityEnum.getGregorianCalendarFieldIdentifiers().length; i++) {
			int field = granularityEnum.getGregorianCalendarFieldIdentifiers()[i];
			calInf.set(field, calInf.getActualMinimum(field));
			calSup.set(field, calSup.getActualMaximum(field));
		}

		try {
			contextGranularityEnum = association.getAssociation(granularity.getContextGranularity());
		}
		catch (NullPointerException e) {
			throw new TemporalDataException("Failed to create granule: Context granularity is null.", e);
		}

		if (granularityEnum == GregorianGranularity.Decade) {
			calInf.set(GregorianCalendar.YEAR,
					(calInf.get(GregorianCalendar.YEAR) - 1) / 10 * 10 + 1);
			calSup.set(GregorianCalendar.YEAR,
					(calSup.get(GregorianCalendar.YEAR) - 1) / 10 * 10 + 10);
		} else if (granularityEnum == GregorianGranularity.Quarter) {
			// calInf DAY_OF_MONTH is already at 1
			// calSup DAY_OF_MONTH needs to be set to 1 first
			// because last day may change (e.g., 31 March --June--> 1 July)
			switch (calInf.get(GregorianCalendar.MONTH)) {
				case GregorianCalendar.JANUARY:
				case GregorianCalendar.FEBRUARY:
				case GregorianCalendar.MARCH:
					calInf.set(GregorianCalendar.MONTH, GregorianCalendar.JANUARY);
					calSup.set(GregorianCalendar.DAY_OF_MONTH, 1);
					calSup.set(GregorianCalendar.MONTH, GregorianCalendar.MARCH);
					calSup.set(GregorianCalendar.DAY_OF_MONTH, 31);
					break;
				case GregorianCalendar.APRIL:
				case GregorianCalendar.MAY:
				case GregorianCalendar.JUNE:
					calInf.set(GregorianCalendar.MONTH, GregorianCalendar.APRIL);
					calSup.set(GregorianCalendar.DAY_OF_MONTH, 1);
					calSup.set(GregorianCalendar.MONTH, GregorianCalendar.JUNE);
					calSup.set(GregorianCalendar.DAY_OF_MONTH, 30);
					break;
				case GregorianCalendar.JULY:
				case GregorianCalendar.AUGUST:
				case GregorianCalendar.SEPTEMBER:
					calInf.set(GregorianCalendar.MONTH, GregorianCalendar.JULY);
					calSup.set(GregorianCalendar.DAY_OF_MONTH, 1);
					calSup.set(GregorianCalendar.MONTH, GregorianCalendar.SEPTEMBER);
					calSup.set(GregorianCalendar.DAY_OF_MONTH, 30);
					break;
				case GregorianCalendar.OCTOBER:
				case GregorianCalendar.NOVEMBER:
				case GregorianCalendar.DECEMBER:
					calInf.set(GregorianCalendar.MONTH, GregorianCalendar.OCTOBER);
					calSup.set(GregorianCalendar.DAY_OF_MONTH, 1);
					calSup.set(GregorianCalendar.MONTH, GregorianCalendar.DECEMBER);
					calSup.set(GregorianCalendar.DAY_OF_MONTH, 31);
					break;
			}
		} else if (granularityEnum == GregorianGranularity.Week) {
			long dow = (calInf.get(GregorianCalendar.DAY_OF_WEEK) + 5) % 7;
			long oldInf = calInf.getTimeInMillis();
			calInf.setTimeInMillis(oldInf - dow * 86400000L);
			long oldSup = calSup.getTimeInMillis();
			calSup.setTimeInMillis(oldSup + (6 - dow) * 86400000L);
			if ((contextGranularityEnum == GregorianGranularity.Month &&
					calInf.get(GregorianCalendar.MONTH) != calSup.get(GregorianCalendar.MONTH)) ||
					(contextGranularityEnum == GregorianGranularity.Quarter &&
							calInf.get(GregorianCalendar.MONTH) / 3 != calSup.get(GregorianCalendar.MONTH) / 3) ||
					(contextGranularityEnum == GregorianGranularity.Year) &&
							calInf.get(GregorianCalendar.YEAR) != calSup.get(GregorianCalendar.YEAR)) {
				GregorianCalendar calBorder = new GregorianCalendar();
				calBorder.setTimeZone(TimeZone.getTimeZone("UTC"));
				calBorder.setTimeInMillis(calInf.getTimeInMillis());
				calBorder.set(GregorianCalendar.DAY_OF_MONTH, 1);
				calBorder.add(GregorianCalendar.MONTH, 1);
				boolean front = oldSup < calBorder.getTimeInMillis();
				if (!front)
					front = calBorder.getTimeInMillis() - oldInf > oldSup - calBorder.getTimeInMillis();
				if (front) {
					calSup.set(GregorianCalendar.DAY_OF_MONTH, 1);
					calSup.set(GregorianCalendar.MONTH, calInf.get(GregorianCalendar.MONTH));
					calSup.set(GregorianCalendar.YEAR, calInf.get(GregorianCalendar.YEAR));
					calSup.set(GregorianCalendar.DAY_OF_MONTH, calSup.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
				} else {
					calInf.set(GregorianCalendar.DAY_OF_MONTH, 1);
					calInf.set(GregorianCalendar.MONTH, calSup.get(GregorianCalendar.MONTH));
					calInf.set(GregorianCalendar.YEAR, calSup.get(GregorianCalendar.YEAR));
				}
			}
		}

		return new Granule(calInf.getTimeInMillis(), calSup.getTimeInMillis(), Granule.MODE_FORCE, granularity);
	}

	protected Granule createGranule(long chronon, Granularity granularity) throws TemporalDataException {
		GregorianCalendar calInf = new GregorianCalendar();
		GregorianCalendar calSup = new GregorianCalendar();
		calInf.setTimeZone(TimeZone.getTimeZone("UTC"));
		calSup.setTimeZone(TimeZone.getTimeZone("UTC"));
		calInf.setTimeInMillis(chronon);
		calSup.setTimeInMillis(chronon);

		return createGranule(calInf, calSup, granularity);
	}
}
