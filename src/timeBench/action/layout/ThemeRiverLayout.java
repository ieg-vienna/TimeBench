package timeBench.action.layout;

import ieg.prefuse.data.DataHelper;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;

import prefuse.Constants;
import prefuse.action.layout.Layout;
import prefuse.data.Table;
import prefuse.data.expression.BooleanLiteral;
import prefuse.data.expression.Predicate;
import prefuse.data.tuple.TupleSet;
import prefuse.data.util.Index;
import prefuse.render.PolygonRenderer;
import prefuse.util.collections.IntIterator;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;
import prefuse.visual.expression.VisiblePredicate;
import timeBench.action.layout.timescale.AdvancedTimeScale;
import timeBench.action.layout.timescale.TimeScale;
import timeBench.calendar.JavaDateCalendarManager;
import timeBench.data.AnchoredTemporalElement;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalElement;
import timeBench.data.TemporalObject;
import timeBench.util.DebugHelper;

public class ThemeRiverLayout extends Layout {

    protected TimeScale timeScale = null;
    protected TemporalDataset sourceDataset = null;

    protected Predicate m_filter = VisiblePredicate.TRUE;
    
    protected Hashtable<String, Integer> classes = null;

    public ThemeRiverLayout(String group,TemporalDataset sourceDataset, Hashtable<String, Integer> classes) {
        super(group);
        this.sourceDataset = sourceDataset;
        this.classes = classes;
    }

    public ThemeRiverLayout(String group,TemporalDataset source,  Hashtable<String, Integer> classes2, TimeScale timeScale) {
        super(group);
        this.sourceDataset = source;
        this.classes = classes2;
        setTimeScale(timeScale);
    }

    @SuppressWarnings({ "rawtypes" })
    @Override
    public void run(double frac) {
        if (timeScale == null) {
            throw new RuntimeException("cannot layout without timescale");
        }
        
        VisualTable workingDataset = (VisualTable)m_vis.getGroup(m_group);
        Table workingBaseDataset = null;
        if (workingDataset == null || !(workingDataset instanceof VisualTable)) {
        	workingBaseDataset = new Table(0,0);
        	workingBaseDataset.addColumn("label", String.class);
        	workingBaseDataset.addColumn("class", int.class);
        	workingBaseDataset.addColumn("labelX", double.class);
        	workingBaseDataset.addColumn("labelY", double.class);
        	workingBaseDataset.addColumn(PolygonRenderer.POLYGON, float[].class);
        	m_vis.addTable(m_group,workingBaseDataset);
            m_vis.addDecorators(m_group+"_decorator", m_group);
        } else {
        	workingBaseDataset = (Table) m_vis.getSourceData(m_group);
        }
                
        int[] dataColumnIndices = sourceDataset.getDataColumnIndices();              
        
        for (int i : dataColumnIndices) {
        	int index = workingBaseDataset.addRow();
        	workingBaseDataset.set(index,"label",sourceDataset.getNodeTable().getSchema().getColumnName(i));
        	workingBaseDataset.set(index,"class",classes.get(sourceDataset.getNodeTable().getSchema().getColumnName(i)));
        }
        
        int medIndex = dataColumnIndices.length/2;
        float[][] buffer = new float[dataColumnIndices.length][sourceDataset.getTemporalObjectCount()*4];
        int i=0;
        float maxUpper = 0;
        float maxLower = 0;
        IntIterator elIterator = sourceDataset.getTemporalElements().getNodeTable().index(TemporalElement.INF).allRows(Index.TYPE_ASCENDING);
        while (elIterator.hasNext()) {
            TemporalElement el = sourceDataset.getTemporalElements().getTemporalPrimitiveByRow(elIterator.nextInt());
            TemporalObject iTO = (TemporalObject)el.temporalObjects().iterator().next();
        	float upper = 0;
        	float lower = 0;
        	//System.out.print(JavaDateCalendarManager.formatDebugString(el.asGeneric().getInf())+"|");
        	for(int j=0; j<dataColumnIndices.length; j++) {
            	buffer[j][i*2] = timeScale.getPixelForDate(iTO.getTemporalElement().asGeneric().getInf());
            	float val = iTO.getFloat(dataColumnIndices[j]);
            	//System.out.print(val+"|");
        		if(j % 2 == 0) {
            		upper += val;
            		buffer[j][i*2+1] = upper;
            	} else {
            		lower -= val;
            		buffer[j][i*2+1] = lower;
            	}
        	}
        	//System.out.println();
        	maxUpper = Math.max(maxUpper,upper);
        	maxLower = Math.min(maxLower,lower);
            i++;
        }
    	float max = maxUpper-maxLower;
    	
        float height = (float)m_vis.getDisplay(0).getBounds().getHeight();
        //float yBase = (float)m_vis.getDisplay(0).getBounds().getY();
        float factor = height/max;
        for(int j=0; j<buffer.length; j++) {
            float maxDiff = 0;
            float xAtMaxDiff = 0;
            float yAtMaxDiff = 0;
        	for(int k=0; k<buffer[j].length/2; k+=2) {
       			buffer[j][k+1] -= maxLower;
       			buffer[j][k+1] *= factor;     		
       			//buffer[j][k+1] += yBase;       			
   				buffer[j][sourceDataset.getTemporalObjectCount()*4-k-2] = buffer[j][k];
       			if(j>1) {
       				buffer[j][sourceDataset.getTemporalObjectCount()*4-k-1] = buffer[j-2][k+1];
       			} else {
       				buffer[j][sourceDataset.getTemporalObjectCount()*4-k-1] = -maxLower*factor/*+yBase*/;
       			}
            	if (Math.abs(buffer[j][k+1]-buffer[j][sourceDataset.getTemporalObjectCount()*4-k-1]) > maxDiff) {
            		maxDiff = Math.abs(buffer[j][k+1]-buffer[j][sourceDataset.getTemporalObjectCount()*4-k-1]);
            		xAtMaxDiff = buffer[j][k];
            		yAtMaxDiff = buffer[j][k+1] - (buffer[j][k+1] - buffer[j][sourceDataset.getTemporalObjectCount()*4-k-1])/2f;
            	}
        	}
        	workingBaseDataset.set(j, PolygonRenderer.POLYGON, buffer[j]);
        	//workingBaseDataset.set(j, "labelX" , xAtMaxDiff);
        	//workingBaseDataset.set(j, "labelY" , yAtMaxDiff);
        }
        
        System.out.println(workingBaseDataset.getRowCount());
        //DataHelper.printTable(System.out, workingBaseDataset);
    }
    
    public TimeScale getTimeScale() {
        return timeScale;
    }

    public void setTimeScale(TimeScale timeScale) {
        this.timeScale = timeScale;
    }
}
