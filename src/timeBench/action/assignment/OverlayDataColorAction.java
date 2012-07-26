package timeBench.action.assignment;

import java.util.logging.Logger;

import prefuse.Constants;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.util.MathLib;
import prefuse.visual.VisualItem;
import timeBench.util.BiColorMap;
import timeBench.util.ColorLib;

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
    protected BiColorMap m_cmap;
	
    public OverlayDataColorAction(String group, String dataField, 
            int dataType, String colorField)
    {
    	super(group, dataField,dataType,colorField);
    	setDataType(dataType);
    	setDataField(dataField);
    }

    public OverlayDataColorAction(String group, String dataField, 
    		int dataType, String colorField, int[][] palette)
    {
    	this(group, dataField,dataType,colorField);
    	m_palette = palette;
    }
    
    @Override
    protected void setup() {
        int size = 64;
        
        int[][] palette = m_palette;
             
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
            m_dist = getDistribution();
            size = m_omap.size();
            palette = (m_palette!=null ? m_palette : createBiPalette(size));
            m_cmap.setColorPalette(palette);
            m_cmap.setMinValue1(m_dist[0]);
            m_cmap.setMaxValue1(m_dist[1]);
            m_cmap.setMinValue2(m_dist[0]);
            m_cmap.setMaxValue2(m_dist[1]);
            return;
        case Constants.NUMERICAL:
            m_dist = getDistribution();
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
    
    /*@Override
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
        switch ( m_type ) {
        case Constants.NUMERICAL:
            double v = item.getDouble(m_dataField);
            double f = MathLib.interp(m_scale, v, m_dist);
            return m_cmap.getColor(f);
        default:
            Integer idx = (Integer)m_omap.get(item.get(m_dataField));
            return m_cmap.getColor(idx.doubleValue());
        }
    }*/
}
