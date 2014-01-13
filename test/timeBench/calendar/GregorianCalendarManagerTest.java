package timeBench.calendar;

import junit.framework.TestCase;
import timeBench.calendar.util.IdentifierConverter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.Arrays;

public class GregorianCalendarManagerTest extends TestCase {
	private static final int CALENDAR_LOCAL_IDENTIFIER = 1;
	private static final int CALENDAR_LOCAL_IDENTIFIER_DOES_NOT_EXIST = 2;
	private CalendarManager gregorianCalendarManager;

	public void testGetDefaultCalendar(){
		initializeClasses();

		Calendar calendar = gregorianCalendarManager.getDefaultCalendar();
		assertNotNull(calendar);
		assertEquals(CALENDAR_LOCAL_IDENTIFIER, calendar.getLocalCalendarIdentifier());
	}

	public void testGetCalendar(){
		initializeClasses();

		Calendar calendar = gregorianCalendarManager.getCalendar(CALENDAR_LOCAL_IDENTIFIER);
		assertNotNull(calendar);
		assertEquals(CALENDAR_LOCAL_IDENTIFIER, calendar.getLocalCalendarIdentifier());
	}

	public void testGetCalendarFails(){
		initializeClasses();

		Calendar calendar = gregorianCalendarManager.getCalendar(CALENDAR_LOCAL_IDENTIFIER_DOES_NOT_EXIST);
		assertNull(calendar);
	}

	public void testGetGlobalGranularityIdentifiers(){
		initializeClasses();

		Calendar calendar = gregorianCalendarManager.getCalendar(CALENDAR_LOCAL_IDENTIFIER);
		assertNotNull(calendar);

		System.out.println(Arrays.toString(gregorianCalendarManager.getGlobalGranularityIdentifiers()));
	}

	private void initializeClasses(){
		try {
			Class.forName("timeBench.calendar.GregorianCalendarManager");

			JAXBContext jaxbContext = JAXBContext.newInstance(Calendar.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

			unmarshaller.unmarshal(new File("resources/calendars/GregorianCalendar.xml"));

			gregorianCalendarManager = CalendarFactory.getInstance().getCalendarManager(
							IdentifierConverter.getInstance().buildCalendarManagerVersionIdentifier(2, 1), true);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
