package timeBench.demo.vis;

import ieg.prefuse.RangeModelTransformationDisplay;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;

import prefuse.Constants;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.StrokeAction;
import prefuse.controls.ToolTipControl;
import prefuse.data.io.DataIOException;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import timeBench.action.analytical.GranularityAggregationAction;
import timeBench.action.analytical.GranularityAggregationSettings;
import timeBench.action.assignment.OverlayDataColorAction;
import timeBench.action.layout.GranularityTreeLabelLayout;
import timeBench.action.layout.GranularityTreeLayout;
import timeBench.action.layout.GranularityTreeLayoutSettings;
import timeBench.calendar.Calendar;
import timeBench.calendar.CalendarFactory;
import timeBench.calendar.CalendarManagers;
import timeBench.calendar.Granularity;
import timeBench.calendar.JavaDateCalendarManager;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
import timeBench.data.io.TextTableTemporalDatasetReader;
import timeBench.util.DemoEnvironmentFactory;
import visual.sort.TreeItemSorter;

/**
 * Simple demo of a GROOVE.
 * @author Lammarsch
 */
public class GROOVEDemo {

    private static final String COL_DATA = "value";

    private static final String GROUP_DATA = "data";
    private static final String GROUP_X_LABELS = "xlab";
    private static final String GROUP_Y_LABELS = "ylab";
    
