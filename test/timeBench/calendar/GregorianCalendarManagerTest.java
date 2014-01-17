package timeBench.calendar;

import junit.framework.TestCase;
import timeBench.calendar.util.IdentifierConverter;
import timeBench.data.TemporalDataException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.*;

public class GregorianCalendarManagerTest extends TestCase {
	private static final int CALENDAR_LOCAL_IDENTIFIER = 1;
	private static final int CALENDAR_LOCAL_IDENTIFIER_DOES_NOT_EXIST = 2;

	private static final String GRANULARITY_MILLISECOND_LABEL = "Millisecond";
	private static final String GRANULARITY_SECOND_LABEL = "Second";
	private static final String GRANULARITY_MINUTE_LABEL = "Minute";
	private static final String GRANULARITY_HOUR_LABEL = "Hour";
	private static final String GRANULARITY_DAY_LABEL = "Day";
	private static final String GRANULARITY_WEEK_LABEL = "Week";
	private static final String GRANULARITY_MONTH_LABEL = "Month";
	private static final String GRANULARITY_QUARTER_LABEL = "Quarter";
	private static final String GRANULARITY_YEAR_LABEL = "Year";
	private static final String GRANULARITY_DECADE_LABEL = "Decade";
	private static final String GRANULARITY_TOP_LABEL = "Top";

	private static final long TEST_DATE = 1389800502180l; //2014-01-15T16:41:42.180 + 1
	private static final String MILLISECOND_OF_HOUR = "2502181";
	private static final String SECOND_OF_MINUTE = "43";
	private static final String MINUTE_OF_DAY = "942";
	private static final String WEEK_OF_YEAR = "3";
	private static final String MONTH_OF_TOP = "M529";
	private static final String QUARTER_OF_DECADE = "Q13";
	private static final String YEAR_OF_TOP = "2014";

	private static final File CALENDAR_XML_FILE = new File("resources/calendars/GregorianCalendar.xml");

