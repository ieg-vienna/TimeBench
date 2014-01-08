package timeBench.calendar.util;

import timeBench.data.TemporalDataException;

/**
 * This class is a factory responsible for converting between composite and single identifier fields.
 * The composite identifier stored in a 32bit integer and is laid out as follows:
 * 31 30 29 28 27 26 25 24 23 22 21 20 19 18 17 16 15 14 13 12 11 10 09 08 07 06 05 04 03 02 01 00
 * M  M  M  M  M  V  V  V  V  V  V  V  V  C  C  C  C  C  C  C  T  T  T  T  T  G  G  G  G  G  G  G
 * <p/>
 * M: Calendar Manager
 * V: Version of the Calendar Manager
 * C: Calendar
 * T: Type Granularity (e.g., western modern (week), metric (kilosecond), ...)
 * G: Granularity (e.g., week, kilosecond, ...)
 * <p/>
 */
public class IdentifierConverter {
	private static final int GRANULARITY_BITS = 7;
	private static final int TYPE_GRANULARITY_BITS = 5;
	private static final int CALENDAR_BITS = 7;
	private static final int VERSION_BITS = 8;
	private static final int MANAGER_BITS = 5;

	private static final int GRANULARITY_MAX = getMax(GRANULARITY_BITS);
	private static final int TYPE_GRANULARITY_MAX = getMax(TYPE_GRANULARITY_BITS);
	private static final int CALENDAR_MAX = getMax(CALENDAR_BITS);
	private static final int VERSION_MAX = getMax(VERSION_BITS);
	private static final int MANAGER_MAX = getMax(MANAGER_BITS);

	private static final int GRANULARITY_MIN = 0;
	private static final int TYPE_GRANULARITY_MIN = 0;
	private static final int CALENDAR_MIN = 0;
	private static final int VERSION_MIN = 0;
	private static final int MANAGER_MIN = 0;

	//TODO: convert bitmask to dynamic masks based off of the BIT fields
	private static final int GRANULARITY_BITMASK = 0b00000000000000000000000001111111;
	private static final int TYPE_GRANULARITY_BITMASK = 0b00000000000000000000111110000000;
	private static final int CALENDAR_BITMASK = 0b00000000000001111111000000000000;
	private static final int VERSION_BITMASK = 0b00000111111110000000000000000000;
	private static final int MANAGER_BITMASK = 0b11111000000000000000000000000000;

	private static IdentifierConverter instance = null;

	public static IdentifierConverter getInstance() {
		if (instance == null) {
			instance = new IdentifierConverter();
		}
		return instance;
	}

	private IdentifierConverter() {
	}

	/**
	 * Assembles a global identifier capable of identifying all fields: manager, managerVersion, calendar, granularityType, granularity.
	 *
	 * @param managerIdentifier         The calendar manager identifier. Permitted values: {@link #MANAGER_MIN} to {@link #MANAGER_MAX}
	 * @param versionIdentifier         The calendar manager version identifier. Permitted values: {@link #VERSION_MIN} to {@link #VERSION_MAX}
	 * @param calendarIdentifier        The calendar identifier. Permitted values: {@link #CALENDAR_MIN} to {@link #CALENDAR_MAX}
	 * @param granularityTypeIdentifier The granularity type identifier. Permitted values: {@link #TYPE_GRANULARITY_MIN} to {@link #TYPE_GRANULARITY_MAX}
	 * @param granularityIdentifier     The granularity identifier. Permitted values: {@link #GRANULARITY_MIN} to {@link #GRANULARITY_MAX}
	 * @return The assembled global identifier.
	 * @throws TemporalDataException Thrown if any of the fields exceeds the values allowed by their respective bit length allocation.
	 */
	public int buildGlobalIdentifier(int managerIdentifier, int versionIdentifier, int calendarIdentifier, int granularityTypeIdentifier, int granularityIdentifier) throws TemporalDataException {
		if (!(
				(managerIdentifier >= MANAGER_MIN && managerIdentifier <= MANAGER_MAX) &&
						(versionIdentifier >= VERSION_MIN && versionIdentifier <= VERSION_MAX) &&
						(calendarIdentifier >= CALENDAR_MIN && calendarIdentifier <= CALENDAR_MAX) &&
						(granularityTypeIdentifier >= TYPE_GRANULARITY_MIN && granularityTypeIdentifier <= TYPE_GRANULARITY_MAX) &&
						(granularityIdentifier >= GRANULARITY_MIN && granularityIdentifier <= GRANULARITY_MAX))) {
			throw new TemporalDataException("Passed identifiers " +
					"(M=" + managerIdentifier + ", " +
					"V=" + versionIdentifier + ", " +
					"C=" + calendarIdentifier + ", " +
					"T=" + granularityTypeIdentifier + ", " +
					"G=" + granularityIdentifier + ") not within acceptable range " +
					"M=" + MANAGER_MIN + " - " + MANAGER_MAX + ", " +
					"V=" + VERSION_MIN + " - " + VERSION_MAX + ", " +
					"C=" + CALENDAR_MIN + " - " + CALENDAR_MAX + ", " +
					"T=" + TYPE_GRANULARITY_MIN + " - " + TYPE_GRANULARITY_MAX + ", " +
					"G=" + GRANULARITY_MIN + " - " + GRANULARITY_MAX + ")");
		}
		return (((((((managerIdentifier << VERSION_BITS) | versionIdentifier) << CALENDAR_BITS) | calendarIdentifier) << TYPE_GRANULARITY_BITS) | granularityTypeIdentifier) << GRANULARITY_BITS) | granularityIdentifier;
	}

