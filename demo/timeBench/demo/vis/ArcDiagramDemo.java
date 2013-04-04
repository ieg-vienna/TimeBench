package timeBench.demo.vis;

import ieg.prefuse.data.DataHelper;
import ieg.prefuse.renderer.IntervalBarRenderer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import javax.swing.BorderFactory;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import prefuse.Constants;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.AxisLayout;
import prefuse.data.io.DataIOException;
import prefuse.render.Renderer;
import prefuse.render.RendererFactory;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import render.ArcRenderer;
import timeBench.action.layout.IntervalAxisLayout;
import timeBench.action.layout.TimeAxisLayout;
import timeBench.action.layout.timescale.AdvancedTimeScale;
import timeBench.action.layout.timescale.RangeAdapter;
import timeBench.controls.BranchHighlightControl;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalObject;
import timeBench.data.io.GraphMLTemporalDatasetReader;
import timeBench.util.DebugHelper;
import timeBench.ui.TimeAxisDisplay;
import visual.sort.SizeItemSorter;

public class ArcDiagramDemo {

    private static final String MAXX_FIELD = VisualItem.X2;

    private static void createVisualization(TemporalDataset patterns, TemporalDataset events) {
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

        long border = (events.getSup() - events.getInf()) / 20;
        final AdvancedTimeScale timeScale = new AdvancedTimeScale(
                events.getInf() - border, events.getSup() + border,
                display.getWidth() - 1);
        final AdvancedTimeScale overviewTimeScale = new AdvancedTimeScale(timeScale);
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
        		VisualItem.FILLCOLOR, new int[] {DemoEnvironmentFactory.set3Qualitative[3],
        		DemoEnvironmentFactory.set3Qualitative[4], DemoEnvironmentFactory.set3Qualitative[6]}));
        layout.add(new DataColorAction("patterns.nodes", "class", prefuse.Constants.NOMINAL,
        		VisualItem.FILLCOLOR, new int[] { DemoEnvironmentFactory.set3Qualitative[3],
        		DemoEnvironmentFactory.set3Qualitative[4], DemoEnvironmentFactory.set3Qualitative[6]}));
        // layout.add(new SizeAction(DATA, 1)); // TODO try granularity -> size
        layout.add(new RepaintAction());
        vis.putAction(DemoEnvironmentFactory.ACTION_INIT, layout);
        vis.putAction(DemoEnvironmentFactory.ACTION_UPDATE, layout);

        // --------------------------------------------------------------------
        // STEP 4: set up a display and controls
        display.setHighQuality(true);
        display.setBorder(BorderFactory.createEmptyBorder(7, 0, 7, 0));
        display.setItemSorter(new SizeItemSorter());


        //display.addControlListener(new ToolTipControl("caption"));
        display.addControlListener(new BranchHighlightControl());
              
//        vis.run("layout");
       
        // --------------------------------------------------------------------
        // STEP 5: launching the visualization

        DemoEnvironmentFactory env = new DemoEnvironmentFactory("gantt chart");
        env.setPaintWeekends(false);
        System.out.println("--------");
        env.show(display, rangeAdapter);
    }

    /**
     * @param args
     * @throws TemporalDataException
     */
    public static void main(String[] args) throws TemporalDataException {
//		String datasetFileName = "data\\cardiovascular_mb.csv";
//		TemporalDataset sourceDataset = null;
		
        //UILib.setPlatformLookAndFeel();

		/*try {
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

		DebugHelper.printTemporalDatasetTable(System.out, events,"label","class",TemporalObject.ID);
		
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
		
		//DebugHelper.printTemporalDatasetForest(System.out, action4.getTemporalDataset(), "label",TemporalObject.ID);
		
		TemporalDataset patterns = action4.getTemporalDataset();*/
						
        /*try {
            GraphMLTemporalDatasetWriter writer = new GraphMLTemporalDatasetWriter();
            events.setRoots(new long[] {0});
			writer.writeData(events, "data\\cardiovascular_events.graphml");
	        writer.writeData(patterns, "data\\cardiovascular_patterns.graphml");
		} catch (DataIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/      
        
    	Locale.setDefault(Locale.US);
		TemporalDataset events = null;
		TemporalDataset patterns = null;
		try {
			GraphMLTemporalDatasetReader gmltdr = new GraphMLTemporalDatasetReader();
			File f = new File("data\\cardiovascular_events.graphml.gz");
			InputStream is =  new GZIPInputStream(new FileInputStream(f));
			events = gmltdr.readData(is);
			is.close();
			events.setRoots(null);
			DebugHelper.printTemporalDatasetTable(System.out, events,"label","class",TemporalObject.ID);
			gmltdr = new GraphMLTemporalDatasetReader();
			f = new File("data\\cardiovascular_patterns.graphml.gz");
			is = new GZIPInputStream(new FileInputStream(f));
			patterns = gmltdr.readData(is);
			is.close();
			//DebugHelper.printTemporalDatasetForest(System.out,patterns, "label",TemporalObject.ID);
		} catch (DataIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        DataHelper.printMetadata(System.out, events.getNodeTable());
		DataHelper.printMetadata(System.out, patterns.getNodeTable());

		
        createVisualization(patterns,events);
    }
}
