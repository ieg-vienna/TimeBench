package org.timebench.action.analytical;

import org.timebench.data.TemporalObject;

public abstract class GranularityAggregationFunction {	
	public abstract double[] aggregate(Iterable<TemporalObject> childs, int[] dataColumnIndices, Double missingValueIdentifier);
}
