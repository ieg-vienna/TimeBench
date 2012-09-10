package timeBench.R;

import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineException;

import timeBench.action.analytical.GranularityAggregationFunction;
import timeBench.data.TemporalObject;

public class RAggregationFunction implements GranularityAggregationFunction {
	String rAggregationFunction = "mean";
	REngine engine;
	
	public RAggregationFunction(REngine eng) {
		engine = eng;
	}
	
	public RAggregationFunction(REngine eng, String fct) {
		engine = eng;
		rAggregationFunction = fct;
	}

	@Override
	public double[] aggregate(Iterable<TemporalObject> childs,
			int[] dataColumnIndices, Double missingValueIdentifier) {
		String data = "c(";
		int childcount = 0;
		double[] firstvalues = new double[dataColumnIndices.length]; 
		for(int i=0; i<dataColumnIndices.length; i++) {
			firstvalues[i] = 0;
		}
		for (TemporalObject temporalObject : childs) {
			childcount++;
			for(int j=0; j<dataColumnIndices.length; j++) {
				if(temporalObject.canGetDouble(dataColumnIndices[j])) {
					double value = temporalObject.getDouble(dataColumnIndices[j]);
					data += value+", ";
					if (childcount == 1)
						firstvalues[j] = value;
				}
			}
		}
		if (childcount == 1) {
			return firstvalues;
		}
		if (data.endsWith(", "))
			data = data.substring(0, data.length()-2);
		data += ")";
		
		try {
			engine.parseAndEval("x <- "+data);
			engine.parseAndEval("x <- matrix(x, ncol="+dataColumnIndices.length+", byrow=TRUE)");
			engine.parseAndEval("x <- data.frame(x)");
			double[] result = engine.parseAndEval("sapply(x,"+rAggregationFunction+")", null, true).asDoubles();
			return result;
		} catch (REngineException e) {
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			e.printStackTrace();
		}
		return null;
	}

}
