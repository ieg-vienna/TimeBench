package org.timebench.ui.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import org.timebench.action.layout.timescale.FisheyeTimeScale;

/**
 * Toggles the fisheye distortion of a given {@link FisheyeTimeScale}.
 * 
 * @author peterw
 * 
 */
public class ToggleFisheyeAction extends AbstractAction {
    
    private static final long serialVersionUID = 89670022822876252L;
    
    public static final String SELECTED = "selected";
	private FisheyeTimeScale timeScale;

	public ToggleFisheyeAction() {
		putValue(NAME, "Fisheye");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
				| KeyEvent.SHIFT_DOWN_MASK));
	}

	public void actionPerformed(ActionEvent e) {
		if (getTimeScale() != null) {
			getTimeScale().setFisheyeEnabled(!getTimeScale().isFisheyeEnabled());

			firePropertyChange(SELECTED, !isSelected(), isSelected());
		}
	}

	public FisheyeTimeScale getTimeScale() {
		return timeScale;
	}

	public void setTimeScale(FisheyeTimeScale timeScale) {
		boolean oldSelected = getTimeScale() != null && getTimeScale().isFisheyeEnabled();
		this.timeScale = timeScale;
		firePropertyChange(SELECTED, oldSelected, isSelected());
	}

	public boolean isSelected() {
		if (getTimeScale() != null) {
			return getTimeScale().isFisheyeEnabled();
		}
		return false;
	}
}
