package timeBench.demo.vis;

import java.awt.Color;
import java.io.IOException;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.bind.JAXBException;

import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.controls.ToolTipControl;
import prefuse.data.io.DataIOException;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.PolygonRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;
import prefuse.visual.sort.ItemSorter;
import timeBench.action.layout.ThemeRiverLayout;
import timeBench.action.layout.timescale.AdvancedTimeScale;
import timeBench.action.layout.timescale.RangeAdapter;
import timeBench.calendar.Calendar;
import timeBench.calendar.CalendarFactory;
import timeBench.calendar.CalendarManagerFactory;
import timeBench.calendar.CalendarManagers;
import timeBench.calendar.Granularity;
import timeBench.calendar.JavaDateCalendarManager;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
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
 * </ul>
 * 
 * @author Rind
 */
public class ThemeRiverDemo {

    private static final String FILE_DATA = "data/nmmaps-resp-3-12monthly-matrix.csv";
    private static final int GRANULARITY_ID = JavaDateCalendarManager.Granularities.Month.toInt();

    private static final String COL_LABEL = "label";

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
        TextTableTemporalDatasetReader reader = new TextTableTemporalDatasetReader(
                CalendarFactory.getSingleton().getGranularity(GRANULARITY_ID,
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

        final AdvancedTimeScale timeScale = new AdvancedTimeScale(
                tmpds.getInf(), tmpds.getSup(),
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

        ThemeRiverLayout themeRiver = new ThemeRiverLayout(GROUP_DATA, tmpds, classes, timeScale);
        
        DataColorAction fill = new DataColorAction(GROUP_DATA, "class", prefuse.Constants.ORDINAL,
        		VisualItem.FILLCOLOR,DemoEnvironmentFactory.set12Qualitative);       
        ColorAction stroke = new ColorAction(GROUP_DATA, VisualItem.STROKECOLOR,ColorLib.color(Color.WHITE));

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

        // --------------------------------------------------------------------
        // STEP 5: launching the visualization

        DemoEnvironmentFactory env = new DemoEnvironmentFactory(
                "theme river");
        env.setPaintWeekends(false);
        env.show(display, rangeAdapter);
    }
}
