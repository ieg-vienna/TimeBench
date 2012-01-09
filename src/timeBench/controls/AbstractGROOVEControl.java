package timeBench.controls;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.data.Tuple;
import prefuse.visual.VisualItem;
import timeBench.data.oo.TemporalObject;

public class AbstractGROOVEControl extends prefuse.controls.ControlAdapter {	
	private ArrayList<TemporalObject> brushedObjects;
	
	@Override
	public void itemKeyPressed(VisualItem item, java.awt.event.KeyEvent e) {
		brushedObjects = new ArrayList<TemporalObject>();
		addTemporalObject(item,e);
	}
	
	@Override
	public void itemKeyReleased(VisualItem item, java.awt.event.KeyEvent e) {
		temporalObjectsBrushed(brushedObjects, e);
		brushedObjects = new ArrayList<TemporalObject>();	
	}
	
	@Override
    public void itemMoved(VisualItem item, MouseEvent e) {
		addTemporalObject(item,e);
    }
	
	private void addTemporalObject(VisualItem item,InputEvent e) {
		Display d = (Display)e.getComponent();
		Visualization m_vis = d.getVisualization(); 
		Tuple sourceTuple = m_vis.getSourceTuple(item);
		if (sourceTuple instanceof TemporalObject) {
			TemporalObject temporalObject = (TemporalObject)sourceTuple;
			if (!brushedObjects.contains(temporalObject))
				brushedObjects.add(temporalObject);
		}
	}
	
	public void temporalObjectsBrushed(ArrayList<TemporalObject> brushedObjects,InputEvent e) {
		
	}
}
