// Pattern Over Time Semantic Blended Levels Integrated Temporal Zoom

// TODO
// Labels (how best? multiple renderers per item?)
// soft fading (animation)

package org.timebench.demo.vis;

import ieg.prefuse.data.DataHelper;
import ieg.prefuse.renderer.OldIntervalBarRenderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.timebench.action.analytical.PatternCountAction;
import org.timebench.action.analytical.TreeDebundlingAction;
import org.timebench.action.layout.GreedyDistributionLayout;
import org.timebench.action.layout.OldIntervalAxisLayout;
import org.timebench.action.layout.PatternOverlayCheckLayout;
import org.timebench.action.layout.ThemeRiverLayout;
import org.timebench.action.layout.TimeAxisLayout;
import org.timebench.action.layout.TimeAxisLayout.Placement;
import org.timebench.action.layout.timescale.AdvancedTimeScale;
import org.timebench.action.layout.timescale.RangeAdapter;
import org.timebench.calendar.CalendarManager;
import org.timebench.calendar.JavaDateCalendarManager;
import org.timebench.controls.BranchHighlightControl;
import org.timebench.data.TemporalDataException;
import org.timebench.data.TemporalDataset;
import org.timebench.data.TemporalObject;
import org.timebench.data.io.GraphMLTemporalDatasetReader;
import org.timebench.render.ArcRenderer;
import org.timebench.ui.TimeAxisDisplay;
import org.timebench.util.DebugHelper;
import org.timebench.util.DemoEnvironmentFactory;
import org.timebench.visual.sort.SizeItemSorter;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.AxisLayout;
import prefuse.action.layout.Layout;
import prefuse.controls.ControlAdapter;
import prefuse.controls.ToolTipControl;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.expression.BooleanLiteral;
import prefuse.data.expression.Predicate;
import prefuse.data.io.DataIOException;
import prefuse.data.tuple.TupleSet;
import prefuse.render.LabelRenderer;
import prefuse.render.PolygonRenderer;
import prefuse.render.Renderer;
import prefuse.render.RendererFactory;
import prefuse.util.ColorLib;
import prefuse.visual.DecoratorItem;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;

public class POTSBLITZDemo {

    private static final String MAXX_FIELD = VisualItem.X2;
    private static final String ARCDIAGRAM_PATTERNS = "arcdiagram_patterns"; // Don't know if . is reserved in prefuse
    private static final String ARCDIAGRAM_EVENTS = "arcdiagram_events"; // Don't know if . is reserved in prefuse
    private static final String PATTERNTIMELINES = "patterntimelines";
    private static final String PATTERNTHEMERIVER = "patternthemeriver";
    
    private static final String PATTERNTIMELINES_DECORATOR = "patterntimelines_decorator";
    private static final String PATTERNTHEMERIVER_DECORATOR = "patternthemeriver_decorator";
    
    //private static final String PATTERNTIMELINES_EVENTS = "arcdiagram_patterns"; // Don't know if . is reserved in prefuse
    
    static private Hashtable<String,Integer> classes;
    
    private static int[] enforcedView = new int[] { -1 };

    private static void createVisualization(TemporalDataset patterns, TemporalDataset events,TemporalDataset flatPatterns, TemporalDataset countedPatterns) {
        final Visualization vis = new Visualization();
        final TimeAxisDisplay display = new TimeAxisDisplay(vis);
        display.setSize(1200, 600);

        // --------------------------------------------------------------------
        // STEP 1: setup the visualized data
       
        VisualGraph vg = vis.addGraph(ARCDIAGRAM_PATTERNS, patterns);
        VisualGraph vge = vis.addGraph(ARCDIAGRAM_EVENTS, events);
        VisualGraph vgf = vis.addGraph(PATTERNTIMELINES,flatPatterns);
        
        vis.addDecorators(PATTERNTIMELINES_DECORATOR, PATTERNTIMELINES+".nodes");
        
        vg.getNodeTable().addColumn(MAXX_FIELD, int.class);
        vge.getNodeTable().addColumn(MAXX_FIELD, int.class);
        vgf.getNodeTable().addColumn(MAXX_FIELD, int.class);

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
        	PolygonRenderer polygonRenderer = new PolygonRenderer();
        	Renderer intRenderer = new OldIntervalBarRenderer(MAXX_FIELD);
        	LabelRenderer labelRenderer = new LabelRenderer("label");

                public Renderer getRenderer(VisualItem item) {
                	if(item.isInGroup(ARCDIAGRAM_PATTERNS))
                		return arcRenderer;
                	else if(item.isInGroup(PATTERNTHEMERIVER))
                		return polygonRenderer;
                	else if(item.isInGroup(PATTERNTIMELINES_DECORATOR) || item.isInGroup(PATTERNTHEMERIVER_DECORATOR))
                		return labelRenderer;
                	else return intRenderer;
                }
        };
 
