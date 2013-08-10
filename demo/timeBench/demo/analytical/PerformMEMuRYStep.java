package timeBench.demo.analytical;

import ieg.prefuse.data.ParentChildGraph;
import ieg.prefuse.data.ParentChildNode;
import ieg.prefuse.renderer.IntervalBarRenderer;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.GZIPOutputStream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.AxisLayout;
import prefuse.data.expression.AndPredicate;
import prefuse.data.expression.ColumnExpression;
import prefuse.data.expression.ComparisonPredicate;
import prefuse.data.expression.NotPredicate;
import prefuse.data.expression.NumericLiteral;
import prefuse.data.expression.OrPredicate;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.data.io.DataIOException;
import prefuse.data.tuple.TableEdge;
import prefuse.render.Renderer;
import prefuse.render.RendererFactory;
import prefuse.util.ColorLib;
import prefuse.util.display.PaintListener;
import prefuse.util.ui.JRangeSlider;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import render.ArcRenderer;
import timeBench.action.analytical.GranularityAggregationAction;
import timeBench.action.analytical.IntervalEventFindingAction;
import timeBench.action.analytical.MultiPredicatePatternDiscovery;
import timeBench.action.analytical.PatternInstanceCountAction;
import timeBench.action.layout.IntervalAxisLayout;
import timeBench.action.layout.TimeAxisLayout;
import timeBench.action.layout.timescale.AdvancedTimeScale;
import timeBench.action.layout.timescale.RangeAdapter;
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
import timeBench.data.io.GraphMLTemporalDatasetReader;
import timeBench.data.io.GraphMLTemporalDatasetWriter;
import timeBench.data.io.TextTableTemporalDatasetReader;
import timeBench.ui.MouseTracker;
import timeBench.ui.TimeAxisDisplay;
import timeBench.ui.TimeScaleHeader;
import timeBench.ui.TimeScalePainter;
import timeBench.util.DebugHelper;
import visual.sort.SizeItemSorter;

public class PerformMEMuRYStep {

