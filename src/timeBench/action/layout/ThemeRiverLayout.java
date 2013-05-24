package timeBench.action.layout;

import java.util.Iterator;

import org.apache.log4j.Logger;

import prefuse.Constants;
import prefuse.action.layout.Layout;
import prefuse.data.expression.Predicate;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.VisiblePredicate;
import timeBench.action.layout.timescale.TimeScale;
import timeBench.data.AnchoredTemporalElement;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalElement;
import timeBench.data.TemporalObject;

public class ThemeRiverLayout extends Layout {

    protected TimeScale timeScale = null;
    protected TemporalDataset source = null;

    protected Predicate m_filter = VisiblePredicate.TRUE;

    public ThemeRiverLayout(String group,TemporalDataset source) {
        super(group);
        this.source = source;
    }

    public ThemeRiverLayout(String group,TemporalDataset source, TimeScale timeScale) {
        super(group);
        this.source = source;
        setTimeScale(timeScale);
    }

    @SuppressWarnings({ "rawtypes" })
    @Override
    public void run(double frac) {
        if (timeScale == null) {
            throw new RuntimeException("cannot layout without timescale");
        }
        
        m_vis.getGroup(m_group);
        
        //int pixel = timeScale.getPixelForDate(time);
    }
    
    public TimeScale getTimeScale() {
        return timeScale;
    }

    public void setTimeScale(TimeScale timeScale) {
        this.timeScale = timeScale;
    }
}
