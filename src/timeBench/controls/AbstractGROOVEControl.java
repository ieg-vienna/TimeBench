package timeBench.controls;

import ieg.prefuse.controls.AbstractBrushControl;

import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.data.Tuple;
import prefuse.util.ColorLib;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalObject;

public class AbstractGROOVEControl extends AbstractBrushControl {	
	private ArrayList<TemporalObject> brushedObjects;
	private ArrayList<VisualItem> brushedItems;
	String update;
	
	public AbstractGROOVEControl(String update) {
		super();
		this.update = update;
	}
	
//	@Override
//	public void itemPressed(VisualItem item, MouseEvent e) {
//		brushedObjects = new ArrayList<TemporalObject>();
//		brushedItems = new ArrayList<VisualItem>();
//		addTemporalObject(item,e);
//	}
//	
//	@Override
//	public void itemReleased(VisualItem item, MouseEvent e) {
//		temporalObjectsBrushed(brushedObjects, e);
//		brushedObjects = new ArrayList<TemporalObject>();
//		for(VisualItem iItem : brushedItems) {
//			iItem.setStrokeColor(ColorLib.rgba(0, 0, 0, 0));
//			iItem.render(((Graphics2D)((Display)e.getComponent()).getGraphics()));
//		}
//		brushedItems = new ArrayList<VisualItem>();
//	}
	
	@Override
    public void itemEntered(VisualItem item, MouseEvent e) {
		item.setHover(true);
		Display d = (Display)e.getComponent();
		Visualization m_vis = d.getVisualization(); 
		m_vis.run(update);
    }
	
	@Override
    public void itemExited(VisualItem item, MouseEvent e) {
		item.setHover(false);
		Display d = (Display)e.getComponent();
		Visualization m_vis = d.getVisualization(); 
		m_vis.run(update);
    }

	/* (non-Javadoc)
	 * @see ieg.prefuse.controls.AbstractBrushControl#brushedItemAdded(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
	 */
	@Override
	public void brushedItemAdded(VisualItem item, MouseEvent e) {
		item.setHighlighted(true);
		if(item instanceof NodeItem) {
			hightlightChilds((NodeItem)item);
		}
	}

	/**
	 * @param item
	 */
	private void hightlightChilds(NodeItem item) {
		Iterator<NodeItem> i = item.children();
		while( i.hasNext()) {
			NodeItem iChild = i.next();
			iChild.setHighlighted(true);
			hightlightChilds(iChild);
		}		
	}

	/* (non-Javadoc)
	 * @see ieg.prefuse.controls.AbstractBrushControl#brushComplete(java.util.Set, java.awt.event.MouseEvent)
	 */
	@Override
	public void brushComplete(Set<VisualItem> items, MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
//	private void addTemporalObject(VisualItem item,InputEvent e) {
//		System.err.print(brushedItems.size()+"/");
//		if (!brushedItems.contains(item)) {
//			System.err.println();
//			System.err.println("added");
//			brushedItems.add(item);
//			item.setStrokeColor(ColorLib.rgba(0, 0, 0, 255));
//			Display d = (Display)e.getComponent();
//			item.render((Graphics2D)d.getGraphics());
//			Visualization m_vis = d.getVisualization(); 
//			Tuple sourceTuple = m_vis.getSourceTuple(item);
//			if (sourceTuple instanceof TemporalObject) {
//				TemporalObject temporalObject = (TemporalObject)sourceTuple;
//				if (!brushedObjects.contains(temporalObject))
//					brushedObjects.add(temporalObject);
//			}
//			m_vis.run(update);
//		}
//	}
	
//	public void temporalObjectsBrushed(ArrayList<TemporalObject> brushedObjects,InputEvent e) {
//		
//	}
}
