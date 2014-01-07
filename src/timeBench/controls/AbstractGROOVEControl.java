package timeBench.controls;

import ieg.prefuse.controls.AbstractBrushControl;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.data.Node;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import prefuse.visual.tuple.TableNodeItem;
import timeBench.calendar.Granule;
import timeBench.data.GenericTemporalElement;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalElement;
import timeBench.data.TemporalObject;

public class AbstractGROOVEControl extends AbstractBrushControl {	
	private ArrayList<VisualItem> selectedItems = new ArrayList<VisualItem>();
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
		selectedItems.add(item);
		Display d = (Display)e.getComponent();
		Visualization m_vis = d.getVisualization(); 
		m_vis.run(update);
		//if(item instanceof NodeItem) {
			//addChilds((NodeItem)item);
		//}
	}

	/**
	 * @param item
	 */
	private void addChilds(NodeItem item) {
		Iterator<NodeItem> i = item.children();
		while( i.hasNext()) {
			NodeItem iChild = i.next();
			iChild.setHighlighted(true);
			selectedItems.add(iChild);
			addChilds(iChild);
		}		
	}

	/* (non-Javadoc)
	 * @see ieg.prefuse.controls.AbstractBrushControl#brushComplete(java.util.Set, java.awt.event.MouseEvent)
	 */
	@Override
	public void brushComplete(Set<VisualItem> items, MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see ieg.prefuse.controls.AbstractBrushControl#itemPressed(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
	 */
	@Override
	public void itemPressed(VisualItem item, MouseEvent e) {
		// TODO Auto-generated method stub
		super.itemPressed(item, e);
		
		anyPressed(e);
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		super.mousePressed(e);
		
		anyPressed(e);
	}
	
	private void anyPressed(MouseEvent e) {

		if(e.getButton() == MouseEvent.BUTTON2) {
			Display d = (Display) e.getComponent();
			Visualization v = d.getVisualization();
			VisualGraph vg = (VisualGraph)v.getVisualGroup("GROOVE");
			Node root = vg.getNode(0);
			while(root.getParent() != null)
				root = root.getParent();
			TemporalObject toRoot = (TemporalObject)v.getSourceTuple((VisualItem)root);
			clear(v,toRoot);
		}
		
		if(e.getButton() == MouseEvent.BUTTON3) {
			Display d = (Display) e.getComponent();
			Visualization v = d.getVisualization();
			VisualGraph vg = (VisualGraph)v.getVisualGroup("GROOVE");
			Node root = vg.getNode(0);
			while(root.getParent() != null)
				root = root.getParent();
				TemporalObject toRoot = (TemporalObject)v.getSourceTuple((VisualItem)root);
			try {
				TemporalDataset pattern = new TemporalDataset(((TemporalDataset)v.getSourceData(vg)).getDataColumnSchema());
				GenericTemporalElement te = toRoot.getTemporalElement().asGeneric();
				TemporalObject patternRoot = pattern.addTemporalObject(pattern.addTemporalElement(te.getInf(),te.getSup(),te.getGranularityId(),
						te.getGranularityContextId(),te.getKind()));
				patternRoot.setDouble("value", Double.NaN);
				buildPattern(root,pattern,patternRoot);
				searchPattern(v,toRoot,patternRoot);
			} catch (TemporalDataException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			v.run(update);
		}
	}

	/**
	 * @param toRoot
	 */
	private void clear(Visualization v,TemporalObject toRoot) {
		v.getVisualItem("GROOVE", toRoot).setHighlighted(false);
		for(TemporalObject to : toRoot.childObjects())
			clear(v,to);
	}

	/**
	 * @param root
	 * @param pattern
	 * @param patternRoot
	 */
	private void searchPattern(Visualization v,TemporalObject dataNode,TemporalObject patternNode) {
//		Granularity granularity = new Granularity(JavaDateCalendarManager.getInstance().getDefaultCalendar(),
//				dataNode.getTemporalElement().asGeneric().getGranularityId(),
//				dataNode.getTemporalElement().asGeneric().getGranularityContextId());
//		Granularity patternGranularity = new Granularity(JavaDateCalendarManager.getInstance().getDefaultCalendar(),
//				patternNode.getTemporalElement().asGeneric().getGranularityId(),
//				patternNode.getTemporalElement().asGeneric().getGranularityContextId());
//		if (granularity.getGlobalGranularityIdentifier() == patternGranularity.getGlobalGranularityIdentifier() &&
//				granularity.getGranularityContextIdentifier() == patternGranularity.getGranularityContextIdentifier()) {
//		}
//	} else {
//		for(TemporalObject ipo : patternNode.childObjects())
//			searchPatternRecurse(v,dataNode,ipo,baseValue);
//	}
	
		if(Double.isNaN(patternNode.getDouble("value"))) {
			for(TemporalObject iChild : dataNode.childObjects())
				searchPattern(v,iChild,(TemporalObject)patternNode.getChild(0));
		} else {
			try {
				searchPatternRecurse(v,dataNode,patternNode,dataNode.getDouble("value")/patternNode.getDouble("value"),
						patternNode.getTemporalElement().getGranule().getIdentifier()-
						dataNode.getTemporalElement().getGranule().getIdentifier());
			} catch (TemporalDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}	
	
	private boolean searchPatternRecurse(Visualization v,TemporalObject dataNode,
			TemporalObject patternNode,double baseValue,Long baseIdentifier) {
		boolean result = true;
		int checked = 0;
		
		TemporalObject dataParent = (TemporalObject)dataNode.getParent();		
		TemporalObject patternParent = (TemporalObject)patternNode.getParent();

		ArrayList<TemporalObject> toHighlight = new ArrayList<TemporalObject>();
		
		for(TemporalObject ipo : patternParent.childObjects()) {
			for(TemporalObject io : dataParent.childObjects()) {
				Granule dataGranule;
				try {
					dataGranule = io.getTemporalElement().getGranule();
					Granule patternGranule = ipo.getTemporalElement().getGranule();
					if((baseIdentifier == null && dataGranule.getIdentifier() == patternGranule.getIdentifier())
							|| patternGranule.getIdentifier()-dataGranule.getIdentifier() == baseIdentifier) {
						result &= Double.isNaN(ipo.getDouble("value")) || Math.abs(io.getDouble("value")/ipo.getDouble("value")-baseValue) < 0.1;
						checked++;
						if (result && io.getChildCount() > 0 && ipo.getChildCount() > 0)				
							result &= searchPatternRecurse(v,(TemporalObject)io.getChild(0),(TemporalObject)ipo.getChild(0),baseValue,null);
						if(result && !Double.isNaN(ipo.getDouble("value")))
							toHighlight.add(io);
					}
				} catch (TemporalDataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		if (result && checked == patternParent.getChildCount()) {
			for(TemporalObject highlight : toHighlight)
				v.getVisualItem("GROOVE", highlight).setHighlighted(true);
		}
		
		return result;
	}

	/**
	 * @param root
	 * @param patternRoot
	 */
	private boolean buildPattern(Node data, TemporalDataset pattern, TemporalObject current) {
		boolean result = false;
		Iterator<Node> i = data.children();
		while(i.hasNext()) {
			Node iChild = i.next();
			TemporalElement el = ((TemporalObject)((TableNodeItem)iChild).getSourceTuple()).getTemporalElement();
			try {
				TemporalElement newel = pattern.addInstant(el.getGranule());
				TemporalObject newObj = pattern.addTemporalObject(newel);
				boolean keep = buildPattern(iChild,pattern,newObj);
				if (((VisualItem)iChild).isHighlighted()) {				
					newObj.setDouble("value",iChild.getDouble("value"));			
					current.linkWithChild(newObj);
					result = true;
				} else if(keep) {
					newObj.setDouble("value",Double.NaN);
					current.linkWithChild(newObj);
					result = true;
				} else {
					pattern.removeNode(newObj);
				}
			} catch (TemporalDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		
		return result;
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