    /**
     * @param args
     * @throws TemporalDataException
     */
    public static void main(String[] args) throws TemporalDataException {
		
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    	
    	TemporalDataset events = null;

		try {
			GraphMLTemporalDatasetReader gmltdr = new GraphMLTemporalDatasetReader();
			events = gmltdr.readData("data/events.graphml.gz");			
		} catch (DataIOException e) {
			e.printStackTrace();
		}
								
		System.out.println(events.getNodeCount());
		DebugHelper.printTemporalDatasetTable(System.out, events,"label","class",TemporalObject.ID);
		
		TemporalDataset patterns = events;
		
		for(int i=0; i<3; i++) {
		
			MultiPredicatePatternDiscovery action = new MultiPredicatePatternDiscovery(patterns, 
					events, new Predicate[] {
//					new TemporalComparisonPredicate(TemporalComparisonPredicate.MEETS,
//							new TemporalElementExpression(),
//							new TemporalElementArrayExpression(new ArrayList<TemporalObject>())),
//					new TemporalComparisonPredicate(TemporalComparisonPredicate.MEETS,
//							new TemporalShiftExpression(new TemporalElementExpression(),-1000L*60L*5L),
//							new TemporalElementArrayExpression(new ArrayList<TemporalObject>())),
//					new TemporalComparisonPredicate(TemporalComparisonPredicate.STARTS,
//							new TemporalShiftExpression(new TemporalElementExpression(),-1000L*60L*60L),
//							new TemporalElementArrayExpression(new ArrayList<TemporalObject>())),

							new TemporalComparisonPredicate(TemporalComparisonPredicate.MEETS,
									new TemporalElementExpression(),
									new TemporalElementArrayExpression(new ArrayList<TemporalObject>())),
							new TemporalComparisonPredicate(TemporalComparisonPredicate.MEETS,
									new TemporalShiftExpression(new TemporalElementExpression(),-1000L*60L*60L),
									new TemporalElementArrayExpression(new ArrayList<TemporalObject>())),
							new TemporalComparisonPredicate(TemporalComparisonPredicate.STARTS,
									new TemporalShiftExpression(new TemporalElementExpression(),-1000L*60L*60L*24L),
									new TemporalElementArrayExpression(new ArrayList<TemporalObject>())),

//							new TemporalComparisonPredicate(TemporalComparisonPredicate.MEETS,
//									new TemporalElementExpression(),
//									new TemporalElementArrayExpression(new ArrayList<TemporalObject>())),
//							new TemporalComparisonPredicate(TemporalComparisonPredicate.MEETS,
//									new TemporalShiftExpression(new TemporalElementExpression(),-1000L*60L*60L*24L),
//									new TemporalElementArrayExpression(new ArrayList<TemporalObject>())),
//							new TemporalComparisonPredicate(TemporalComparisonPredicate.STARTS,
//									new TemporalShiftExpression(new TemporalElementExpression(),-1000L*60L*60L*24L*7L),
//									new TemporalElementArrayExpression(new ArrayList<TemporalObject>())),

							
							/*new AndPredicate(
						new NotPredicate(
								new OrPredicate(
										new TemporalComparisonPredicate(TemporalComparisonPredicate.OVERLAPS,
												new TemporalShiftExpression(new TemporalElementExpression(),-1000L*60L*60L*24L),
												new TemporalElementArrayExpression(new ArrayList<TemporalObject>())),
												new TemporalComparisonPredicate(TemporalComparisonPredicate.AFTER,
														new TemporalShiftExpression(new TemporalElementExpression(),-1000L*60L*60L*24L),
														new TemporalElementArrayExpression(new ArrayList<TemporalObject>())))),
						new OrPredicate(
								new TemporalComparisonPredicate(TemporalComparisonPredicate.OVERLAPS,
										new TemporalShiftExpression(new TemporalElementExpression(),-1000L*60L*60L*24L*7L),
										new TemporalElementArrayExpression(new ArrayList<TemporalObject>())),
										new TemporalComparisonPredicate(TemporalComparisonPredicate.AFTER,
												new TemporalShiftExpression(new TemporalElementExpression(),-1000L*60L*60L*24L*7L),
												new TemporalElementArrayExpression(new ArrayList<TemporalObject>()))))*/
			}, MultiPredicatePatternDiscovery.SPACING_ALLOWED);		
			action.run(0.0);
			patterns = action.getTemporalDataset();

			DebugHelper.printTemporalDatasetForest(System.out, patterns,"label");

			PatternInstanceCountAction action2 = new PatternInstanceCountAction(patterns);
			action2.run(0);
			ParentChildGraph countedPatterns = action2.getResult();

			int totalPattern = 0;
			Iterator rowIterator = countedPatterns.getNodeTable().rows(ExpressionParser.predicate("OUTDEGREE()=0"));
			while(rowIterator.hasNext()) {
				ParentChildNode root = (ParentChildNode)countedPatterns.getNode((Integer)rowIterator.next());
				totalPattern += root.getInt("count");
			}

//			int neededPattern = (int) Math.ceil((double)totalPattern*0.0);
			int neededPattern = (int) Math.ceil((double)totalPattern*0.005);
//			int neededPattern = (int) Math.ceil((double)totalPattern*0.0005);
			System.out.println("Needed: "+ neededPattern);

			ArrayList<ArrayList<Integer>> pruneTables = new ArrayList<ArrayList<Integer>>(); 
			rowIterator = countedPatterns.getNodeTable().rows(ExpressionParser.predicate("INDEGREE()=0"));
			while(rowIterator.hasNext()) {
				ParentChildNode leaf = (ParentChildNode)countedPatterns.getNode((Integer)rowIterator.next());
				if (leaf.getInt("count") < neededPattern) {
					pruneTables.add(new ArrayList<Integer>());
					int col = pruneTables.size()-1;
					pruneTables.get(col).add(leaf.getInt("class"));
					ParentChildNode parent = leaf.getFirstParent();
					pruneTables.get(col).add(((TableEdge)leaf.outEdges().next()).getInt("class"));
					while(parent != null) {
						leaf = parent;
						pruneTables.get(col).add(leaf.getInt("class"));
						parent = parent.getFirstParent();
						if(parent != null) {
							pruneTables.get(col).add(((TableEdge)leaf.outEdges().next()).getInt("class"));
						}
					}
				}
			}
			for(TemporalObject leaf : patterns.temporalObjects(ExpressionParser.predicate("INDEGREE()=0"))) {
				for(ArrayList<Integer> pruneTable : pruneTables) {
					if (isPrune(leaf,pruneTable,0)) {
						TemporalObject parent = (TemporalObject)leaf.getFirstParent();
						patterns.removeNode(leaf);
						while(parent != null) {
							if (parent.getFirstChild() == null) {
								leaf = parent;
								parent = (TemporalObject)leaf.getFirstParent();
								if (parent == null) {
									patterns.setRoot(leaf, false);
								}
								patterns.removeNode(leaf);
							} else
								break;
						}
						break;
					}
				}
			}

			DebugHelper.printTemporalDatasetForest(System.out, patterns,"label");
		
		}
		
		//DebugHelper.printTemporalDatasetForest(System.out, action3.getTemporalDataset(), "label",TemporalObject.ID);
				
		GraphMLTemporalDatasetWriter writer = new GraphMLTemporalDatasetWriter();
		try {
			GZIPOutputStream patternStream = new GZIPOutputStream(new FileOutputStream("data\\patterns.graphml.gz"));
			writer.writeData(patterns,patternStream);
			patternStream.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (DataIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    }

	private static boolean isPrune(TemporalObject leaf,ArrayList<Integer> pruneTable,int index) {
		if (leaf.getInt("class") == pruneTable.get(index)) {
			if(leaf.getFirstParent() == null) {
				return true;
			} else if (((TableEdge)leaf.outEdges().next()).getInt(MultiPredicatePatternDiscovery.predicateColumn) == pruneTable.get(index+1))
				return isPrune((TemporalObject)leaf.getFirstParent(),pruneTable,index+2);
		}
		
		return false;
	}
}
