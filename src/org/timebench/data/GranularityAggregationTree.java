package org.timebench.data;

import ieg.prefuse.data.ParentChildNode;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.event.EventConstants;
import prefuse.data.event.TableListener;

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
public class GranularityAggregationTree extends TemporalDataset {
	double[][] minValues;
	double[][] maxValues;
	    
	public GranularityAggregationTree(Schema dataColumnSchema, int levelCount) throws TemporalDataException {
	    // TODO handle int columns (canGetDouble but not canSetDouble) -> add as double columns
	    // TODO handle boolean, Object columns -> exclude?

		super(dataColumnSchema);
		
		super.getNodeTable().addColumn(ParentChildNode.DEPTH, Integer.TYPE);
		additionalNonDataColums = new String[] {ParentChildNode.DEPTH};
		
		minValues = new double[dataColumnSchema.getColumnCount()][levelCount];
		maxValues = new double[dataColumnSchema.getColumnCount()][levelCount];
		int l=0;
		for(int k : getDataColumnIndices()) {
			if (getNodeTable().canGetDouble(k)) {
				for(int j=0; j<levelCount; j++) {
					minValues[l][j] = Double.MAX_VALUE;
					maxValues[l][j] = Double.MIN_VALUE;
				}
			}
			l++;
		}
		
		getNodeTable().addTableListener(new GranularityAggregationTreeListener());
	}

	public Double getMinValue(int level,int index) {
		if(minValues == null || minValues.length <= index)
			return null;
		else {
			if(minValues[index][level] == Double.MAX_VALUE)
				return Double.NaN;
			else
				return minValues[index][level];
		}
			
	}

	public Double getMaxValue(int level,int index) {
		if(maxValues == null || maxValues.length <= index)
			return null;
		else {
			if(minValues[index][level] == Double.MAX_VALUE)
				return Double.NaN;
			else
				return maxValues[index][level];
		}
	}
	
	public int getMaxDepth() {
		return minValues[0].length-1;
	}
	
	class GranularityAggregationTreeListener implements TableListener {

		/* (non-Javadoc)
		 * @see prefuse.data.event.TableListener#tableChanged(prefuse.data.Table, int, int, int, int)
		 */
		@Override
		public void tableChanged(Table t, int start, int end, int col, int type) {
			if(type == EventConstants.UPDATE && start == end) {
				TemporalObject to = (TemporalObject)t.getTuple(start);
				int treeLevel = to.getTreeLevel();
				int[] dataColumnIndices = getDataColumnIndices();
				for(int i=0; i<dataColumnIndices.length; i++) {
					if(col == dataColumnIndices[i]) {
						if (t.canGetDouble(col)) {
							double d = t.getDouble(start, col);
							if (!Double.isNaN(d)) {
								minValues[i][treeLevel] = Math.min(minValues[i][treeLevel], d);
								maxValues[i][treeLevel] = Math.max(maxValues[i][treeLevel], d);
							}
						}
					}
					break;
				}
			}
		}		
	}
}
