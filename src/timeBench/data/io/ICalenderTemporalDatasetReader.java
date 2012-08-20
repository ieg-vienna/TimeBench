package timeBench.data.io;

import java.awt.List;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;

import javax.print.DocFlavor.STRING;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter.DEFAULT;

import org.quartz.utils.counter.Counter;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.component.VJournal;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.RDate;
import net.fortuna.ical4j.model.property.RRule;

import prefuse.data.io.DataIOException;
import prefuse.data.parser.DataParseException;
import prefuse.data.parser.DateParser;
import prefuse.data.tuple.TupleManager;
import timeBench.calendar.Granularity;
import timeBench.calendar.Granule;
import timeBench.calendar.JavaDateCalendarManager.Granularities;
import timeBench.data.Instant;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalElement;
import timeBench.data.TemporalObject;

public class ICalenderTemporalDatasetReader extends
		AbstractTemporalDatasetReader {

	public static final String EVENT = Component.VEVENT;
	// public static final String TODO = Component.VTODO;
	public static final String JOURNAL = Component.VJOURNAL;
	public static final String FREEBUSY = Component.VFREEBUSY;

	private static final String CREATED = "created";
	private static final String DESCRIPTION = "description";
	private static final String LOCATION = "location";
	private static final String SUMMARY = "summary";
	private static final String UID = "uid";

	private String m_componentType;

	private TemporalDataset dataset;
	private TemporalElement element;
	private TemporalObject object;

	// The (final) value of granularityContextId which is used
	// throughout the fill-methods for all temporalElements
	private final int granularityContextId = Granularities.Top.toInt();
	private int granularityId;

	public ICalenderTemporalDatasetReader() {
		this(ICalenderTemporalDatasetReader.EVENT);
	}

	public ICalenderTemporalDatasetReader(String componentType) {
		m_componentType = componentType;
		dataset = new TemporalDataset();
	}

	@Override
	public TemporalDataset readData(InputStream is) throws DataIOException,
			TemporalDataException {

		Calendar calender = null;

		// Building a calendar object from the given FileStream 
		try {
			calender = new CalendarBuilder().build(is);
		} catch (Exception e) {
			e.printStackTrace();
			throw new DataIOException("Calendar could not be instantiated!");
		}
		
		if (calender.getCalendarScale() != null && calender.getCalendarScale() != CalScale.GREGORIAN) {
			
			throw new TemporalDataException("Calendar is not gregorian!");
		}

		// Extracting only those components which match
		// the previously specified componentType
		ComponentList componentList = calender.getComponents(m_componentType);

		return readComponent(componentList);
	}

	/**
	 * @param componentList
	 *            - receives a list of components which contains either Events,
	 *            ToDos, Journal entries or Free/Busy times
	 * @return temporalDataSet - returns a TemporalDataSet containing all the
	 *         data (Temp.Elements & Temp.Objects) of each component contained
	 *         in the given componentList
	 * @throws ParseException 
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	private TemporalDataset readComponent(ComponentList componentList) {

		// Adding several new columns to the temporalDataSet to
		// present data occurring in different types of iCalendar entries
		dataset.addColumn(CREATED, Date.class, new Date(0L));
		dataset.addColumn(DESCRIPTION, String.class, "");
		dataset.addColumn(LOCATION, String.class, "");
		dataset.addColumn(SUMMARY, String.class, "");
		dataset.addColumn(UID, String.class, "");

		for (int i = 0; i < componentList.size(); i++) {

			if (m_componentType == Component.VEVENT) {

				VEvent event = (VEvent) componentList.get(i);
				fillEvent(event);

			} else if (m_componentType == Component.VJOURNAL) {

				VJournal journal = (VJournal) componentList.get(i);
				fillJournal(journal);

			} else if (m_componentType == Component.VFREEBUSY) {
				
				VFreeBusy freeBusy = (VFreeBusy) componentList.get(i);
				fillFreeBusy(freeBusy);
			}

		}

		return dataset;
	}

	/**
	 * @param event - A Event component of the ical4j library
	 * 
	 * Checks if the specified values exist in the component, 
	 * adds them to Temporal Elements/Objects and, 
	 * where required, uses default values for non-existent fields
	 * @throws ParseException 
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	private void fillEvent(VEvent event) {
		
	
		
		//getting start and end dates of the event
		//and calculating the appropriate granularityID
		Date dStart = (checkNull(event.getStartDate())) ? event.getStartDate()
				.getDate() : new Date(0L);
		Date dEnd = (checkNull(event.getEndDate())) ? event.getEndDate()
				.getDate() : new Date(Long.MAX_VALUE);
				
				
		if (checkNull(event.getProperty(Property.RRULE)) || checkNull(event.getProperty(Property.RDATE))){
			checkRecurrences(event, dStart);
		}
				
		granularityId = determineGranularity(dStart, dEnd);

		
		//add a new temporalElement to the temporalDataSet
		element = dataset.addTemporalElement(dStart.getTime(), dEnd.getTime(),
				granularityId, granularityContextId,
				TemporalDataset.PRIMITIVE_INTERVAL);

		// Creating the temporalObject and linking it to a
		// temporalElement
		object = dataset.addTemporalObject(element);
		
		//getting attributes from the event 
		//if an attribute is null a default value is used instead
		Date created = (checkNull(event.getCreated())) ? event.getCreated().getDate() : new Date(0L);
		String description = (checkNull(event.getDescription())) ? event.getDescription().getValue() : "";
		String location = (checkNull(event.getLocation())) ? event.getLocation().getValue() : "";
		String summary = (checkNull(event.getSummary())) ? event.getSummary().getValue() : "";
		String uid = (checkNull(event.getUid())) ? event.getUid().getValue() : "";

		//add the attributes to the temoparlObject
		object.set(CREATED, created);
		object.set(DESCRIPTION, description);
		object.set(LOCATION, location);
		object.set(SUMMARY, summary);
		object.set(UID, uid);
	}

	/**
	 * @param journal - A Journal component of the ical4j library
	 * 
	 * Checks if the specified values exist in the component, 
	 * adds them to Temporal Elements/Objects and, 
	 * where required, uses default values for non-existent fields
	 */
	private void fillJournal(VJournal journal) {
		
		Date dStamp = (checkNull(journal.getDateStamp())) ? journal
				.getDateStamp().getDate() : new Date(0L);

		granularityId = 2;

		element = dataset.addTemporalElement(dStamp.getTime(),
				dStamp.getTime(), granularityId, granularityContextId,
				TemporalDataset.PRIMITIVE_INSTANT);

		object = dataset.addTemporalObject(element);
		
		Date created = (checkNull(journal.getCreated())) ? journal.getCreated().getDate() : new Date(0L);
		String description = (checkNull(journal.getDescription())) ? journal.getDescription().getValue() : "";
		String summary = (checkNull(journal.getSummary())) ? journal.getSummary().getValue() : "";
		String uid = (checkNull(journal.getUid())) ? journal.getUid().getValue() : "";
		
		object.set(CREATED, created);
		object.set(DESCRIPTION, description);
		object.set(SUMMARY, summary);
		object.set(UID, uid);

	}

	/**
	 * @param freeBusy - A Free/Busy component of the ical4j library
	 * 
	 * Checks if the specified values exist in the component, 
	 * adds them to Temporal Elements/Objects and, 
	 * where required, uses default values for non-existent fields
	 */
	private void fillFreeBusy(VFreeBusy freeBusy) {
		
		Date dStart = (checkNull(freeBusy.getStartDate())) ? freeBusy.getStartDate().getDate() : new Date(0L);
		Date dEnd = (checkNull(freeBusy.getEndDate())) ? freeBusy.getEndDate().getDate() : new Date(Long.MAX_VALUE);

		granularityId = determineGranularity(dStart, dEnd);

		element = dataset.addTemporalElement(dStart.getTime(),
				dEnd.getTime(), granularityId, granularityContextId,
				TemporalDataset.PRIMITIVE_INTERVAL);

		object = dataset.addTemporalObject(element);
		
		String uid = (checkNull(freeBusy.getUid())) ? freeBusy.getUid().getValue() : "";
		
		object.set(UID, uid);
		
	}

	
	private void checkRecurrences (VEvent event, Date startDate) {
		
		int counter = 0;
		Recur recur = (Recur) ((RRule)event.getProperty(Property.RRULE)).getRecur();
		
		Date nextDate = recur.getNextDate(startDate, startDate);
		
		while (nextDate != null && counter < 50){
			
			try {
				VEvent textEvent =  event.getOccurrence(recur.getNextDate(startDate, startDate));
				fillEvent(textEvent);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			
			nextDate = recur.getNextDate(nextDate, nextDate);
			
			counter++;
		}
		
		
	}
	
	/**
	 * @param obj - The object to be checked
	 * @return returns a boolean which is FALSE if the given object IS NULL
	 */
	private boolean checkNull(Object obj) {

		if (obj == null)
			return false;

		return true;
	}

	/**
	 * @param dStart
	 *            - The start date of the Event or FreeBusy
	 * @param dEnd
	 *            - The end date of the Event or FreeBusy
	 * @return granularityID - returns an appropriate granularityID depending on
	 *         the granularity if the dates. Default is Minutes(2)
	 */
	private int determineGranularity(Date dStart, Date dEnd) {

		int granularityId = 2;

		// Determine to which extent the different attributes of
		// the dates are zero and set granularityId to a fitting value
		// (see JavaDateCalendarManager.Granularities for according
		// value-definitions)
		if (dStart.getSeconds() == 0 && dEnd.getSeconds() == 0) {
			granularityId = 2;

			if (dStart.getMinutes() == 0 && dEnd.getMinutes() == 0) {
				granularityId = 3;

				if (dStart.getHours() == 0 && dEnd.getHours() == 0) {
					granularityId = 4;

					if (dStart.getDay() == 0 && dEnd.getDay() == 0) {
						granularityId = 6;

						if (dStart.getMonth() == 0 && dEnd.getMonth() == 0) {
							granularityId = 8;
						}
					}
				}
			}
		} else {

			// If even the seconds of the given dates
			// do not match retun the value 1 (Seconds)
			granularityId = 1;
		}

		return granularityId;
	}

	// TODO PARSING (NOT USED)
	// else if (m_componentType == Component.VTODO) {
	//
	// granularityId = 2;
	//
	// element = dataset.addTemporalElement(
	// ((VToDo) componentList.get(i)).getDateStamp().getDate()
	// .getTime(), ((VToDo) componentList.get(i))
	// .getDateStamp().getDate().getTime(),
	// granularityId, granularityContextId,
	// TemporalDataset.PRIMITIVE_INSTANT);
	//
	// object = dataset.addTemporalObject(element);
	// object.set(ORGANIZER, String.valueOf(((VToDo) componentList
	// .get(i)).getOrganizer().getValue()));
	// object.set(SUMMARY, String.valueOf(((VToDo) componentList
	// .get(i)).getSummary().getValue()));
}
