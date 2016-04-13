package org.timebench.action.layout;

import ieg.prefuse.RangeModelTransformationProvider;
import ieg.prefuse.data.LinkedTree;
import ieg.prefuse.data.LinkedTree.LinkedNode;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;

import org.timebench.calendar.Granule;
import org.timebench.data.GranularityAggregationTree;
import org.timebench.data.TemporalDataException;
import org.timebench.data.TemporalObject;

import prefuse.Constants;
import prefuse.action.layout.Layout;
import prefuse.data.Schema;
import prefuse.data.query.NumberRangeModel;
import prefuse.util.PrefuseLib;
import prefuse.util.ui.ValuedRangeModel;
import prefuse.visual.VisualItem;

public class GranularityTreeLayout extends Layout implements RangeModelTransformationProvider {

	public static final int FITTING_FULL_AVAILABLE_SPACE = 0;
	public static final int FITTING_DEPENDING_ON_POSSIBLE_VALUES = 1;
	
	public static final String FIELD_POSITION = "_position";
	public static final String FIELD_GRANULE = "_granule";
	
    // XXX assume depth is given via size of settings
    protected int depth;

    // TODO consider circumstances to invalidate these min max ident. arrays
    protected long[] minIdentifiers;
    protected long[] maxIdentifiers;
    
    protected boolean[] axisActive = new boolean[Constants.AXIS_COUNT];
    
    protected NumberRangeModel[] rangeModels = new NumberRangeModel[Constants.AXIS_COUNT];
    
    protected Rectangle2D rootBounds;
    
    protected double minSize;	//minimum size of a "pixel"
    
    HashMap<Integer,double[]> additionalVisualItemInformation; // size x,y before size stretching, half border size

    GranularityTreeLayoutSettings[] settings;
    
    LinkedTree[] labels = new LinkedTree[Constants.AXIS_COUNT];

    public GranularityTreeLayout(String group,
            GranularityTreeLayoutSettings[] settings, double minSize) {
        super(group);
        this.settings = settings;
        this.minSize = minSize;
    }

