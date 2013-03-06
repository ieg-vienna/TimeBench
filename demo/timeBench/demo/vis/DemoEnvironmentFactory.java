package timeBench.demo.vis;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import prefuse.Display;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.util.display.PaintListener;
import prefuse.util.ui.JRangeSlider;
import timeBench.action.layout.timescale.AdvancedTimeScale;
import timeBench.action.layout.timescale.FisheyeTimeScale;
import timeBench.action.layout.timescale.RangeAdapter;
import timeBench.calendar.CalendarManager;
import timeBench.calendar.JavaDateCalendarManager;
import timeBench.controls.RangePanControl;
import timeBench.controls.RangeZoomControl;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalElement;
import timeBench.data.TemporalObject;
import timeBench.ui.FisheyeSlider;
import timeBench.ui.MouseTracker;
import timeBench.ui.TimeAxisDisplay;
import timeBench.ui.TimeScaleHeader;
import timeBench.ui.TimeScalePainter;
import timeBench.ui.TimeScaleStatusBar;
import timeBench.ui.actions.PanAction;
import timeBench.ui.actions.RangePanAction;
import timeBench.ui.actions.RangeZoomAction;
import timeBench.ui.actions.ZoomAction;

public class DemoEnvironmentFactory {

    public static final String ACTION_INIT = "init";
    public static final String ACTION_UPDATE = "update";

    /**
     * demo name to be displayed in the window title
     */
    private String name;

    /**
     * If the {@link RangeAdapter} is null, the panning in time will be
     * unconstrained and there will be no range slider.
     */
    // RangeAdapter rangeAdapter = null;

    /**
     * Time scale of the main display. This is the "detail" time scale in an
     * overview and detail setting.
     */
    // AdvancedTimeScale timeScale;

    /**
     * pan time with a {@link JScrollBar} instead of a {@link JRangeSlider}. The
     * scroll bar is less powerful, but more familiar to novice users.
     */
    private boolean useScrollbar = false;

    private boolean paintWeekends = true;

    public DemoEnvironmentFactory(String name) {
        super();
        this.name = name;
    }

    public void show(TimeAxisDisplay display, RangeAdapter rangeAdapter) {
        show(display, rangeAdapter, rangeAdapter.getActualScale());
    }

    public void show(TimeAxisDisplay display, AdvancedTimeScale timeScale) {
        show(display, null, timeScale);
    }

    private void show(TimeAxisDisplay display, RangeAdapter rangeAdapter,
            AdvancedTimeScale timeScale) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("TimeBench | " + name);

        prepareTimeDisplay(display, rangeAdapter, timeScale);
        frame.getContentPane()
                .add(prepareGui(display, rangeAdapter, timeScale));
        frame.getContentPane()
                .add(prepareToolbar(SwingConstants.VERTICAL, rangeAdapter,
                        timeScale), BorderLayout.EAST);

