package timeBench.calendar;

import timeBench.data.TemporalDataException;

import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class GregorianCalendarManager implements CalendarManager {
	protected int identifier = 0x01;

	@Override
	public Calendar getDefaultCalendar() {
		return null;  //TODO: auto-generated body, implement me!
	}

	@Override
	public Calendar getCalendar(int identifier) {
		return null;  //TODO: auto-generated body, implement me!
	}

	@Override
	public int[] getGranularityIdentifiers() {
		return new int[0];  //TODO: auto-generated body, implement me!
	}

	@Override
	public Granule createGranule(Date input, Granularity granularity) throws TemporalDataException {
		return null;  //TODO: auto-generated body, implement me!
	}

	@Override
	public Granule createGranule(long inf, long sup, int mode, Granularity granularity) throws TemporalDataException {
		return null;  //TODO: auto-generated body, implement me!
	}

	@Override
	public Granule[] createGranules(long inf, long sup, double cover, Granularity granularity) throws TemporalDataException {
		return new Granule[0];  //TODO: auto-generated body, implement me!
	}

	@Override
	public Granule[] createGranules(Granule[] granules, double cover, Granularity granularity) throws TemporalDataException {
		return new Granule[0];  //TODO: auto-generated body, implement me!
	}

	@Override
	public long createGranuleIdentifier(Granule granule) throws TemporalDataException {
		return 0;  //TODO: auto-generated body, implement me!
	}

	@Override
	public String createGranuleLabel(Granule granule) throws TemporalDataException {
		return null;  //TODO: auto-generated body, implement me!
	}

	@Override
	public long createInf(Granule granule) throws TemporalDataException {
		return 0;  //TODO: auto-generated body, implement me!
	}

	@Override
	public long createSup(Granule granule) throws TemporalDataException {
		return 0;  //TODO: auto-generated body, implement me!
	}

	@Override
	public long getMinGranuleIdentifier(Granularity granularity) throws TemporalDataException {
		return 0;  //TODO: auto-generated body, implement me!
	}

	@Override
	public long getMaxGranuleIdentifier(Granularity granularity) throws TemporalDataException {
		return 0;  //TODO: auto-generated body, implement me!
	}

	@Override
	public long getMaxLengthInIdentifiers(Granularity granularity) throws TemporalDataException {
		return 0;  //TODO: auto-generated body, implement me!
	}

	@Override
	public boolean contains(Granule granule, long chronon) throws TemporalDataException {
		return false;  //TODO: auto-generated body, implement me!
	}

	@Override
	public long getStartOfTime() {
		return 0;  //TODO: auto-generated body, implement me!
	}

	@Override
	public long getEndOfTime() {
		return 0;  //TODO: auto-generated body, implement me!
	}

	@Override
	public Granularity getBottomGranularity(Calendar calendar) {
		return null;  //TODO: auto-generated body, implement me!
	}

	@Override
	public Granularity getTopGranularity(Calendar calendar) {
		return null;  //TODO: auto-generated body, implement me!
	}

	@Override
	public Granularity getGranularity(Calendar calendar, String granularityName, String contextGranularityName) {
		return null;  //TODO: auto-generated body, implement me!
	}
}
