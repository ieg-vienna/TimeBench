package timeBench.ui.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.BoundedRangeModel;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import timeBench.action.layout.timescale.RangeAdapter;

/**
 * Zooms a given {@link RangeAdapter}, by in/decreasing the interval between
 * it's value and extent, as declared in {@link BoundedRangeModel}.
 * 
 * <p>
 * Added: 2012-05-17 / Peter Weishapl<br>
 * Modifications: 2010-08-20 / AR / make zoom more smooth at edges of range & set a maximum zoom <br>
 *                2012-06-14 / AR / constructor with RangeAdapter
 * </p>
 * 
 * @author peterw
 * 
 */
public class RangeZoomAction extends AbstractRangeAction {
    
    private static final long serialVersionUID = -6530293893342804635L;
    
    private int factor;

	/**
	 * Creates a {@link RangeZoomAction}, which zooms a given
	 * {@link RangeAdapter}, by changing it's value(see
	 * {@link BoundedRangeModel#setValue(int)}) and extent(see
	 * {@link BoundedRangeModel#setExtent(int)}).
	 * 
	 * @param factor
	 *            the zoom factor
	 */
	public RangeZoomAction(int factor) {
		this.factor = factor;

		putValue(NAME, "Zoom " + (isZoomIn() ? "In" : "Out"));
		putValue(SMALL_ICON, new ImageIcon(getClass().getClassLoader().getResource(
		        "timeBench/ui/resources/" +
				(isZoomIn() ? "zoomplus_on.gif" : "zoomminus_on.gif"))));
		putValue(ACCELERATOR_KEY, (isZoomIn() ? KeyStroke.getKeyStroke(KeyEvent.VK_UP, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())
				: KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())));
	}
	
	public RangeZoomAction(RangeAdapter rangeModel, int factor) {
	    this(factor);
	    super.setRangeModel(rangeModel);
	}

	// update Alex Rind 2010-Aug-20: make zoom more smooth at edges of range & set a maximum zoom 
	public void actionPerformed(ActionEvent e) {
		if (getRangeModel() != null) {
			int extent = getRangeModel().getExtent() - factor * 2;
			int value = getRangeModel().getValue() + factor;
			
			if (factor < 0) {
				value = Math.min(value, getRangeModel().getMaximum() - extent);
				
				getRangeModel().setValue(value);
				getRangeModel().setExtent(extent);
			}
			else {
				if (extent > 0) {
					getRangeModel().setExtent(extent);
					getRangeModel().setValue(value);
				}
			}
		}
		else
			System.err.println("no range model " + this);
	}

	public boolean isZoomIn() {
		return factor > 0;
	}
}
