package timeBench.data.oo;

import java.util.ArrayList;

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
	protected ArrayList<Object> dataAspects = new ArrayList<Object>();
	
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
	public TemporalObject(TemporalElement temporalElement,ArrayList<Object> dataAspects) {
		this.temporalElement = temporalElement;
		this.dataAspects = dataAspects;
	}
	
	/**
	 * An object-oriented TemporalObject can be constructed from a relational TemporalObject
	 * @param relationalTemporalObject
	 */
	public TemporalObject(timeBench.data.relational.TemporalObject relationalTemporalObject) {
		this(new TemporalElement(relationalTemporalObject.getTemporalElement()),new ArrayList<Object>());
		for(int i=0; i<relationalTemporalObject.getDataElement().getColumnCount();i++)
		{
			dataAspects.add(relationalTemporalObject.getDataElement().get(i));
		}
	}
}