    @Override
    public void run(double frac) {

        GranularityAggregationTree tree = (GranularityAggregationTree) m_vis
                .getSourceData(m_group);
        TemporalObject root = tree.roots().iterator().next();

        LinkedNode[] labelRoots = new LinkedNode[Constants.AXIS_COUNT];

    	Schema s = new Schema(1);
    	s.addColumn(FIELD_GRANULE, Granule.class);
    	s.addColumn(FIELD_POSITION, double.class);
        for(int i=0; i<Constants.AXIS_COUNT; i++) {
        	axisActive[i] = false;
        	labels[i] = new LinkedTree(s);
        	labelRoots[i] = labels[i].addRoot();
        }
                
        try {
        
        	double xFactor = 0;
        	double yFactor = 0;
    		Rectangle2D bounds = this.getLayoutBounds();
    		VisualItem visRoot = m_vis.getVisualItem(m_group, root);
        	
            this.depth = settings.length+1;
        	do {
        		depth--;
        		additionalVisualItemInformation = new HashMap<Integer, double[]>();	// reset
        		
        		calculateSizes(root);
        
        		for(int i=0; i<Constants.AXIS_COUNT; i++) {
        			double size = additionalVisualItemInformation.get(root.getRow())[i];
        			if (rangeModels[i] == null) 
        				rangeModels[i] = new NumberRangeModel(0,size,0,size);
        			else if (((Number)rangeModels[i].getMaxValue()).doubleValue() != size)
        				rangeModels[i].setMaxValue(size);
        		}
			
        		xFactor = bounds.getWidth() * 10000.0 / rangeModels[Constants.X_AXIS].getExtent() / ((Number)rangeModels[Constants.X_AXIS].getMaxValue()).doubleValue();        			
        		yFactor = bounds.getHeight() * 10000.0 / rangeModels[Constants.Y_AXIS].getExtent() / ((Number)rangeModels[Constants.Y_AXIS].getMaxValue()).doubleValue();      

        	} while ( Math.min(xFactor, yFactor) < minSize );
        	
        	if (xFactor < yFactor)
        	{
        		double newWidth = additionalVisualItemInformation.get(visRoot.getRow())[Constants.X_AXIS] * xFactor;
        		double newHeight = additionalVisualItemInformation.get(visRoot.getRow())[Constants.Y_AXIS] * xFactor;
        		bounds.setRect(bounds.getX()-((Number)rangeModels[Constants.X_AXIS].getLowValue()).doubleValue()*xFactor,
        				bounds.getY()-((Number)rangeModels[Constants.Y_AXIS].getLowValue()).doubleValue()*xFactor,
        				newWidth,newHeight);
        		rootBounds = (Rectangle2D)bounds.clone();
        		calculatePositions(root,0,bounds,xFactor,labelRoots);
        	} else {
        		double newWidth = additionalVisualItemInformation.get(visRoot.getRow())[Constants.X_AXIS] * yFactor;
        		double newHeight = additionalVisualItemInformation.get(visRoot.getRow())[Constants.Y_AXIS] * yFactor;
        		bounds.setRect(bounds.getX()-((Number)rangeModels[Constants.X_AXIS].getLowValue()).doubleValue()*yFactor,
        				bounds.getY()-((Number)rangeModels[Constants.Y_AXIS].getLowValue()).doubleValue()*yFactor,
        				newWidth,newHeight);
        		rootBounds = (Rectangle2D)bounds.clone();
            	calculatePositions(root,0,bounds,yFactor,labelRoots);
        	}       
        } catch (TemporalDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        //_printLabels(labels[Constants.X_AXIS].getRoot());
    }
    
    private void _printLabels(LinkedNode parent) {
    	Iterator children = parent.children();
    	while(children.hasNext()) {
    		LinkedNode child = (LinkedNode)children.next();
    		for(int i=0; i<child.getDepth(); i++)
    			System.out.print("  ");
    		try {
				System.out.print(((Granule)child.get(FIELD_GRANULE)).getLabel());
			} catch (TemporalDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		System.out.println();
    		_printLabels(child);
    	}
    }

    /**
	 * @param root
     * @throws TemporalDataException 
	 */
	private void calculatePositions(TemporalObject node,int level,Rectangle2D bounds,double factor,LinkedNode[] currentLabels) throws TemporalDataException {
		
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
		visualNode.setVisible(true);
		
		if (level > 0)
			currentLabels[settings[level-1].getTargetAxis()].setDouble(FIELD_POSITION,
				settings[level-1].getTargetAxis() == Constants.X_AXIS ? bounds.getCenterX() : bounds.getCenterY());
		
    	double xbase = bounds.getX() + (aviivn[Constants.AXIS_COUNT+Constants.X_AXIS])*factor;
    	double ybase = bounds.getY() + (aviivn[Constants.AXIS_COUNT+Constants.Y_AXIS])*factor;
        if (level < depth) {
            Iterator childIterator = currentLabels[settings[level].getTargetAxis()].children();
            for (TemporalObject o : node.childObjects()) {
            	double x = xbase;
            	double y = ybase;
            	VisualItem vo = m_vis.getVisualItem(m_group, o);
            	double[] aviivo = additionalVisualItemInformation.get(vo.getRow());
            	if(settings[level].getTargetAxis() == Constants.X_AXIS)
            		x += (o.getTemporalElement().getGranule().getIdentifier()-minIdentifiers[level]) * aviivo[Constants.X_AXIS]*factor;
            	if(settings[level].getTargetAxis() == Constants.Y_AXIS)
            		y += (o.getTemporalElement().getGranule().getIdentifier()-minIdentifiers[level]) * aviivo[Constants.Y_AXIS]*factor;
            	LinkedNode[] nextLabels = new LinkedNode[Constants.AXIS_COUNT];
                for(int i=0; i<Constants.AXIS_COUNT;i++)
                	nextLabels[i] = currentLabels[i];
               	if(childIterator.hasNext())
                   	nextLabels[settings[level].getTargetAxis()] = (LinkedNode)childIterator.next();
               	else
               		nextLabels[settings[level].getTargetAxis()] = currentLabels[settings[level].getTargetAxis()].addChild();
            	nextLabels[settings[level].getTargetAxis()].set(FIELD_GRANULE,o.getTemporalElement().getGranule());
            	calculatePositions(o, level + 1, new Rectangle2D.Double(x,
            			y,
            			aviivo[Constants.X_AXIS]*factor,aviivo[Constants.Y_AXIS]*factor),factor,nextLabels);
            }
        } else {
        	setChildsInvisible(node);
        }        
	}
	
	private void setChildsInvisible(TemporalObject node) {
    	for (TemporalObject o : node.childObjects()) {
    		VisualItem visualNode = m_vis.getVisualItem(m_group, o);
    		visualNode.setVisible(false);
    		setChildsInvisible(o);
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

	/* (non-Javadoc)
	 * @see ieg.prefuse.RangeModelTransformationProvider#getAxes()
	 */
	@Override
	public int[] getAxes() {		
		int numActive = 0;
		for(int i=0; i<axisActive.length; i++) {
			if(axisActive[i])
				numActive++;
		}
		int[] result = new int[numActive];
		 numActive = 0;
		for(int i=0; i<axisActive.length; i++) {
			if(axisActive[i]) {
				result[numActive] = i;
				numActive++;
			};
		}
		
		return result;
	}
	
	public LinkedTree[] getLabels() {
		return labels;
	}

	/* (non-Javadoc)
	 * @see ieg.prefuse.RangeModelTransformationProvider#getRangeModel(int)
	 */
	@Override
	public ValuedRangeModel getRangeModel(int axis) {
		return rangeModels[axis];
	}

	/* (non-Javadoc)
	 * @see ieg.prefuse.RangeModelTransformationProvider#getMinPosition(int)
	 */
	@Override
	public Double getMinPosition(int axis) {
		return axis == Constants.X_AXIS ? rootBounds.getMinX() : rootBounds.getMinY();
	}

	/* (non-Javadoc)
	 * @see ieg.prefuse.RangeModelTransformationProvider#getMaxPosition(int)
	 */
	@Override
	public Double getMaxPosition(int axis) {
		return axis == Constants.X_AXIS ? rootBounds.getMaxX() : rootBounds.getMaxY();
	}

	public GranularityTreeLayoutSettings[] getSettings() {		
		return settings;
	}

	public long[] getMinIdentifiers() {		
		return minIdentifiers;		
	}
	
	public long[] getMaxIdentifiers() {		
		return maxIdentifiers;		
	}
}
