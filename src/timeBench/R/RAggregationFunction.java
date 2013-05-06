package timeBench.R;

import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineException;

import timeBench.action.analytical.GranularityAggregationFunction;
import timeBench.data.TemporalObject;

public class RAggregationFunction extends GranularityAggregationFunction {
	String rAggregationFunction = "mean";
	REngine engine;
	boolean ignoreMissings = false;
	
	public RAggregationFunction(REngine eng) {
		engine = eng;
	}
	
	public RAggregationFunction(REngine eng, String fct) {
		engine = eng;
		rAggregationFunction = fct;
	}
	
	public RAggregationFunction(REngine eng, String fct, boolean ignoreMissings) {
		engine = eng;
		rAggregationFunction = fct;
		this.ignoreMissings = ignoreMissings;
	}
	
	public RAggregationFunction(REngine eng, boolean ignoreMissings) {
		engine = eng;
		this.ignoreMissings = ignoreMissings;
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
					if (!Double.isNaN(value) && value != missingValueIdentifier)
						data += value+", ";
					else
						data += "NA, ";
					if (childcount == 1) {
						if (!Double.isNaN(value) && value != missingValueIdentifier)
							firstvalues[j] = value;
						else
							firstvalues[j] = Double.NaN;
					}
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
			String aggFct = new String("sapply(x,"+rAggregationFunction+", na.rm="+Boolean.toString(ignoreMissings).toUpperCase()+")");
			double[] result = engine.parseAndEval(aggFct, null, true).asDoubles();
			return result;
		} catch (REngineException e) {
			e.printStackTrace();
		} catch (REXPMismatchException e) {
			e.printStackTrace();
		}
		return null;
	}

}
