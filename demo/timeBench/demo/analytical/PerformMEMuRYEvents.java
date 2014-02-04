package timeBench.demo.analytical;

import ieg.prefuse.renderer.IntervalBarRenderer;
import ieg.util.xml.JaxbMarshaller;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
import prefuse.data.expression.NumericLiteral;
import prefuse.data.expression.Predicate;
import prefuse.data.io.DataIOException;
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
import timeBench.action.layout.IntervalAxisLayout;
import timeBench.action.layout.TimeAxisLayout;
import timeBench.action.layout.timescale.AdvancedTimeScale;
import timeBench.action.layout.timescale.RangeAdapter;
import timeBench.calendar.JavaDateCalendarManager;
import timeBench.controls.BranchHighlightControl;
import timeBench.controls.RangePanControl;
import timeBench.controls.RangeZoomControl;
import timeBench.data.GenericTemporalElement;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalObject;
import timeBench.data.expression.TemporalComparisonPredicate;
import timeBench.data.expression.TemporalElementArrayExpression;
import timeBench.data.expression.TemporalElementExpression;
import timeBench.data.expression.TemporalShiftExpression;
import timeBench.data.io.GraphMLTemporalDatasetWriter;
import timeBench.data.io.TextTableTemporalDatasetReader;
import timeBench.data.io.schema.TemporalDataColumnSpecification;
import timeBench.ui.MouseTracker;
import timeBench.ui.TimeAxisDisplay;
import timeBench.ui.TimeScaleHeader;
import timeBench.ui.TimeScalePainter;
import timeBench.util.DebugHelper;
import visual.sort.SizeItemSorter;

public class PerformMEMuRYEvents {

