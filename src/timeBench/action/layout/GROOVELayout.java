package timeBench.action.layout;

import ieg.prefuse.data.DataHelper;
import ieg.util.color.HCL;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;

import org.apache.commons.lang3.tuple.Pair;

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
import timeBench.calendar.CalendarManager;
import timeBench.calendar.CalendarManagerFactory;
import timeBench.calendar.CalendarManagers;
import timeBench.calendar.Granule;
import timeBench.data.GranularityAggregationTreeProvider;
import timeBench.data.TemporalDataException;
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
	GranularityAggregationTreeProvider dataProvider;
	GranularityGROOVELayoutSettings[] settings;
	String labelGroup = "GROOVELabels";
	int hDepth = 0;
	int vDepth = 0;
	double minLuminance = HCL.getMinLuminanceForChroma(60, 2.2);
	double maxLuminance = HCL.getMaxLuminanceForChroma(60, 2.2);

	public static final int ORIENTATION_HORIZONTAL = 0;
	public static final int ORIENTATION_VERTICAL = 1;
	public static final int COLOR_CALCULATION_GLOWING_METAL = 0;
	public static final int COLOR_CALCULATION_H_BLUE_RED = 1;
	public static final int COLOR_CALCULATION_L = 2;
	public static final int FITTING_FULL_AVAILABLE_SPACE = 0;
	public static final int FITTING_DEPENDING_ON_POSSIBLE_VALUES = 1;
	
	public GROOVELayout(String group,String labelGroup,CalendarManagers calendarManager,GranularityAggregationTreeProvider dataProvider,
			int columnUsed,GranularityGROOVELayoutSettings[] settings) {		
		this.calendarManager = CalendarManagerFactory.getSingleton(calendarManager);
		hotPalette = prefuse.util.ColorLib.getHotPalette(768);
		this.group = group;
		this.labelGroup = labelGroup;
		this.dataProvider = dataProvider;
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
		Rectangle position = new Rectangle(vDepth*60+30,hDepth*20+10,display.getWidth()-vDepth*60-30,display.getHeight()-hDepth*20-10);
		
		m_vis.removeGroup(group);
		m_vis.removeGroup(labelGroup);
		VisualGraph vg = m_vis.addGraph(group, dataProvider.getGranularityAggregationTree());
		
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
			
            ArrayList<Float> relativeSize = new ArrayList<Float>();
            ArrayList<Long> minIdentifiers = new ArrayList<Long>();
            ArrayList<Long> maxIdentifiers = new ArrayList<Long>();
            buildSizeChart(dataProvider.getGranularityAggregationTree().getTemporalObject(
            		dataProvider.getGranularityAggregationTree().getRoots()[0]),relativeSize,minIdentifiers,maxIdentifiers,0);
            
			layoutGranularity(vg,vgl,vgl.getRoot().getChild(0),vgl.getRoot().getChild(1),(NodeItem)m_vis.getVisualItem(group, dataProvider.getGranularityAggregationTree().getTemporalObject(
					dataProvider.getGranularityAggregationTree().getRoots()[0])),position,0,minIdentifiers,maxIdentifiers);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void buildSizeChart(TemporalObject node, ArrayList<Float> relativeSize, ArrayList<Long> minIdentifiers, ArrayList<Long> maxIdentifiers, int level) throws TemporalDataException {
	    // TODO relativeSize is never read
		if(relativeSize.size() <= level)
			relativeSize.add(0f);
		if(minIdentifiers.size() <= level)
			minIdentifiers.add(Long.MAX_VALUE);
		if(maxIdentifiers.size() <= level)
			maxIdentifiers.add(Long.MIN_VALUE);
		if (settings[level].getFitting() == FITTING_DEPENDING_ON_POSSIBLE_VALUES) {
			minIdentifiers.set(level,node.getTemporalElement().getGranule().getGranularity().getMinGranuleIdentifier());
			maxIdentifiers.set(level,node.getTemporalElement().getGranule().getGranularity().getMaxGranuleIdentifier());
		} else {
			minIdentifiers.set(level, Math.min(minIdentifiers.get(level), node.getTemporalElement().getGranule().getIdentifier()));
			maxIdentifiers.set(level, Math.max(maxIdentifiers.get(level), node.getTemporalElement().getGranule().getIdentifier()));
		}		
		if(node.getChildCount() == 0)
			relativeSize.add(1f);
		else {
			for(TemporalObject o : node.childObjects()) {
				buildSizeChart(o,relativeSize,minIdentifiers,maxIdentifiers,level+1);
			}
			//settings[level]
		}
	}
	
	private void getMinMax(int level,double[] minmax) {
		minmax[0] = Double.NaN;
		minmax[1] = Double.NaN;		
			for(int i=0; i<settings.length; i++) {
				if (settings[i].getColorCalculation() == settings[level].getColorCalculation()) {
					minmax[0] = dataProvider.getGranularityAggregationTree().getMinValue(i, settings[level].getSourceColumn());
					minmax[1] = dataProvider.getGranularityAggregationTree().getMaxValue(i, settings[level].getSourceColumn());
				}
			}
	}

	/**
	 * @param vgl 
	 * @throws Exception 
	 * 
	 */
	private void layoutGranularity(VisualGraph vg,VisualTree vgl, Node hNode,Node vNode, NodeItem node,Rectangle position,int granularityLevel,ArrayList<Long> minIdentifiers,ArrayList<Long> maxIdentifiers) throws Exception {		

		if(granularityLevel > 0) {
			position.x += settings[granularityLevel-1].getBorderWith()[0];
			position.y += settings[granularityLevel-1].getBorderWith()[1];
			position.width -= (settings[granularityLevel-1].getBorderWith()[0]+settings[granularityLevel].getBorderWith()[2]);
			position.height -= (settings[granularityLevel-1].getBorderWith()[1]+settings[granularityLevel].getBorderWith()[3]);
		}
		
		node.setX(position.getCenterX());
		node.setY(position.getCenterY());
		node.setSizeX(position.getWidth());
		node.setSizeY(position.getHeight());
		node.setDOI(granularityLevel);
		node.setStrokeColor(ColorLib.rgba(0, 0, 0, 0));			
		
		if(granularityLevel + 1 < settings.length) {
			Iterator<NodeItem> iChilds = node.inNeighbors();
			while(iChilds.hasNext()) {
				NodeItem iChild = iChilds.next();				
				Granule granule = ((TemporalObject)iChild.getSourceTuple()).getTemporalElement().getGranule();
				int numberOfSubElements = (int)(maxIdentifiers.get(granularityLevel+1)-minIdentifiers.get(granularityLevel+1)+1);
				Rectangle subPosition = (Rectangle)position.clone();
				Node hTargetNode = null;
				Node vTargetNode = null;
				Node targetNode = null;
				if (settings[granularityLevel+1].getOrientation() == ORIENTATION_HORIZONTAL) {
					subPosition.x += position.width/numberOfSubElements*(granule.getIdentifier()-minIdentifiers.get(granularityLevel+1));
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
					subPosition.y += position.height/numberOfSubElements*(granule.getIdentifier()-minIdentifiers.get(granularityLevel+1));
					subPosition.height = position.height/numberOfSubElements;					
					for(int i=0; i<vNode.getChildCount();i++) {
						if (vNode.getChild(i).getString(VisualItem.LABEL) == granule.getLabel()) {
							targetNode = vNode.getChild(i);
							break;
						}
					}
					if(targetNode == null) {
						targetNode = vgl.addChild(vNode);
						((VisualItem)targetNode).setX((targetNode.getDepth()-1)*60);
						((VisualItem)targetNode).setY(subPosition.getCenterY());
						targetNode.setString(VisualItem.LABEL, granule.getLabel());
						((VisualItem)targetNode).setTextColor(ColorLib.gray(0));
					}
					hTargetNode = hNode;
					vTargetNode = targetNode;
				}

				layoutGranularity(vg,vgl,hTargetNode,vTargetNode,iChild, subPosition, granularityLevel+1,minIdentifiers,maxIdentifiers);
			}
		}
	}


}
