package org.timebench.action.layout;

import org.timebench.action.layout.timescale.FisheyeTimeScale;
import org.timebench.action.layout.timescale.TimeScale;
import org.timebench.data.AnchoredTemporalElement;
import org.timebench.data.Span;

import prefuse.data.expression.Predicate;

/**
 * Layout for a {@link Span} based on a parent {@link AnchoredTemporalElement}.
 * 
 * Idea: Inf/Sup of {@link AnchoredTemporalElement} -> middle,
 * calculated inf/sup if span was an interval center aligned to parent with Granularity operations,
 * calculate width in pixels of span,
 * calculate x & width of parent,
 * calculate x of span to be visually centered with parent.
 * 
 * Rationale: A {@link Span} of 1 month might be a different number of pixels depending on its temporal location.
 * It might also depend on its screen location, if there is distortion ({@link FisheyeTimeScale}).
 * 
 * @author Rind
 */
@Deprecated
public class SpanAxisLayout extends IntervalAxisLayout {

    public SpanAxisLayout(String group) {
        super(group);
    }

    public SpanAxisLayout(String group, TimeScale timeScale) {
        super(group, timeScale);
    }

    public SpanAxisLayout(String group, TimeScale timeScale,
            int[] pathToInterval) {
        super(group, timeScale, pathToInterval);
    }

    public SpanAxisLayout(String group, int axis, TimeScale timeScale,
            Placement placement, Predicate filter) {
        super(group, axis, timeScale, placement, filter);
    }
    
    @Override
    public void run(double frac) {
    	super.run(frac);
    }
}
