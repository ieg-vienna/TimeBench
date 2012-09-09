package timeBench.action.analytical;

import timeBench.data.TemporalObject;

public interface GranularityAggregationFunction {	
	public double[] aggregate(Iterable<TemporalObject> childs, int[] dataColumnIndices, Double missingValueIdentifier);
}
