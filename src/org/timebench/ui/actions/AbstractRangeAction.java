package org.timebench.ui.actions;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.timebench.action.layout.timescale.RangeAdapter;

/**
 * Abstract base class for {@link Action}s that operate on a
 * {@link RangeAdapter}.
 * 
 * @author peterw
 * 
 */
public abstract class AbstractRangeAction extends AbstractAction {
    
    private static final long serialVersionUID = 885335569017703949L;
    
    private RangeAdapter rangeModel;

	public RangeAdapter getRangeModel() {
		return rangeModel;
	}

	public void setRangeModel(RangeAdapter rangeModel) {
		this.rangeModel = rangeModel;
	}
}
