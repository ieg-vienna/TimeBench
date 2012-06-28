package timeBench.data;

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
	

	public GranularityAggregationTree(Schema dataColumnSchema, int columnCount, int levelCount) throws TemporalDataException {
	    // TODO handle int columns (canGetDouble but not canSetDouble) -> add as double columns
	    // TODO handle boolean, Object columns -> exclude?
		super(dataColumnSchema);
		
		minValues = new double[columnCount][levelCount];
		maxValues = new double[columnCount][levelCount];
		int i2=0;
		for(int i : getDataColumnIndices()) {
			if (getNodeTable().canGetDouble(i)) {
				for(int j=0; j<levelCount; j++) {
					minValues[i2][j] = Double.MAX_VALUE;
					maxValues[i2][j] = Double.MIN_VALUE;
				}
			}
			i2++;
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
