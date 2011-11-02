package timeBench.data.oo;

import java.util.ArrayList;

import timeBench.data.TemporalDataException;

/**
 * Base class for general Temporal Object.
 * 
 * <p>
 * Added:         2011-07-13 / TL<br>
 * Modifications:
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public class TemporalObject {
	protected TemporalElement temporalElement;
	protected Object dataAspects = null;
	
	/**
	 * The parameterless constructor may only be used by classes that inherit from this class but explicitely
	 * set the temporalAspects and dataAspects in their constructor(s).
	 */
	protected TemporalObject() {		
	}	
	
	/**
	 * A TemporalObject can be constructed from the scratch when a temporalElement and dataAspects are given.
	 * @param temporalAspects
	 * @param dataAspects
	 */
	public TemporalObject(TemporalElement temporalElement,Object dataAspects) {
		this.temporalElement = temporalElement;
		this.dataAspects = dataAspects;
	}
	
	/**
	 * An object-oriented TemporalObject can be constructed from a relational TemporalObject
	 * @param relationalTemporalObject
	 * @throws TemporalDataException 
	 */
	public TemporalObject(timeBench.data.relational.TemporalObject relationalTemporalObject) throws TemporalDataException {
		this(TemporalElement.createFromRelationalTemporalElement(relationalTemporalObject.getTemporalElement()),new ArrayList<Object>());
		ArrayList<Object> data = new ArrayList<Object>();
		for(int i=0; i<relationalTemporalObject.getDataElement().getColumnCount();i++)
		{
			data.add(relationalTemporalObject.getDataElement().get(i));
		}
		dataAspects = data;
	}
	
	protected ArrayList<TemporalObject> getSubObjects() {
		ArrayList<TemporalObject> result = new ArrayList<TemporalObject>();
		if (dataAspects instanceof ArrayList) {
			for(Object o : (ArrayList<Object>)dataAspects) {
				if (o instanceof TemporalObject)
					result.add((TemporalObject)o);
			}
		}
		return result;
	}
	
	public void anchorRelational(timeBench.data.relational.TemporalDataset dataset) throws TemporalDataException {
		long inf = 0, sup = 0;
		int kind = timeBench.data.relational.TemporalDataset.PRIMITIVE_SET;
		if (temporalElement instanceof AnchoredTemporalElement) {
			inf = ((AnchoredTemporalElement)temporalElement).getInf();
			sup = ((AnchoredTemporalElement)temporalElement).getSup();
			if (temporalElement instanceof Instant)
				kind = timeBench.data.relational.TemporalDataset.PRIMITIVE_INSTANT;
			else if (temporalElement instanceof Interval)
				kind = timeBench.data.relational.TemporalDataset.PRIMITIVE_INTERVAL;
		} else if(temporalElement instanceof UnanchoredTemporalElement) {
			inf = ((UnanchoredTemporalElement)temporalElement).getDuration();
			sup = ((UnanchoredTemporalElement)temporalElement).getDuration();
			if (temporalElement instanceof Span)
				kind = timeBench.data.relational.TemporalDataset.PRIMITIVE_SPAN;
		}
		int temporalIndex = dataset.addTemporalElement(inf, sup, temporalElement.getGranularity().getIdentifier(), kind);
		int objectIndex = dataset.getDataElements().addRow();
		dataset.getDataElements().set(objectIndex, 0, dataAspects);
		dataset.addOccurrence(objectIndex, temporalIndex);
	}
	
	public TemporalElement getTemporalElement() {
		return temporalElement;
	}
	
	public Object getDataAspects() {
		return dataAspects;
	}
}
