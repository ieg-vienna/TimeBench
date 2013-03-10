package timeBench.R;

import java.util.Arrays;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineException;

import timeBench.R.data.ACFDataObject;
import timeBench.R.data.ScatterPlotDataObject;

public class RsarimaModelProvider {
	REngine engine;
	private RTemporalDatasetProvider tmpdsProvider = null;
	
	public RsarimaModelProvider(REngine re) {
		engine = re;
	}
	
	public String estimateSARIMA(String dataset, Object key, int p, int d, int q, 
			int seasonal_p, int seasonal_d, int seasonal_q, int seasonal_length, boolean no_constant) {
		String resultName = "res"+Integer.toHexString(key.hashCode());
		if (seasonal_length >= 0) {
			resultName = resultName + String.valueOf(seasonal_length);
		}
		if (seasonal_length <= 0) {
			seasonal_length = -1;
		}
		
		String command;
		
		if ((d == 0) && (seasonal_d == 0)) {
			command = "stats::arima("+dataset+
					", order = c("+p+", "+d+", "+q+"), " +
					"seasonal = list(order = c("+seasonal_p+", "+seasonal_d+", "+
					seasonal_q+"), period = "+seasonal_length+
					"), xreg = xmean, include.mean = FALSE, " +
					"optim.control = list(trace = "+0+", REPORT = 1, reltol = tol))";
		}
		else if (((d == 1) != (seasonal_d == 1)) && (no_constant == false)) {
			command = "stats::arima("+dataset+
					", order = c("+p+", "+d+", "+q+"), " +
					"seasonal = list(order = c("+seasonal_p+", "+seasonal_d+", "+
					seasonal_q+"), period = "+seasonal_length+
					"), xreg = constant, " +
					"optim.control = list(trace = "+0+", REPORT = 1, reltol = tol))";
		} else {
			command = "stats::arima("+dataset+
					", order = c("+p+", "+d+", "+q+"), " +
					"seasonal = list(order = c("+seasonal_p+", "+seasonal_d+", "+
					seasonal_q+"), period = "+seasonal_length+"), " +
					"optim.control = list(trace = "+0+", REPORT = 1, reltol = tol))";
		}
		
		
		try {
			engine.parseAndEval("n = length("+dataset+"); constant = 1:n; xmean = rep(1, n); tol = sqrt(.Machine$double.eps)");
			if (no_constant) {
				engine.parseAndEval("xmean = NULL");
			}
			REXP result = engine.parseAndEval(resultName+" <- "+command, null, true);
			if (result.isNull()) {
				return null;
			} else {
				return resultName;
			}
		} catch (REngineException e) {
			e.printStackTrace();
			resultName = null;
		} catch (REXPMismatchException e) {
			e.printStackTrace();
			resultName = null;
		}
		
		return null;
	}
	
	public String estimateSARIMA(String dataset, Object key, int p, int d, int q, 
			int seasonal_p, int seasonal_d, int seasonal_q, int seasonal_length) {
		return estimateSARIMA(dataset, key, p, d, q, seasonal_p, seasonal_d, seasonal_q, seasonal_length, false);
	}
	
	public String estimateSARIMA(String dataset, Object key, int p, int d, int q, 
			int seasonal_p, int seasonal_d, int seasonal_q) {
		return estimateSARIMA(dataset, key, p, d, q, seasonal_p, seasonal_d, seasonal_q, -1, false);
	}
	
	public String estimateSARIMA(String dataset, Object key, int p, int d, int q) {
		return estimateSARIMA(dataset, key, p, d, q, 0, 0, 0, -1, false);
	}

	private String standardizeResiduals(String res) {
		try {
	    	engine.parseAndEval("rs <- "+res+"$residuals", null, false);
	    	engine.parseAndEval("num <- sum(!is.na(rs))", null, false);
	    	engine.parseAndEval("u <- c(0, stats::pacf(rs, lag.max = num, plot = FALSE, na.action = na.pass)$acf)", null, false);
	    	engine.parseAndEval("u <- sqrt("+res+"$sigma2 * base::cumprod(1 - u^2))", null, false);
	    	REXP result = engine.parseAndEval("std"+res+" <- rs/u", null, true);
	    	if (result.isNull()) {
	    		return null;
	    	} else {
	    		return "std"+res;
	    	}
		} catch (REXPMismatchException e) {
			e.printStackTrace();
		} catch (REngineException e) {
			e.printStackTrace();
		}
		return null;
    }
	
