package timeBench.action.layout;

import ieg.prefuse.data.DataHelper;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;

import prefuse.Display;
import prefuse.data.Tuple;
import prefuse.util.ColorLib;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;
import timeBench.action.analytical.MinMaxValuesProvider;
import timeBench.calendar.CalendarManager;
import timeBench.calendar.CalendarManagerFactory;
import timeBench.calendar.CalendarManagers;
import timeBench.calendar.Granule;
import timeBench.data.TemporalDatasetProvider;
import timeBench.data.TemporalObject;

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
	//boolean[] granularityVisible;
	//int[] granularityColorCalculation;
	//boolean[] granularityColorOverlay;
	//int[] granularityOrientation;
	GranularityGROOVELayoutSettings[] settings;

	public static final int ORIENTATION_HORIZONTAL = 0;
	public static final int ORIENTATION_VERTICAL = 1;
	public static final int COLOR_CALCULATION_GLOWING_METAL = 0;
	public static final int COLOR_CALCULATION_H_BLUE_RED = 1;
	public static final int COLOR_CALCULATION_L = 2;
	
	public GROOVELayout(String group,CalendarManagers calendarManager,TemporalDatasetProvider datasetProvider,
			int columnUsed,GranularityGROOVELayoutSettings[] settings) {		
		this.calendarManager = CalendarManagerFactory.getSingleton(calendarManager);
		hotPalette = prefuse.util.ColorLib.getHotPalette(768);
		this.group = group;
		this.datasetProvider = datasetProvider;
		this.settings = settings;
	}
	
	@Override
	public void run(double frac) {
		Display display = m_vis.getDisplay(0);	
		Rectangle position = new Rectangle(new Point(0,0),display.getSize());
		
		m_vis.removeGroup(group);
		VisualGraph vg = m_vis.addGraph(group, datasetProvider.getTemporalDataset());
		
		try {
			layoutGranularity(vg,(NodeItem)m_vis.getVisualItem(group, datasetProvider.getTemporalDataset().getTemporalObject(
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
	private void layoutGranularity(VisualGraph vg,NodeItem node,Rectangle position,int granularityLevel,double parentValue) throws Exception {
		node.setStartX(position.getMinX());
		node.setStartY(position.getMinY());
		node.setEndX(position.getMaxX());
		node.setEndY(position.getMaxY());
		node.setDOI(granularityLevel);
		node.setStrokeColor(ColorLib.rgba(0, 0, 0, 0));
			
		double value = node.getDouble(datasetProvider.getTemporalDataset().getDataColumnSchema().getColumnName(settings[granularityLevel].getSourceColumn()));
		DataHelper.printTable(System.out,datasetProvider.getTemporalDataset().getNodeTable());
		
		if (granularityLevel < 0)
			node.setVisible(false);
		else {
			node.setVisible(settings[granularityLevel].isVisible());

			double min = Double.NaN;
			double max = Double.NaN;
			if (datasetProvider instanceof MinMaxValuesProvider) {
				min = ((MinMaxValuesProvider)datasetProvider).getMinValue(granularityLevel,settings[granularityLevel].getSourceColumn());
				max = ((MinMaxValuesProvider)datasetProvider).getMaxValue(granularityLevel,settings[granularityLevel].getSourceColumn());
			}

			switch(settings[granularityLevel].getColorCalculation()) {
			case COLOR_CALCULATION_GLOWING_METAL:
				
				if (value == -1)
					node.setFillColor(prefuse.util.ColorLib.gray(127));
				else
					node.setFillColor(hotPalette[Math.min(767,(int)Math.round((value-min)/(max-min)*1280.0))]);
				
				//node.setFillColor(prefuse.util.ColorLib.gray((int)Math.round((value-min)/(max-min)*255.0)));
				break;
			case COLOR_CALCULATION_H_BLUE_RED:
				node.setFillColor(prefuse.util.ColorLib.hsb((float)((value-min)/(max-min)/3.0+(2.0/3.0)), 1.0f, 0.5f));
				break;
			case COLOR_CALCULATION_L:
				if (settings[granularityLevel].isColorOverlay()) {
					node.setFillColor(prefuse.util.ColorLib.hsb((float)((parentValue-min)/(max-min)/3.0+(2.0/3.0)),
							1.0f,(float)((value-min)/(max-min))));				
				} else {
					node.setFillColor(prefuse.util.ColorLib.hsb(0.0f,0.0f,(float)((value-min)/(max-min))));
				}
				break;
			default:
				node.setFillColor(0);
			}
		}
		
		if(granularityLevel+1 < settings.length) {
			Iterator<NodeItem> iChilds = node.inNeighbors();
			TreeMap<Long,NodeItem> orderedChilds = new TreeMap<Long, NodeItem>();
			while(iChilds.hasNext()) {
				NodeItem iChild = iChilds.next();				
				orderedChilds.put(((TemporalObject)iChild.getSourceTuple()).getTemporalElement().asGeneric().getInf(), iChild);
			}
			int numberOfSubElements = orderedChilds.size();
			Long iKey = orderedChilds.firstKey();
			for(int i=0; iKey != null; i++)
			{
				NodeItem iChild = orderedChilds.get(iKey);
				iKey = orderedChilds.higherKey(iKey);
				Rectangle subPosition = (Rectangle)position.clone();
				if (settings[granularityLevel+1].getOrientation() == ORIENTATION_HORIZONTAL) {
					Granule granule = (Granule)iChild.get("GranuleIdentifier");
					granule.getIdentifier();
					granule.getGranularity().getMinGranuleIdentifier();
					granule.getGranularity().getMaxGranuleIdentifier();
					subPosition.x += position.width/numberOfSubElements*i;
					subPosition.width = position.width/numberOfSubElements;
				} else if (settings[granularityLevel+1].getOrientation() == ORIENTATION_VERTICAL) {
					subPosition.y += position.height/numberOfSubElements*i;
					subPosition.height = position.height/numberOfSubElements;					
				}			
				if (granularityLevel >= 0)
				{
					int[] borderWidth = settings[granularityLevel+1].getBorderWith();
					subPosition.x += borderWidth[0];
					subPosition.y += borderWidth[1];
					subPosition.width -= (borderWidth[0] + borderWidth[2]);
					subPosition.height -= (borderWidth[1] + borderWidth[3]);
				}
				layoutGranularity(vg,iChild, subPosition, granularityLevel+1,value);
			}
		}
	}


}
