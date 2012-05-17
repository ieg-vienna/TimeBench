package timeBench.ui.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import timeBench.action.layout.timescale.BasicTimeScale;

/**
 * Pan's a given {@link BasicTimeScale}
 * 
 * @author peterw
 * 
 */
public class PanAction extends AbstractTimeScaleAction {
    
    private static final long serialVersionUID = 1180303548963943176L;
    
    private int d;

	/**
	 * <p>
	 * Create a {@link PanAction} that pans a {@link BasicTimeScale} d pixels.
	 * </p>
	 * <p>
	 * Use positive values for panning right and negative values for panning
	 * left.
	 * </p>
	 * 
	 * @param d
	 *            the amount of pixels to be panned
	 */
	public PanAction(int d) {
		this(null, d);
	}

	public PanAction(BasicTimeScale timeScale, int d) {
		super(timeScale);
		this.d = d;

		String dir = isLeft() ? "left" : "right";
		putValue(NAME, "Pan " + dir);
		putValue(SMALL_ICON, new ImageIcon(getClass().getClassLoader().getResource(
		        "timeBench/ui/resources/" + "pan_" + dir + ".gif")));
		putValue(ACCELERATOR_KEY, (isLeft() ? KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())
				: KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())));
	}

	protected boolean isLeft() {
		return d < 0;
	}

	public void actionPerformed(ActionEvent e) {
		if (timeScale == null) {
			return;
		}

		timeScale.pan(d);
	}
}