        frame.pack();
        frame.setVisible(true);
    }

    private void prepareTimeDisplay(final TimeAxisDisplay display,
            RangeAdapter rangeAdapter, final AdvancedTimeScale timeScale) {
        display.setTimeScale(timeScale);
        display.setBorder(BorderFactory.createEmptyBorder(7, 0, 7, 0));

        final TimeScalePainter timeScalePainter = new TimeScalePainter(display);
        timeScalePainter.setTimeScale(timeScale);
        timeScalePainter.setPaintWeekend(this.paintWeekends);

        final MouseTracker mouseTracker = new MouseTracker(display, timeScale);

        display.addPaintListener(new PaintListener() {
            public void postPaint(Display d, Graphics2D g) {
                mouseTracker.paintTimeAtPosition(g);
            }

            public void prePaint(Display d, Graphics2D g) {
                g.transform(d.getInverseTransform());
                timeScalePainter.paint(g);
                g.transform(d.getTransform());
            }
        });

        // react on window resize
        display.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                timeScale.setDisplayWidth(display.getWidth() - 1);
            }
        });

        // enable pan/zoom on mouse dragging
        if (rangeAdapter != null) {
            display.addControlListener(new RangePanControl(rangeAdapter));
            display.addControlListener(new RangeZoomControl(rangeAdapter));
        } else {
            display.addControlListener(new PanControl());
            display.addControlListener(new ZoomControl());
        }

        display.getVisualization().run(DemoEnvironmentFactory.ACTION_INIT);
    }

    private JComponent prepareGui(JComponent display,
            RangeAdapter rangeAdapter, AdvancedTimeScale timeScale) {
        JPanel gui = new JPanel(new BorderLayout());
        gui.add(new TimeScaleHeader(timeScale), BorderLayout.NORTH);

        gui.add(display, BorderLayout.CENTER);

        Box south = Box.createVerticalBox();
        if (rangeAdapter != null) {
            if (useScrollbar) {
                JScrollBar scroll = new JScrollBar(JScrollBar.HORIZONTAL);
                scroll.setModel(rangeAdapter);
                south.add(scroll);
            } else {
                south.add(new JRangeSlider(rangeAdapter,
                        JRangeSlider.HORIZONTAL,
                        JRangeSlider.LEFTRIGHT_TOPBOTTOM));
            }
            south.add(new TimeScaleHeader(rangeAdapter.getFullScale()));
        } else {
            TimeScaleStatusBar statusBar = new TimeScaleStatusBar(timeScale);
            // Mac-Workaround: GrowBox hides Label
            statusBar.add(Box.createHorizontalStrut(20));
            south.add(statusBar);
        }
        gui.add(south, BorderLayout.SOUTH);

        return gui;
    }

    private JToolBar prepareToolbar(int orientation, RangeAdapter rangeAdapter,
            AdvancedTimeScale timeScale) {
        JToolBar toolbar = new JToolBar();
        toolbar.setOrientation(orientation);

        if (rangeAdapter != null) {
            toolbar.add(new RangeZoomAction(rangeAdapter, 10));
            toolbar.add(new RangeZoomAction(rangeAdapter, -10));
            toolbar.add(new RangePanAction(rangeAdapter, -20));
            toolbar.add(new RangePanAction(rangeAdapter, 20));
        } else {
            toolbar.add(new ZoomAction(timeScale, 1 / 1.2));
            toolbar.add(new ZoomAction(timeScale, 1.2));
            toolbar.add(new PanAction(timeScale, -20));
            toolbar.add(new PanAction(timeScale, 20));
        }

        // TODO test fisheye
        if (timeScale instanceof FisheyeTimeScale) {
            FisheyeSlider fs = new FisheyeSlider(
                    (FisheyeTimeScale) rangeAdapter.getActualScale());
            fs.setOrientation(SwingConstants.VERTICAL);
            toolbar.add(fs);
        }

        return toolbar;
    }

    public static interface Delegate {
        public void dataLoaded(TemporalDataset tmpds);
    }

    /**
     * Generates a {@link TemporalDataset} with one data column filled with
     * random double values. The temporal elements are chronon instants with a
     * random gap (between 1 and 4 days). The data is uniformly distributed
     * between 0 and 100.
     * 
     * @param count
     *            number of objects to generate
     * @param dataColumn
     *            field name of the data column
     * @return the temporal data set
     * @throws TemporalDataException
     */
    public static TemporalDataset generateRandomNumericalInstantData(int count,
            String dataColumn) throws TemporalDataException {
        final long MIN_GAP_MS = 360000 * 24; // 1 day
        final long MAX_GAP_MS = 360000 * 24 * 4; // 4 days
        final double MIN_VALUE = 0.0d;
        final double VALUE_RANGE = 100.0d;

        TemporalDataset tmpds = new TemporalDataset();
        tmpds.addDataColumn(dataColumn, double.class, 0.0d);

        double value = Math.random() * VALUE_RANGE + MIN_VALUE;
        long time = System.currentTimeMillis();
        CalendarManager calMan = JavaDateCalendarManager.getSingleton();
        TemporalElement elem;
        TemporalObject obj;

        for (int i = 0; i < count; i++) {
            elem = tmpds.addInstant(time, time,
                    calMan.getBottomGranularityIdentifier(),
                    calMan.getTopGranularityIdentifier());
            obj = tmpds.addTemporalObject(elem);
            obj.set(dataColumn, value);
            // auto-regressive with order 1
            value = (Math.random() * VALUE_RANGE + MIN_VALUE) * 0.2d + value * 0.8d;
            time += Math.round(Math.random() * (MAX_GAP_MS - MIN_GAP_MS)
                    + MIN_VALUE);
        }

        return tmpds;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUseScrollbar(boolean useScrollbar) {
        this.useScrollbar = useScrollbar;
    }

    public void setPaintWeekends(boolean paintWeekends) {
        this.paintWeekends = paintWeekends;
    }
}
