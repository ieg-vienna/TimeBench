package timeBench.demo.vis;

import ieg.prefuse.action.LinePlotAction;
import ieg.prefuse.action.layout.LinePlotLayout;
import ieg.prefuse.renderer.LineRenderer;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.TimeZone;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.bind.JAXBException;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.StrokeAction;
import prefuse.action.layout.AxisLayout;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.data.io.DataIOException;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.display.PaintListener;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.sort.ItemSorter;
import timeBench.action.layout.TimeAxisLayout;
import timeBench.action.layout.timescale.FisheyeTimeScale;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
import timeBench.data.io.TextTableTemporalDatasetReader;
import timeBench.ui.FisheyeSlider;
import timeBench.ui.MouseTracker;
import timeBench.ui.TimeAxisDisplay;
import timeBench.ui.TimeScaleHeader;
import timeBench.ui.TimeScalePainter;
import timeBench.ui.TimeScaleStatusBar;
import timeBench.ui.actions.PanAction;
import timeBench.ui.actions.ZoomAction;

/**
 * <p>
 * Demo application showcasing the use of the TimeBench time axis API to
 * visualize temporal data.
 * </p>
 * 
 * <p>
 * This demo uses climate data, which is layed out along the x- or Time-Axis
 * using TimeBench facilities.
 * </p>
 * <p>
 * Other aspects of this demo are implemented using prefuse and Java Swing.
 * </p>
 * 
 * @author peterw
 * 
 */
public class ClimateDemo extends TimeAxisDisplay {
    private static final long serialVersionUID = 5968502537651940645L;

    private static final String TEMP_FIELD = "AvgTemp";
    private static final String CLIMATE_DATA = "data/climate.csv";
    private static final String CLIMATE_DATA_SPEC = "data/climate-spec.xml";
    private static final String DATA = "data";
    private static final String LINES = "lines";

    private final FisheyeTimeScale timeScale;
    private final TimeScalePainter timeScalePainter;
    private final MouseTracker mouseTracker;

