package timeBench.data.oo;

import java.util.ArrayList;
import java.util.Iterator;

import timeBench.data.TemporalDataException;

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
	private timeBench.data.relational.TemporalDataset sourceData = null;
	
	public timeBench.data.relational.TemporalDataset getSourceData() {
		return sourceData;
	}

	public void setSourceData(timeBench.data.relational.TemporalDataset sourceData) {
		this.sourceData = sourceData;
	}

	public TemporalDataset(timeBench.data.relational.TemporalDataset data) throws TemporalDataException
	{
		long totalInf = Long.MAX_VALUE;
		long totalSup = Long.MIN_VALUE;
        Iterator<timeBench.data.relational.TemporalObject> iTemporalObject = data.temporalObjects();
        while (iTemporalObject.hasNext()) {
        	timeBench.data.relational.TemporalObject relationalTemporalObject = iTemporalObject.next();
        	timeBench.data.oo.TemporalObject ooTemporalObject = new timeBench.data.oo.TemporalObject(relationalTemporalObject);
        	subObjects.add(ooTemporalObject);
        	if (relationalTemporalObject.getTemporalElement().isAnchored()) {
        	    timeBench.data.relational.GenericTemporalElement relationalTemporalElement = relationalTemporalObject.getTemporalElement().asGeneric();
                totalInf = Math.min(totalInf, relationalTemporalElement.getInf());
                totalSup = Math.max(totalInf, relationalTemporalElement.getSup());          
        	}
	    }
        temporalElement = new Interval(totalInf,totalSup);
        sourceData = data;
	}
	
	public ArrayList<TemporalObject> getTemporalObjects() {
		return getSubObjects();
	}
}
