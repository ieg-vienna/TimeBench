package timeBench.demo.vis;

import ieg.prefuse.data.DataHelper;
import ieg.prefuse.renderer.OldIntervalBarRenderer;

import java.util.Locale;

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
import timeBench.action.layout.OldIntervalAxisLayout;
import timeBench.action.layout.TimeAxisLayout;
import timeBench.action.layout.timescale.AdvancedTimeScale;
import timeBench.action.layout.timescale.RangeAdapter;
import timeBench.controls.BranchHighlightControl;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalObject;
import timeBench.data.io.GraphMLTemporalDatasetReader;
import timeBench.ui.TimeAxisDisplay;
import timeBench.util.DebugHelper;
import timeBench.util.DemoEnvironmentFactory;
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
        	Renderer intRenderer = new OldIntervalBarRenderer(MAXX_FIELD);

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
        TimeAxisLayout time_axis = new OldIntervalAxisLayout("patterns", MAXX_FIELD,
                timeScale);
        TimeAxisLayout time_axis2 = new OldIntervalAxisLayout("events", MAXX_FIELD,
                timeScale);                       
        //axis.setAxis(Constants.Y_AXIS);
        layout.add(time_axis);
        layout.add(time_axis2);
//        layout.add(new prefuse.action.assignment.SizeAction("events", 1, Constants.Y_AXIS));
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

        DemoEnvironmentFactory env = new DemoEnvironmentFactory("arc diagram");
        env.setPaintWeekends(false);
        System.out.println("--------");
        env.show(display, rangeAdapter);
    }

    /**
     * @param args
     * @throws TemporalDataException
     */
    public static void main(String[] args) throws TemporalDataException {
    	Locale.setDefault(Locale.US);
		TemporalDataset events = null;
		TemporalDataset patterns = null;
		try {
			GraphMLTemporalDatasetReader gmltdr = new GraphMLTemporalDatasetReader();
			events = gmltdr.readData("data/cardiovascular_events.graphml.gz");
			
			DebugHelper.printTemporalDatasetTable(System.out, events,"label","class",TemporalObject.ID);
			
			patterns = gmltdr.readData("data/cardiovascular_patterns.graphml.gz");
			
			//DebugHelper.printTemporalDatasetForest(System.out,patterns, "label",TemporalObject.ID);
		} catch (DataIOException e) {
			e.printStackTrace();
		}
		
        DataHelper.printMetadata(System.out, events.getNodeTable());
		DataHelper.printMetadata(System.out, patterns.getNodeTable());
		
        createVisualization(patterns,events);
    }
}
