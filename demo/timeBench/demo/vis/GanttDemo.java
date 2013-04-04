package timeBench.demo.vis;

import ieg.prefuse.renderer.IntervalBarRenderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import prefuse.Constants;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.layout.AxisLayout;
import prefuse.action.layout.Layout;
import prefuse.controls.ToolTipControl;
import prefuse.data.query.ObjectRangeModel;
import prefuse.data.tuple.TupleSet;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.render.Renderer;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.DataLib;
import prefuse.util.ui.UILib;
import prefuse.visual.DecoratorItem;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import timeBench.action.layout.IntervalAxisLayout;
import timeBench.action.layout.TimeAxisLayout;
import timeBench.action.layout.timescale.AdvancedTimeScale;
import timeBench.action.layout.timescale.RangeAdapter;
import timeBench.calendar.Calendar;
import timeBench.calendar.CalendarManagerFactory;
import timeBench.calendar.CalendarManagers;
import timeBench.calendar.Granularity;
import timeBench.calendar.Granule;
import timeBench.calendar.JavaDateCalendarManager;
import timeBench.data.Interval;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalObject;
import timeBench.demo.vis.DemoEnvironmentFactory;
import timeBench.demo.vis.IntervalDemo;
import timeBench.util.DebugHelper;
import timeBench.ui.TimeAxisDisplay;

/**
 * Intermediate demo of a Gantt chart.
 * <p>
 * This extends {@link IntervalDemo} with the following features:
 * <ul>
 * <li>Scheduling the project plan based on durations and dependencies.
 * <li>Add arrows representing dependencies.
 * <li>Render arrows using with rectangular corners using a custom
 * {@link ShapeRenderer}.
 * <li>Add labels showing the task captions using {@link DecoratorItem}s.
 * <li>Flip the y-axis layout using a subclass of {@link AxisLayout}.
 * </ul>
 * 
 * @author Rind
 */
public class GanttDemo {

    private static final String COL_CAPTION = "caption";

    private static final String GROUP_CAPTIONS = "captions";

    /**
     * additional field to store x coordinate of supremum
     */
    private static final String MAXX_FIELD = VisualItem.X2;

    private static final String GROUP_DATA = "data";

    private static void schedule(TemporalDataset tmpds, long startDate,
            Granularity granularity) throws TemporalDataException {
        // forward planning -- move tasks based on dependencies
        // Assumption: tasks are already ordered
        for (TemporalObject obj : tmpds.temporalObjects()) {
            long earliestStart = startDate;
            for (TemporalObject prev : obj.childObjects()) {
                earliestStart = Math.max(earliestStart, prev
                        .getTemporalElement().getSup() + 1);
            }

            Granule granule = new Granule(earliestStart, earliestStart,
                    granularity);
            ((Interval) obj.getTemporalElement().asPrimitive())
                    .setBegin(granule);
        }
    }