	private static CalendarManager gregorianCalendarManager;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (gregorianCalendarManager == null){
			CalendarFactory.getInstance().loadCalendar(CALENDAR_XML_FILE);
			gregorianCalendarManager = CalendarFactory.getInstance().getCalendarManager(
												IdentifierConverter.getInstance().buildCalendarManagerVersionIdentifier(2, 1), true);
		}
	}

	public void testGetDefaultCalendar() throws TemporalDataException {
		Calendar calendar = gregorianCalendarManager.getDefaultCalendar();
		assertNotNull(calendar);
		assertEquals(CALENDAR_LOCAL_IDENTIFIER, calendar.getLocalCalendarIdentifier());
	}

	public void testGetCalendar() throws TemporalDataException {
		Calendar calendar = gregorianCalendarManager.getCalendar(CALENDAR_LOCAL_IDENTIFIER);
		assertNotNull(calendar);
		assertEquals(CALENDAR_LOCAL_IDENTIFIER, calendar.getLocalCalendarIdentifier());
	}

	public void testGetCalendarFails() throws TemporalDataException {
		Calendar calendar = gregorianCalendarManager.getCalendar(CALENDAR_LOCAL_IDENTIFIER_DOES_NOT_EXIST);
		assertNull(calendar);
	}

	public void testGetGlobalGranularityIdentifiers() throws TemporalDataException {
		Calendar calendar = gregorianCalendarManager.getCalendar(CALENDAR_LOCAL_IDENTIFIER);
		assertNotNull(calendar);

	    ArrayList<Integer> granularityIdentifiers = new ArrayList<>();
		for (int currentIdentifier : gregorianCalendarManager.getGlobalGranularityIdentifiers()){
			granularityIdentifiers.add(currentIdentifier);
		}

		int count = 0;
		for(Granularity currentGranularity : calendar.getGranularities()){
			int currentIdentifier = IdentifierConverter.getInstance().buildGlobalIdentifier(
									gregorianCalendarManager.getLocalCalendarManagerIdentifier(),
									gregorianCalendarManager.getLocalCalendarManagerVersionIdentifier(),
									calendar.getLocalCalendarIdentifier(),
									currentGranularity.getIdentifier().getTypeIdentifier(),
									currentGranularity.getIdentifier().getIdentifier());

			assertTrue(granularityIdentifiers.contains(currentIdentifier));
			count++;
		}
		assertEquals(count, gregorianCalendarManager.getGlobalGranularityIdentifiers().length);
	}

	public void testCreateDateGranule() throws TemporalDataException {
		Calendar calendar = gregorianCalendarManager.getCalendar(CALENDAR_LOCAL_IDENTIFIER);
		assertNotNull(calendar);

		Granularity millisecondHourGranularity = gregorianCalendarManager.getGranularity(calendar, GRANULARITY_MILLISECOND_LABEL, GRANULARITY_HOUR_LABEL);
		Granularity secondMinuteGranularity = gregorianCalendarManager.getGranularity(calendar, GRANULARITY_SECOND_LABEL, GRANULARITY_MINUTE_LABEL);
		Granularity minuteDayGranularity = gregorianCalendarManager.getGranularity(calendar, GRANULARITY_MINUTE_LABEL, GRANULARITY_DAY_LABEL);
		Granularity weekYearGranularity = gregorianCalendarManager.getGranularity(calendar, GRANULARITY_WEEK_LABEL, GRANULARITY_YEAR_LABEL);
		Granularity monthTopGranularity = gregorianCalendarManager.getGranularity(calendar, GRANULARITY_MONTH_LABEL, GRANULARITY_TOP_LABEL);
		Granularity quarterDecadeGranularity = gregorianCalendarManager.getGranularity(calendar, GRANULARITY_QUARTER_LABEL, GRANULARITY_DECADE_LABEL);
		Granularity yearTopGranularity = gregorianCalendarManager.getGranularity(calendar, GRANULARITY_YEAR_LABEL, GRANULARITY_TOP_LABEL);
		assertNotNull(millisecondHourGranularity);
		assertNotNull(secondMinuteGranularity);
		assertNotNull(minuteDayGranularity);
		assertNotNull(weekYearGranularity);
		assertNotNull(monthTopGranularity);
		assertNotNull(quarterDecadeGranularity);
		assertNotNull(yearTopGranularity);

		GregorianCalendar testDate = new GregorianCalendar();
		testDate.setTime(new Date(1389800502180l));
		testDate.setTimeZone(TimeZone.getTimeZone("UTC"));

		Granule millisecondHourGranule = gregorianCalendarManager.createGranule(testDate.getTime(), millisecondHourGranularity);
		Granule secondMinuteGranule = gregorianCalendarManager.createGranule(testDate.getTime(), secondMinuteGranularity);
		Granule minuteDayGranule = gregorianCalendarManager.createGranule(testDate.getTime(), minuteDayGranularity);
		Granule weekYearGranule = gregorianCalendarManager.createGranule(testDate.getTime(), weekYearGranularity);
		Granule monthTopGranule = gregorianCalendarManager.createGranule(testDate.getTime(), monthTopGranularity);
		Granule quarterDecadeGranule = gregorianCalendarManager.createGranule(testDate.getTime(), quarterDecadeGranularity);
		Granule yearTopGranule = gregorianCalendarManager.createGranule(testDate.getTime(), yearTopGranularity);

		assertEquals(gregorianCalendarManager.createGranuleLabel(millisecondHourGranule), MILLISECOND_OF_HOUR);
		assertEquals(gregorianCalendarManager.createGranuleLabel(secondMinuteGranule), SECOND_OF_MINUTE);
		assertEquals(gregorianCalendarManager.createGranuleLabel(minuteDayGranule), MINUTE_OF_DAY);
		assertEquals(gregorianCalendarManager.createGranuleLabel(weekYearGranule), WEEK_OF_YEAR);
		assertEquals(gregorianCalendarManager.createGranuleLabel(monthTopGranule), MONTH_OF_TOP);
		assertEquals(gregorianCalendarManager.createGranuleLabel(quarterDecadeGranule), QUARTER_OF_DECADE);
		assertEquals(gregorianCalendarManager.createGranuleLabel(yearTopGranule), YEAR_OF_TOP);
	}

//	public void testRofl() throws TemporalDataException{
//		System.out.println(IdentifierConverter.getInstance().buildCalendarManagerVersionIdentifier(0,0));
//		System.out.println(IdentifierConverter.getInstance().buildCalendarManagerVersionIdentifier(31, 127));
//		System.out.println(IdentifierConverter.getInstance().buildGlobalIdentifier(
//				IdentifierConverter.MANAGER_MAX,
//				IdentifierConverter.VERSION_MAX,
//				IdentifierConverter.CALENDAR_MAX,
//				IdentifierConverter.TYPE_GRANULARITY_MAX,
//				IdentifierConverter.GRANULARITY_MAX));
//		System.out.println(0b1111111111110000000000000000000);
//	}
}
