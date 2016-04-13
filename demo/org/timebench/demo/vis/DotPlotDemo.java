package org.timebench.demo.vis;

import ieg.prefuse.data.DataHelper;

import javax.swing.BorderFactory;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.timebench.action.layout.TimeAxisLayout;
import org.timebench.action.layout.timescale.AdvancedTimeScale;
import org.timebench.action.layout.timescale.RangeAdapter;
import org.timebench.data.TemporalDataException;
import org.timebench.data.TemporalDataset;
import org.timebench.ui.TimeAxisDisplay;
import org.timebench.util.DemoEnvironmentFactory;

import prefuse.Constants;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.ShapeAction;
import prefuse.action.layout.AxisLabelLayout;
import prefuse.action.layout.AxisLayout;
import prefuse.controls.ToolTipControl;
import prefuse.render.AxisRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.expression.VisiblePredicate;
import prefuse.visual.sort.ItemSorter;

/**
 * Simple demo of a dot plot showing a numerical variable over time. 
 * @author Rind
 */
public class DotPlotDemo {

    private static final String COL_DATA = "value";

    private static final String GROUP_DATA = "data";
    private static final String GROUP_AXIS_LABELS = "ylab";
    
    /**
     * @param args
     * @throws TemporalDataException
     */
    public static void main(String[] args) throws TemporalDataException {
//        java.util.Locale.setDefault(java.util.Locale.US);
        TemporalDataset tmpds = DemoEnvironmentFactory
                .generateRandomNumericalInstantData(100, COL_DATA);
        DataHelper.printTable(System.out, tmpds.getNodeTable());

        final Visualization vis = new Visualization();
        final TimeAxisDisplay display = new TimeAxisDisplay(vis);
        // display width must be set before the time scale
        // otherwise the initial layout does not match the display width
        display.setSize(700, 450);

        // --------------------------------------------------------------------
        // STEP 1: setup the visualized data & time scale
        vis.addTable(GROUP_DATA, tmpds.getTemporalObjectTable());

        long border = (tmpds.getSup() - tmpds.getInf()) / 20;
        final AdvancedTimeScale timeScale = new AdvancedTimeScale(
                tmpds.getInf() - border, tmpds.getSup() + border,
                display.getWidth() - 1);
        AdvancedTimeScale overviewTimeScale = new AdvancedTimeScale(timeScale);
        RangeAdapter rangeAdapter = new RangeAdapter(overviewTimeScale,
                timeScale);

        timeScale.setAdjustDateRangeOnResize(true);
        timeScale.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                vis.run(DemoEnvironmentFactory.ACTION_UPDATE);
            }
        });

        // --------------------------------------------------------------------
        // STEP 2: set up renderers for the visual data
        ShapeRenderer dotRenderer = new ShapeRenderer(8);
        DefaultRendererFactory rf = new DefaultRendererFactory(dotRenderer);
        rf.add(new InGroupPredicate(GROUP_AXIS_LABELS), new AxisRenderer(
                Constants.FAR_LEFT, Constants.CENTER));
        vis.setRendererFactory(rf);

        // --------------------------------------------------------------------
        // STEP 3: create actions to process the visual data

        TimeAxisLayout time_axis = new TimeAxisLayout(GROUP_DATA, timeScale);

        AxisLayout y_axis = new AxisLayout(GROUP_DATA, COL_DATA, Constants.Y_AXIS,
                VisiblePredicate.TRUE);

        // add value axis labels and horizontal grid lines 
        AxisLabelLayout y_labels = new AxisLabelLayout(GROUP_AXIS_LABELS, y_axis);

        // color must be set -> otherwise nothing displayed
        ColorAction color = new ColorAction(GROUP_DATA, VisualItem.FILLCOLOR,
                ColorLib.rgb(100, 100, 255));

        ShapeAction shape = new ShapeAction(GROUP_DATA, Constants.SHAPE_ELLIPSE);

        // runs on layout updates (e.g., window resize, pan)
        ActionList update = new ActionList();
        update.add(time_axis);
        update.add(y_axis);
        update.add(y_labels);
        update.add(new RepaintAction());
        vis.putAction(DemoEnvironmentFactory.ACTION_UPDATE, update);

        // runs once (at startup)
        ActionList draw = new ActionList();
        draw.add(update);
        draw.add(color);
        draw.add(shape);
        draw.add(new RepaintAction());
        vis.putAction(DemoEnvironmentFactory.ACTION_INIT, draw);

        // --------------------------------------------------------------------
        // STEP 4: set up a display and controls

        // enable anti-aliasing
        display.setHighQuality(true);
        
        // ensure there is space on left for tick mark label (FAR_LEFT setting)
        display.setBorder(BorderFactory.createEmptyBorder(7, 25, 7, 0));

        // ensure (horizontal) grid lines are in back of data items 
        display.setItemSorter(new ItemSorter() {
            public int score(VisualItem item) {
                int score = super.score(item);
                if (item.isInGroup(GROUP_AXIS_LABELS))
                    score--;
                return score;
            }
        });

        // show value in tooltip 
        display.addControlListener(new ToolTipControl(COL_DATA));

        // --------------------------------------------------------------------
        // STEP 5: launching the visualization

        DemoEnvironmentFactory env = new DemoEnvironmentFactory("dot plot");
        env.setPaintWeekends(false);
        env.show(display, rangeAdapter);
    }
}
