// Pattern Over Time Semantic Blended Levels Integrated Temporal Zoom

// TODO
// Labels (how best? multiple renderers per item?)
// soft fading (animation)

package timeBench.demo.vis;

import ieg.prefuse.action.layout.TreeRangeAxisLayout;
import ieg.prefuse.data.DataHelper;
import ieg.prefuse.data.ParentChildGraph;
import ieg.prefuse.data.ParentChildNode;
import ieg.prefuse.renderer.IntervalBarRenderer;
import ieg.prefuse.renderer.RectangleRenderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import prefuse.Constants;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.AxisLayout;
import prefuse.action.layout.Layout;
import prefuse.controls.ToolTipControl;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.expression.BooleanLiteral;
import prefuse.data.expression.Predicate;
import prefuse.data.io.DataIOException;
import prefuse.data.tuple.TableTuple;
import prefuse.data.tuple.TupleSet;
import prefuse.render.LabelRenderer;
import prefuse.render.PolygonRenderer;
import prefuse.render.Renderer;
import prefuse.render.RendererFactory;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.DataLib;
import prefuse.util.PrefuseLib;
import prefuse.visual.DecoratorItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import render.ArcRenderer;
import timeBench.action.analytical.PatternCountAction;
import timeBench.action.analytical.PatternInstanceCountAction;
import timeBench.action.analytical.TreeDebundlingAction;
import timeBench.action.layout.GreedyDistributionLayout;
import timeBench.action.layout.IntervalAxisLayout;
import timeBench.action.layout.PatternOverlayCheckLayout;
import timeBench.action.layout.ThemeRiverLayout;
import timeBench.action.layout.TimeAxisLayout;
import timeBench.action.layout.TimeAxisLayout.Placement;
import timeBench.action.layout.timescale.AdvancedTimeScale;
import timeBench.action.layout.timescale.RangeAdapter;
import timeBench.calendar.CalendarManager;
import timeBench.calendar.JavaDateCalendarManager;
import timeBench.controls.BranchHighlightControl;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalObject;
import timeBench.data.io.GraphMLTemporalDatasetReader;
import timeBench.demo.vis.POTSBLITZDemo.DecoratorLayout;
import timeBench.ui.TimeAxisDisplay;
import timeBench.util.DebugHelper;
import timeBench.util.DemoEnvironmentFactory;
import visual.sort.SizeItemSorter;

public class PatternCountDemo {

    private static final String MAXX_FIELD = VisualItem.X2;
    private static final String GRAPH = "arcdiagram_patterns"; // Don't know if . is reserved in prefuse
    
    //private static final String PATTERNTIMELINES_EVENTS = "arcdiagram_patterns"; // Don't know if . is reserved in prefuse
    
    static private ArrayList<String> classes;

