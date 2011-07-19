package timeBench.calendar;

import timeBench.data.TemporalDataException;

/**
 * 
 * 
 * <p>
 * Added:          / TL<br>
 * Modifications: 
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public class Granularity {
	private Calendar calendar = null;
	int identifier;	
	
	Granularity(Calendar calendar, int identifier){
		this.calendar = calendar;
		this.identifier = identifier;
	}
	
	public long before(long timeStamp,int granules) throws TemporalDataException
	{
		return calendar.before(timeStamp,granules,identifier);
	}

	public long after(long timeStamp,int granules) throws TemporalDataException
	{
		return calendar.after(timeStamp,granules,identifier);
	}
}
