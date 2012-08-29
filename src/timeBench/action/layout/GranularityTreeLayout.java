package timeBench.action.layout;

import prefuse.Constants;
import prefuse.action.GroupAction;
import prefuse.action.layout.Layout;
import prefuse.util.PrefuseLib;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

import ieg.prefuse.data.DataHelper;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;

import timeBench.data.GranularityAggregationTree;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalObject;
import timeBench.test.DebugHelper;

public class GranularityTreeLayout extends Layout {

	public static final int FITTING_FULL_AVAILABLE_SPACE = 0;
	public static final int FITTING_DEPENDING_ON_POSSIBLE_VALUES = 1;
	
    // XXX assume depth is given via size of settings
    protected int depth;

    // TODO consider circumstances to invalidate these min max ident. arrays
    protected long[] minIdentifiers;
    protected long[] maxIdentifiers;
    
    protected boolean[] axisActive = new boolean[Constants.AXIS_COUNT];
    
    HashMap<Integer,double[]> additionalVisualItemInformation = new HashMap<Integer, double[]>(); // size x,y before size stretching, half border size

    GranularityTreeLayoutSettings[] settings;

    public GranularityTreeLayout(String group,
            GranularityTreeLayoutSettings[] settings) {
        super(group);
        this.settings = settings;
        this.depth = settings.length;
    }