        // DefaultRendererFactory rf = new DefaultRendererFactory(new
        // LabelRenderer("caption"));
        vis.setRendererFactory(rf);

        // --------------------------------------------------------------------
        // STEP 3: create actions to process the visual data

        ActionList layout = new ActionList();
        
        AxisLayout y_axis = new AxisLayout(ARCDIAGRAM_EVENTS, VisualItem.VISIBLE, Constants.Y_AXIS);
        layout.add(y_axis);
        AxisLayout y_axis2 = new AxisLayout(ARCDIAGRAM_PATTERNS+".nodes", VisualItem.VISIBLE, Constants.Y_AXIS);
        layout.add(y_axis2);
        TimeAxisLayout time_axis = new OldIntervalAxisLayout(ARCDIAGRAM_PATTERNS, MAXX_FIELD, Constants.X_AXIS,
        		timeScale,Placement.MIDDLE,new BooleanLiteral(true));
        TimeAxisLayout time_axis2 = new OldIntervalAxisLayout(ARCDIAGRAM_EVENTS, MAXX_FIELD, timeScale);                       
        layout.add(time_axis);
        layout.add(time_axis2);       
        
        TimeAxisLayout time_axis3 = new OldIntervalAxisLayout(PATTERNTIMELINES, MAXX_FIELD, Constants.X_AXIS,
        		timeScale,Placement.MIDDLE,new BooleanLiteral(true));
        GreedyDistributionLayout y_axis3 = new GreedyDistributionLayout(PATTERNTIMELINES, PATTERNTHEMERIVER, 28);
        layout.add(time_axis3);
        layout.add(y_axis3);
        
        PatternOverlayCheckLayout patternOverlapCheckLayout = new PatternOverlayCheckLayout(ARCDIAGRAM_PATTERNS,ARCDIAGRAM_EVENTS,PATTERNTIMELINES,6);
        layout.add(patternOverlapCheckLayout);
        
        ThemeRiverLayout themeRiver = new ThemeRiverLayout(PATTERNTHEMERIVER,countedPatterns,classes,timeScale);
        layout.add(themeRiver);

        layout.add(new DecoratorLayout(PATTERNTIMELINES_DECORATOR));
        //layout.add(new DecoratorLayout2(PATTERNTHEMERIVER_DECORATOR));
        
