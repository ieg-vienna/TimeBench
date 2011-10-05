package timeBench.data.oo;

import java.util.Iterator;

import prefuse.data.Tuple;
import timeBench.data.relational.TemporalElement;
import timeBench.data.relational.TemporalObject;

/**
 * 
 * 
 * <p>
 * Added:         2011-09-28 / TL<br>
 * Modifications: 
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public class TemporalDataset extends TemporalObject {
	public TemporalDataset(timeBench.data.relational.TemporalDataset data)
	{
		long totalInf = Long.MAX_VALUE;
		long totalSup = Long.MIN_VALUE;
        Iterator<TemporalObject> iTemporalObject = data.temporalObjects();
        while (iTemporalObject.hasNext()) {
        	timeBench.data.relational.TemporalObject relationalTemporalObject = iTemporalObject.next();
        	timeBench.data.oo.TemporalObject ooTemporalObject = new timeBench.data.oo.TemporalObject(relationalTemporalObject); 
        	totalInf = Math.min(totalInf, relationalTemporalObject.getInf());
        	totalSup = Math.max(totalInf, relationalTemporalObject.getSup());
	    }
	}
}
