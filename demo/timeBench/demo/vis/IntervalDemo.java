package timeBench.demo.vis;

import ieg.prefuse.renderer.IntervalBarRenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.layout.AxisLayout;
import prefuse.controls.ToolTipControl;
import prefuse.render.DefaultRendererFactory;
import prefuse.util.ColorLib;
import prefuse.util.display.PaintListener;
import prefuse.util.ui.JRangeSlider;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import timeBench.action.layout.IntervalAxisLayout;
import timeBench.action.layout.TimeAxisLayout;
import timeBench.action.layout.timescale.AdvancedTimeScale;
import timeBench.action.layout.timescale.RangeAdapter;
import timeBench.controls.RangePanControl;
import timeBench.controls.RangeZoomControl;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
import timeBench.test.DebugHelper;
import timeBench.ui.MouseTracker;
import timeBench.ui.TimeAxisDisplay;
import timeBench.ui.TimeScaleHeader;
import timeBench.ui.TimeScalePainter;
import timeBench.ui.actions.RangePanAction;
import timeBench.ui.actions.RangeZoomAction;

public class IntervalDemo {

    private static final String MAXX_FIELD = VisualItem.X2;

    private static final String GROUP_DATA = "data";

    private static JComponent createVisualization(TemporalDataset tmpds) {
        final Visualization vis = new Visualization();
        final TimeAxisDisplay display = new TimeAxisDisplay(vis);
        display.setSize(700, 450);

        // --------------------------------------------------------------------
        // STEP 1: setup the visualized data

        VisualGraph vg = vis.addGraph(GROUP_DATA, tmpds);
        vg.getNodeTable().addColumn(MAXX_FIELD, int.class);

        // --------------------------------------------------------------------
        // STEP 2: set up renderers for the visual data

        IntervalBarRenderer intRenderer = new IntervalBarRenderer(MAXX_FIELD);
        // intRenderer.setAxis(Constants.Y_AXIS);
        DefaultRendererFactory rf = new DefaultRendererFactory(intRenderer);
        // DefaultRendererFactory rf = new DefaultRendererFactory(new
        // LabelRenderer("caption"));
        vis.setRendererFactory(rf);

        // --------------------------------------------------------------------
        // STEP 3: create actions to process the visual data

        long border = (tmpds.getSup() - tmpds.getInf()) / 20;
        final AdvancedTimeScale timeScale = new AdvancedTimeScale(
                tmpds.getInf() - border, tmpds.getSup() + border, display.getWidth() - 1);
        AdvancedTimeScale overviewTimeScale = new AdvancedTimeScale(timeScale);
        RangeAdapter rangeAdapter = new RangeAdapter(overviewTimeScale, timeScale);

        timeScale.setAdjustDateRangeOnResize(true);
        timeScale.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                vis.run("layout");
            }
        });

        ActionList layout = new ActionList();
        AxisLayout y_axis = new AxisLayout(GROUP_DATA, "caption", Constants.Y_AXIS);
        layout.add(y_axis);
        // layout.add(new TimeAxisLayout(GROUP_DATA, timeScale));
        TimeAxisLayout time_axis = new IntervalAxisLayout(GROUP_DATA, MAXX_FIELD,
                timeScale);
        // axis.setAxis(Constants.Y_AXIS);
        layout.add(time_axis);
        layout.add(new ColorAction(GROUP_DATA, VisualItem.TEXTCOLOR, ColorLib
                .color(Color.BLACK)));
        layout.add(new ColorAction(GROUP_DATA, VisualItem.FILLCOLOR, ColorLib
                .color(Color.ORANGE)));
        layout.add(new ColorAction(GROUP_DATA, VisualItem.STROKECOLOR, ColorLib
                .color(Color.ORANGE.darker())));
        // layout.add(new SizeAction(GROUP_DATA, 1)); // TODO try granularity -> size
        layout.add(new RepaintAction());
        vis.putAction("layout", layout);

        // --------------------------------------------------------------------
        // STEP 4: set up a display and controls
        display.setTimeScale(timeScale);
        display.setHighQuality(true);
        display.setBorder(BorderFactory.createEmptyBorder(7, 0, 7, 0));

        final TimeScalePainter timeScalePainter = new TimeScalePainter(display);
        timeScalePainter.setTimeScale(timeScale);

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

        display.addControlListener(new ToolTipControl("caption"));
        display.addControlListener(new RangePanControl(rangeAdapter));
        display.addControlListener(new RangeZoomControl(rangeAdapter));

        vis.run("layout");

       
        
        // --------------------------------------------------------------------
        // STEP 5: set up Swing GUI
        JPanel box = new JPanel(new BorderLayout());
        box.add(new TimeScaleHeader(timeScale), BorderLayout.NORTH);
        box.add(display);
        Box south = Box.createVerticalBox();
        south.add(new JRangeSlider(rangeAdapter, JRangeSlider.HORIZONTAL, JRangeSlider.LEFTRIGHT_TOPBOTTOM));
        south.add(new TimeScaleHeader(overviewTimeScale));
        box.add(south, BorderLayout.SOUTH);
        
        JToolBar toolbar = new JToolBar();
        toolbar.setOrientation(JToolBar.VERTICAL);
        toolbar.add(new RangeZoomAction(rangeAdapter, 10));
        toolbar.add(new RangeZoomAction(rangeAdapter, -10));
        toolbar.add(new RangePanAction(rangeAdapter, -20));
        toolbar.add(new RangePanAction(rangeAdapter, 20));
        box.add(toolbar, BorderLayout.EAST);

        return box;
    }

    private static void createAndShowGUI(JComponent display) {
        JFrame frame = new JFrame();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("TimeBench | Interval Demo");

        frame.getContentPane().add(display);

        frame.pack();
        frame.setVisible(true);
    }

    /**
     * @param args
     * @throws TemporalDataException
     */
    public static void main(String[] args) throws TemporalDataException {
        Locale.setDefault(Locale.US);
        UILib.setPlatformLookAndFeel();

        TemporalDataset tmpds = DebugHelper.generateValidInstants(500);
        final JComponent display = createVisualization(tmpds);

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(display);
            }
        });
    }
}