        layout.add(new DataColorAction(ARCDIAGRAM_EVENTS, "class", prefuse.Constants.NOMINAL,
        		VisualItem.FILLCOLOR, new int[] {ColorLib.rgb(55, 126, 184),                
        		ColorLib.rgb(77, 175, 74),
        		ColorLib.rgb(255, 255, 51),
        		ColorLib.rgb(228, 26, 28)}));
        layout.add(new DataColorAction(ARCDIAGRAM_PATTERNS+".nodes", "class", prefuse.Constants.NOMINAL,
        		VisualItem.FILLCOLOR, new int[] {ColorLib.rgb(55, 126, 184),                
        		ColorLib.rgb(77, 175, 74),
        		ColorLib.rgb(255, 255, 51),
        		ColorLib.rgb(228, 26, 28)}));
        layout.add(new DataColorAction(PATTERNTIMELINES, "class", prefuse.Constants.ORDINAL,
        		VisualItem.FILLCOLOR,DemoEnvironmentFactory.set12Qualitative));
        layout.add(new ColorAction(PATTERNTIMELINES_DECORATOR, VisualItem.TEXTCOLOR, ColorLib
                .color(Color.BLACK)));
        /*layout.add(new ColorAction(PATTERNTHEMERIVER_DECORATOR, VisualItem.TEXTCOLOR, ColorLib
                .color(Color.BLACK)));*/
        layout.add(new DataColorAction(PATTERNTHEMERIVER, "class", prefuse.Constants.ORDINAL,
        		VisualItem.FILLCOLOR,DemoEnvironmentFactory.set12Qualitative));       
        layout.add(new ColorAction(PATTERNTHEMERIVER, VisualItem.STROKECOLOR,ColorLib.color(Color.WHITE)));

        layout.add(new EnforceViewAction(enforcedView));
        
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


        //display.addControlListener(new ToolTipControl("caption"));
        display.addControlListener(new BranchHighlightControl());
        display.addControlListener(new ToolTipControl("label"));
              
        display.addControlListener(new ViewSwitchControl(enforcedView));
        
//        vis.run("layout");
       
        // --------------------------------------------------------------------
        // STEP 5: launching the visualization

