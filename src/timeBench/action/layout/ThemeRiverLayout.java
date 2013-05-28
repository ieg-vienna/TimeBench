package timeBench.action.layout;

import java.util.Iterator;

import org.apache.log4j.Logger;

import prefuse.Constants;
import prefuse.action.layout.Layout;
import prefuse.data.Table;
import prefuse.data.expression.Predicate;
import prefuse.data.tuple.TupleSet;
import prefuse.render.PolygonRenderer;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;
import prefuse.visual.expression.VisiblePredicate;
import timeBench.action.layout.timescale.TimeScale;
import timeBench.data.AnchoredTemporalElement;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalElement;
import timeBench.data.TemporalObject;

public class ThemeRiverLayout extends Layout {

    protected TimeScale timeScale = null;
    protected TemporalDataset sourceDataset = null;

    protected Predicate m_filter = VisiblePredicate.TRUE;

    public ThemeRiverLayout(String group,TemporalDataset sourceDataset) {
        super(group);
        this.sourceDataset = sourceDataset;
    }

    public ThemeRiverLayout(String group,TemporalDataset source, TimeScale timeScale) {
        super(group);
        this.sourceDataset = source;
        setTimeScale(timeScale);
    }

    @SuppressWarnings({ "rawtypes" })
    @Override
    public void run(double frac) {
        if (timeScale == null) {
            throw new RuntimeException("cannot layout without timescale");
        }
        
        TupleSet workingDataset = m_vis.getGroup(m_group);
        Table workingBaseDataset = null;
        if (workingDataset == null || !(workingDataset instanceof VisualTable)) {
        	workingBaseDataset = new Table(0,0);
        	workingBaseDataset.addColumn("label", String.class);
        	workingBaseDataset.addColumn(PolygonRenderer.POLYGON, float[].class);
        	workingDataset = new VisualTable(workingBaseDataset, m_vis, m_group);
        } else {
        	workingBaseDataset = (Table) m_vis.getSourceData(m_group);
        }
        
        int[] dataColumnIndices = sourceDataset.getDataColumnIndices();
        
        for (int i : dataColumnIndices) {
        	int index = workingBaseDataset.addRow();
        	workingBaseDataset.set(index,"label",sourceDataset.getDataColumnSchema().getColumnName(i));
        }
        
        int medIndex = dataColumnIndices.length/2;
        float[][] buffer = new float[dataColumnIndices.length][sourceDataset.getTemporalObjectCount()*2];
        int i=0;
        float max = 0;
        float medPos = 0;
        for(TemporalObject iTO : sourceDataset.temporalObjects()) {
        	float upper = 0;
        	float lower = 0;
        	for(int j=0; j<dataColumnIndices.length; j++) {
            	buffer[j][i*2] = timeScale.getPixelForDate(iTO.getTemporalElement().asGeneric().getInf());
            	float val = (float)iTO.getInt(dataColumnIndices[j]);
            	if(j <= medIndex) {
            		upper += val;
            		buffer[j][i*2+1] = upper;
            	} else {
            		lower -= val;
            		buffer[j][i*2+1] = lower;
            	}            	
        	}
        	float diff = upper-lower;
        	if(diff > max) {
        		max = diff;
        		medPos = -lower;
        	}
            i++;
        }

        float height = (float)m_vis.getBounds(m_group).getHeight();
        float yBase = (float)m_vis.getBounds(m_group).getY();
        float factor = height/max;
        for(int j=0; j<buffer.length; j++) {
        	for(int k=0; k<buffer[j].length; k+=2) {
       			buffer[j][k+1] += medPos;
       			buffer[j][k+1] *= factor;     		
       			buffer[j][k+1] += yBase;
        	}
        	workingBaseDataset.set(j, PolygonRenderer.POLYGON, buffer[j]);
        }
    }
    
    public TimeScale getTimeScale() {
        return timeScale;
    }

    public void setTimeScale(TimeScale timeScale) {
        this.timeScale = timeScale;
    }
}
