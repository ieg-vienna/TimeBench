package timeBench.calendar.util;

import junit.framework.TestCase;
import timeBench.data.TemporalDataException;

public class IdentifierConverterTest extends TestCase{
	private static final int TEST_COMPOSITE = 134746241; //represents 1 for every sub-identifier
	private static final int TEST_MANAGER_IDENTIFIER = 1;
	private static final int TEST_VERSION_IDENTIFIER = 1;
	private static final int TEST_CALENDAR_IDENTIFIER = 1;
	private static final int TEST_TYPE_GRANULARITY_IDENTIFIER = 1;
	private static final int TEST_GRANULARITY_IDENTIFIER = 1;

	public void testGetManagerIdentifier() {
		int managerIdentifier = IdentifierConverter.getInstance().getManagerIdentifier(TEST_COMPOSITE);
		assertEquals(TEST_MANAGER_IDENTIFIER, managerIdentifier);
	}

	public void testGetVersionIdentifier() {
		int versionIdentifier = IdentifierConverter.getInstance().getVersionIdentifier(TEST_COMPOSITE);
		assertEquals(TEST_VERSION_IDENTIFIER, versionIdentifier);
	}

	public void testGetCalendarIdentifier() {
			int calendarIdentifier = IdentifierConverter.getInstance().getCalendarIdentifier(TEST_COMPOSITE);
			assertEquals(TEST_CALENDAR_IDENTIFIER, calendarIdentifier);
	}

	public void testGetGranularityTypeIdentifier() {
		int granularityTypeIdentifier = IdentifierConverter.getInstance().getTypeIdentifier(TEST_COMPOSITE);
		assertEquals(TEST_TYPE_GRANULARITY_IDENTIFIER, granularityTypeIdentifier);
	}

	public void testGetGranularityIdentifier() {
		int granularityIdentifier = IdentifierConverter.getInstance().getGranularityIdentifier(TEST_COMPOSITE);
		assertEquals(TEST_GRANULARITY_IDENTIFIER, granularityIdentifier);
	}
	public void testBuildCompositeIdentifier() throws TemporalDataException {
//		int compositeIdentifier = IdentifierFactory.getInstance().buildGlobalIdentifier(0b10001, 0b10000000, 0b1000000, 0b10000, 0b1000000);
		int compositeIdentifier = IdentifierConverter.getInstance().buildGlobalIdentifier(
				TEST_MANAGER_IDENTIFIER,
				TEST_VERSION_IDENTIFIER,
				TEST_CALENDAR_IDENTIFIER,
				TEST_TYPE_GRANULARITY_IDENTIFIER,
				TEST_GRANULARITY_IDENTIFIER);
//		System.out.println(0b1000000000000000000000000000 + 0b10000000000000000000 + 0b1000000000000 + 0b10000000 + 0b1);
//		System.out.println("composite: " + compositeIdentifier);
//		System.out.println("manager: " + IdentifierFactory.getInstance().getManagerIdentifier(compositeIdentifier));
//		System.out.println("version: " + IdentifierFactory.getInstance().getVersionIdentifier(compositeIdentifier));
//		System.out.println("calender: " + IdentifierFactory.getInstance().getCalendarIdentifier(compositeIdentifier));
//		System.out.println("typeGranularity: " + IdentifierFactory.getInstance().getTypeIdentifier(compositeIdentifier));
//		System.out.println("granularity: " + IdentifierFactory.getInstance().getGranularityIdentifier(compositeIdentifier));
		assertEquals(TEST_COMPOSITE, compositeIdentifier);
	}
}