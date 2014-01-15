package timeBench.calendar;

import junit.framework.TestCase;
import timeBench.calendar.util.GranularityAssociation;
import timeBench.calendar.util.IdentifierConverter;
import timeBench.data.TemporalDataException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class GregorianCalendarManagerTest extends TestCase {
	private static final int CALENDAR_LOCAL_IDENTIFIER = 1;
	private static final int CALENDAR_LOCAL_IDENTIFIER_DOES_NOT_EXIST = 2;

	private static final String GRANULARITY_SECOND_LABEL = "Quarter";
	private static final String GRANULARITY_MINUTE_LABEL = "Decade";

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

	public void testGetGlobalGranularityIdentifiers() throws TemporalDataException {
		initializeClasses();

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
		initializeClasses();

		Calendar calendar = gregorianCalendarManager.getCalendar(CALENDAR_LOCAL_IDENTIFIER);
		assertNotNull(calendar);

		Granularity granularity = gregorianCalendarManager.getGranularity(calendar, GRANULARITY_SECOND_LABEL, GRANULARITY_MINUTE_LABEL);
		assertNotNull(granularity);

//		Granule granule = gregorianCalendarManager.createGranule(new Date(1389800502180l), granularity);
		Granule granule = gregorianCalendarManager.createGranule(new Date(System.currentTimeMillis()), granularity);
		System.out.println(granule.getIdentifier());
		System.out.println(granule.getInf());
		System.out.println(granule.getSup());
		System.out.println(gregorianCalendarManager.createGranuleLabel(granule));
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
