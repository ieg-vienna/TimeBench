package timeBench.demo.vis;

import ieg.prefuse.data.DataHelper;
import ieg.prefuse.renderer.IntervalBarRenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
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
import prefuse.action.assignment.DataColorAction;
import prefuse.action.filter.VisibilityFilter;
import prefuse.action.layout.AxisLayout;
import prefuse.controls.HoverActionControl;
import prefuse.controls.ToolTipControl;
import prefuse.data.expression.AndPredicate;
import prefuse.data.expression.ColumnExpression;
import prefuse.data.expression.ComparisonPredicate;
import prefuse.data.expression.NumericLiteral;
import prefuse.data.expression.Predicate;
import prefuse.data.io.DataIOException;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.AxisRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.Renderer;
import prefuse.render.RendererFactory;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.display.PaintListener;
import prefuse.util.ui.JRangeSlider;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;
import render.ArcRenderer;
import timeBench.action.analytical.GranularityAggregationAction;
import timeBench.action.analytical.GranularityAggregationSettings;
import timeBench.action.analytical.IntervalEventFindingAction;
import timeBench.action.analytical.MultiPredicatePatternDiscovery;
import timeBench.action.layout.IntervalAxisLayout;
import timeBench.action.layout.TimeAxisLayout;
import timeBench.action.layout.timescale.AdvancedTimeScale;
import timeBench.action.layout.timescale.RangeAdapter;
import timeBench.calendar.CalendarManagers;
import timeBench.calendar.JavaDateCalendarManager;
import timeBench.controls.BranchHighlightControl;
import timeBench.controls.RangePanControl;
import timeBench.controls.RangeZoomControl;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalObject;
import timeBench.data.expression.TemporalComparisonPredicate;
import timeBench.data.expression.TemporalElementArrayExpression;
import timeBench.data.expression.TemporalElementExpression;
import timeBench.data.expression.TemporalShiftExpression;
import timeBench.data.io.TextTableTemporalDatasetReader;
import timeBench.test.DebugHelper;
import timeBench.test.DebugHelper.TemporalElementInformation;
import timeBench.ui.MouseTracker;
import timeBench.ui.TimeAxisDisplay;
import timeBench.ui.TimeScaleHeader;
import timeBench.ui.TimeScalePainter;
import timeBench.ui.actions.RangePanAction;
import timeBench.ui.actions.RangeZoomAction;
import visual.sort.SizeItemSorter;

public class ArcDiagramDemo {

    private static final String MAXX_FIELD = VisualItem.X2;