    public ClimateDemo(final TemporalDataset t) {
        super(new Visualization());

        setSize(new Dimension(700, 400));
        setHighQuality(true);

        timeScale = new FisheyeTimeScale(getEarliestDate(t), getLatestDate(t),
                getWidth() - 1);
        timeScale.setAdjustDateRangeOnResize(false);
        timeScale.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                getVisualization().run("update");
            }
        });

        timeScalePainter = new TimeScalePainter(this);
        timeScalePainter.setTimeScale(timeScale);

        mouseTracker = new MouseTracker(this, timeScale);

        DefaultRendererFactory rf = new DefaultRendererFactory();
        LabelRenderer renderer = new LabelRenderer(TEMP_FIELD);
        renderer.setRoundedCorner(6, 6);
        rf.setDefaultRenderer(renderer);
        rf.add(new InGroupPredicate(LINES), new LineRenderer());
        getVisualization().setRendererFactory(rf);

        addPaintListener(new PaintListener() {
            public void postPaint(Display d, Graphics2D g) {
                mouseTracker.paintTimeAtPosition(g);
            }

            public void prePaint(Display d, Graphics2D g) {
                g.transform(d.getInverseTransform());
                timeScalePainter.paint(g);
                g.transform(d.getTransform());
            }
        });

        addControlListener(new PanControl());
        addControlListener(new ZoomControl());

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                timeScale.setDisplayWidth(getWidth() - 1);
            }
        });
        setTimeScale(timeScale);
        setItemSorter(new ItemSorter(){

            @Override
            public int score(VisualItem item) {
                int score = super.score(item); 
                return LINES.equals(item.getGroup()) ? score - 1 : score;
            }
            
        });
        
        AxisLayout yAxis = new AxisLayout(DATA, TEMP_FIELD, Constants.Y_AXIS);
        TimeAxisLayout xAxis = new TimeAxisLayout(DATA, timeScale);
        LinePlotLayout lineLayout = new LinePlotLayout(LINES); 

        getVisualization().addGraph(DATA, t);
        ActionList layout = new ActionList();
        layout.add(yAxis);
        layout.add(xAxis);
        layout.add(new LinePlotAction(LINES, DATA, VisualItem.X));
        // lineLayout updates x and y coordinates of lines
        layout.add(lineLayout);

        layout.add(new ColorAction(DATA, VisualItem.TEXTCOLOR, ColorLib
                .color(Color.BLACK)));
        layout.add(new ColorAction(DATA, VisualItem.FILLCOLOR, ColorLib
                .color(Color.ORANGE)));
        layout.add(new ColorAction(DATA, VisualItem.STROKECOLOR, ColorLib
                .color(Color.ORANGE.darker())));
        layout.add(new ColorAction(LINES, VisualItem.STROKECOLOR, ColorLib
                .color(Color.GRAY)));
        layout.add(new StrokeAction(LINES, new BasicStroke(3f)));
        layout.add(new RepaintAction());
        getVisualization().putAction("layout", layout);

        getVisualization().run("layout");

        ActionList update = new ActionList();
        update.add(yAxis);
        update.add(xAxis);
        update.add(lineLayout);
        update.add(new RepaintAction());
        getVisualization().putAction("update", update);
    
    }

    private long getEarliestDate(final TemporalDataset t) {
        return t.getInf();
    }

    private long getLatestDate(final TemporalDataset t) {
        return t.getSup();
    }

    public static void main(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.showGrowBox", "false");
        System.setProperty("apple.awt.brushMetalLook", "false");
        System.setProperty("apple.awt.brushMetalRounded", "true");
        System.setProperty("apple.awt.brushMetalRounded", "true");
        
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        String datafile = CLIMATE_DATA;
        if (args.length > 0)
            datafile = args[0];

        try {
            JFrame frame = demo(datafile);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static JFrame demo() {
        try {
            return demo(CLIMATE_DATA);
        } catch (Exception e) {
            return null;
        }
    }

    public static JFrame demo(String table) throws DataIOException,
            TemporalDataException, IOException, JAXBException {
        // TemporalDataColumnSpecification spec = new
        // TemporalDataColumnSpecification();
        // spec.setCalendar(CalendarManagerFactory.getSingleton(
        // CalendarManagers.JavaDate).getDefaultCalendar());
        // spec.setTableFormat(new TextTableFormat(Method.REGEX, true, true,
        // "\t"));
        // TemporalObjectEncoding enc = new DateInstantEncoding("", "Date");
        // String[] dataCols = {"AvgTemp", "MaxTemp", "MinTemp",
        // "Precipitation", "RelHumidity", "CloudCover", "SunshineDuration",
        // "AirPressure", "Wind", "VaporContent"};
        // enc.setDataColumns(dataCols);
        // spec.addEncoding(enc);
        // JaxbMarshaller.save(CLIMATE_DATA_SPEC, spec);

        TextTableTemporalDatasetReader reader = new TextTableTemporalDatasetReader(
                CLIMATE_DATA_SPEC);
        TemporalDataset tmpds = reader.readData(table);
//        ieg.prefuse.data.DataHelper.printTable(System.out, tmpds.getNodeTable());

        ClimateDemo cd = new ClimateDemo(tmpds);

        JFrame frame = new JFrame("TimeBench  |  climate");
        frame.add(new ViewMenu(cd.timeScale), BorderLayout.EAST);
        frame.add(cd, BorderLayout.CENTER);

        TimeScaleHeader tsh = new TimeScaleHeader(cd.timeScale);
        frame.add(tsh, BorderLayout.NORTH);

        TimeScaleStatusBar statusBar = new TimeScaleStatusBar(cd.timeScale);
        // Mac-Workaround: GrowBox hides Label
        statusBar.add(Box.createHorizontalStrut(20));
        frame.add(statusBar, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);

        return frame;
    }

    static class ViewMenu extends JToolBar {
        private static final long serialVersionUID = 3347990507358891057L;

        ViewMenu(FisheyeTimeScale ts) {
            super("View");
            setOrientation(VERTICAL);
            add(new ZoomAction(ts, 1 / 1.2));
            add(new ZoomAction(ts, 1.2));
            add(new PanAction(ts, -20));
            add(new PanAction(ts, 20));
            FisheyeSlider fs = new FisheyeSlider(ts);
            fs.setOrientation(VERTICAL);
            add(fs);
        }
    }

}