        DemoEnvironmentFactory env = new DemoEnvironmentFactory("arc diagram");
        env.setPaintWeekends(false);
        System.out.println("--------");
        env.show(display, rangeAdapter,false);
    }

    /**
     * @param args
     * @throws TemporalDataException
     */
    public static void main(String[] args) throws TemporalDataException {
    	Locale.setDefault(Locale.US);
		TemporalDataset events = null;
		TemporalDataset patterns = null;
		TemporalDataset flatPatterns = null;
		TemporalDataset countedPatterns = null;
		try {
			GraphMLTemporalDatasetReader gmltdr = new GraphMLTemporalDatasetReader();
			events = gmltdr.readData("data/events.graphml.gz");
			
			//DebugHelper.printTemporalDatasetTable(System.out, events,"label","class",TemporalObject.ID);
			
			patterns = gmltdr.readData("data/patterns.graphml.gz");
						
			//DebugHelper.printTemporalDatasetForest(System.out,patterns, "label",TemporalObject.ID);						
		} catch (DataIOException e) {
			e.printStackTrace();
		}			
		
        //DataHelper.printMetadata(System.out, events.getNodeTable());
		//DataHelper.printMetadata(System.out, patterns.getNodeTable());
		
		TreeDebundlingAction action = new TreeDebundlingAction(patterns);
		action.run(0);
		flatPatterns = action.getTemporalDataset();
		classes = action.getClasses();

		System.out.println(flatPatterns.getNodeCount());
		//DebugHelper.printTemporalDatasetTable(System.out, flatPatterns,"label","class",TemporalObject.ID);
		
		PatternCountAction action2 = new PatternCountAction(flatPatterns);
		action2.run(0);
		countedPatterns = action2.getTemporalDataset();
		
		//DebugHelper.printTemporalDatasetTable(System.out, countedPatterns);
		
		Hashtable<String,Integer> patternCount = action2.getPatterns();
//		System.out.println(patternCount.size());
//		Enumeration<String> e = patternCount.keys();
//		while(e.hasMoreElements()) {
//			String pattern = e.nextElement();
//			System.out.println(pattern+": "+patternCount.get(pattern));
//		}

		//System.out.println(flatPatterns.getNodeCount());
		//DataHelper.printTable(System.out,countedPatterns.getTemporalObjectTable());
		//try {
			//DataHelper.printTable(new PrintStream("test.txt"),countedPatterns.getTemporalObjectTable());
		//} catch (FileNotFoundException e) {e.printStackTrace();}
		
        createVisualization(patterns,events,flatPatterns,countedPatterns);
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
                item.setVisible(node.isVisible());
            }
        }
    }
    
    static class DecoratorLayout2 extends Layout {
        public DecoratorLayout2(String group) {
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
                double x = node.getDouble("labelX");
                double y = node.getDouble("labelY");
                if (x != 0 && y != 0 && m_vis.getDisplay(0).contains((int)x,(int)y)) {
                	setX(item, null, x);
                	setY(item, null, y);
                    item.setVisible(node.isVisible());
                } else
                    item.setVisible(false);
            }
        }
    }
    
    static class ViewSwitchControl extends ControlAdapter {
    	int[] enforcedView;
    	
		public ViewSwitchControl(int[] enforcedView) {
			this.enforcedView = enforcedView;
		}
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			enforcedView[0]+=e.getWheelRotation();
			if (enforcedView[0] == 3)
				enforcedView[0] = -1;
			
			((Display)e.getComponent()).getVisualization().run(DemoEnvironmentFactory.ACTION_UPDATE);
		}
		
		@Override
		public void itemWheelMoved(VisualItem item, MouseWheelEvent e) {
			this.mouseWheelMoved(e);
		}	
    }
    
    static class EnforceViewAction extends Action {

    	int[] enforcedView;
        private static final String ARCDIAGRAM_PATTERNS = "arcdiagram_patterns"; // Don't know if . is reserved in prefuse
        private static final String ARCDIAGRAM_EVENTS = "arcdiagram_events"; // Don't know if . is reserved in prefuse
        private static final String PATTERNTIMELINES = "patterntimelines";
        private static final String PATTERNTHEMERIVER = "patternthemeriver";
        
        private static final String PATTERNTIMELINES_DECORATOR = "patterntimelines_decorator";
        private static final String PATTERNTHEMERIVER_DECORATOR = "patternthemeriver_decorator";
    	
		public EnforceViewAction(int[] enforcedView) {
			super();
			this.enforcedView = enforcedView;
		}

		@Override
		public void run(double frac) {
			switch(enforcedView[0]) {
			case 0:
				setVisibility(m_vis.getGroup(ARCDIAGRAM_PATTERNS),true);
				setVisibility(m_vis.getGroup(ARCDIAGRAM_EVENTS),true);
				setVisibility(m_vis.getGroup(PATTERNTIMELINES),false);
				setVisibility(m_vis.getGroup(PATTERNTHEMERIVER),false);
				setVisibility(m_vis.getGroup(PATTERNTIMELINES_DECORATOR),false);
				setVisibility(m_vis.getGroup(PATTERNTHEMERIVER_DECORATOR),false);
				break;
			case 1:
				setVisibility(m_vis.getGroup(ARCDIAGRAM_PATTERNS),false);
				setVisibility(m_vis.getGroup(ARCDIAGRAM_EVENTS),false);
				setVisibility(m_vis.getGroup(PATTERNTIMELINES),true);
				setVisibility(m_vis.getGroup(PATTERNTHEMERIVER),false);
				setVisibility(m_vis.getGroup(PATTERNTIMELINES_DECORATOR),true);
				setVisibility(m_vis.getGroup(PATTERNTHEMERIVER_DECORATOR),false);
				break;
			case 2:
				setVisibility(m_vis.getGroup(ARCDIAGRAM_PATTERNS),false);
				setVisibility(m_vis.getGroup(ARCDIAGRAM_EVENTS),false);
				setVisibility(m_vis.getGroup(PATTERNTIMELINES),false);
				setVisibility(m_vis.getGroup(PATTERNTHEMERIVER),true);
				setVisibility(m_vis.getGroup(PATTERNTIMELINES_DECORATOR),false);
				setVisibility(m_vis.getGroup(PATTERNTHEMERIVER_DECORATOR),true);
				break;
			}
		}

		private void setVisibility(TupleSet group, boolean b) {
			Iterator i = group.tuples();
			while(i.hasNext()) {
				((VisualItem)i.next()).setVisible(b);
			}			
		}
    	
    }
}

