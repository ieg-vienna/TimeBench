package org.timebench.action.assignment;

import ieg.prefuse.data.DataHelper;
import ieg.prefuse.data.ParentChildNode;
import ieg.prefuse.data.expression.IsNanPredicate;

import java.util.Map;
import java.util.logging.Logger;

import org.timebench.data.GranularityAggregationTree;
import org.timebench.util.BiColorMap;
import org.timebench.util.ColorLib;

import prefuse.Constants;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.data.CascadedTable;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.expression.AndPredicate;
import prefuse.data.expression.ColumnExpression;
import prefuse.data.expression.ComparisonPredicate;
import prefuse.data.expression.NotPredicate;
import prefuse.data.expression.NumericLiteral;
import prefuse.data.expression.Predicate;
import prefuse.data.tuple.TupleSet;
import prefuse.data.util.FilterIteratorFactory;
import prefuse.util.DataLib;
import prefuse.util.MathLib;
import prefuse.util.PrefuseLib;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 * 
 * 
 * <p>
 * Added:          / TL<br>
 * Modifications: 
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public class OverlayDataColorAction extends DataColorAction {
	protected int[][] m_palette; 
	protected int m_type1,m_type2;
    protected BiColorMap m_cmap = new BiColorMap(null,0.0,1.0,0.0,1.0);
    protected double[][] m_dist;
    protected boolean m_dist_separation;
    protected int m_separation_level;
    protected Map[] m_omaps;
    protected GranularityAggregationTree m_tree = null;
    protected String m_group_without_terror;
	
    public OverlayDataColorAction(String group, String dataField, 
            int dataType, String colorField, boolean distSeparation,int separationLevel)
    {
    	super(PrefuseLib.getGroupName(group, Graph.NODES), dataField,dataType,colorField);    	
    	setDataType(dataType);
    	setDataField(dataField);
    	m_dist_separation = distSeparation;
    	m_separation_level = separationLevel;
    	m_group_without_terror = group;
    }

    public OverlayDataColorAction(String group, String dataField, 
    		int dataType, String colorField, boolean distSeparation, int separationLevel, int[][] palette)
    {
    	this(group, dataField,dataType,colorField,distSeparation,separationLevel);
    	m_palette = palette;
    }
    
    @Override
    protected void setup() {
        int size = 64;
        
        int[][] palette = m_palette;
             
    	m_tree = (GranularityAggregationTree)m_vis.getSourceData(m_group_without_terror);    	
        
        // switch up scale if necessary
        m_tempScale = m_scale;
        if ( m_scale == Constants.QUANTILE_SCALE && m_bins <= 0 ) {
            Logger.getLogger(getClass().getName()).warning(
                    "Can't use quantile scale with no binning. " +
                    "Defaulting to linear scale. Set the bin value " +
                    "greater than zero to use a quantile scale.");
            m_scale = Constants.LINEAR_SCALE;
        }
        
        // compute distribution and color map
        switch ( m_type ) {
        case Constants.NOMINAL:
        case Constants.ORDINAL:
            m_dist = getDistributions();
            size = m_omap.size();
            palette = (m_palette!=null ? m_palette : createBiPalette(size));
            m_cmap.setColorPalette(palette);
            m_cmap.setMinValue1(m_dist[0][0]);
            m_cmap.setMaxValue1(m_dist[0][1]);
            m_cmap.setMinValue2(m_dist[1][0]);
            m_cmap.setMaxValue2(m_dist[1][1]);
            return;
        case Constants.NUMERICAL:
            m_dist = getDistributions();
            size = m_bins > 0 ? m_bins : size;
            palette = (m_palette!=null ? m_palette : createBiPalette(size));
            m_cmap.setColorPalette(palette);
            m_cmap.setMinValue1(0.0);
            m_cmap.setMaxValue1(1.0);
            m_cmap.setMinValue2(0.0);
            m_cmap.setMaxValue2(1.0);
            return;
        }    
    }
    
    protected double[][] getDistributions() {
    	int level0,level1;
    	if (m_dist_separation) {
    		level0 = Math.max(0,m_separation_level-1);
    		level1 = m_tree.getMaxDepth();
    	} else {
    		level0 = m_tree.getMaxDepth();    		
    		level1 = m_tree.getMaxDepth();    		
    	}
        TupleSet ts = m_vis.getVisualGroup(m_group);
        Predicate prednan = new NotPredicate(new IsNanPredicate(new ColumnExpression(m_dataField)));        
        Predicate pred0 = new AndPredicate(prednan,  
        	new ComparisonPredicate(ComparisonPredicate.LTEQ, new ColumnExpression( ParentChildNode.DEPTH ), new NumericLiteral(level0)));
        //DataHelper.printTable(System.out, (Table)ts);
        TupleSet ts0 = new CascadedTable((Table)ts, pred0);  
        Predicate pred1 = new AndPredicate(prednan,
        	new ComparisonPredicate(ComparisonPredicate.LTEQ, new ColumnExpression( ParentChildNode.DEPTH ), new NumericLiteral(level1)));
        TupleSet ts1 = new CascadedTable((Table)ts, pred1);
        double[][] result = new double[2][];

        if ( m_type == Constants.NUMERICAL ) {
            m_omaps = null;
            if ( m_scale == Constants.QUANTILE_SCALE && m_bins > 0 ) {
                double[] values0 = DataLib.toDoubleArray(ts0.tuples(), m_dataField);               
                double[] values1 = DataLib.toDoubleArray(ts1.tuples(), m_dataField);               
                result[0] = MathLib.quantiles(m_bins, values0);
                result[1] = MathLib.quantiles(m_bins, values1);
                return result;
            } else {
                double[][] dist = new double[2][2];
                dist[0][0] = DataLib.min(ts0, m_dataField).getDouble(m_dataField);
                dist[0][1] = DataLib.max(ts0, m_dataField).getDouble(m_dataField);
                dist[1][0] = DataLib.min(ts1, m_dataField).getDouble(m_dataField);
                dist[1][1] = DataLib.max(ts1, m_dataField).getDouble(m_dataField);
                return dist;
            }
        } else {
            if ( m_olist == null ) { 
                m_omaps = new Map[2];
                m_omaps[0] = DataLib.ordinalMap(ts0, m_dataField);
                m_omaps[1] = DataLib.ordinalMap(ts1, m_dataField);
            }
            return new double[][] { { 0, m_omaps[0].size()-1 },{ 0, m_omaps[1].size()-1 } };
        }
    }
    
    protected int[][] createBiPalette(int size) {
        switch ( m_type ) {
        case Constants.NOMINAL:
            return ColorLib.getCategoryPalette(size,size);
        case Constants.NUMERICAL:
        case Constants.ORDINAL:
        default:
            return ColorLib.getOrdinalPalette(size,size);
        }
    } 
    
    @Override
    public int getColor(VisualItem item) {
        // check for any cascaded rules first
        Object o = lookup(item);
        if ( o != null ) {
            if ( o instanceof ColorAction ) {
                return ((ColorAction)o).getColor(item);
            } else if ( o instanceof Integer ) {
                return ((Integer)o).intValue();
            } else {
                Logger.getLogger(this.getClass().getName())
                    .warning("Unrecognized Object from predicate chain.");
            }
        }
        
        // otherwise perform data-driven assignment
        double f0 = Double.NaN,f1 = Double.NaN;
        switch ( m_type ) {
        case Constants.NUMERICAL:
        	if(item.getInt(ParentChildNode.DEPTH) >= m_separation_level) {
        		VisualItem parent = item;
        		while(parent.getInt(ParentChildNode.DEPTH) >= m_separation_level)
        			parent = (VisualItem)(((NodeItem)parent).getParent());
        		double v = item.getDouble(m_dataField);
        		f1 = MathLib.interp(m_scale, v, m_dist[1]);
        		v = parent.getDouble(m_dataField);
        		f0 = MathLib.interp(m_scale, v, m_dist[0]);
        	} else {
        		double v = item.getDouble(m_dataField);
        		f0 = MathLib.interp(m_scale, v, m_dist[0]);
        		f1 = 0.5;
        	}
            return m_cmap.getColor(f0,f1);
        default:
            Integer idx0 = (Integer)m_omaps[0].get(item.get(m_dataField));
            Integer idx1 = (Integer)m_omaps[1].get(item.get(m_dataField));
            return m_cmap.getColor(idx0.doubleValue(),idx1.doubleValue());
        }
    }
}
