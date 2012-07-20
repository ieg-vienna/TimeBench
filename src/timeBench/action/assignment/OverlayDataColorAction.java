package timeBench.action.assignment;

import prefuse.action.assignment.DataColorAction;

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
public class OverlayDataColorAction extends DataColorAction {
	private int[][] m_palette; 
	
    public OverlayDataColorAction(String group, String dataField, 
            int dataType, String colorField)
    {
    	super(group, dataField,dataType,colorField);
    	setDataType(dataType);
    	setDataField(dataField);
    }

    public OverlayDataColorAction(String group, String dataField, 
    		int dataType, String colorField, int[][] palette)
    {
    	this(group, dataField,dataType,colorField);
    	m_palette = palette;
    }
}
