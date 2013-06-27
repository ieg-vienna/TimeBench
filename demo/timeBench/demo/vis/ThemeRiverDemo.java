package timeBench.demo.vis;

import ieg.prefuse.action.layout.CategoryLinePlotAction;
import ieg.prefuse.action.layout.LinePlotLayout;
import ieg.prefuse.action.layout.TickAxisLabelLayout;
import ieg.prefuse.data.DataHelper;
import ieg.prefuse.renderer.LineRenderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.bind.JAXBException;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.assignment.ShapeAction;
import prefuse.action.assignment.StrokeAction;
import prefuse.action.layout.AxisLabelLayout;
import prefuse.action.layout.AxisLayout;
import prefuse.controls.ControlAdapter;
import prefuse.controls.ToolTipControl;
import prefuse.data.Schema;
import prefuse.data.Tuple;
import prefuse.data.expression.AbstractExpression;
import prefuse.data.expression.ColumnExpression;
import prefuse.data.io.DataIOException;
import prefuse.data.query.NumberRangeModel;
import prefuse.render.AxisRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.PolygonRenderer;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.expression.VisiblePredicate;
import prefuse.visual.sort.ItemSorter;
import timeBench.action.analytical.ColumnToRowsTemporalDataTransformation;
import timeBench.action.analytical.InterpolationIndexingAction;
import timeBench.action.analytical.TemporalDataIndexingAction;
import timeBench.action.layout.ThemeRiverLayout;
import timeBench.action.layout.TimeAxisLayout;
import timeBench.action.layout.timescale.AdvancedTimeScale;
import timeBench.action.layout.timescale.RangeAdapter;
import timeBench.action.layout.timescale.TimeScale;
import timeBench.calendar.Calendar;
import timeBench.calendar.CalendarManagerFactory;
import timeBench.calendar.CalendarManagers;
import timeBench.calendar.Granularity;
import timeBench.calendar.JavaDateCalendarManager;
import timeBench.data.AnchoredTemporalElement;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalObject;
import timeBench.data.io.TextTableTemporalDatasetReader;
import timeBench.ui.TimeAxisDisplay;
import timeBench.util.DebugHelper;
import timeBench.util.DemoEnvironmentFactory;

/**
 * Intermediate demo of a line plot showing multiple numerical variables over
 * time. The demo works with a data file containing weekly mortality counts from
 * respiratory diseases in multiple cities. The data has 2 references (time and
 * city) and one characteristic (mortality count).
 * <p>
 * This extends {@link MultipleLinePlotDemo} with the following features:
 * <ul>
 * <li>Transforming the abstract data from a pivot table to key value pairs,
 * retaining references to a shared temporal element 
 * ({@link ColumnToRowsTemporalDataTransformation}).
 * <li>Calculating indexed values in a new column of the visual table 
 * ({@link TemporalDataIndexingAction}).
 * <li>Updating the indexing point using an interactive control 
 * ({@link IndexingControl}).
 * </ul>
 * 
 * @author Rind
 */
public class ThemeRiverDemo {

    private static final String FILE_DATA = "data/nmmaps-resp-20monthly-matrix.csv";
    private static final int GRANULARITY_ID = JavaDateCalendarManager.Granularities.Month.toInt();

    private static final String COL_DATA = "value";
    private static final String COL_CITY = "category";
    private static final String COL_LABEL = "label";
    private static final String COL_INDEXED = "indexed";

    private static final String GROUP_DATA = "data";
    private static final String GROUP_AXIS_LABELS = "ylab";
    private static final String GROUP_LINES = "lines";

