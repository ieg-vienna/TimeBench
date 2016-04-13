package org.timebench.demo.vis;

import ieg.prefuse.action.LinePlotAction;
import ieg.prefuse.action.layout.LinePlotLayout;
import ieg.prefuse.action.layout.TickAxisLabelLayout;
import ieg.prefuse.data.DataHelper;
import ieg.prefuse.renderer.LineRenderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.GregorianCalendar;

import javax.swing.BorderFactory;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.timebench.action.layout.TimeAxisLayout;
import org.timebench.action.layout.timescale.AdvancedTimeScale;
import org.timebench.action.layout.timescale.RangeAdapter;
import org.timebench.data.AnchoredTemporalElement;
import org.timebench.data.TemporalDataException;
import org.timebench.data.TemporalDataset;
import org.timebench.data.TemporalObject;
import org.timebench.ui.TimeAxisDisplay;
import org.timebench.util.DemoEnvironmentFactory;

import prefuse.Constants;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.ShapeAction;
import prefuse.action.assignment.StrokeAction;
import prefuse.action.layout.AxisLabelLayout;
import prefuse.action.layout.AxisLayout;
import prefuse.controls.HoverActionControl;
import prefuse.controls.ToolTipControl;
import prefuse.data.Schema;
import prefuse.data.Tuple;
import prefuse.data.expression.AbstractExpression;
import prefuse.data.expression.ColumnExpression;
import prefuse.data.expression.Expression;
import prefuse.data.query.NumberRangeModel;
import prefuse.render.AxisRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.expression.VisiblePredicate;
import prefuse.visual.sort.ItemSorter;

/**
 * Intermediate demo of a line plot showing a numerical variable over time.
 * <p>
 * This extends {@link DotPlotDemo} with the following features:
 * <ul>
 * <li>Add line segments between dots using {@link LinePlotAction}.
 * <li>Show more readable tooltips using
 * {@link VisualTable#addColumn(String, Expression)} and custom code in
 * {@link LabelExpression}.
 * <li>Change color of hovered dots using {@link HoverActionControl}.
 * <li>Use {@link TickAxisLabelLayout} to avoid horizontal grid lines.
 * <li>Always show the same value range 0..100 on the y axis. 
 * </ul>
 * 
 * @author Rind
 */
public class LinePlotDemo {

    private static final String COL_DATA = "value";
    private static final String COL_LABEL = "label";

    private static final String GROUP_DATA = "data";
    private static final String GROUP_AXIS_LABELS = "ylab";
    private static final String GROUP_LINES = "lines";

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
        VisualTable vt = vis.addTable(GROUP_DATA,
                tmpds.getTemporalObjectTable());
        vt.addColumn(COL_LABEL, new LabelExpression());

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
        rf.add(new InGroupPredicate(GROUP_LINES), new LineRenderer());
        rf.add(new InGroupPredicate(GROUP_AXIS_LABELS), new AxisRenderer(
                Constants.FAR_LEFT, Constants.CENTER));
        vis.setRendererFactory(rf);

        // --------------------------------------------------------------------
        // STEP 3: create actions to process the visual data

        TimeAxisLayout time_axis = new TimeAxisLayout(GROUP_DATA, timeScale);

        AxisLayout y_axis = new AxisLayout(GROUP_DATA, COL_DATA,
                Constants.Y_AXIS, VisiblePredicate.TRUE);
        // set visible value range to 0..100
        y_axis.setRangeModel(new NumberRangeModel(0.0d, 100d, 0d, 100d));

        // add value axis labels and horizontal grid lines
        AxisLabelLayout y_labels = new TickAxisLabelLayout(GROUP_AXIS_LABELS,
                y_axis, 5);

        // lineCreation add lines between all items in the group
        Action lineCreation = new LinePlotAction(GROUP_LINES, GROUP_DATA,
                VisualItem.X);
        // lineLayout updates x and y coordinates of lines
        LinePlotLayout lineLayout = new LinePlotLayout(GROUP_LINES);

        // visual attributes of line segments (can be created directly in add())
        Action lineColor = new ColorAction(GROUP_LINES, VisualItem.STROKECOLOR,
                ColorLib.color(Color.GRAY));
        Action lineStroke = new StrokeAction(GROUP_LINES, new BasicStroke(3f));

        // color must be set -> otherwise nothing displayed
        ColorAction color = new ColorAction(GROUP_DATA, VisualItem.FILLCOLOR,
                ColorLib.rgb(100, 100, 255));
        color.add(new ColumnExpression(VisualItem.HOVER),
                ColorLib.rgb(255, 100, 255));

        ShapeAction shape = new ShapeAction(GROUP_DATA, Constants.SHAPE_ELLIPSE);

        // runs on layout updates (e.g., window resize, pan)
        ActionList update = new ActionList();
        update.add(time_axis);
        update.add(y_axis);
        update.add(y_labels);
        update.add(lineLayout);
        update.add(color);
        update.add(new RepaintAction());
        vis.putAction(DemoEnvironmentFactory.ACTION_UPDATE, update);

        // runs once (at startup)
        ActionList draw = new ActionList();
        draw.add(lineCreation);
        draw.add(update);
        draw.add(shape);
        draw.add(lineColor);
        draw.add(lineStroke);
        draw.add(new RepaintAction());
        vis.putAction(DemoEnvironmentFactory.ACTION_INIT, draw);

        // --------------------------------------------------------------------
        // STEP 4: set up a display and controls

        // enable anti-aliasing
        display.setHighQuality(true);

        // ensure there is space on left for tick mark label (FAR_LEFT setting)
        display.setBorder(BorderFactory.createEmptyBorder(7, 20, 7, 0));

        // ensure (horizontal) grid lines are in back of data items
        display.setItemSorter(new ItemSorter() {
            public int score(VisualItem item) {
                int score = super.score(item);
                if (GROUP_LINES.equals(item.getGroup()))
                    score -= 1;
                if (item.isInGroup(GROUP_AXIS_LABELS))
                    score -= 2;
                return score;
            }
        });

        // show value in tooltip
        display.addControlListener(new ToolTipControl(COL_LABEL));
        display.addControlListener(new prefuse.controls.HoverActionControl(
                DemoEnvironmentFactory.ACTION_UPDATE));

        // --------------------------------------------------------------------
        // STEP 5: launching the visualization

        DemoEnvironmentFactory env = new DemoEnvironmentFactory("line plot");
        env.setPaintWeekends(false);
        env.show(display, rangeAdapter);
    }

    static class LabelExpression extends AbstractExpression {

        @Override
        public Class<String> getType(Schema s) {
            return String.class;
        }

        @Override
        public Object get(Tuple t) {
            double value = t.getDouble(COL_DATA);

            long date = ((AnchoredTemporalElement) t
                    .get(TemporalObject.TEMPORAL_ELEMENT)).getInf();
            GregorianCalendar cal = new GregorianCalendar(
                    java.util.TimeZone.getTimeZone("UTC"));
            cal.setTimeInMillis(date);

            return String.format("%tF %<tR %6.2f", cal, value);
        }
    }
}
