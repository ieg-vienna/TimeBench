package timeBench.ui.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.BoundedRangeModel;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

/**
 * Pans a {@link RangeAdapter} by in/decreasing it's current value, as declared
 * in {@link BoundedRangeModel}.
 * 
 * @author peterw
 * 
 */
public class RangePanAction extends AbstractRangeAction {
    
    private static final long serialVersionUID = 405512961430470821L;
    
    private double d;

	/**
	 * <p>
	 * Creates a {@link RangePanAction} which pans the fiven
	 * {@link RangeAdapter} by adding d to it's current value (see
	 * {@link BoundedRangeModel#setValue(int)}.
	 * </p>
	 * <p>
	 * Use positive values for right panning and negative values for left
	 * panning.
	 * </p>
	 * 
	 * @param d
	 *            the amount of
	 */
	public RangePanAction(double d) {
		this.d = d;

		String dir = isLeft() ? "Left" : "Right";
		putValue(NAME, "Pan " + dir);
		putValue(SMALL_ICON,
				new ImageIcon(getClass().getClassLoader().getResource("timeBench/ui/resources/" + "pan_" + dir.toLowerCase() + ".gif")));
		putValue(ACCELERATOR_KEY, (isLeft() ? KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())
				: KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())));
	}

	protected boolean isLeft() {
		return d < 0;
	}

	public void actionPerformed(ActionEvent e) {
		if (getRangeModel() != null) {
			getRangeModel().setValue((int) (getRangeModel().getValue() + d));
		}
	}
}
