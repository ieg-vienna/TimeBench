package timeBench.action.analytical;

import timeBench.data.TemporalObject;

public abstract class GranularityAggregationFunction {	
	public abstract double[] aggregate(Iterable<TemporalObject> childs, int[] dataColumnIndices, Double missingValueIdentifier);
}