    /**
     * @param args
     * @throws TemporalDataException
     */
    public static void main(String[] args) throws TemporalDataException {
        java.util.Locale.setDefault(java.util.Locale.US);
        UILib.setPlatformLookAndFeel();

        Calendar calendar = CalendarManagerFactory.getSingleton(
                CalendarManagers.JavaDate).getDefaultCalendar();
        Granularity granularity = new Granularity(calendar,
                JavaDateCalendarManager.Granularities.Week.toInt(),
                JavaDateCalendarManager.getSingleton()
                        .getTopGranularityIdentifier());

        TemporalDataset tmpds = DebugHelper
                .generateProjectPlan(20, granularity);
        schedule(tmpds, System.currentTimeMillis(), granularity);
        // DataHelper.printTable(System.out, tmpds.getNodeTable());
        // DataHelper.printTable(System.out, tmpds.getEdgeTable());

        final Visualization vis = new Visualization();
        final TimeAxisDisplay display = new TimeAxisDisplay(vis);
        // display width must be set before the time scale
        // otherwise the initial layout does not match the display width
        display.setSize(900, 650);

        // --------------------------------------------------------------------
        // STEP 1: setup the visualized data & time scale
        VisualGraph vg = vis.addGraph(GROUP_DATA, tmpds);
        vg.getNodeTable().addColumn(MAXX_FIELD, int.class);
        vis.addDecorators(GROUP_CAPTIONS, GROUP_DATA + ".nodes");

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
        IntervalBarRenderer intRenderer = new IntervalBarRenderer(MAXX_FIELD,
                12);
        LabelRenderer labRenderer = new LabelRenderer(COL_CAPTION);
        labRenderer.setHorizontalAlignment(Constants.LEFT);
        Renderer edgeRenderer = new RectangularDependencyEdgeRenderer();

        DefaultRendererFactory rf = new DefaultRendererFactory(intRenderer,
                edgeRenderer);
        rf.add(new InGroupPredicate(GROUP_CAPTIONS), labRenderer);
        vis.setRendererFactory(rf);

        // --------------------------------------------------------------------
        // STEP 3: create actions to process the visual data

        TimeAxisLayout time_axis = new IntervalAxisLayout(GROUP_DATA,
                MAXX_FIELD, timeScale);

        // edges (=dependencies) do not have a caption
        AxisLayout y_axis = new InverseAxisLayout(GROUP_DATA + ".nodes",
                COL_CAPTION, Constants.Y_AXIS);

        // runs on layout updates (e.g., window resize, pan)
        ActionList update = new ActionList();
        update.add(time_axis);
        update.add(y_axis);
        update.add(new DecoratorLayout(GROUP_CAPTIONS));
        update.add(new RepaintAction());
        vis.putAction(DemoEnvironmentFactory.ACTION_UPDATE, update);

        // runs once (at startup)
        ActionList draw = new ActionList();
        draw.add(update);
        // draw.add(new prefuse.action.assignment.FontAction(GROUP_CAPTIONS,
        // prefuse.util.FontLib.getFont("Verdana", 11)));
        draw.add(new ColorAction(GROUP_CAPTIONS, VisualItem.TEXTCOLOR, ColorLib
                .color(Color.BLACK)));
        draw.add(new ColorAction(GROUP_DATA + ".nodes", VisualItem.FILLCOLOR,
                ColorLib.rgb(77, 175, 74))); // green from ColorBrewer Set 1
//                ColorLib.rgb(141, 211, 199))); // green from ColorBrewer Set 3
//                ColorLib.rgb(0, 90, 50))); // proposed by WA
        draw.add(new ColorAction(GROUP_DATA + ".edges", VisualItem.STROKECOLOR,
                ColorLib.color(Color.DARK_GRAY)));
        draw.add(new RepaintAction());
        vis.putAction(DemoEnvironmentFactory.ACTION_INIT, draw);

        // --------------------------------------------------------------------
        // STEP 4: set up a display and controls

        // enable anti-aliasing
        display.setHighQuality(true);

        // ensure there is space on bottom for caption label
        display.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        display.addControlListener(new ToolTipControl(COL_CAPTION));

        // --------------------------------------------------------------------
        // STEP 5: launching the visualization

        DemoEnvironmentFactory env = new DemoEnvironmentFactory("gantt chart");
        env.setPaintWeekends(false);
        System.out.println("--------");
        env.show(display, rangeAdapter);
    }

    /**
     * Set positions of {@link DecoratorItem}s. These items decorate their
     * respective items. The layout simply gets the bounds of the decorated item
     * and assigns the decorator's coordinates to the right of the center of
     * those bounds. (adapted from prefuse.demos.TreeMap by jeffrey heer)
     */
    static class DecoratorLayout extends Layout {
        public DecoratorLayout(String group) {
            super(group);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public void run(double frac) {
            System.out.println("###########");
            Iterator iter = super.m_vis.items(super.m_group);
            while (iter.hasNext()) {
                DecoratorItem item = (DecoratorItem) iter.next();
                VisualItem node = item.getDecoratedItem();
                Rectangle2D bounds = node.getBounds();
                setX(item, null, bounds.getX());
                setY(item, null, bounds.getCenterY() + bounds.getHeight() + 0);
                System.out.println("#" + item.getString(COL_CAPTION));
            }
        }
    } // end of inner class DecoratorLayout