	/**
	 * Builds a global identifier capable of identifying: manager, managerVersion. All remaining fields are 0.
	 *
	 * @param managerIdentifier The calendar manager identifier. Permitted values: {@link #MANAGER_MIN} to {@link #MANAGER_MAX}
	 * @param versionIdentifier The calendar manager version identifier. Permitted values: {@link #VERSION_MIN} to {@link #VERSION_MAX}
	 * @return The assembled global identifier with manager and manager version set, all other bits 0.
	 * @throws TemporalDataException
	 */
	public int buildCalendarManagerVersionIdentifier(int managerIdentifier, int versionIdentifier) throws TemporalDataException {
		return buildGlobalIdentifier(managerIdentifier, versionIdentifier, 0, 0, 0);
	}

	/**
	 * Returns the granularity identifier from a composite identifier.
	 *
	 * @param identifier The composite identifier.
	 * @return The extracted granularity identifier.
	 */
	public int getGranularityIdentifier(int identifier) {
		return identifier & GRANULARITY_BITMASK;
	}

	/**
	 * Returns the granularity type identifier from a composite identifier.
	 *
	 * @param identifier The composite identifier.
	 * @return The extracted granularity type identifier.
	 */
	public int getTypeIdentifier(int identifier) {
		return (identifier & TYPE_GRANULARITY_BITMASK) >> GRANULARITY_BITS;
	}

	/**
	 * Returns the calendar identifier from a composite identifier.
	 *
	 * @param identifier The composite identifier.
	 * @return The extracted calendar identifier.
	 */
	public int getCalendarIdentifier(int identifier) {
		return (identifier & CALENDAR_BITMASK) >> (GRANULARITY_BITS + TYPE_GRANULARITY_BITS);
	}

	/**
	 * Returns the calendar manager version identifier from a composite identifier.
	 *
	 * @param identifier The composite identifier.
	 * @return The extracted calendar manager version identifier.
	 */
	public int getVersionIdentifier(int identifier) {
		return (identifier & VERSION_BITMASK) >> (GRANULARITY_BITS + TYPE_GRANULARITY_BITS + CALENDAR_BITS);
	}

	/**
	 * Returns the calendar manager identifier from a composite identifier.
	 *
	 * @param identifier The composite identifier.
	 * @return The extracted calendar manager identifier.
	 */
	public int getManagerIdentifier(int identifier) {
		return (identifier & MANAGER_BITMASK) >> (GRANULARITY_BITS + TYPE_GRANULARITY_BITS + CALENDAR_BITS + VERSION_BITS);
	}

	private static int getMax(int bitLength) {
		return (int) Math.pow(2, bitLength) - 1;
	}
}