    private static JComponent createVisualization(TemporalDataset patterns, TemporalDataset events) {
        final Visualization vis = new Visualization();
        final TimeAxisDisplay display = new TimeAxisDisplay(vis);
        display.setSize(1200, 300);

        // --------------------------------------------------------------------
        // STEP 1: setup the visualized data

        //DebugHelper.printTemporalDatasetForest(System.out, tmpds, "label",TemporalObject.ID);
        
        VisualGraph vg = vis.addGraph("patterns", patterns);
        VisualGraph vge = vis.addGraph("events", events);
        
        //DataHelper.printForest(System.out, vg.getNodeTable(), tmpds.getRoots(), tmpds.getDepth(), TemporalObject.ID, new TemporalElementInformation(),  "label");
        
        vg.getNodeTable().addColumn(MAXX_FIELD, int.class);
        vge.getNodeTable().addColumn(MAXX_FIELD, int.class);

        // --------------------------------------------------------------------
        // STEP 2: set up renderers for the visual data
                
        // intRenderer.setAxis(Constants.Y_AXIS);
        RendererFactory rf = new RendererFactory() {
        	ArcRenderer arcRenderer = new ArcRenderer();
        	IntervalBarRenderer intRenderer = new IntervalBarRenderer(MAXX_FIELD);

                public Renderer getRenderer(VisualItem item) {
                    return item.isInGroup("patterns") ? arcRenderer
                            : intRenderer;
                }
        };
 
        // DefaultRendererFactory rf = new DefaultRendererFactory(new
        // LabelRenderer("caption"));
        vis.setRendererFactory(rf);

        // --------------------------------------------------------------------
        // STEP 3: create actions to process the visual data

        long border = (patterns.getSup() - patterns.getInf()) / 20;
        final AdvancedTimeScale timeScale = new AdvancedTimeScale(
        		patterns.getInf() - border, patterns.getSup() + border, display.getWidth() - 1);
        AdvancedTimeScale overviewTimeScale = new AdvancedTimeScale(timeScale);
        RangeAdapter rangeAdapter = new RangeAdapter(overviewTimeScale, timeScale);

        timeScale.setAdjustDateRangeOnResize(true);
        timeScale.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                vis.run("layout");
            }
        });

        ActionList layout = new ActionList();
        AxisLayout y_axis = new AxisLayout("events", VisualItem.VISIBLE, Constants.Y_AXIS);
        layout.add(y_axis);
        AxisLayout y_axis2 = new AxisLayout("patterns.nodes", VisualItem.VISIBLE, Constants.Y_AXIS);
        layout.add(y_axis2);
        // layout.add(new TimeAxisLayout(DATA, timeScale));
        TimeAxisLayout time_axis = new IntervalAxisLayout("patterns", MAXX_FIELD,
                timeScale);
        TimeAxisLayout time_axis2 = new IntervalAxisLayout("events", MAXX_FIELD,
                timeScale);                       
        //axis.setAxis(Constants.Y_AXIS);
        layout.add(time_axis);
        layout.add(time_axis2);
        layout.add(new DataColorAction("events", "class", prefuse.Constants.NOMINAL,
        		VisualItem.FILLCOLOR, new int[] { ColorLib.rgb(255,0,0), ColorLib.rgb(0,255,0),ColorLib.rgb(0,0,255)}));
        layout.add(new DataColorAction("patterns.nodes", "class", prefuse.Constants.NOMINAL,
        		VisualItem.FILLCOLOR, new int[] { ColorLib.rgb(255,0,0), ColorLib.rgb(0,255,0),ColorLib.rgb(0,0,255)}));
        // layout.add(new SizeAction(DATA, 1)); // TODO try granularity -> size
        layout.add(new RepaintAction());
        vis.putAction("layout", layout);

        // --------------------------------------------------------------------
        // STEP 4: set up a display and controls
        display.setTimeScale(timeScale);
        display.setHighQuality(true);
        display.setBorder(BorderFactory.createEmptyBorder(7, 0, 7, 0));
        display.setItemSorter(new SizeItemSorter());

        final TimeScalePainter timeScalePainter = new TimeScalePainter(display);
        timeScalePainter.setPaintWeekend(false);
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

        //display.addControlListener(new ToolTipControl("caption"));
        display.addControlListener(new RangePanControl(rangeAdapter));
        display.addControlListener(new RangeZoomControl(rangeAdapter));
        display.addControlListener(new BranchHighlightControl());
              
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
        
        //JToolBar toolbar = new JToolBar();
        //toolbar.setOrientation(JToolBar.VERTICAL);
        //toolbar.add(new RangeZoomAction(rangeAdapter, 10));
        //toolbar.add(new RangeZoomAction(rangeAdapter, -10));
        //toolbar.add(new RangePanAction(rangeAdapter, -20));
        //toolbar.add(new RangePanAction(rangeAdapter, 20));
        //box.add(toolbar, BorderLayout.EAST);

        return box;
    }

    private static void createAndShowGUI(JComponent display) {
        JFrame frame = new JFrame();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("TimeBench | ArcDiagram Demo using MultiPredicatePatternDiscovery");

        frame.getContentPane().add(display);

        frame.pack();
        frame.setVisible(true);
    }

    /**
     * @param args
     * @throws TemporalDataException
     */
    public static void main(String[] args) throws TemporalDataException {
		String datasetFileName = "data\\cardiovascular_mb.csv";
		TemporalDataset sourceDataset = null;
		
        //UILib.setPlatformLookAndFeel();

		try {
	        TextTableTemporalDatasetReader reader = new TextTableTemporalDatasetReader();
	        sourceDataset = reader.readData(datasetFileName);			
		} catch(TemporalDataException e) {
			System.out.println("TemporalDataException while reading data: " + e.getMessage());
			System.exit(1);
		} catch (DataIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	Locale.setDefault(Locale.US);
		
		//DebugHelper.printTemporalDatasetTable(System.out, sourceDataset,"value");
		
		GranularityAggregationAction action = new GranularityAggregationAction(sourceDataset,
				CalendarManagers.JavaDate,new GranularityAggregationSettings[]
						{ new GranularityAggregationSettings( JavaDateCalendarManager.Granularities.Day.toInt(),
								JavaDateCalendarManager.Granularities.Top.toInt() ) },-1.0);		
		action.run(0.0);
		
		DebugHelper.printTemporalDatasetTable(System.out, action.getTemporalDataset(),"value");
		
		Predicate[] templates = new Predicate[3];
		templates[0] = new ComparisonPredicate(ComparisonPredicate.LTEQ, new ColumnExpression("value"), new NumericLiteral(40));						
		templates[1] = new AndPredicate(
				new ComparisonPredicate(ComparisonPredicate.GT, new ColumnExpression("value"), new NumericLiteral(40)),
				new ComparisonPredicate(ComparisonPredicate.LT, new ColumnExpression("value"), new NumericLiteral(50)));
		templates[2] = new ComparisonPredicate(ComparisonPredicate.GTEQ, new ColumnExpression("value"), new NumericLiteral(50));						
			
		IntervalEventFindingAction action2 = new IntervalEventFindingAction(action.getTemporalDataset(), templates,IntervalEventFindingAction.SPACING_ALLOWED);
		action2.run(0.0);

		TemporalDataset events = action2.getTemporalDataset();

		//DebugHelper.printTemporalDatasetTable(System.out, events,"label","class",TemporalObject.ID);
		
		MultiPredicatePatternDiscovery action3 = new MultiPredicatePatternDiscovery(events, 
				events, new Predicate[] {
			new TemporalComparisonPredicate(TemporalComparisonPredicate.MEETS,
					new TemporalShiftExpression(new TemporalElementExpression(),-1000L*60L*60*24),
					new TemporalElementArrayExpression(new ArrayList<TemporalObject>())),
				new TemporalComparisonPredicate(TemporalComparisonPredicate.MEETS,
					new TemporalShiftExpression(new TemporalElementExpression(),-1000L*60L*60*48),
					new TemporalElementArrayExpression(new ArrayList<TemporalObject>())),
				}, MultiPredicatePatternDiscovery.SPACING_ALLOWED);		
		action3.run(0.0);

		//DebugHelper.printTemporalDatasetForest(System.out, action3.getTemporalDataset(), "label",TemporalObject.ID);
		
		MultiPredicatePatternDiscovery action4 = new MultiPredicatePatternDiscovery(action3.getTemporalDataset(), 
				events, new Predicate[] {
				new TemporalComparisonPredicate(TemporalComparisonPredicate.MEETS,
					new TemporalShiftExpression(new TemporalElementExpression(),-1000L*60L*60*24),
					new TemporalElementArrayExpression(new ArrayList<TemporalObject>())),
				new TemporalComparisonPredicate(TemporalComparisonPredicate.MEETS,
					new TemporalShiftExpression(new TemporalElementExpression(),-1000L*60L*60*48),
					new TemporalElementArrayExpression(new ArrayList<TemporalObject>())),
				}, MultiPredicatePatternDiscovery.SPACING_ALLOWED);		
		action4.run(0.0);
		
		DebugHelper.printTemporalDatasetForest(System.out, action4.getTemporalDataset(), "label",TemporalObject.ID);
		
		TemporalDataset patterns = action4.getTemporalDataset();
		
        final JComponent display = createVisualization(patterns,events);

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(display);
            }
        });
    }
}