    /**
     * a variant of AxisLayout that lays out ordinal data from top to bottom.
     */
    static class InverseAxisLayout extends AxisLayout {

        public InverseAxisLayout(String group, String field, int axis) {
            super(group, field, axis);
        }

        protected void ordinalLayout(TupleSet ts) {

            if (!m_modelSet) {
                Object[] array = DataLib.ordinalArray(ts, super.getDataField());

                if (m_model == null) {
                    m_model = new ObjectRangeModel(array);
                } else {
                    ((ObjectRangeModel) m_model).setValueRange(array);
                }
            }

            ObjectRangeModel model = (ObjectRangeModel) m_model;
            int start = model.getValue();
            int end = start + model.getExtent();
            double total = (double) (end - start);

            @SuppressWarnings("rawtypes")
            Iterator iter = m_vis.items(m_group, m_filter);
            while (iter.hasNext()) {
                VisualItem item = (VisualItem) iter.next();
                int order = model.getIndex(item.get(super.getDataField()))
                        - start;
                set(item, (total > 0.0) ? 1 - order / total : 0.5);
            }
        }
    }

    /**
     * Renders an edge between two tasks. Based on ActivityEdgeRenderer by
     * PeterW.
     */
    static class RectangularDependencyEdgeRenderer extends
            AbstractShapeRenderer {
        private static final int WIDTH = 10;
        private static final int HEIGHT = 12;

        public void render(Graphics2D g, VisualItem item) {
            Polygon poly = (Polygon) getShape(item);
            g.setStroke(item.getStroke());
            g.setColor(ColorLib.getColor(item.getStrokeColor()));

            g.drawPolyline(poly.xpoints, poly.ypoints, poly.npoints);
            g.fillPolygon(new Arrow(poly.xpoints[0], poly.ypoints[0]));
        }

        protected Shape getRawShape(VisualItem item) {
            Polygon shape = new Polygon();
            EdgeItem edge = (EdgeItem) item;

            Rectangle2D srcBounds = edge.getSourceItem().getBounds();
            Rectangle2D targetBounds = edge.getTargetItem().getBounds();

            addPoint(shape, targetBounds.getX(), targetBounds.getCenterY());
            addPoint(shape, targetBounds.getX() - WIDTH,
                    targetBounds.getCenterY());

            double endX;
            int endWidth;
            endWidth = WIDTH;
            endX = srcBounds.getMaxX();

            if (srcBounds.getMaxX() + WIDTH < targetBounds.getX() - WIDTH) {
                // direct draw possible
                addPoint(shape, targetBounds.getX() - WIDTH,
                        srcBounds.getCenterY());
                addPoint(shape, srcBounds.getMaxX(), srcBounds.getCenterY());
            } else {
                // cannot draw directly
                double y;
                if (srcBounds.getCenterY() < targetBounds.getCenterY()) {
                    y = srcBounds.getMaxY() + HEIGHT;
                } else {
                    y = srcBounds.getY() - HEIGHT;
                }

                addPoint(shape, targetBounds.getX() - WIDTH, y);
                addPoint(shape, endX + endWidth, y);
                addPoint(shape, endX + endWidth, srcBounds.getCenterY());
                addPoint(shape, endX, srcBounds.getCenterY());
            }

            return shape;
        }

        private void addPoint(Polygon shape, double x, double y) {
            shape.addPoint((int) x, (int) y);
        }

        static class Arrow extends Polygon {
            private static final long serialVersionUID = 1L;

            public Arrow(int toX, int toY) {
                addPoint(toX, toY);
                addPoint(toX - 6, toY + 3);
                addPoint(toX - 6, toY - 3);
            }
        }
    }
}
