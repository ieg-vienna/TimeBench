package timeBench.data.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.ParseException;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.component.VJournal;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStamp;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.ExRule;
import net.fortuna.ical4j.model.property.RDate;
import net.fortuna.ical4j.model.property.RRule;
import prefuse.data.io.DataIOException;
import timeBench.calendar.JavaDateCalendarManager.Granularities;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalElement;
import timeBench.data.TemporalElementStore;
import timeBench.data.TemporalObject;

public class ICalenderTemporalDatasetReader extends
		AbstractTemporalDatasetReader {

	public static final String EVENT = Component.VEVENT;
	// public static final String xTODO = Component.VTODO;
	public static final String JOURNAL = Component.VJOURNAL;
	public static final String FREEBUSY = Component.VFREEBUSY;

	private final String CREATED = "created";
	private final String DESCRIPTION = "description";
	private final String LOCATION = "location";
	private final String SUMMARY = "summary";
	private final String UID = "uid";

	private String m_componentType;

	private TemporalDataset dataset;
	private TemporalElement tempElement;
	private TemporalObject tempObject;

	private final int maxRecurrences = 50;

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

		if (calender.getCalendarScale() != null
				&& calender.getCalendarScale() != CalScale.GREGORIAN) {

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
	 * @throws TemporalDataException
	 * @throws ParseException
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	private TemporalDataset readComponent(ComponentList componentList)
			throws TemporalDataException {

		// Adding several new columns to the temporalDataSet to
		// present data occurring in different types of iCalendar entries
		dataset.addDataColumn(CREATED, Date.class, new Date(0L));
		dataset.addDataColumn(DESCRIPTION, String.class, "");
		dataset.addDataColumn(LOCATION, String.class, "");
		dataset.addDataColumn(SUMMARY, String.class, "");
		dataset.addDataColumn(UID, String.class, "");

		// iterate through the componentList and process every
		// component according to its type (event, journal, freebusy)
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
	 * 	Checks if the specified values exist in the component, adds
	 *  them to Temporal Elements/Objects and, where required, uses
	 *  default values for non-existent fields
	 * 
	 * @param event
	 *            - A Event component of the ical4j library
	 * @throws ParseException
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	private void fillEvent(VEvent event) {

		// getting start and end dates of the event
		Date dStart = (checkNull(event.getStartDate())) ? event.getStartDate()
				.getDate() : new Date(0L);

		Date dEnd = (checkNull(event.getEndDate())) ? event.getEndDate()
				.getDate() : new Date(Long.MAX_VALUE);

		// check if the event occurs more than once
		// in that case recursively add each occurrence
		if (checkNull(event.getProperty(Property.RRULE))) {
			checkRecurrences(event, dStart, dEnd);
		}
		if (checkNull(event.getProperty(Property.RDATE))) {
			checkRDates(event, dStart, dEnd);
		}

		// calculate the appropriate granularityId
		granularityId = determineGranularity(dStart, dEnd);

		// add a new temporalElement to the temporalDataSet
		tempElement = dataset.addTemporalElement(dStart.getTime(),
				dEnd.getTime(), granularityId, granularityContextId,
				TemporalElementStore.PRIMITIVE_INTERVAL);

		// Creating the temporalObject and linking it to a
		// temporalElement
		tempObject = dataset.addTemporalObject(tempElement);

		// getting attributes from the event
		// if an attribute is null a default value is used instead
		Date created = (checkNull(event.getCreated())) ? event.getCreated()
				.getDate() : new Date(0L);
		String description = (checkNull(event.getDescription())) ? event
				.getDescription().getValue() : null;
		String location = (checkNull(event.getLocation())) ? event
				.getLocation().getValue() : null;
		String summary = (checkNull(event.getSummary())) ? event.getSummary()
				.getValue() : null;
		String uid = (checkNull(event.getUid())) ? event.getUid().getValue()
				: null;

		// add the attributes to the temoparlObject
		tempObject.set(CREATED, created);
		tempObject.set(DESCRIPTION, description);
		tempObject.set(LOCATION, location);
		tempObject.set(SUMMARY, summary);
		tempObject.set(UID, uid);
	}

	/**
	 * 	Checks if the specified values exist in the component, adds
	 *  them to Temporal Elements/Objects and, where required, uses
	 *  default values for non-existent fields
	 * 
	 * @param journal
	 *            - A Journal component of the ical4j library
	 */
	private void fillJournal(VJournal journal) {

		// see fillEvent for detailed description

		Date dStamp = (checkNull(journal.getDateStamp())) ? journal
				.getDateStamp().getDate() : new Date(0L);

		if (checkNull(journal.getProperty(Property.RRULE))) {
			checkRecurrences(journal, dStamp, dStamp);
		}
		if (checkNull(journal.getProperty(Property.RDATE))) {
			checkRDates(journal, dStamp, dStamp);
		}

		granularityId = Granularities.Minute.toInt();

		tempElement = dataset.addTemporalElement(dStamp.getTime(),
				dStamp.getTime(), granularityId, granularityContextId,
				TemporalElementStore.PRIMITIVE_INSTANT);

		tempObject = dataset.addTemporalObject(tempElement);

		Date created = (checkNull(journal.getCreated())) ? journal.getCreated()
				.getDate() : new Date(0L);
		String description = (checkNull(journal.getDescription())) ? journal
				.getDescription().getValue() : null;
		String summary = (checkNull(journal.getSummary())) ? journal
				.getSummary().getValue() : null;
		String uid = (checkNull(journal.getUid())) ? journal.getUid()
				.getValue() : null;

		tempObject.set(CREATED, created);
		tempObject.set(DESCRIPTION, description);
		tempObject.set(SUMMARY, summary);
		tempObject.set(UID, uid);
	}

	/**
	 *  Checks if the specified values exist in the component, adds
	 *  them to Temporal Elements/Objects and, where required, uses
	 *  default values for non-existent fields
	 * @param freeBusy
	 *            - A Free/Busy component of the ical4j library
	 */
	private void fillFreeBusy(VFreeBusy freeBusy) {

		// see fillEvent for detailed description

		Date dStart = (checkNull(freeBusy.getStartDate())) ? freeBusy
				.getStartDate().getDate() : new Date(0L);
		Date dEnd = (checkNull(freeBusy.getEndDate())) ? freeBusy.getEndDate()
				.getDate() : new Date(Long.MAX_VALUE);

		granularityId = determineGranularity(dStart, dEnd);

		tempElement = dataset.addTemporalElement(dStart.getTime(),
				dEnd.getTime(), granularityId, granularityContextId,
				TemporalElementStore.PRIMITIVE_INTERVAL);

		tempObject = dataset.addTemporalObject(tempElement);

		String uid = (checkNull(freeBusy.getUid())) ? freeBusy.getUid()
				.getValue() : null;

		tempObject.set(UID, uid);

	}

	/**
	 * Creates a PorpertyList containing the given component's
	 * properties excluding recurrence information.
	 * 
	 * @param component
	 *            - takes the original event from which the needed properties
	 *            are extracted
	 * @return PropertyList - a list containing all the properties from the
	 *         original event
	 */
	private PropertyList getPropertyList(Component component) {
		PropertyList list = new PropertyList();

		
		// check the components type
		// and copy its properties to a separate property list
		if (component instanceof VEvent) {

			VEvent tempEvent = (VEvent) component;

			if (checkNull(tempEvent.getCreated()))
				list.add(tempEvent.getCreated());

			if (checkNull(tempEvent.getDescription()))
				list.add(tempEvent.getDescription());

			if (checkNull(tempEvent.getLocation()))
				list.add(tempEvent.getLocation());

			if (checkNull(tempEvent.getSummary()))
				list.add(tempEvent.getSummary());

			if (checkNull(tempEvent.getUid()))
				list.add(tempEvent.getUid());

		} else if (component instanceof VJournal) {

			VJournal tempJournal = (VJournal) component;

			if (checkNull(tempJournal.getCreated()))
				list.add(tempJournal.getCreated());

			if (checkNull(tempJournal.getDescription()))
				list.add(tempJournal.getDescription());

			if (checkNull(tempJournal.getSummary()))
				list.add(tempJournal.getSummary());

			if (checkNull(tempJournal.getUid()))
				list.add(tempJournal.getUid());

		}

		return list;
	}

	/**
	 * Checks the given component for recurrences and 
	 * if needed processes the various occurrences.
	 * (Adds them to the TemporalDataset)
	 * 
	 * @param component
	 *            - the component which has multiple occurrences
	 * @param startDate
	 *            - the startDate of the given event
	 * @param endDate
	 *            - the endDate of the given event
	 */
	private void checkRDates(Component component, Date startDate, Date endDate) {

		// get all the recurrence dates of the component
		DateList dateList = (DateList) ((RDate) component
				.getProperty(Property.RDATE)).getDates();
		dateList.remove(startDate);

		Recur exRecur = null;
		DateList exDateList = null;

		
		// check if the component contains exceptions
		// if that is the case - get them
		if (checkNull(component.getProperty(Property.EXRULE))) {
			exRecur = (Recur) ((ExRule) component.getProperty(Property.EXRULE))
					.getRecur();
		}

		if (checkNull(component.getProperty(Property.EXDATE))) {
			exDateList = (DateList) ((ExDate) component
					.getProperty(Property.EXDATE)).getDates();
		}

		Date nextException = null;

		// if there are exceptions determine the very next one
		if (checkNull(exRecur))
			nextException = exRecur.getNextDate(startDate, startDate);

		// calculate the duration of the component
		long duration = endDate.getTime() - startDate.getTime();

		
		// loop through all the possible dates
		Outer: for (int i = 0; i < dateList.size(); i++) {

			Date date = (Date) dateList.get(i);

			
			// check if the current date matches the one of the
			// next exception - if so skip the current date and
			// continue with the next one
			if (checkNull(exRecur) && nextException.getTime() == date.getTime()) {
				nextException = exRecur.getNextDate(date, date);
				continue;
			}

			if (checkNull(exDateList)) {
				for (int j = 0; j < exDateList.size(); j++) {
					if (((Date) exDateList.get(j)).getTime() == date.getTime()) {
						continue Outer;
					}
				}
			}

			// if the current date is valid fill create a new component
			fillSingleComponent(component, date, duration);
		}
	}

	/**
	 * Checks the given component for recurrences and 
	 * if needed processes the various occurrences.
	 * (Adds them to the TemporalDataset)
	 * 
	 * @param component
	 *            - the component which has multiple occurrences
	 * @param startDate
	 *            - the startDate of the given event
	 * @param endDate
	 *            - the endDate of the given event
	 */
	private void checkRecurrences(Component component, Date startDate,
			Date endDate) {

		int counter = 0;
		int recurrences_count;

		// get the recurrence of the given component
		Recur recur = (Recur) ((RRule) component.getProperty(Property.RRULE))
				.getRecur();
		Recur exRecur = null;
		DateList exDateList = null;

		
		// check if the component contains exceptions
		// if that is the case - get them
		if (checkNull(component.getProperty(Property.EXRULE))) {
			exRecur = (Recur) ((ExRule) component.getProperty(Property.EXRULE))
					.getRecur();
		}

		if (checkNull(component.getProperty(Property.EXDATE))) {
			exDateList = (DateList) ((ExDate) component
					.getProperty(Property.EXDATE)).getDates();
		}

		// if the recurrence has the COUNT property set it will be added X times
		// to the temporalDataSet - otherwise it will be added 50 times (default)
		recurrences_count = (recur.getCount() > -1) ? recur.getCount() - 1
				: maxRecurrences;

		// get the next occurrence
		Date nextDate = recur.getNextDate(startDate, startDate);
		Date nextException = null;

		if (checkNull(exRecur))
			nextException = exRecur.getNextDate(startDate, startDate);

		// find out the original duration of the event/journal
		long duration = endDate.getTime() - startDate.getTime();

		
		// loop through all recurrences (max. 50)
		Outer: while (counter < recurrences_count) {

			
			// check if the current date matches an exception
			// in that case skip the current date and continue
			if (checkNull(exRecur)
					&& nextException.getTime() == nextDate.getTime()) {
				nextException = exRecur.getNextDate(nextDate, nextDate);
				nextDate = recur.getNextDate(nextDate, nextDate);
				counter++;
				continue;
			}

			if (checkNull(exDateList)) {
				for (int i = 0; i < exDateList.size(); i++) {
					if (((Date) exDateList.get(i)).getTime() == nextDate
							.getTime()) {
						nextDate = recur.getNextDate(nextDate, nextDate);
						counter++;
						continue Outer;
					}
				}
			}

			
			// if the current date is valid create a new component with it
			fillSingleComponent(component, nextDate, duration);

			// get the next occurrence
			nextDate = recur.getNextDate(nextDate, nextDate);

			counter++;
		}

	}

	/**
	 * Creates a copy of a component which doesn't contain the original component's
	 * recurrence information.
	 * 
	 * @param component
	 *            - the component to be filled
	 * @param date
	 *            - the startDate that will be assigned to the component
	 * @param duration
	 *            - the duration (only relevant for events)
	 */
	private void fillSingleComponent(Component component, Date date,
			long duration) {

		// copy all the properties from the original component
		PropertyList list = getPropertyList(component);

		// if the component is an event perform event-specific actions
		if (component instanceof VEvent) {

			// the new endDate of this single event will
			// be the previously calculated duration in combination
			// with the current startDate
			Date newEndDate = new DateTime(date.getTime() + duration);

			// add new dates to the property list
			list.add(new DtStart(date));
			list.add(new DtEnd(newEndDate));

			// create a new event with the properties of the property list
			// and add it to the temporalDataSet
			VEvent singleEvent = new VEvent(list);
			fillEvent(singleEvent);

		} else if (component instanceof VJournal) { // otherwise perfom
													// journal-specific actions

			// set the time stamp of the new journal
			// to the current nextDate (occurrence)
			list.add(new DtStamp((DateTime) date));

			// create a new journal with the properties
			// and add it to the temporalDataSet
			VJournal singleJournal = new VJournal(list);
			fillJournal(singleJournal);
		}
	}

	/**
	 * Checks if the given object is NULL
	 * 
	 * @param obj
	 *            - The object to be checked
	 * @return returns a boolean which is FALSE if the given object IS NULL
	 */
	private boolean checkNull(Object obj) {

		if (obj == null)
			return false;

		return true;
	}

	/**
	 * Determines the best fitting granularity for the given dates.
	 * 
	 * @param dStart
	 *            - The start date of the Event or FreeBusy
	 * @param dEnd
	 *            - The end date of the Event or FreeBusy
	 * @return granularityID - returns an appropriate granularityID depending on
	 *         the granularity if the dates. Default is Minutes(2)
	 */
	private int determineGranularity(Date dStart, Date dEnd) {

		int granularityId = Granularities.Minute.toInt();

		java.util.Calendar cStart = java.util.Calendar.getInstance();
		cStart.setTime(dStart);

		java.util.Calendar cEnd = java.util.Calendar.getInstance();
		cEnd.setTime(dEnd);

		// Determine if the 'second' value of the dates
		// are not zero and set granularityId to a fitting value
		// (see JavaDateCalendarManager.Granularities for according
		// value-definitions)
		if (cStart.get(java.util.Calendar.SECOND) != 0
				|| cEnd.get(java.util.Calendar.SECOND) != 0) {
			granularityId = Granularities.Second.toInt();

		}

		return granularityId;
	}

}