    /**
     * @param args
     * @throws TemporalDataException
     */
    public static void main(String[] args) throws TemporalDataException {
    	
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    	
//		String datasetFileName = "data\\Dodgers-de-1W.csv";
		String datasetFileName = "data\\Dodgers-de-raster3.csv";
//		String datasetFileName = "data\\Dodgers-de-raster4.csv";
		TemporalDataset sourceDataset = null;
		
        //UILib.setPlatformLookAndFeel();

		try {
	        TemporalDataColumnSpecification spec2 = (TemporalDataColumnSpecification) JaxbMarshaller
	                .load("spec-dodgers-raster3.xml", TemporalDataColumnSpecification.class);
//	        TemporalDataColumnSpecification spec2 = (TemporalDataColumnSpecification) JaxbMarshaller
//	                .load("spec-dodgers-raster4.xml", TemporalDataColumnSpecification.class);
	        TextTableTemporalDatasetReader reader = new TextTableTemporalDatasetReader(
	                spec2);
//	        TextTableTemporalDatasetReader reader = new TextTableTemporalDatasetReader();
	        sourceDataset = reader.readData(datasetFileName);			
		} catch(TemporalDataException e) {
			System.out.println("TemporalDataException while reading data: " + e.getMessage());
			System.exit(1);
		} catch (DataIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		for (GenericTemporalElement iE : sourceDataset.temporalElements()) {
//			iE.setSup(iE.getSup()+1000L*60L*5L-1L);
//		}
		
//		DebugHelper.printTemporalDatasetTable(System.out, sourceDataset);		
								
		Predicate[] templates = new Predicate[4];
		templates[0] =  new AndPredicate(
//				new ComparisonPredicate(ComparisonPredicate.GTEQ, new ColumnExpression("value"), new NumericLiteral(0)),
//				new ComparisonPredicate(ComparisonPredicate.LTEQ, new ColumnExpression("value"), new NumericLiteral(9)));
//			templates[1] =  new AndPredicate(
//					new ComparisonPredicate(ComparisonPredicate.GTEQ, new ColumnExpression("value"), new NumericLiteral(10)),
//					new ComparisonPredicate(ComparisonPredicate.LTEQ, new ColumnExpression("value"), new NumericLiteral(22)));
//			templates[2] =  new AndPredicate(
//					new ComparisonPredicate(ComparisonPredicate.GTEQ, new ColumnExpression("value"), new NumericLiteral(23)),
//					new ComparisonPredicate(ComparisonPredicate.LTEQ, new ColumnExpression("value"), new NumericLiteral(31)));
//			templates[3] =  new AndPredicate(
//					new ComparisonPredicate(ComparisonPredicate.GTEQ, new ColumnExpression("value"), new NumericLiteral(32)),
//					new ComparisonPredicate(ComparisonPredicate.LTEQ, new ColumnExpression("value"), new NumericLiteral(90)));

		new ComparisonPredicate(ComparisonPredicate.GTEQ, new ColumnExpression("value"), new NumericLiteral(0)),
			new ComparisonPredicate(ComparisonPredicate.LTEQ, new ColumnExpression("value"), new NumericLiteral(105)));
		templates[1] =  new AndPredicate(
				new ComparisonPredicate(ComparisonPredicate.GTEQ, new ColumnExpression("value"), new NumericLiteral(106)),
				new ComparisonPredicate(ComparisonPredicate.LTEQ, new ColumnExpression("value"), new NumericLiteral(277)));
		templates[2] =  new AndPredicate(
				new ComparisonPredicate(ComparisonPredicate.GTEQ, new ColumnExpression("value"), new NumericLiteral(278)),
				new ComparisonPredicate(ComparisonPredicate.LTEQ, new ColumnExpression("value"), new NumericLiteral(364)));
		templates[3] =  new AndPredicate(
				new ComparisonPredicate(ComparisonPredicate.GTEQ, new ColumnExpression("value"), new NumericLiteral(365)),
				new ComparisonPredicate(ComparisonPredicate.LTEQ, new ColumnExpression("value"), new NumericLiteral(662)));
			
//		templates[0] =  new AndPredicate(
//			new ComparisonPredicate(ComparisonPredicate.GTEQ, new ColumnExpression("value"), new NumericLiteral(3518)),
//			new ComparisonPredicate(ComparisonPredicate.LTEQ, new ColumnExpression("value"), new NumericLiteral(5450)));
//		templates[1] =  new AndPredicate(
//				new ComparisonPredicate(ComparisonPredicate.GTEQ, new ColumnExpression("value"), new NumericLiteral(5451)),
//				new ComparisonPredicate(ComparisonPredicate.LTEQ, new ColumnExpression("value"), new NumericLiteral(6076)));
//		templates[2] =  new AndPredicate(
//				new ComparisonPredicate(ComparisonPredicate.GTEQ, new ColumnExpression("value"), new NumericLiteral(6077)),
//				new ComparisonPredicate(ComparisonPredicate.LTEQ, new ColumnExpression("value"), new NumericLiteral(6566)));
//		templates[3] =  new AndPredicate(
//				new ComparisonPredicate(ComparisonPredicate.GTEQ, new ColumnExpression("value"), new NumericLiteral(6567)),
//				new ComparisonPredicate(ComparisonPredicate.LTEQ, new ColumnExpression("value"), new NumericLiteral(7661)));
			
		IntervalEventFindingAction action = new IntervalEventFindingAction(sourceDataset, templates,IntervalEventFindingAction.SPACING_ALLOWED);
		action.setDoMutiny(false);
		action.run(0.0);

		TemporalDataset events = action.getTemporalDataset();

		DebugHelper.printTemporalDatasetTable(System.out, events,"label","class",TemporalObject.ID);
		System.out.println(events.getNodeCount());
		
		GraphMLTemporalDatasetWriter writer = new GraphMLTemporalDatasetWriter();
		try {
			GZIPOutputStream eventStream = new GZIPOutputStream(new FileOutputStream("data\\events.graphml.gz"));
			writer.writeData(events,eventStream);
			eventStream.close();
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
}
