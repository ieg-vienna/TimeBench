package timeBench.demo.vis;

import ieg.prefuse.action.layout.CategoryLinePlotAction;
import ieg.prefuse.action.layout.LinePlotLayout;
import ieg.prefuse.action.layout.TickAxisLabelLayout;
import ieg.prefuse.data.DataHelper;
import ieg.prefuse.renderer.LineRenderer;

import java.awt.BasicStroke;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.GregorianCalendar;

import javax.swing.BorderFactory;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.bind.JAXBException;

import prefuse.Constants;
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
import prefuse.render.AxisRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.expression.VisiblePredicate;
import prefuse.visual.sort.ItemSorter;
import timeBench.action.analytical.ColumnToRowsTemporalDataTransformation;
import timeBench.action.analytical.IndexingAction;
import timeBench.action.analytical.TemporalDataIndexingAction;
import timeBench.action.layout.TimeAxisLayout;
import timeBench.action.layout.timescale.AdvancedTimeScale;
import timeBench.action.layout.timescale.RangeAdapter;
import timeBench.calendar.Calendar;
import timeBench.calendar.CalendarManagerFactory;
import timeBench.calendar.CalendarManagers;
import timeBench.calendar.Granularity;
import timeBench.calendar.JavaDateCalendarManager;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalObject;
import timeBench.data.io.TextTableTemporalDatasetReader;
import timeBench.ui.TimeAxisDisplay;

/**
 * Intermediate demo of a line plot showing multiple numerical variables over
 * time. The demo works with a data file containing weekly mortality counts from
 * respiratory diseases in multiple cities. The data has 2 references (time and
 * city) and one characteristic (mortality count).
 * <p>
 * This extends {@link LinePlotDemo} with the following features:
 * <ul>
 * <li>Set color depending on a column holding the "city".
 * <li>Add line segments using {@link CategoryLinePlotAction} so that only
 * points of a city are connected.
 * <li>Load data from a file using a data format specification.
 * </ul>
 * 
 * @author Rind
 */
public class IndexingDemo {

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
     * 10-class Paired qualitative color scheme from colorbrewer2.org
     */
    private static int[] palette = { ColorLib.rgb(166, 206, 227),
            ColorLib.rgb(31, 120, 180), ColorLib.rgb(178, 223, 138),
            ColorLib.rgb(51, 160, 44), ColorLib.rgb(251, 154, 153),
            ColorLib.rgb(227, 26, 28), ColorLib.rgb(253, 191, 111),
            ColorLib.rgb(255, 127, 0), ColorLib.rgb(202, 178, 214),
            ColorLib.rgb(106, 61, 154) };;

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
        TemporalDataset tmpdsOrig = reader.readData(FILE_DATA);

        DataHelper.printMetadata(System.out, tmpdsOrig.getNodeTable());
        DataHelper.printMetadata(System.out, tmpdsOrig.getTemporalElements()
                .getNodeTable());
        
        ColumnToRowsTemporalDataTransformation transform = new ColumnToRowsTemporalDataTransformation();
        TemporalDataset tmpds = transform.toRows(tmpdsOrig);
        
        DataHelper.printMetadata(System.out, tmpds.getNodeTable());

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
        // add a column that will store indexed values
        vt.addColumn(COL_INDEXED, double.class);

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
        ShapeRenderer dotRenderer = new ShapeRenderer(6);
        DefaultRendererFactory rf = new DefaultRendererFactory(dotRenderer);
        rf.add(new InGroupPredicate(GROUP_LINES), new LineRenderer());
        rf.add(new InGroupPredicate(GROUP_AXIS_LABELS), new AxisRenderer(
                Constants.FAR_LEFT, Constants.CENTER));
        vis.setRendererFactory(rf);

        // --------------------------------------------------------------------
        // STEP 3: create actions to process the visual data

        IndexingAction indexing = new TemporalDataIndexingAction(GROUP_DATA, COL_DATA, COL_INDEXED, COL_CITY);

        TimeAxisLayout time_axis = new TimeAxisLayout(GROUP_DATA, timeScale);
        
        AxisLayout y_axis = new AxisLayout(GROUP_DATA, COL_INDEXED,
                Constants.Y_AXIS, VisiblePredicate.TRUE);
        // set visible value range to 0..100
//        y_axis.setRangeModel(new NumberRangeModel(0.0d, 400d, 0d, 400d));

        // add value axis labels and horizontal grid lines
        AxisLabelLayout y_labels = new TickAxisLabelLayout(GROUP_AXIS_LABELS,
                y_axis, 5);

        // lineCreation add lines between all items in the group
        Action lineCreation = new CategoryLinePlotAction(GROUP_LINES,
                GROUP_DATA, COL_CITY);
        // lineLayout updates x and y coordinates of lines
        LinePlotLayout lineLayout = new LinePlotLayout(GROUP_LINES);

        // visual attributes of line segments (can be created directly in add())
        Action lineColor = new DataColorAction(GROUP_LINES, COL_CITY,
                Constants.NOMINAL, VisualItem.STROKECOLOR, setAlpha(palette,
                        127));
        Action lineStroke = new StrokeAction(GROUP_LINES, new BasicStroke(3f));

        // color must be set -> otherwise nothing displayed
        ColorAction color = new DataColorAction(GROUP_DATA, COL_CITY,
                Constants.NOMINAL, VisualItem.FILLCOLOR, palette);
        color.add(new ColumnExpression(VisualItem.HOVER),
                ColorLib.rgb(255, 100, 255));

        ShapeAction shape = new ShapeAction(GROUP_DATA, Constants.SHAPE_ELLIPSE);

        // runs on layout updates (e.g., window resize, pan)
        ActionList update = new ActionList();
        update.add(time_axis);
        update.add(indexing);
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
        display.addControlListener(new IndexingControl(indexing));

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

            long date = ((TemporalObject) ((VisualItem) t).getSourceTuple())
                    .getTemporalElement().getInf();
            GregorianCalendar cal = new GregorianCalendar(
                    java.util.TimeZone.getTimeZone("UTC"));
            cal.setTimeInMillis(date);

            return String.format("%tF %s: %3.0f", cal, t.getString(COL_CITY),
                    value);
        }
    }
    
    static class IndexingControl extends ControlAdapter {

        protected IndexingAction action;

        public IndexingControl(IndexingAction action) {
            this.action = action;
        }

        @Override
        public void itemClicked(VisualItem item, MouseEvent e) {
            action.setIndexedPoint(item);
            item.getVisualization().run(DemoEnvironmentFactory.ACTION_UPDATE);
        }
    }
}
