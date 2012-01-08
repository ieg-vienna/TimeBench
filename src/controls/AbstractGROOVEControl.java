package controls;

import java.awt.event.MouseEvent;
import java.util.ArrayList;

import prefuse.data.Tuple;
import prefuse.visual.VisualItem;
import timeBench.data.oo.TemporalObject;

public class AbstractGROOVEControl extends prefuse.controls.ControlAdapter {	
	private ArrayList<TemporalObject> brushedObjects;
	
	@Override
	public void itemKeyPressed(VisualItem item, java.awt.event.KeyEvent e) {
		brushedObjects = new ArrayList<TemporalObject>();
		addTemporalObject(item);
	}
	
	@Override
	public void itemKeyReleased(VisualItem item, java.awt.event.KeyEvent e) {
		brushedObjects = new ArrayList<TemporalObject>();	
	}
	
	@Override
    public void itemMoved(VisualItem item, MouseEvent e) {
		addTemporalObject(item);
    }
	
	private void addTemporalObject(VisualItem item) {
		Tuple sourceTuple = m_vis.getSourceTuple(node);
		if (sourceTuple instanceof TemporalObject) {
			TemporalObject temporalObject = (TemporalObject)sourceTuple;
	}
	
	public void temporalObjectsBrushed(ArrayList<TemporalObject> brushedObjects) {
		
	}
}
