package timeBench.calendar;

import junit.framework.TestCase;
import timeBench.calendar.util.GranularityIdentifier;
import timeBench.calendar.util.IdentifierConverter;
import timeBench.data.TemporalDataException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;

public class MarshallTester extends TestCase {
	private static final Granularity DAY = new Granularity();
	private static final Granularity MONTH = new Granularity();
	private static final Granularity YEAR = new Granularity();
	private static final Calendar CALENDAR = new Calendar();

	private static Marshaller marshaller;
	private static Unmarshaller unmarshaller;


	static {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Calendar.class);
			marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			unmarshaller = jaxbContext.createUnmarshaller();
		}
		catch (JAXBException e) {
			e.printStackTrace();
			fail();
		}

		try {
			CalendarFactory.getInstance().registerCalendarManager(
					IdentifierConverter.getInstance().buildCalendarManagerVersionIdentifier(1, 1),
					new GregorianCalendarManager());
		}
		catch (TemporalDataException e) {
			e.printStackTrace();
			fail();
		}

		DAY.setCalendar(CALENDAR);
		DAY.setGranularityLabel("Day");
		DAY.setIdentifier(new GranularityIdentifier(1, 1));
		DAY.setBottomGranularity(true);
		ArrayList<GranularityIdentifier> dayAllowedContextGranularities = new ArrayList<>();
		dayAllowedContextGranularities.add(new GranularityIdentifier(2, 1));
		dayAllowedContextGranularities.add(new GranularityIdentifier(3, 1));
		DAY.setPermittedContextIdentifiers(dayAllowedContextGranularities);


		MONTH.setCalendar(CALENDAR);
		MONTH.setGranularityLabel("Month");
		MONTH.setIdentifier(new GranularityIdentifier(2, 1));
		ArrayList<GranularityIdentifier> monthAllowedContextGranularities = new ArrayList<>();
		monthAllowedContextGranularities.add(new GranularityIdentifier(3, 1));
		MONTH.setPermittedContextIdentifiers(monthAllowedContextGranularities);

		YEAR.setCalendar(CALENDAR);
		YEAR.setGranularityLabel("Year");
		YEAR.setIdentifier(new GranularityIdentifier(3, 1));
		YEAR.setTopGranularity(true);

		ArrayList<Granularity> granularities = new ArrayList<>();
		granularities.add(DAY);
		granularities.add(MONTH);
		granularities.add(YEAR);

		CALENDAR.setGranularities(granularities);
		CALENDAR.setLocalCalendarIdentifier(1);
		CALENDAR.setLocalCalendarManagerIdentifier(1);
		CALENDAR.setLocalCalendarManagerVersionIdentifier(1);
	}


	public void testMarshall() throws JAXBException {
		marshaller.marshal(CALENDAR, new File("out.xml"));
	}

	public void testUnmarshall() throws JAXBException {
		Object unmarshalledObject = unmarshaller.unmarshal(new File("resources/calendars/GregorianCalendar.xml"));
		Calendar calendar = null;

		if (unmarshalledObject != null && unmarshalledObject instanceof Calendar) {
			calendar = (Calendar) unmarshalledObject;
		}
		else{
			fail();
		}

		for (Granularity currentGranularity : calendar.getGranularities()) {
			System.out.println(currentGranularity.getGranularityLabel());
			System.out.println(currentGranularity.getCalendar().getLocalCalendarIdentifier());
			System.out.println(currentGranularity.getGlobalGranularityIdentifier());
		}
	}
}