    @Override
    public void run(double frac) {

        GranularityAggregationTree tree = (GranularityAggregationTree) m_vis
                .getSourceData(m_group);
        TemporalObject root = tree.getTemporalObject(tree.getRoots()[0]);

        for(int i=0; i<Constants.AXIS_COUNT; i++) {
        	axisActive[i] = false;
        }
        
        try {
			calculateSizes(root);
        
			Rectangle2D bounds = this.getLayoutBounds();
        	VisualItem visRoot = m_vis.getVisualItem(m_group, root);
        	double xFactor = bounds.getWidth() / additionalVisualItemInformation.get(visRoot.getRow())[Constants.X_AXIS];
        	double yFactor = bounds.getHeight() / additionalVisualItemInformation.get(visRoot.getRow())[Constants.Y_AXIS];      
        
        	if (xFactor < yFactor)
        	{
        		double newHeight = additionalVisualItemInformation.get(visRoot.getRow())[Constants.Y_AXIS] * xFactor;
        		bounds.setRect(bounds.getX(),bounds.getY()+(bounds.getHeight()-newHeight)/2,bounds.getWidth(),newHeight);
        		calculatePositions(root,0,bounds,xFactor);
        	} else {
        		double newWidth = additionalVisualItemInformation.get(visRoot.getRow())[Constants.X_AXIS] * yFactor;
        		bounds.setRect(bounds.getX()+(bounds.getWidth()-newWidth)/2,bounds.getY(),bounds.getHeight(),newWidth);
            	calculatePositions(root,0,bounds,yFactor);
        	}       

            DataHelper.printGraph(System.out, (NodeItem)visRoot, null, VisualItem.X,VisualItem.Y,VisualItem.SIZE,VisualItem.SIZEY);
        } catch (TemporalDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
	 * @param root
     * @throws TemporalDataException 
	 */
	private void calculatePositions(TemporalObject node,int level,Rectangle2D bounds,double factor) throws TemporalDataException {
		
    	VisualItem visualNode = m_vis.getVisualItem(m_group, node);
    	
    	double[] aviivn = additionalVisualItemInformation.get(visualNode.getRow());
    	
    	if (axisActive[Constants.X_AXIS]) {
    		setX(visualNode,null,bounds.getCenterX());
    		PrefuseLib.setSizeX(visualNode,null,aviivn[Constants.X_AXIS]*factor);
    	}
    	if (axisActive[Constants.Y_AXIS]) {
    		setY(visualNode,null,bounds.getCenterY());
    		PrefuseLib.setSizeY(visualNode,null,aviivn[Constants.Y_AXIS]*factor);
    	}
		
    	double xbase = bounds.getX() + aviivn[Constants.AXIS_COUNT+Constants.X_AXIS]*factor;
    	double ybase = bounds.getY() + aviivn[Constants.AXIS_COUNT+Constants.Y_AXIS]*factor;
        if (level < depth) {
            for (TemporalObject o : node.childObjects()) {
            	double x = xbase;
            	double y = ybase;
            	VisualItem vo = m_vis.getVisualItem(m_group, o);
            	double[] aviivo = additionalVisualItemInformation.get(vo.getRow());
            	if(settings[level].getTargetAxis() == Constants.X_AXIS)
            		x += (o.getTemporalElement().getGranule().getIdentifier()-minIdentifiers[level]) * aviivo[Constants.X_AXIS]*factor;
            	if(settings[level].getTargetAxis() == Constants.Y_AXIS)
            		y += (o.getTemporalElement().getGranule().getIdentifier()-minIdentifiers[level]) * aviivo[Constants.Y_AXIS]*factor;
            	calculatePositions(o, level + 1, new Rectangle2D.Double(x,
            			y,
            			aviivo[Constants.X_AXIS]*factor,aviivo[Constants.Y_AXIS]*factor) ,factor);
            }
        }
        
        
	}

	private void calculateSizes(TemporalObject root)
            throws TemporalDataException {
        minIdentifiers = new long[depth];
        maxIdentifiers = new long[depth];

        TemporalObject node = root;
        for (int level = 0; level < depth; level++) {
            node = node.getFirstChildObject();
        	
        	if ( !settings[level].isIgnore() ) {
        	
        		if (null == node) {
        			throw new TemporalDataException(
        					"Aggregation Tree and Settings not matching at level "
        							+ level);
        		}

        		if (settings[level].getFitting() == FITTING_FULL_AVAILABLE_SPACE) {
        			minIdentifiers[level] = Long.MAX_VALUE;
        			maxIdentifiers[level] = Long.MIN_VALUE;
        		} else {
        			minIdentifiers[level] = node.getTemporalElement().getGranule()
        					.getGranularity().getMinGranuleIdentifier();
        			maxIdentifiers[level] = node.getTemporalElement().getGranule()
        					.getGranularity().getMaxGranuleIdentifier();
        		}
        	}          
        }

        calculateSizesRecursion(root, 0);
    }

    private void calculateSizesRecursion(TemporalObject node,
            int level) throws TemporalDataException {

    	VisualItem visualNode = m_vis.getVisualItem(m_group, node);
    	double[] size = new double[Constants.AXIS_COUNT];
    			
            for (TemporalObject o : node.childObjects()) {
                if (level + 1 < depth)
                	calculateSizesRecursion(o, level + 1);
                
            	if (!settings[level].isIgnore() && settings[level].getFitting() == FITTING_FULL_AVAILABLE_SPACE) {
                    minIdentifiers[level] = Math.min(minIdentifiers[level], o.getTemporalElement().getGranule().getIdentifier());
                    maxIdentifiers[level] = Math.max(maxIdentifiers[level], o.getTemporalElement().getGranule().getIdentifier());
                }
                
                VisualItem vo = m_vis.getVisualItem(m_group, o);
                double[] aviivo = additionalVisualItemInformation.get(vo.getRow());
                if (aviivo == null) {
                	aviivo = new double[Constants.AXIS_COUNT*2];
                	additionalVisualItemInformation.put(vo.getRow(),aviivo);
                }
                // TODO - Nothing right now, but edit here when implementing more axes than x,y
                double[] subSize = new double[] { aviivo[0], aviivo[1] };
                for(int i=0; i<Constants.AXIS_COUNT; i++) {
                	if (Double.isNaN(subSize[i]) || subSize[i] == 0) {                		
                		subSize[i] = 1.0;
                		aviivo[i] = subSize[i];
                	}                	
                	if (settings[level].getTargetAxis() == i && !settings[level].isIgnore()) 
                		size[settings[level].getTargetAxis()] += subSize[i];
               		else
               			size[i] = Math.max(size[i], subSize[i]);
                }
            }
            if(settings[level].getFitting() == FITTING_DEPENDING_ON_POSSIBLE_VALUES)
            	size[settings[level].getTargetAxis()] *= (((double)(maxIdentifiers[level] - minIdentifiers[level] + 1))/((double)node.getChildCount()));

       	axisActive[settings[level].getTargetAxis()] = true;
       	double[] aviivn = additionalVisualItemInformation.get(visualNode.getRow());
       	if(aviivn == null) {
       		aviivn = new double[Constants.AXIS_COUNT*2];
       		additionalVisualItemInformation.put(visualNode.getRow(),aviivn);
       	}
                 	
        for(int i=0; i<size.length; i++) {
           	aviivn[Constants.AXIS_COUNT+i] = settings[level].getBorder();
        	aviivn[i] = size[i] + 2*aviivn[Constants.AXIS_COUNT+i];
        }           	
    }
}
