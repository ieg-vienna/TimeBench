package timeBench.action.analytical;

import timeBench.data.TemporalObject;

public class GranularityAggregationSum extends
		GranularityAggregationFunction {
	private boolean naomit = false;
	
	public GranularityAggregationSum() {
		naomit = false;
	}
	
	public GranularityAggregationSum(boolean missingValuesOmit) {
		naomit = missingValuesOmit;
	}

	@Override
	public double[] aggregate(Iterable<TemporalObject> childs,
			int[] dataColumnIndices, Double missingValueIdentifier) {
		double[] totalValue = new double[dataColumnIndices.length]; 
		for(int i=0; i<dataColumnIndices.length; i++) {
			totalValue[i] = 0;
		}

        for (TemporalObject temporalObject : childs) {
			for(int j=0; j<dataColumnIndices.length; j++) {
				if(temporalObject.canGetDouble(dataColumnIndices[j])) {
					double value = temporalObject.getDouble(dataColumnIndices[j]);
					if (naomit) {
						if (!Double.isNaN(value) && value != missingValueIdentifier) {
							totalValue[j] += value;
						}
					} else {
						if (Double.isNaN(value) || value == missingValueIdentifier) {
							totalValue[j] = Double.NaN;
						}
						if (!Double.isNaN(totalValue[j])) {
							totalValue[j] += value;
						}
					}
				}
			}
		}
        
		return totalValue;
	}

}
