package timeBench.action.analytical;

import timeBench.data.TemporalObject;

public class GranularityAggregationMean extends
		GranularityAggregationFunction {
	private boolean naomit = false;
	
	public GranularityAggregationMean() {
		naomit = true;
	}
	
	public GranularityAggregationMean(boolean missingValuesOmit) {
		naomit = missingValuesOmit;
	}

	@Override
	public double[] aggregate(Iterable<TemporalObject> childs,
			int[] dataColumnIndices, Double missingValueIdentifier) {
		
		double[] numObjects = new double[dataColumnIndices.length]; 
		double[] totalValue = new double[dataColumnIndices.length]; 
		for(int i=0; i<dataColumnIndices.length; i++) {
			numObjects[i] = 0;
			totalValue[i] = 0;
		}

        for (TemporalObject temporalObject : childs) {
			for(int j=0; j<dataColumnIndices.length; j++) {
				if(temporalObject.canGetDouble(dataColumnIndices[j])) {
					double value = temporalObject.getDouble(dataColumnIndices[j]);
					if (naomit) {
						if (!Double.isNaN(value) && value != missingValueIdentifier) {
							totalValue[j] += value;
							numObjects[j]++;
						}
					} else {
						if (Double.isNaN(value) || value == missingValueIdentifier) {
							totalValue[j] = Double.NaN;
						}
						if (!Double.isNaN(totalValue[j])) {
							totalValue[j] += value;
							numObjects[j]++;
						}
					}
				}
			}
		}
        
        for(int i=0; i<dataColumnIndices.length; i++) {
        	if (!Double.isNaN(totalValue[i])) {
        		totalValue[i] /= numObjects[i];
        	}
        }
        
		return totalValue;
	}

}
