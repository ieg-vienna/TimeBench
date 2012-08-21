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
    
    Hashtable<Integer,double[]> additionalVisualItemInformation = new Hashtable<Integer, double[]>(); // size x,y before size stretching, half border size

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
		} catch (TemporalDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        Rectangle2D bounds = this.getLayoutBounds();
        VisualItem visRoot = m_vis.getVisualItem(m_group, root);
        double xFactor = bounds.getWidth() / visRoot.getSizeX();
        double yFactor = bounds.getHeight() / visRoot.getSizeY();      
      
        if (xFactor < yFactor)
        {
        	double newHeight = visRoot.getSizeY() * xFactor;
        	bounds.setRect(bounds.getX(),bounds.getY()+(bounds.getHeight()-newHeight)/2,bounds.getWidth(),newHeight);
            calculatePositions(root,0,bounds,xFactor);
        } else {
        	double newWidth = visRoot.getSizeX() * yFactor;
        	bounds.setRect(bounds.getX()+(bounds.getWidth()-newWidth)/2,bounds.getY(),bounds.getHeight(),newWidth);
            calculatePositions(root,0,bounds,yFactor);
        }

        DataHelper.printGraph(System.out, (NodeItem)visRoot, null, VisualItem.X,VisualItem.Y,VisualItem.SIZE,VisualItem.SIZEY);
    }

    /**
	 * @param root
	 */
	private void calculatePositions(TemporalObject node,int level,Rectangle2D bounds,double factor) {
		
    	VisualItem visualNode = m_vis.getVisualItem(m_group, node);
    	
    	if (axisActive[Constants.X_AXIS]) {
    		setX(visualNode,null,bounds.getCenterX());
    		PrefuseLib.setSizeX(visualNode,null,visualNode.getSizeX()*factor);
    	}
    	if (axisActive[Constants.Y_AXIS]) {
    		setY(visualNode,null,bounds.getCenterY());
    		PrefuseLib.setSizeY(visualNode,null,visualNode.getSizeY()*factor);
    	}
		
        if (level < depth) {
        	double x = bounds.getX();
        	double y = bounds.getY();
            for (TemporalObject o : node.childObjects()) {           
            	VisualItem vo = m_vis.getVisualItem(m_group, o);
            	calculatePositions(o, level + 1, new Rectangle2D.Double(x,y,vo.getSizeX()*factor,vo.getSizeY()*factor) ,factor);
            	if(settings[level].getTargetAxis() == Constants.X_AXIS)
            		x+=vo.getSizeX()*factor;
            	if(settings[level].getTargetAxis() == Constants.Y_AXIS)
            		y+=vo.getSizeY()*factor;
            }
        }
        
        
	}

	private void calculateSizes(TemporalObject root)
            throws TemporalDataException {
        minIdentifiers = new long[depth];
        maxIdentifiers = new long[depth];

        TemporalObject node = root;
        for (int level = 0; level < depth; level++) {
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
            
            node = node.getFirstChildObject();
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
                VisualItem vo = m_vis.getVisualItem(m_group, o);
                // TODO - Nothing right now, but edit here when implementing more axes than x,y
                double[] subSize = new double[] { vo.getSizeX(),vo.getSizeY()};
                for(int i=0; i<Constants.AXIS_COUNT; i++) {
                	if (Double.isNaN(subSize[i]) || subSize[i] == 0) {
                		subSize[i] = 1.0;
                		if (i == Constants.X_AXIS)
                			PrefuseLib.setSizeX(vo, null, subSize[i]);
                		else if (i == Constants.Y_AXIS)
                			PrefuseLib.setSizeY(vo, null, subSize[i]);
                	}
                	if (settings[level].getTargetAxis() == i && !settings[level].isIgnore()) 
                		size[settings[level].getTargetAxis()] += subSize[i];
               		else
               			size[i] = Math.max(size[i], subSize[i]);
                }
            }
            if(settings[level].getFitting() == FITTING_DEPENDING_ON_POSSIBLE_VALUES)
            	size[settings[level].getTargetAxis()] *= (((double)(maxIdentifiers[level] - minIdentifiers[level]))/((double)node.getChildCount()));
        
        for(int i=0; i<size.length; i++)
        	size[i] += 2*settings[level].getBorder()*size[i];
        
       	if(settings[level].getTargetAxis() == Constants.X_AXIS)
       		axisActive[Constants.X_AXIS] = true;
       	if(settings[level].getTargetAxis() == Constants.Y_AXIS)
       		axisActive[Constants.Y_AXIS] = true;
       	if(axisActive[Constants.X_AXIS])
       		PrefuseLib.setSizeX(visualNode,null,size[Constants.X_AXIS]);
       	if(axisActive[Constants.Y_AXIS])
       		PrefuseLib.setSizeY(visualNode,null,size[Constants.Y_AXIS]);
    	
    	if (level > 0 && !settings[level-1].isIgnore() && settings[level-1].getFitting() == FITTING_FULL_AVAILABLE_SPACE) {
            minIdentifiers[level-1] = Math.min(minIdentifiers[level-1], node
                    .getTemporalElement().getGranule().getIdentifier());
            maxIdentifiers[level-1] = Math.max(maxIdentifiers[level-1], node
                    .getTemporalElement().getGranule().getIdentifier());
        }    	
    }
}
