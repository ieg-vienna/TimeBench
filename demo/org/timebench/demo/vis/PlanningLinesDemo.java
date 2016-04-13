package org.timebench.demo.vis;

import ieg.prefuse.renderer.ExtendedShapeRenderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.timebench.action.layout.IntervalAxisLayout;
import org.timebench.action.layout.TimeAxisLayout;
import org.timebench.action.layout.timescale.AdvancedTimeScale;
import org.timebench.action.layout.timescale.RangeAdapter;
import org.timebench.action.layout.timescale.TimeScale;
import org.timebench.calendar.CalendarFactory;
import org.timebench.calendar.Granularity;
import org.timebench.calendar.Granule;
import org.timebench.calendar.JavaDateCalendarManager;
import org.timebench.data.GenericTemporalElement;
import org.timebench.data.Interval;
import org.timebench.data.TemporalDataException;
import org.timebench.data.TemporalDataset;
import org.timebench.data.TemporalObject;
import org.timebench.ui.TimeAxisDisplay;
import org.timebench.util.DebugHelper;
import org.timebench.util.DemoEnvironmentFactory;

import prefuse.Constants;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.ShapeAction;
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
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.sort.ItemSorter;

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
public class PlanningLinesDemo {

    private static final String COL_CAPTION = "caption";

    private static final String GROUP_CAPTIONS = "captions";

    /**
     * additional field to store x coordinate of supremum
     */
    private static final String GROUP_DATA_LEFT = "data_left";
    private static final String GROUP_DATA_RIGHT = "data_right";
    private static final String GROUP_DATA_MINDURATION = "data_minduration";
    private static final String GROUP_DATA_MAXDURATION = "data_maxduration";
    
    private static final Granularity GRANULARITY = CalendarFactory.getSingleton().getGranularity(
    		JavaDateCalendarManager.getSingleton().getDefaultCalendar(),"Week","Top");

    @SuppressWarnings("unused")
    private static void schedule(TemporalDataset tmpds, long startDate,
            Granularity granularity) throws TemporalDataException {
        // forward planning -- move tasks based on dependencies
        // Assumption: tasks are already ordered
        for (TemporalObject obj : tmpds.temporalObjects()) {
            long earliestStart = startDate;
            long latestStart = startDate;
            for (TemporalObject prev : obj.childObjects()) {
                earliestStart = Math.max(earliestStart, 
                		((Interval)(prev.getTemporalElement().getLastChildPrimitive())).getInf());
                latestStart = Math.max(latestStart, 
                		((Interval)(prev.getTemporalElement().getLastChildPrimitive())).getSup());
            }
            earliestStart++; earliestStart++;
            latestStart++;
            
            long minLength = ((Interval)obj.getTemporalElement().getLastChildPrimitive()).getInf() -
            		((Interval)obj.getTemporalElement().getFirstChildPrimitive()).getSup() + 1;
            long maxLength = ((Interval)obj.getTemporalElement().getLastChildPrimitive()).getSup() -
            		((Interval)obj.getTemporalElement().getFirstChildPrimitive()).getInf() + 1;

            long oldDiff = ((Interval)obj.getTemporalElement().getLastChildPrimitive()).getInf() -
            		((Interval)obj.getTemporalElement().getFirstChildPrimitive()).getInf();
            
            Granule granule = new Granule(earliestStart, earliestStart,
                    granularity);
            ((Interval) obj.getTemporalElement().getFirstChildPrimitive())	// Set first interval by granule
                    .setBegin(granule);
            obj.getTemporalElement().asGeneric().setInf( ((Interval) obj.getTemporalElement().getFirstChildPrimitive()).getInf()); // Set parent by first child

            granule = new Granule(earliestStart+oldDiff, earliestStart+oldDiff,
                    granularity);
            ((Interval) obj.getTemporalElement().getLastChildPrimitive())	// Set last interval by whatever
                    .setBegin(granule);
            obj.getTemporalElement().asGeneric().setSup( ((Interval) obj.getTemporalElement().getLastChildPrimitive()).getSup()); // Set parent by last child
        }
    }

