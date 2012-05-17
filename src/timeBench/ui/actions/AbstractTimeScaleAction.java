package timeBench.ui.actions;

import javax.swing.AbstractAction;
import javax.swing.Action;

import timeBench.action.layout.timescale.BasicTimeScale;

/**
 * Abstract base class for {@link Action}s that operate on a
 * {@link BasicTimeScale}.
 * 
 * @author peterw
 * 
 */
public abstract class AbstractTimeScaleAction extends AbstractAction {
    
    private static final long serialVersionUID = 6232883748177889550L;
    
    protected BasicTimeScale timeScale;

	public AbstractTimeScaleAction() {
		this(null);
	}

	public AbstractTimeScaleAction(BasicTimeScale timeScale) {
		this.timeScale = timeScale;
	}

	public BasicTimeScale getTimeScale() {
		return timeScale;
	}

	public void setTimeScale(BasicTimeScale timeScale) {
		this.timeScale = timeScale;
	}

}
