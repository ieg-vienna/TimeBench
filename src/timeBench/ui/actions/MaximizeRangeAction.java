package timeBench.ui.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

/**
 * Maximizes a given {@link RangeAdapter}.
 */
public class MaximizeRangeAction extends AbstractRangeAction {
    
    private static final long serialVersionUID = -7988724359004986219L;

    public MaximizeRangeAction() {
		putValue(NAME, "View All");
		putValue(SMALL_ICON, new ImageIcon(getClass().getClassLoader().getResource("timeBench/ui/resources/" +"zoom_on.gif")));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_0, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	}

	public void actionPerformed(ActionEvent e) {
		if (getRangeModel() != null) {
			getRangeModel().maximize();
		}
	}
}
