package org.timebench.controls;

import java.awt.Cursor;
import java.awt.event.MouseEvent;

import javax.swing.BoundedRangeModel;

import org.timebench.action.layout.timescale.RangeAdapter;

import prefuse.Display;
import prefuse.controls.ControlAdapter;
import prefuse.util.ui.UILib;

/**
 * Pans a {@link RangeAdapter} by in/decreasing it's current value, as declared
 * in {@link BoundedRangeModel}.
 * 
 * Panning is accomplished by clicking on the background of a {@link Display}
 * with the left mouse button and then dragging.
 * 
 * @author peterw
 * @see Display
 * 
 */
public class RangePanControl extends ControlAdapter {
	private RangeAdapter rangeModel;

	private int BUTTON = LEFT_MOUSE_BUTTON;
	private int xDown;
	private int yDown;
	
	private boolean panInDetailSpace = true;

	public RangePanControl() {
		this(null);
	}

	public RangePanControl(RangeAdapter timeScale) {
		this.rangeModel = timeScale;
	}

	public RangeAdapter getRangeModel() {
		return rangeModel;
	}

	public void setRangeModel(RangeAdapter rangeModel) {
		this.rangeModel = rangeModel;
	}

	/**
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		if (UILib.isButtonPressed(e, BUTTON)) {
			e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			xDown = e.getX();
			yDown = e.getY();
		}
	}

	/**
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		if (UILib.isButtonPressed(e, BUTTON)) {
			e.getComponent().setCursor(Cursor.getDefaultCursor());
			xDown = -1;
			yDown = -1;
		}
	}

	/**
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent e) {
		if (UILib.isButtonPressed(e, BUTTON)) {
			Display display = (Display) e.getComponent();
			int x = e.getX(), y = e.getY();
			int dx = x - xDown, dy = y - yDown;
			int rangeModeldx = 1;
			if (rangeModel != null) {
				if (panInDetailSpace)
					rangeModeldx = rangeModel.panActual(dx);
				else 
					rangeModel.setValue(rangeModel.getValue() - dx);
			}
			display.pan(0, dy);
			
			// next panning step starts here (if last pan had a result)
			if (rangeModeldx != 0) {
				xDown = x;
				yDown = y;
			}
			display.repaint();
		}
	}
}
