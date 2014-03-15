package timeBench.calendar;

import junit.framework.TestCase;
import timeBench.calendar.util.CalendarRegistry;
import timeBench.calendar.util.IdentifierConverter;
import timeBench.data.TemporalDataException;

import java.io.File;

public class CalendarRegistryTest extends TestCase {
	private static final int MANAGER_IDENTIFIER = 2;
	private static final int VERSION_IDENTIFIER = 1;
	private static final int CALENDAR_IDENTIFIER = 1;
	private static final String TOP_GRANULARITY_LABEL = "Top";
	private static final String BOTTOM_GRANULARITY_LABEL = "Millisecond";
	private static final String NO_TOP_BOTTOM_MESSAGE = "Top and/or bottom granularity not set.";
	private static final String MISSING_REFERENCE_MESSAGE = "Failed to initialize permissible context granularities: could not find granularity reference";
	private static final String MISSING_GRANULARITY_MESSAGE = "Failed to map all internal granularities defined in";

	public void testLoadCalendar() throws TemporalDataException {
		CalendarRegistry.getInstance().loadCalendar(new File("resources/calendars/GregorianCalendar.xml"));

		CalendarManager gregorianCalendarManager = CalendarRegistry.getInstance().getCalendarManager(
				IdentifierConverter.getInstance().buildCalendarManagerVersionIdentifier(
						MANAGER_IDENTIFIER, VERSION_IDENTIFIER),
				true);

		assertNotNull(gregorianCalendarManager);

		Calendar calendar = CalendarRegistry.getInstance().getCalendar(
				IdentifierConverter.getInstance().buildCalendarIdentifier(
						MANAGER_IDENTIFIER, VERSION_IDENTIFIER, CALENDAR_IDENTIFIER),
				true);

		assertNotNull(calendar);

		assertTrue(BOTTOM_GRANULARITY_LABEL.equalsIgnoreCase(calendar.getBottomGranularity().getGranularityLabel()));
		assertTrue(TOP_GRANULARITY_LABEL.equalsIgnoreCase(calendar.getTopGranularity().getGranularityLabel()));
	}

	public void testNoBottom() {
		try {
			CalendarRegistry.getInstance().loadCalendar(new File("resources/calendars/test/noBottom.xml"));
			CalendarManager gregorianCalendarManager = CalendarRegistry.getInstance().getCalendarManager(
					IdentifierConverter.getInstance().buildCalendarManagerVersionIdentifier(
							MANAGER_IDENTIFIER, VERSION_IDENTIFIER),
					true);
			System.out.println(gregorianCalendarManager.getCalendar(CALENDAR_IDENTIFIER).getBottomGranularity());
		}
		catch (TemporalDataException e) {
			//expected
			assertTrue(NO_TOP_BOTTOM_MESSAGE.equalsIgnoreCase(e.getLocalizedMessage()));
			return;
		}
		fail();
	}

	public void testNoTop() {
		try {
			CalendarRegistry.getInstance().loadCalendar(new File("resources/calendars/test/noTop.xml"));
			CalendarManager gregorianCalendarManager = CalendarRegistry.getInstance().getCalendarManager(
					IdentifierConverter.getInstance().buildCalendarManagerVersionIdentifier(
							MANAGER_IDENTIFIER, VERSION_IDENTIFIER),
					true);
			System.out.println(gregorianCalendarManager.getCalendar(CALENDAR_IDENTIFIER).getBottomGranularity());
		}
		catch (TemporalDataException e) {
			//expected
			assertTrue(NO_TOP_BOTTOM_MESSAGE.equalsIgnoreCase(e.getLocalizedMessage()));
			return;
		}
		fail();
	}

	public void testMissingReference() {
		try {
			CalendarRegistry.getInstance().loadCalendar(new File("resources/calendars/test/missingReference.xml"));
			CalendarManager gregorianCalendarManager = CalendarRegistry.getInstance().getCalendarManager(
					IdentifierConverter.getInstance().buildCalendarManagerVersionIdentifier(
							MANAGER_IDENTIFIER, VERSION_IDENTIFIER),
					true);
			System.out.println(gregorianCalendarManager.getCalendar(CALENDAR_IDENTIFIER).getBottomGranularity());
		}
		catch (TemporalDataException e) {
			//expected
			assertTrue(e.getLocalizedMessage().contains(MISSING_REFERENCE_MESSAGE));
			return;
		}
		fail();
	}

	public void testMissingGranularity() {
		try {
			CalendarRegistry.getInstance().loadCalendar(new File("resources/calendars/test/missingGranularity.xml"));
			CalendarManager gregorianCalendarManager = CalendarRegistry.getInstance().getCalendarManager(
					IdentifierConverter.getInstance().buildCalendarManagerVersionIdentifier(
							MANAGER_IDENTIFIER, VERSION_IDENTIFIER),
					true);
			System.out.println(gregorianCalendarManager.getCalendar(CALENDAR_IDENTIFIER).getBottomGranularity());
		}
		catch (TemporalDataException e) {
			//expected
			assertTrue(e.getLocalizedMessage().contains(MISSING_GRANULARITY_MESSAGE));
			return;
		}
		fail();
	}
}
