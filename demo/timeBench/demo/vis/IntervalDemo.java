package timeBench.demo.vis;

import ieg.prefuse.data.DataHelper;
import ieg.prefuse.renderer.IntervalBarRenderer;

import java.awt.Color;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import prefuse.Constants;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.layout.AxisLayout;
import prefuse.controls.ToolTipControl;
import prefuse.render.DefaultRendererFactory;
import prefuse.util.ColorLib;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import timeBench.action.layout.IntervalAxisLayout;
import timeBench.action.layout.TimeAxisLayout;
import timeBench.action.layout.timescale.AdvancedTimeScale;
import timeBench.action.layout.timescale.RangeAdapter;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
import timeBench.util.DebugHelper;
import timeBench.ui.TimeAxisDisplay;

/**
 * Simple demo of an interval plot (or Lifelines plot or timeline plot) showing
 * a interval data over time.
 * 
 * @author Rind
 */
public class IntervalDemo {

    /**
     * additional field to store x coordinate of supremum
     */
    private static final String MAXX_FIELD = VisualItem.X2;

    private static final String GROUP_DATA = "data";

    /**
     * @param args
     * @throws TemporalDataException
     */
    public static void main(String[] args) throws TemporalDataException {
        // java.util.Locale.setDefault(java.util.Locale.US);
        UILib.setPlatformLookAndFeel();

        TemporalDataset tmpds = DebugHelper.generateValidInstants(250);
        DataHelper.printTable(System.out, tmpds.getNodeTable());

        final Visualization vis = new Visualization();
        final TimeAxisDisplay display = new TimeAxisDisplay(vis);
        // display width must be set before the time scale
        // otherwise the initial layout does not match the display width
        display.setSize(700, 450);

        // --------------------------------------------------------------------
        // STEP 1: setup the visualized data & time scale
        VisualGraph vg = vis.addGraph(GROUP_DATA, tmpds);
        vg.getNodeTable().addColumn(MAXX_FIELD, int.class);

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
        IntervalBarRenderer intRenderer = new IntervalBarRenderer(MAXX_FIELD);
        // intRenderer.setAxis(Constants.Y_AXIS);
        DefaultRendererFactory rf = new DefaultRendererFactory(intRenderer);
        vis.setRendererFactory(rf);

        // --------------------------------------------------------------------
        // STEP 3: create actions to process the visual data

        TimeAxisLayout time_axis = new IntervalAxisLayout(GROUP_DATA,
                MAXX_FIELD, timeScale);

        AxisLayout y_axis = new AxisLayout(GROUP_DATA, "caption",
                Constants.Y_AXIS);

        // runs on layout updates (e.g., window resize, pan)
        ActionList update = new ActionList();
        update.add(time_axis);
        update.add(y_axis);
        update.add(new RepaintAction());
        vis.putAction(DemoEnvironmentFactory.ACTION_UPDATE, update);

        // runs once (at startup)
        ActionList draw = new ActionList();
        draw.add(update);
        draw.add(new ColorAction(GROUP_DATA, VisualItem.TEXTCOLOR, ColorLib
                .color(Color.BLACK)));
        draw.add(new ColorAction(GROUP_DATA, VisualItem.FILLCOLOR, ColorLib
                .color(Color.ORANGE)));
        draw.add(new ColorAction(GROUP_DATA, VisualItem.STROKECOLOR, ColorLib
                .color(Color.ORANGE.darker())));
        draw.add(new RepaintAction());
        vis.putAction(DemoEnvironmentFactory.ACTION_INIT, draw);

        // --------------------------------------------------------------------
        // STEP 4: set up a display and controls

        // enable anti-aliasing
        display.setHighQuality(true);

        display.addControlListener(new ToolTipControl("caption"));

        // --------------------------------------------------------------------
        // STEP 5: launching the visualization

        DemoEnvironmentFactory env = new DemoEnvironmentFactory("interval plot");
        env.setPaintWeekends(false);
        env.show(display, rangeAdapter);
    }
}
