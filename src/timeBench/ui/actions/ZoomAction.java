package timeBench.ui.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import timeBench.action.layout.timescale.BasicTimeScale;

/**
 * Zooms a {@link BasicTimeScale}.
 * 
 * @author peterw
 * 
 */
public class ZoomAction extends AbstractTimeScaleAction {
    
    private static final long serialVersionUID = 9141377613407077396L;
    
    private double factor;

	/**
	 * <p>
	 * Create a {@link ZoomAction} that zooms a {@link BasicTimeScale} about the
	 * given factor.
	 * </p>
	 * <p>
	 * Use values smaller than 1 to zoom in and values greater 1 to zoom out.
	 * </p>
	 * 
	 * @param factor
	 *            the zoom factor
	 * 
	 */
	public ZoomAction(double factor) {
		this(null, factor);
	}

	public ZoomAction(BasicTimeScale timeScale, double factor) {
		super(timeScale);
		this.factor = factor;

		putValue(NAME, "Zoom " + (isZoomIn() ? "In" : "Out"));
		putValue(SMALL_ICON, new ImageIcon(getClass().getClassLoader().getResource(
		        "timeBench/ui/resources/" +
				(isZoomIn() ? "zoomplus_on.gif" : "zoomminus_on.gif"))));
		putValue(ACCELERATOR_KEY, (isZoomIn() ? KeyStroke.getKeyStroke(KeyEvent.VK_UP, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())
				: KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())));
	}

	public boolean isZoomIn() {
		return factor < 1;
	}

	public void actionPerformed(ActionEvent e) {
		if (timeScale != null) {
			timeScale.zoom(factor);
		}
	}
}
