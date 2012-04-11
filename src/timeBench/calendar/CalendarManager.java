package timeBench.calendar;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import timeBench.data.TemporalDataException;

/**
 * The interface used to access any CalendarManager that may exist.
 * 
 * <p>
 * Added:         2011-08-19 / TL<br>
 * Modifications: 
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public interface CalendarManager {

	public Calendar calendar();
	public Calendar getDefaultCalendar();    
	public int[] getGranularityIdentifiers();
	public int getBottomGranularityIdentifier();
	public int getTopGranularityIdentifier();
	public Granule createGranule(Date input, Granularity granularity) throws TemporalDataException;
	public Granule createGranule(long inf, long sup, int mode,
			Granularity granularity) throws TemporalDataException;
	public Granule[] createGranules(long inf, long sup, double cover,
			Granularity granularity) throws TemporalDataException;
	public Granule[] createGranules(Granule[] granules, double cover,
			Granularity granularity) throws TemporalDataException;
	public long createGranuleIdentifier(Granule granule) throws TemporalDataException;
	public String createGranuleLabel(Granule granule) throws TemporalDataException;
	public long createInf(Granule granule) throws TemporalDataException;
	public long createSup(Granule granule) throws TemporalDataException;
}