	private static final String DATASET_FILE_NAME = "data/cardiovascular_mb.csv";

    
    /**
     * @param args
     * @throws TemporalDataException
     * @throws DataIOException 
     */
    public static void main(String[] args) throws TemporalDataException, DataIOException {
        
    	java.util.Locale.setDefault(java.util.Locale.US);

		TemporalDataset tmpds = null;
		try {
			TextTableTemporalDatasetReader reader = new TextTableTemporalDatasetReader();
			tmpds = reader.readData(DATASET_FILE_NAME);
		} catch(TemporalDataException e) {
			System.out.println("TemporalDataException while reading data: " + e.getMessage());
			System.exit(1);
		}
    	
        //DataHelper.printTable(System.out, tmpds.getNodeTable());

        final Visualization vis = new Visualization();
        final RangeModelTransformationDisplay display = new RangeModelTransformationDisplay(vis, new String[] {"update"});

        display.setSize(1500,900);

        // --------------------------------------------------------------------
        // STEP 1: setup the visualized data
        
		Calendar calendar = JavaDateCalendarManager.getSingleton().getDefaultCalendar();
		GranularityAggregationAction timeAggregationAction = new GranularityAggregationAction(tmpds,
				new Granularity[] {
				    CalendarFactory.getSingleton().getGranularity(calendar,"Top","Top"),
				    CalendarFactory.getSingleton().getGranularity(calendar,"Year","Top"),
				    CalendarFactory.getSingleton().getGranularity(calendar,"Week","Year"),
				    CalendarFactory.getSingleton().getGranularity(calendar,"Day","Week") },
				-1.0);				
		/*GranularityAggregationAction timeAggregationAction = new GranularityAggregationAction(tmpds,
				CalendarManagers.JavaDate,
				new GranularityAggregationSettings[] {
				new GranularityAggregationSettings(JavaDateCalendarManager.Granularities.Top.toInt(),JavaDateCalendarManager.Granularities.Top.toInt()),
					new GranularityAggregationSettings(JavaDateCalendarManager.Granularities.Quarter.toInt(),JavaDateCalendarManager.Granularities.Year.toInt()),
					new GranularityAggregationSettings(JavaDateCalendarManager.Granularities.Month.toInt(),JavaDateCalendarManager.Granularities.Quarter.toInt()),
					new GranularityAggregationSettings(JavaDateCalendarManager.Granularities.Week.toInt(),JavaDateCalendarManager.Granularities.Month.toInt()),
					new GranularityAggregationSettings(JavaDateCalendarManager.Granularities.Day.toInt(),JavaDateCalendarManager.Granularities.Week.toInt())},
				-1.0);*/
		timeAggregationAction.run(0);
		
		/*DebugHelper.printTemporalDatasetGraph(
				System.out, timeAggregationAction.getGranularityAggregationTree().getTemporalObject(
						timeAggregationAction.getGranularityAggregationTree().getRoots()[0]));*/
        
        vis.addGraph(GROUP_DATA, timeAggregationAction.getGranularityAggregationTree());        
        
        // --------------------------------------------------------------------
        // STEP 2: set up renderers for the visual data
		DefaultRendererFactory rf = new DefaultRendererFactory();
		rf.add(new InGroupPredicate(GROUP_DATA+".nodes"),new ShapeRenderer(1));
        rf.add(new InGroupPredicate(GROUP_X_LABELS),new	LabelRenderer(VisualItem.LABEL));
        rf.add(new InGroupPredicate(GROUP_Y_LABELS),new LabelRenderer(VisualItem.LABEL));

        vis.setRendererFactory(rf);

        // --------------------------------------------------------------------
        // STEP 3: create actions to process the visual data

		GranularityTreeLayout granularityTreeLayout = new GranularityTreeLayout(GROUP_DATA, new GranularityTreeLayoutSettings[]{
				new GranularityTreeLayoutSettings(false, GranularityTreeLayout.FITTING_FULL_AVAILABLE_SPACE, Constants.Y_AXIS, 0),
				new GranularityTreeLayoutSettings(false, GranularityTreeLayout.FITTING_DEPENDING_ON_POSSIBLE_VALUES, Constants.X_AXIS, 0),
				new GranularityTreeLayoutSettings(false, GranularityTreeLayout.FITTING_DEPENDING_ON_POSSIBLE_VALUES, Constants.Y_AXIS, 0.5)}
			,4);
		/*GranularityTreeLayout granularityTreeLayout = new GranularityTreeLayout(GROUP_DATA, new GranularityTreeLayoutSettings[]{
				new GranularityTreeLayoutSettings(false, GranularityTreeLayout.FITTING_FULL_AVAILABLE_SPACE, Constants.Y_AXIS, 0),
				new GranularityTreeLayoutSettings(false, GranularityTreeLayout.FITTING_FULL_AVAILABLE_SPACE, Constants.X_AXIS, 0),
				new GranularityTreeLayoutSettings(false, GranularityTreeLayout.FITTING_DEPENDING_ON_POSSIBLE_VALUES, Constants.Y_AXIS, 1),
				new GranularityTreeLayoutSettings(false, GranularityTreeLayout.FITTING_FULL_AVAILABLE_SPACE, Constants.X_AXIS, 0)},
			4);*/
		
		GranularityTreeLabelLayout xLabelLayout = new GranularityTreeLabelLayout(GROUP_X_LABELS, granularityTreeLayout, Constants.X_AXIS,
				new Rectangle2D.Double(120,0,1380,20));
		GranularityTreeLabelLayout yLabelLayout = new GranularityTreeLabelLayout(GROUP_Y_LABELS, granularityTreeLayout, Constants.Y_AXIS,
				new Rectangle2D.Double(0,20,120,880));
		/*GranularityTreeLabelLayout xLabelLayout = new GranularityTreeLabelLayout(GROUP_X_LABELS, granularityTreeLayout, Constants.X_AXIS,
				new Rectangle2D.Double(60,0,1440,20));
		GranularityTreeLabelLayout yLabelLayout = new GranularityTreeLabelLayout(GROUP_Y_LABELS, granularityTreeLayout, Constants.Y_AXIS,
				new Rectangle2D.Double(0,20,60,880));*/
		
		OverlayDataColorAction colorAction = new OverlayDataColorAction(GROUP_DATA,"value",Constants.NUMERICAL,VisualItem.FILLCOLOR,true,3);
		/*OverlayDataColorAction colorAction = new OverlayDataColorAction(GROUP_DATA,"value",Constants.NUMERICAL,VisualItem.FILLCOLOR,true,3);*/

        // runs on layout updates (e.g., window resize, pan)
        ActionList update = new ActionList();
        update.add(granularityTreeLayout);
        update.add(xLabelLayout);
        update.add(yLabelLayout);
        update.add(new RepaintAction());
        vis.putAction(DemoEnvironmentFactory.ACTION_UPDATE, update);

        // runs once (at startup)
        ActionList draw = new ActionList();
        draw.add(update);
        draw.add(colorAction);
        draw.add(new StrokeAction(GROUP_X_LABELS,new BasicStroke(0)));
        draw.add(new StrokeAction(GROUP_Y_LABELS,new BasicStroke(0)));
        draw.add(new ColorAction(GROUP_X_LABELS,VisualItem.TEXTCOLOR,ColorLib.color(Color.BLACK)));
        draw.add(new ColorAction(GROUP_Y_LABELS,VisualItem.TEXTCOLOR,ColorLib.color(Color.BLACK)));
        draw.add(new RepaintAction());
        vis.putAction(DemoEnvironmentFactory.ACTION_INIT, draw);

        // --------------------------------------------------------------------
        // STEP 4: set up a display and controls

        // enable anti-aliasing
        display.setHighQuality(true);
        
        display.setBorder(BorderFactory.createEmptyBorder(20, 120, 0, 0));
        /*display.setBorder(BorderFactory.createEmptyBorder(20, 60, 0, 0));*/

		// react on window resize
		display.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				vis.run(DemoEnvironmentFactory.ACTION_UPDATE);
			}
		});
        
		display.setItemSorter(new TreeItemSorter());

        // show value in tooltip 
        display.addControlListener(new ToolTipControl(COL_DATA));

        // --------------------------------------------------------------------
        // STEP 5: launching the visualization

		vis.run(DemoEnvironmentFactory.ACTION_INIT);
        
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI(display);
				}
			});		
    }
    
	protected static void createAndShowGUI(JComponent display) {
		JFrame frame = new JFrame();

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("GROOVE");

		frame.getContentPane().add(display);
		
		frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent arg0) {
                System.out.println("closed -- is never called");
            }

            @Override
            public void windowClosing(WindowEvent arg0) {
                System.out.println("closing");
            }
		});

		frame.pack();
		frame.setVisible(true);
	}
}
