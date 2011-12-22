package timeBench.action.layout;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;

import prefuse.Display;
import prefuse.data.Tuple;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;
import timeBench.calendar.CalendarManager;
import timeBench.calendar.CalendarManagerFactory;
import timeBench.calendar.CalendarManagers;
import timeBench.data.relational.TemporalDatasetProvider;
import timeBench.data.relational.TemporalObject;

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
public class GROOVELayout extends prefuse.action.layout.Layout {
	
	CalendarManager calendarManager;
	int[] hotPalette;
	String group = "GROOVE";
	TemporalDatasetProvider datasetProvider;

	public GROOVELayout(String group,CalendarManagers calendarManager,TemporalDatasetProvider datasetProvider) {
		
		this.calendarManager = CalendarManagerFactory.getSingleton(calendarManager);
		hotPalette = prefuse.util.ColorLib.getHotPalette(768);
		this.group = group;
		this.datasetProvider = datasetProvider;
	}
	
	@Override
	public void run(double frac) {
		Display display = m_vis.getDisplay(0);	
		Rectangle position = new Rectangle(new Point(0,0),display.getSize());
		
		m_vis.removeGroup(group);
		VisualTable vt = m_vis.addTable(group, datasetProvider.getTemporalDataset().getOccurrences());
		
		try {
			layoutGranularity(vt,m_vis.getVisualItem(group, datasetProvider.getTemporalDataset().getOccurrences().getTuple(
					datasetProvider.getTemporalDataset().getRoots()[0])),position,0,0.0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/**
	 * @throws Exception 
	 * 
	 */
	private void layoutGranularity(VisualTable vt,VisualItem node,Rectangle position,int granularityLevel,double parentValue) throws Exception {
		node.setStartX(position.getMinX());
		node.setStartY(position.getMinY());
		node.setEndX(position.getMaxX());
		node.setEndY(position.getMaxY());
		node.setVisible(granularitySettings[granularityLevel].isVisible());

		double value = node.getDouble(0);
		switch(granularitySettings[granularityLevel].getColorCalculation()) {
			case GranularitySettings.COLOR_CALCULATION_GLOWING_METAL:
				node.setFillColor(hotPalette[(int)Math.round((value-datasetContainer.getMinValue())/
						(datasetContainer.getMaxValue()-datasetContainer.getMinValue()))]);
				break;
			case GranularitySettings.COLOR_CALCULATION_H_BLUE_RED:
			    node.setFillColor(prefuse.util.ColorLib.hsb((float)((value-datasetContainer.getMinValue())/
						(datasetContainer.getMaxValue()-datasetContainer.getMinValue())/3.0+(2.0/3.0)), 1.0f, 0.5f));
				break;
			case GranularitySettings.COLOR_CALCULATION_L:
				if (granularitySettings[granularityLevel].isColorOverlay()) {
				    node.setFillColor(prefuse.util.ColorLib.hsb((float)((parentValue-datasetContainer.getMinValue())/
							(datasetContainer.getMaxValue()-datasetContainer.getMinValue())/3.0+(2.0/3.0)),
				    		1.0f,(float)((value-datasetContainer.getMinValue())/
							(datasetContainer.getMaxValue()-datasetContainer.getMinValue()))));				
				} else {
				    node.setFillColor(prefuse.util.ColorLib.hsb(0.0f,0.0f,(float)((value-datasetContainer.getMinValue())/
							(datasetContainer.getMaxValue()-datasetContainer.getMinValue()))));
				}
				break;
			default:
				node.setFillColor(0);
		}
		
		Tuple sourceTuple = m_vis.getSourceTuple(node);
		if (sourceTuple instanceof TemporalObject) {
			TemporalObject temporalObject = (TemporalObject)sourceTuple;
			Iterator<TemporalObject> iter = temporalObject.childElements();
			int numberOfSubElements = temporalObject.getChildElementCount();
			for(int i=0; i<numberOfSubElements && iter.hasNext(); i++)
			{
				TemporalObject iChild = iter.next();
				Rectangle subPosition = (Rectangle)position.clone();
				if (granularitySettings[granularityLevel].getOrientation() == GranularitySettings.ORIENTATION_HORIZONTAL) {
					subPosition.x = position.width/numberOfSubElements*i;
					subPosition.width = position.width/numberOfSubElements;
				} else if (granularitySettings[granularityLevel].getOrientation() == GranularitySettings.ORIENTATION_VERTICAL) {
						subPosition.y = position.height/numberOfSubElements*i;
						subPosition.height = position.height/numberOfSubElements;					
				}				
				layoutGranularity(vt,m_vis.getVisualItem("GROOVE",iChild), subPosition, granularityLevel+1,value);
			}
		}
		else
			throw new Exception("VisualTable not built off TemporalObject instances");
	}


}