    /**
     * @param args
     * @throws TemporalDataException
     * @throws JAXBException
     * @throws IOException
     * @throws DataIOException
     */
    public static void main(String[] args) throws TemporalDataException,
            IOException, JAXBException, DataIOException {
        // java.util.Locale.setDefault(java.util.Locale.US);
        Calendar calendar = CalendarManagerFactory.getSingleton(
                CalendarManagers.JavaDate).getDefaultCalendar();
        TextTableTemporalDatasetReader reader = new TextTableTemporalDatasetReader(
                new Granularity(calendar, GRANULARITY_ID,
                        JavaDateCalendarManager.Granularities.Top.toInt()));
        TemporalDataset tmpds = reader.readData(FILE_DATA);

        DebugHelper.printTemporalDatasetTable(System.out, tmpds);
        
        Hashtable<String,Integer> classes = new Hashtable<String, Integer>();
        int[] indices = tmpds.getDataColumnIndices(); 
        for (int i=0; i<indices.length; i++) {
        	classes.put(tmpds.getNodeTable().getSchema().getColumnName(indices[i]),i);
        }
        
        final Visualization vis = new Visualization();
        final TimeAxisDisplay display = new TimeAxisDisplay(vis);
        // display width must be set before the time scale
        // otherwise the initial layout does not match the display width
        display.setSize(700, 450);

        // --------------------------------------------------------------------
        // STEP 1: setup the visualized data & time scale
//        VisualTable vt = vis.addTable(GROUP_DATA,
//                tmpds.getTemporalObjectTable());
//        vt.addColumn(COL_LABEL, new LabelExpression());
//        // add a column that will store indexed values
//        vt.addColumn(COL_INDEXED, double.class);

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
        PolygonRenderer polygonRenderer = new PolygonRenderer();
        DefaultRendererFactory rf = new DefaultRendererFactory(polygonRenderer);
        vis.setRendererFactory(rf);

        // --------------------------------------------------------------------
        // STEP 3: create actions to process the visual data

        InterpolationIndexingAction indexing = new InterpolationIndexingAction(GROUP_DATA, COL_DATA, COL_INDEXED, COL_CITY);
        indexing.setIndexTime(tmpds.getInf());

        ThemeRiverLayout themeRiver = new ThemeRiverLayout(GROUP_DATA, tmpds, classes, timeScale);
        
        DataColorAction fill = new DataColorAction(GROUP_DATA, "class", prefuse.Constants.ORDINAL,
        		VisualItem.FILLCOLOR,DemoEnvironmentFactory.set12Qualitative);       
        ColorAction stroke = new ColorAction(GROUP_DATA, VisualItem.STROKECOLOR,ColorLib.color(Color.WHITE));

        ShapeAction shape = new ShapeAction(GROUP_DATA, Constants.SHAPE_ELLIPSE);

        // runs on layout updates (e.g., window resize, pan)
        ActionList update = new ActionList();
        update.add(themeRiver);
        update.add(new RepaintAction());
        vis.putAction(DemoEnvironmentFactory.ACTION_UPDATE, update);

        // runs once (at startup)
        ActionList draw = new ActionList();
        draw.add(update);
        draw.add(fill);
        draw.add(stroke);
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
        display.addControlListener(new IndexingControl(indexing, timeScale));

        // --------------------------------------------------------------------
        // STEP 5: launching the visualization

        DemoEnvironmentFactory env = new DemoEnvironmentFactory(
                "multiple line plot");
        env.setPaintWeekends(false);
        env.show(display, rangeAdapter);
    }

    public static int[] setAlpha(int[] c, int alpha) {
        int[] result = new int[c.length];
        for (int i = 0; i < c.length; i++) {
            result[i] = ColorLib.setAlpha(c[i], alpha);
        }
        return result;
    }

    static class LabelExpression extends AbstractExpression {

        @Override
        public Class<String> getType(Schema s) {
            return String.class;
        }

        @Override
        public Object get(Tuple t) {
            double value = t.getDouble(COL_DATA);

            long date = ((AnchoredTemporalElement)((TemporalObject) ((VisualItem) t).getSourceTuple())
                    .getTemporalElement()).getInf();
            GregorianCalendar cal = new GregorianCalendar(
                    java.util.TimeZone.getTimeZone("UTC"));
            cal.setTimeInMillis(date);

            return String.format("%tF %s: %3.0f", cal, t.getString(COL_CITY),
                    value);
        }
    }
    
    static class IndexingControl extends ControlAdapter {

        protected InterpolationIndexingAction action;
        protected TimeScale timeScale;

        public IndexingControl(InterpolationIndexingAction action,
                TimeScale timeScale) {
            this.action = action;
            this.timeScale = timeScale;
        }

        @Override
        public void itemClicked(VisualItem item, MouseEvent e) {
            this.mouseClicked(e);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            long time = timeScale.getDateAtPixel(e.getX());
            // System.out.println("item " + e.getX() + " " + time);
            action.setIndexTime(time);
            ((Display) e.getComponent()).getVisualization().run(
                    DemoEnvironmentFactory.ACTION_UPDATE);
        }

    }
}
