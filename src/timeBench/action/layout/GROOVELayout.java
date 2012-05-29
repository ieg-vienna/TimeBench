package timeBench.action.layout;

import ieg.prefuse.data.DataHelper;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;

import prefuse.Display;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Schema;
import prefuse.data.Tree;
import prefuse.data.Tuple;
import prefuse.util.ColorLib;
import prefuse.util.PrefuseLib;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;
import prefuse.visual.VisualTree;
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
	GranularityGROOVELayoutSettings[] settings;
	String labelGroup = "GROOVELabels";
	int hDepth = 0;
	int vDepth = 0;

	public static final int ORIENTATION_HORIZONTAL = 0;
	public static final int ORIENTATION_VERTICAL = 1;
	public static final int COLOR_CALCULATION_GLOWING_METAL = 0;
	public static final int COLOR_CALCULATION_H_BLUE_RED = 1;
	public static final int COLOR_CALCULATION_L = 2;
	public static final int FITTING_FULL_AVAILABLE_SPACE = 0;
	public static final int FITTING_DEPENDING_ON_POSSIBLE_VALUES = 1;
	
	public GROOVELayout(String group,String labelGroup,CalendarManagers calendarManager,TemporalDatasetProvider datasetProvider,
			int columnUsed,GranularityGROOVELayoutSettings[] settings) {		
		this.calendarManager = CalendarManagerFactory.getSingleton(calendarManager);
		hotPalette = prefuse.util.ColorLib.getHotPalette(768);
		this.group = group;
		this.labelGroup = labelGroup;
		this.datasetProvider = datasetProvider;
		this.settings = settings;
		
		for(int i=1; i<settings.length; i++) {
			if(settings[i].getOrientation() == ORIENTATION_HORIZONTAL)
				hDepth++;
			else if(settings[i].getOrientation() == ORIENTATION_VERTICAL)
				vDepth++;
		}
	}
	
	@Override
	public void run(double frac) {
		Display display = m_vis.getDisplay(0);	
		Rectangle position = new Rectangle(vDepth*20+10,hDepth*20+10,display.getWidth()-vDepth*20-10,display.getHeight()-hDepth*20-10);
		
		m_vis.removeGroup(group);
		m_vis.removeGroup(labelGroup);
		VisualGraph vg = m_vis.addGraph(group, datasetProvider.getTemporalDataset());
		
        Schema labelNodeSchema = PrefuseLib.getVisualItemSchema();
        labelNodeSchema.addColumn(VisualItem.LABEL, String.class);
        Integer defColor = new Integer(ColorLib.gray(150));
        labelNodeSchema.setInterpolatedDefault(VisualItem.TEXTCOLOR, defColor);
		VisualTree vgl = m_vis.addTree(labelGroup, labelNodeSchema);
		
		Node root = vgl.addRoot();
		vgl.addChild(root);
        vgl.addChild(root);
		
		try {
            System.out.println(vgl.getRoot());
            System.out.println(vgl.getRoot().getChild(0));
            System.out.println(vgl.getRoot().getChild(1));
			
			layoutGranularity(vg,vgl,vgl.getRoot().getChild(0),vgl.getRoot().getChild(1),(NodeItem)m_vis.getVisualItem(group, datasetProvider.getTemporalDataset().getTemporalObject(
					datasetProvider.getTemporalDataset().getRoots()[0])),position,0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		DataHelper.printGraph(System.out, vgl.getRoot(),null,VisualItem.LABEL);
	}

	private void calculateColorPart(int level,int currentLevel,NodeItem currentNode, float[] hsb) {	
		while(currentLevel > level) {
			currentNode = (NodeItem)currentNode.getParent();
			currentLevel--;
		}
		
		double value = currentNode.getDouble(datasetProvider.getTemporalDataset().getDataColumnSchema().getColumnName(settings[currentLevel].getSourceColumn()));
		double[] minmax = new double[2];
		getMinMax(currentLevel,minmax);
		
		switch(settings[currentLevel].getColorCalculation()) {
			case COLOR_CALCULATION_H_BLUE_RED:
				hsb[0] = (float)((value-minmax[0])/(minmax[1]-minmax[0])/3.0+(2.0/3.0));
				if(Float.isNaN(hsb[0]))
					hsb[0] = 0.5f;
				break;
			case COLOR_CALCULATION_L:
				hsb[2] = (float)((value-minmax[0])/(minmax[1]-minmax[0]));
				if(Float.isNaN(hsb[2]))
					hsb[2] = 0.5f;
				break;
		}
	}
	
	private void getMinMax(int level,double[] minmax) {
		minmax[0] = Double.NaN;
		minmax[1] = Double.NaN;
		if (datasetProvider instanceof MinMaxValuesProvider) {
			for(int i=0; i<settings.length; i++) {
				if (settings[i].getColorCalculation() == settings[level].getColorCalculation()) {
					minmax[0] = ((MinMaxValuesProvider)datasetProvider).getMinValue(i,settings[level].getSourceColumn());
					minmax[1] = ((MinMaxValuesProvider)datasetProvider).getMaxValue(i,settings[level].getSourceColumn());
				}
			}
		}
	}

	/**
	 * @param vgl 
	 * @throws Exception 
	 * 
	 */
	private void layoutGranularity(VisualGraph vg,VisualTree vgl, Node hNode,Node vNode, NodeItem node,Rectangle position,int granularityLevel) throws Exception {
		node.setStartX(position.getMinX());
		node.setStartY(position.getMinY());
		node.setEndX(position.getMaxX());
		node.setEndY(position.getMaxY());
		node.setDOI(granularityLevel);
		node.setStrokeColor(ColorLib.rgba(0, 0, 0, 0));			
		
		if (granularityLevel < 0)
			node.setVisible(false);
		else {
			node.setVisible(settings[granularityLevel].isVisible());

			switch(settings[granularityLevel].getColorCalculation()) {			
				case COLOR_CALCULATION_GLOWING_METAL:
					double[] minmax = new double[2];
					getMinMax(granularityLevel,minmax);
					double value = node.getDouble(datasetProvider.getTemporalDataset().getDataColumnSchema().getColumnName(settings[granularityLevel].getSourceColumn()));					
					if (Double.isNaN(value))
						node.setFillColor(prefuse.util.ColorLib.gray(127));
					else
						node.setFillColor(hotPalette[Math.min(767,(int)Math.round((value-minmax[0])/(minmax[1]-minmax[0])*768.0))]);
					break;
				default:
					float[] hsb = new float[3];
					hsb[0] = Float.NaN;
					hsb[1] = Float.NaN;
					hsb[2] = Float.NaN;
					if (settings[granularityLevel].getColorOverlayLevel() >= 0) {
						calculateColorPart(settings[granularityLevel].getColorOverlayLevel(),granularityLevel,node,hsb);
					}
					calculateColorPart(granularityLevel,granularityLevel,node,hsb);
					if(Float.isNaN(hsb[0]) && Float.isNaN(hsb[1])) {
						hsb[0] = 0.0f;
						hsb[1] = 0.0f;
					} else if(Float.isNaN(hsb[0]) && Float.isNaN(hsb[2])) {
						hsb[0] = 1.0f/3.0f;
						hsb[2] = 0.5f;
					} else if(Float.isNaN(hsb[1]) && Float.isNaN(hsb[2])) {
						hsb[1] = 0.5f;
						hsb[2] = 0.5f;
					} else if(Float.isNaN(hsb[0])) {
						hsb[0] = 1.0f/3.0f;
					} else if(Float.isNaN(hsb[1])) {
						hsb[1] = 0.5f;
					} else if(Float.isNaN(hsb[2])) {
						hsb[2] = 0.5f;
					}
					node.setFillColor(prefuse.util.ColorLib.hsb(hsb[0],hsb[1],hsb[2]));
				break;
			}
		}
		
		if(granularityLevel + 1 < settings.length) {
			Iterator<NodeItem> iChilds = node.inNeighbors();
			int numberOfSubElements = Integer.MIN_VALUE;
			long minIdent = Long.MAX_VALUE;
			long maxIdent = Long.MIN_VALUE;
			if(settings[granularityLevel+1].getFitting() == FITTING_FULL_AVAILABLE_SPACE) {
				while(iChilds.hasNext()) {
					NodeItem iChild = iChilds.next();
					Granule granule = (Granule)iChild.get("GranuleIdentifier");
					minIdent = Math.min(minIdent, granule.getIdentifier());
					maxIdent = Math.max(maxIdent, granule.getIdentifier());
				}
				numberOfSubElements = (int)(maxIdent - minIdent + 1); 
				iChilds = node.inNeighbors();
			}
			while(iChilds.hasNext()) {
				NodeItem iChild = iChilds.next();				
				Granule granule = (Granule)iChild.get("GranuleIdentifier");
				if(numberOfSubElements == Integer.MIN_VALUE) {
					minIdent = granule.getGranularity().getMinGranuleIdentifier();
					maxIdent = granule.getGranularity().getMaxGranuleIdentifier();
					numberOfSubElements = (int)(maxIdent - minIdent + 1);
				}
				Rectangle subPosition = (Rectangle)position.clone();
				Node hTargetNode = null;
				Node vTargetNode = null;
				Node targetNode = null;
				if (settings[granularityLevel+1].getOrientation() == ORIENTATION_HORIZONTAL) {
					subPosition.x += position.width/numberOfSubElements*(granule.getIdentifier()-minIdent);
					subPosition.width = position.width/numberOfSubElements;
					for(int i=0; i<hNode.getChildCount();i++) {
						if (hNode.getChild(i).getString(VisualItem.LABEL) == granule.getLabel()) {
							targetNode = hNode.getChild(i);
							break;
						}
					}
					if(targetNode == null) {
						targetNode = vgl.addChild(hNode);
						((VisualItem)targetNode).setX(subPosition.getCenterX());
						((VisualItem)targetNode).setY((targetNode.getDepth()-1)*20);
						targetNode.setString(VisualItem.LABEL, granule.getLabel());
						((VisualItem)targetNode).setTextColor(ColorLib.gray(0));
					}
					hTargetNode = targetNode;
					vTargetNode = vNode;
				} else if (settings[granularityLevel+1].getOrientation() == ORIENTATION_VERTICAL) {
					subPosition.y += position.height/numberOfSubElements*(granule.getIdentifier()-minIdent);
					subPosition.height = position.height/numberOfSubElements;					
					for(int i=0; i<vNode.getChildCount();i++) {
						if (vNode.getChild(i).getString(VisualItem.LABEL) == granule.getLabel()) {
							targetNode = vNode.getChild(i);
							break;
						}
					}
					if(targetNode == null) {
						targetNode = vgl.addChild(vNode);
						((VisualItem)targetNode).setX((targetNode.getDepth()-1)*20);
						((VisualItem)targetNode).setY(subPosition.getCenterY());
						targetNode.setString(VisualItem.LABEL, granule.getLabel());
						((VisualItem)targetNode).setTextColor(ColorLib.gray(0));
					}
					hTargetNode = hNode;
					vTargetNode = targetNode;
				}

				int[] borderWidth = settings[granularityLevel+1].getBorderWith();
				subPosition.x += borderWidth[0];
				subPosition.y += borderWidth[1];
				subPosition.width -= (borderWidth[0] + borderWidth[2]);
				subPosition.height -= (borderWidth[1] + borderWidth[3]);
				layoutGranularity(vg,vgl,hTargetNode,vTargetNode,iChild, subPosition, granularityLevel+1);
			}
		}
	}


}