    private static void createVisualization(ParentChildGraph countedPatterns) {
        final Visualization vis = new Visualization();
        final TimeAxisDisplay display = new TimeAxisDisplay(vis);
        display.setSize(1200, 600);

        // --------------------------------------------------------------------
        // STEP 1: setup the visualized data
       
        VisualGraph vg = vis.addGraph(GRAPH, countedPatterns);
        
        vg.getNodeTable().addColumn(MAXX_FIELD, int.class);
        Iterator i = vg.getNodes().tuples();
        while (i.hasNext())
        	PrefuseLib.setSizeY(((VisualItem)i.next()),null,
        			vis.getDisplay(0).getBounds().height/DataLib.max(countedPatterns.getNodes(), "depth").getDouble("depth")-10);

        vis.addDecorators(GRAPH+"_decorator", GRAPH+".nodes");

        
        // --------------------------------------------------------------------
        // STEP 2: set up renderers for the visual data
                
        // intRenderer.setAxis(Constants.Y_AXIS);
        RendererFactory rf = new RendererFactory() {
        	ShapeRenderer renderer = new ShapeRenderer(1);
        	LabelRenderer labelRenderer = new LabelRenderer("label");

                public Renderer getRenderer(VisualItem item) {
                	if(item.isInGroup(GRAPH))
                		return renderer;
                	else return labelRenderer;
                }
        };
 
        // DefaultRendererFactory rf = new DefaultRendererFactory(new
        // LabelRenderer("caption"));
        vis.setRendererFactory(rf);

        // --------------------------------------------------------------------
        // STEP 3: create actions to process the visual data

        ActionList layout = new ActionList();
        
        AxisLayout y_axis = new AxisLayout(GRAPH+".nodes", "depth", Constants.Y_AXIS);        
        layout.add(y_axis);
        AxisLayout x_axis = new TreeRangeAxisLayout(GRAPH, "count", Constants.X_AXIS);
        layout.add(x_axis);
        
        layout.add(new DecoratorLayout(GRAPH+"_decorator"));
        
        layout.add(new DataColorAction(GRAPH+".nodes", "label", prefuse.Constants.NOMINAL,
        		VisualItem.FILLCOLOR, DemoEnvironmentFactory.set3Qualitative));
        layout.add(new ColorAction(GRAPH+".nodes", VisualItem.STROKECOLOR, ColorLib.color(Color.white)));
        layout.add(new ColorAction(GRAPH+"_decorator", VisualItem.TEXTCOLOR, ColorLib
                .color(Color.BLACK)));

        //layout.add(new DataColorAction(PATTERNTIMELINES, VisualItem.VISIBLE, ColorLib.gray(0),VisualItem.FILLCOLOR));
        //layout.add(new DataColorAction(PATTERNTHEMERIVER, VisualItem.VISIBLE, ColorLib.gray(0),VisualItem.FILLCOLOR));
        
        layout.add(new RepaintAction());
        
        vis.putAction(DemoEnvironmentFactory.ACTION_INIT, layout);
        vis.putAction(DemoEnvironmentFactory.ACTION_UPDATE, layout);

        // --------------------------------------------------------------------
        // STEP 4: set up a display and controls
        display.setHighQuality(true);
        display.setBorder(BorderFactory.createEmptyBorder(7, 0, 7, 0));
        display.setItemSorter(new SizeItemSorter());

		// react on window resize
		display.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				vis.run(DemoEnvironmentFactory.ACTION_UPDATE);
			}
		});
        
        //display.addControlListener(new ToolTipControl("caption"));
        //display.addControlListener(new BranchHighlightControl());
        //display.addControlListener(new ToolTipControl("label"));
              
//        vis.run("layout");
       
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
    
    /**
     * @param args
     * @throws TemporalDataException
     */
    public static void main(String[] args) throws TemporalDataException {
    	Locale.setDefault(Locale.US);

		TemporalDataset events = null;
		TemporalDataset patterns = null;
		ParentChildGraph countedPatterns = null;
		
		try {
			GraphMLTemporalDatasetReader gmltdr = new GraphMLTemporalDatasetReader();
			events = gmltdr.readData("data/Dodgers-events.graphml.gz");
			
			//DebugHelper.printTemporalDatasetTable(System.out, events,"label","class",TemporalObject.ID);
			
			patterns = gmltdr.readData("data/Dodgers-patterns.graphml.gz");
						
			DebugHelper.printTemporalDatasetForest(System.out,patterns, "label",TemporalObject.ID);						
		} catch (DataIOException e) {
			e.printStackTrace();
		}			
		
        //DataHelper.printMetadata(System.out, events.getNodeTable());
		//DataHelper.printMetadata(System.out, patterns.getNodeTable());
		
		PatternInstanceCountAction action = new PatternInstanceCountAction(patterns);
		action.run(0);

		countedPatterns = action.getResult();
		
		Iterator nodes = countedPatterns.nodes();
		while(nodes.hasNext()) {
			ParentChildNode node = (ParentChildNode)nodes.next();
			printNode(node);
			//System.out.println("N: "+node.getRow()+" - P: "+node.getParentCount()+" - C: "+node.getChildCount());
		}
		
        createVisualization(countedPatterns);
    }

	private static void printNode(ParentChildNode node) {
		/*Iterator i = node.parentEdges();
		if(i.hasNext())
			System.out.print("p"+((Edge)i.next()).get("class"));*/
		System.out.print(node.get("label")+" - ");
		System.out.print(node.get("count")+" - ");
		System.out.print(node.get("depth")+" - ");
		System.out.println(node.getChildCount());
		//node.inEdges()
		
	}
	
    static class DecoratorLayout extends Layout {
        public DecoratorLayout(String group) {
            super(group);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public void run(double frac) {
            Iterator iter = super.m_vis.items(super.m_group);
            while (iter.hasNext()) {
                DecoratorItem item = (DecoratorItem) iter.next();
                VisualItem node = item.getDecoratedItem();
                Rectangle2D bounds = node.getBounds();
                setX(item, null, bounds.getCenterX());
                setY(item, null, bounds.getCenterY());
                item.setSize(0.8);
                item.setVisible(node.isVisible());
            }
        }
    }
}

