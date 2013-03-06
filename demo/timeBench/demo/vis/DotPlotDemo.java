package timeBench.demo.vis;

import ieg.prefuse.data.DataHelper;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import prefuse.Constants;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.ShapeAction;
import prefuse.action.layout.AxisLayout;
import prefuse.controls.ToolTipControl;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.VisiblePredicate;
import timeBench.action.layout.TimeAxisLayout;
import timeBench.action.layout.timescale.AdvancedTimeScale;
import timeBench.action.layout.timescale.RangeAdapter;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
import timeBench.ui.TimeAxisDisplay;

public class DotPlotDemo {

    private static final String GROUP = "data";
    private static final String DATA_COL = "value";

    /**
     * @param args
     * @throws TemporalDataException
     */
    public static void main(String[] args) throws TemporalDataException {
        TemporalDataset tmpds = DemoEnvironmentFactory
                .generateRandomNumericalInstantData(100, DATA_COL);
        DataHelper.printTable(System.out, tmpds.getNodeTable());
//        tmpds.setRoots(new long[] {0, 1, 2});
//        timeBench.test.DebugHelper.printTemporalDatasetForest(System.out, tmpds);

        final Visualization vis = new Visualization();
        final TimeAxisDisplay display = new TimeAxisDisplay(vis);
        // display width must be set before the time scale
        // otherwise the initial layout does not match the display width 
        display.setSize(700, 450);

        // --------------------------------------------------------------------
        // STEP 1: setup the visualized data & time scale
        vis.addTable(GROUP, tmpds.getTemporalObjectTable());

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
        vis.setRendererFactory(rf);

        // --------------------------------------------------------------------
        // STEP 3: create actions to process the visual data

        TimeAxisLayout time_axis = new TimeAxisLayout(GROUP, timeScale);

        AxisLayout y_axis = new AxisLayout(GROUP, DATA_COL, Constants.Y_AXIS,
                VisiblePredicate.TRUE);

        // TODO Axis labels missing
        // y_axis.setLayoutBounds(boundsData);
        // AxisLabelLayout y_labels = new AxisLabelLayout("ylab", y_axis,
        // boundsLabelsY);

        ColorAction color = new ColorAction(GROUP, VisualItem.FILLCOLOR,
                ColorLib.rgb(100, 100, 255));

        ShapeAction shape = new ShapeAction(GROUP, Constants.SHAPE_ELLIPSE);

        ActionList update = new ActionList();
        update.add(time_axis);
        update.add(y_axis);
        // update.add(y_labels);
        update.add(new RepaintAction());
        vis.putAction(DemoEnvironmentFactory.ACTION_UPDATE, update);

        ActionList draw = new ActionList();
        draw.add(update);
        draw.add(color);
        draw.add(shape);
        draw.add(new RepaintAction());
        vis.putAction(DemoEnvironmentFactory.ACTION_INIT, draw);

        // --------------------------------------------------------------------
        // STEP 4: set up a display and controls

        display.setHighQuality(true);

        // optional ItemSorter

        display.addControlListener(new ToolTipControl(DATA_COL));

        // --------------------------------------------------------------------
        // STEP 5: launching the visualization

        DemoEnvironmentFactory env = new DemoEnvironmentFactory("dot plot");
        env.setPaintWeekends(false);
        env.show(display, rangeAdapter);
    }
}