	public ScatterPlotDataObject getStandardizedResiduals(String res) {
		try {
			String stdres = standardizeResiduals(res);
			if (stdres == null) {
				return null;
			} else {
				double[] y = engine.parseAndEval("coredata("+stdres+")", null, true).asDoubles();
				double[] x = engine.parseAndEval("coredata(time("+stdres+"))",null, true).asDoubles();
				return new ScatterPlotDataObject(x, y, 0.0, 0.0);
			}
		} catch (REXPMismatchException e) {
			e.printStackTrace();
		} catch (REngineException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public ScatterPlotDataObject getQQPlotResiduals(String stdres) {
		try {
			String qqReturn = "qret"+stdres;
			REXP result = engine.parseAndEval(qqReturn+" <- qqnorm(as.numeric("+stdres+"), plot.it=FALSE)", null, true);
			if (result.isNull()) {
				return null;
			}
			double[] x = engine.parseAndEval(qqReturn+"$x",null, true).asDoubles();
			double[] y = engine.parseAndEval(qqReturn+"$y",null, true).asDoubles();
			
			engine.parseAndEval("probs = c(0.25,0.75)",null, false);
			engine.parseAndEval("y <- quantile("+qqReturn+"$y, probs, type=7, na.rm=TRUE)",null, false);
			engine.parseAndEval("x <- qnorm(probs)",null, false);
			double slope = engine.parseAndEval("slope <- as.numeric(diff(y)/diff(x))",null, true).asDouble();
			double intercept = engine.parseAndEval("as.numeric(y[1L]-slope*x[1L])",null, true).asDouble();
			
			return new ScatterPlotDataObject(x, y, intercept, slope);
		} catch (REXPMismatchException e) {
			e.printStackTrace();
		} catch (REngineException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public ScatterPlotDataObject getLjungBoxStatisticResiduals(String res) {
		try {
			REXP r = engine.parseAndEval(res+"$arma", null, true);
			if (r.isNull()) {
				return null;
			}
			
			double[] parameter = r.asDoubles();
			
			if (parameter == null || parameter.length < 5) {
				return null;
			}
			
			engine.parseAndEval("p <- "+parameter[0]+"; q <- "+parameter[1]+"; P <- "+parameter[2]+
					"; Q <- "+parameter[3]+"; S <- "+parameter[4],null,false);
			engine.parseAndEval("rs <- "+res+"$residuals",null,false);
			engine.parseAndEval("nlag <- ifelse(S < 4, 20, 3 * S)",null,false);
			engine.parseAndEval("ppq <- p + q + P + Q",null,false);
			engine.parseAndEval("pval <- numeric(nlag)",null,false);
			engine.parseAndEval("for (i in (ppq + 1):nlag) { u <- stats::Box.test(rs, i, type = \"Ljung-Box\")$statistic;"+
			" pval[i] <- stats::pchisq(u, i - ppq, lower.tail = FALSE);}",null,false);
			int[] x = engine.parseAndEval("(ppq + 1):nlag", null, true).asIntegers();
			double[] y = engine.parseAndEval("pval[(ppq + 1):nlag]", null, true).asDoubles();
			
			int firstNaNIndex = y.length-1;
			
			for ( ; Double.isNaN(y[firstNaNIndex]); firstNaNIndex--);
			
			if (firstNaNIndex < (y.length-1)) {
				x = Arrays.copyOf(x, firstNaNIndex+1);
				y = Arrays.copyOf(y, firstNaNIndex+1);
			}
			
			return new ScatterPlotDataObject(x,y,0.05,0.0);
		} catch (REXPMismatchException e) {
			e.printStackTrace();
		} catch (REngineException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public ACFDataObject getAutocorrelationFunction(String name, int diff, int seasondiff, int seasonFrequency, int maxlag) {
		if (tmpdsProvider == null) {
			tmpdsProvider = new RTemporalDatasetProvider(engine);
		}
		try {
			return tmpdsProvider.getAutocorrelationFunction(name, diff, seasondiff, seasonFrequency, maxlag);
		} catch (REXPMismatchException e) {
			e.printStackTrace();
		} catch (REngineException e) {
			e.printStackTrace();
		}
		return null;
	}
}