    /**
     * @param args
     * @throws TemporalDataException
     */
    public static void main(String[] args) throws TemporalDataException {
        java.util.Locale.setDefault(java.util.Locale.US);
        UILib.setPlatformLookAndFeel();               

        TemporalDataset tmpds = DebugHelper
                .generateIndeterminateProjectPlan(20, GRANULARITY);
        schedule(tmpds, System.currentTimeMillis(), GRANULARITY);
        // DataHelper.printTable(System.out, tmpds.getNodeTable());
        // DataHelper.printTable(System.out, tmpds.getEdgeTable());

        final Visualization vis = new Visualization();
        final TimeAxisDisplay display = new TimeAxisDisplay(vis);
        // display width must be set before the time scale
        // otherwise the initial layout does not match the display width
        display.setSize(950, 650);

        // --------------------------------------------------------------------
        // STEP 1: setup the visualized data & time scale
        vis.addGraph(GROUP_DATA_MINDURATION, tmpds);
        vis.addDecorators(GROUP_DATA_MAXDURATION, GROUP_DATA_MINDURATION + ".nodes");
        vis.addDecorators(GROUP_DATA_LEFT, GROUP_DATA_MINDURATION + ".nodes");
        vis.addDecorators(GROUP_DATA_RIGHT, GROUP_DATA_MINDURATION + ".nodes");
        vis.addDecorators(GROUP_CAPTIONS, GROUP_DATA_LEFT);

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
        ExtendedShapeRenderer rendererLeft = new ExtendedShapeRenderer(1);
        LabelRenderer labRenderer = new LabelRenderer(COL_CAPTION);
        labRenderer.setHorizontalAlignment(Constants.LEFT);
        Renderer edgeRenderer = new RectangularDependencyEdgeRenderer();

        DefaultRendererFactory rf = new DefaultRendererFactory(rendererLeft,
                edgeRenderer);
        //display.setPredicate(new InGroupPredicate(GROUP_DATA_LEFT+".nodes"));
        rf.add(new InGroupPredicate(GROUP_CAPTIONS), labRenderer);
        vis.setRendererFactory(rf);

        // --------------------------------------------------------------------
        // STEP 3: create actions to process the visual data

        TimeAxisLayout time_axis_left = new IntervalAxisLayout(GROUP_DATA_LEFT,
                timeScale, new int[] {0});
        TimeAxisLayout time_axis_max = new IntervalAxisLayout(GROUP_DATA_MAXDURATION,
                timeScale,new int[] {1});
        TimeAxisLayout time_axis_min = new IntervalAxisLayout(GROUP_DATA_MINDURATION,
                timeScale,new int[] {2});
        TimeAxisLayout time_axis_right = new IntervalAxisLayout(GROUP_DATA_RIGHT,
                timeScale,new int[] {3});

        // edges (=dependencies) do not have a caption
        AxisLayout y_axis_left = new InverseAxisLayout(GROUP_DATA_LEFT,
                COL_CAPTION, Constants.Y_AXIS, 13.0);
        AxisLayout y_axis_right = new InverseAxisLayout(GROUP_DATA_RIGHT,
                COL_CAPTION, Constants.Y_AXIS, 13.0);
        AxisLayout y_axis_max = new InverseAxisLayout(GROUP_DATA_MAXDURATION,
                COL_CAPTION, Constants.Y_AXIS, 10.0);
        AxisLayout y_axis_min = new InverseAxisLayout(GROUP_DATA_MINDURATION + ".nodes",
                COL_CAPTION, Constants.Y_AXIS, 10.0);

        // runs on layout updates (e.g., window resize, pan)
        ActionList update = new ActionList();
        update.add(time_axis_left);
        update.add(y_axis_left);
        update.add(time_axis_right);
        update.add(y_axis_right);
        update.add(time_axis_max);
        update.add(y_axis_max);
        update.add(time_axis_min);
        update.add(y_axis_min);
        update.add(new DecoratorLayout(GROUP_CAPTIONS));
        update.add(new RepaintAction());
        vis.putAction(DemoEnvironmentFactory.ACTION_UPDATE, update);

        // runs once (at startup)
        ActionList draw = new ActionList();
        draw.add(update);
        // draw.add(new prefuse.action.assignment.FontAction(GROUP_CAPTIONS,
        // prefuse.util.FontLib.getFont("Verdana", 11)));
        draw.add(new ShapeAction(GROUP_DATA_LEFT, ExtendedShapeRenderer.SHAPE_LEFT_BRACKET));
        draw.add(new ShapeAction(GROUP_DATA_RIGHT, ExtendedShapeRenderer.SHAPE_RIGHT_BRACKET));
        draw.add(new ShapeAction(GROUP_DATA_MAXDURATION, Constants.SHAPE_RECTANGLE));
        draw.add(new ShapeAction(GROUP_DATA_MINDURATION + ".nodes", Constants.SHAPE_RECTANGLE));
        draw.add(new ColorAction(GROUP_CAPTIONS, VisualItem.TEXTCOLOR, ColorLib
                .color(Color.BLACK)));
        draw.add(new ColorAction(GROUP_DATA_LEFT, VisualItem.STROKECOLOR,
                ColorLib.rgb(0, 0, 0)));
        draw.add(new ColorAction(GROUP_DATA_RIGHT, VisualItem.STROKECOLOR,
                ColorLib.rgb(0, 0, 0)));
        draw.add(new ColorAction(GROUP_DATA_MAXDURATION, VisualItem.FILLCOLOR,
        		DemoEnvironmentFactory.set3Qualitative[1]));
        draw.add(new ColorAction(GROUP_DATA_MINDURATION + ".nodes", VisualItem.FILLCOLOR,
        		DemoEnvironmentFactory.set3Qualitative[5]));
        draw.add(new ColorAction(GROUP_DATA_MINDURATION + ".edges", VisualItem.STROKECOLOR,
                ColorLib.rgb(127,127,127)));
        draw.add(new RepaintAction());
        vis.putAction(DemoEnvironmentFactory.ACTION_INIT, draw);

        // --------------------------------------------------------------------
        // STEP 4: set up a display and controls

        // enable anti-aliasing
        display.setHighQuality(true);

        // ensure there is space on bottom for caption label
        display.setBorder(BorderFactory.createEmptyBorder(12, 0, 22, 0));

        display.setItemSorter(new ItemSorter() {
            public int score(VisualItem item) {
                int score = super.score(item);
                if (item.isInGroup(GROUP_DATA_MAXDURATION))
                    score--;
                if (item.isInGroup(GROUP_DATA_MINDURATION))
                    score = Integer.MAX_VALUE - 1;
                if (item.isInGroup(GROUP_DATA_MINDURATION+".edges"))
                    score=Integer.MAX_VALUE;
                return score;
            }
        });
        
        display.addControlListener(new ToolTipControl(COL_CAPTION));

        // --------------------------------------------------------------------
        // STEP 5: launching the visualization

        DemoEnvironmentFactory env = new DemoEnvironmentFactory("Planning Lines");
        env.setPaintWeekends(false);
//        System.out.println("--------");
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
//            System.out.println("###########");
            Iterator iter = super.m_vis.items(super.m_group);
            while (iter.hasNext()) {
                DecoratorItem item = (DecoratorItem) iter.next();
                VisualItem node = item.getDecoratedItem();
                Rectangle2D bounds = node.getBounds();
                setX(item, null, bounds.getX());
                setY(item, null, bounds.getCenterY() + bounds.getHeight() + 0);
//                System.out.println("#" + item.getString(COL_CAPTION));
            }
        }
    } // end of inner class DecoratorLayout

    static class MaxIntervalAxisLayout extends IntervalAxisLayout {

    	private boolean isMin = false;
    	
    	public MaxIntervalAxisLayout(String group,TimeScale timeScale,boolean isMin) {
    		super(group, timeScale);
    		this.isMin = isMin;
    	}    	
    	
    	protected void layoutItem(VisualItem vi) {		
            GenericTemporalElement te = ((TemporalObject) vi.getSourceTuple())
                    .getTemporalElement().asGeneric();           
            
            long inf =
            		((Interval)te.getFirstChildPrimitive()).getInf() +
            		(long)(Math.round(Math.random()*(double)
            				(((Interval)te.getFirstChildPrimitive()).getSup() - ((Interval)te.getFirstChildPrimitive()).getInf() + 1))); 
            long sup =
            		((Interval)te.getLastChildPrimitive()).getInf() +
            		(long)(Math.round(Math.random()*(double)
            				(((Interval)te.getLastChildPrimitive()).getSup() - ((Interval)te.getLastChildPrimitive()).getInf() + 1))); 

            if(isMin) {
            	inf += (long)Math.round(Math.random()*0.2*((double)(sup-inf+1)));
            	sup -= (long)Math.round(Math.random()*0.8*((double)(sup-inf+1)));
            }
            
            int pixelInf = timeScale.getPixelForDate(inf);
            int pixelSup = timeScale.getPixelForDate(sup);
            double pixelWidth = (double)pixelSup-(double)pixelInf+1.0;
            double pixelMed = (double)pixelInf+pixelWidth/2.0;

            if (super.getAxis() == Constants.X_AXIS) {
                vi.setX(pixelMed);
                vi.setSizeX(pixelWidth);
            } else {
            	// TODO test y axis layout direction 
                vi.setY(pixelMed);
                vi.setSizeY(pixelWidth);
            }
    	}
    }
    
    /**
     * a variant of AxisLayout that lays out ordinal data from top to bottom.
     */
    static class InverseAxisLayout extends AxisLayout {

    	double m_size = 12.0;
    	
    	public InverseAxisLayout(String group, String field, int axis, double size) {
            super(group, field, axis);
            m_size = size;
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
                item.setSizeY(m_size);
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
