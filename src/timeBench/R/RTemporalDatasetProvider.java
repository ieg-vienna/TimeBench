package timeBench.R;

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.SortedMap;
import java.util.TimeZone;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineException;

import timeBench.R.data.ACFDataObject;
import timeBench.calendar.CalendarManager;
import timeBench.calendar.CalendarManagerFactory;
import timeBench.calendar.CalendarManagers;
import timeBench.calendar.JavaDateCalendarManager;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalElementStore;
import timeBench.data.TemporalObject;

public class RTemporalDatasetProvider {
	REngine engine;
	int userSetGranularityId = -1;
	
	public RTemporalDatasetProvider(REngine eng) {
		engine = eng;
	}
	
	public void loadPackage(String lib) {
		loadPackages(new String[]{lib});
	}
	
	public void loadPackages(String[] libs) {
		for (String lib : libs) {
			try {
				engine.parseAndEval("library("+lib+")", null, false);
			} catch (REngineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (REXPMismatchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void loadDataset(String dataset) {
		loadDatasets(new String[]{dataset});
	}
	
	public void loadDatasets(String[] datasets) {
		for (String dataset : datasets) {
			try {
				engine.parseAndEval("data("+dataset+")", null, false);
			} catch (REngineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (REXPMismatchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public TemporalDataset getTemporalDataset(
            String name, int granularityID, String naHandling) throws REngineException, REXPMismatchException,
            TemporalDataException {
		userSetGranularityId = granularityID;
		return getTemporalDataset(name, naHandling);
	}
	
    public TemporalDataset getTemporalDataset(
            String name, String naHandling) throws REngineException, REXPMismatchException,
            TemporalDataException {
        // check R class
//    	re.parseAndEval(name + " <- zoo(" + name + ")",null, false);
        REXP x = engine.parseAndEval("class(" + name + ")",null, true);
        if (!x.isString())
            throw new TemporalDataException("Unsupported R class: " + x);
//        System.out.println(x.asString());

        // ts ... simplest R time series class
        if ("ts".equals(x.asString())) {
            int[] start = engine
                    .parseAndEval("start(" + name + ")", null, true)
                    .asIntegers();
            double frequency = engine.parseAndEval("frequency(" + name + ")",
                    null, true).asDouble();
            double[] data = engine.parseAndEval("as.vector(" + name + ")",
                    null, true).asDoubles();
            double[] missingEstimate = null;
            if ((naHandling != null) && (naHandling.length() > 0)) {
            	missingEstimate = engine.parseAndEval("as.vector("+naHandling+"(" + name + "))",
                    null, true).asDoubles();
//            	missingEstimate = engine.parseAndEval("as.vector(adjustCVD)",
//                        null, true).asDoubles();
            }
//            logger.debug("ts with start: " + Arrays.toString(start)
//                    + ", frequency: " + frequency + ", obs: " + data.length);

            // get calendar and granularity
//            Calendar calendar = CalendarManagerFactory.getSingleton(
//                    CalendarManagers.JavaDate).getDefaultCalendar();
            
            int granularityContextId = JavaDateCalendarManager.Granularities.Top
                    .toInt();
            //JavaDateCalendarManager.Granularities.Quarter
//            granularityContextId = JavaDateCalendarManager.Granularities.Month
//                    .toInt();
            int granularityId = -1;
            if (Math.abs(frequency - 1.0) < 0.005
                    || Math.abs(frequency - 0.1) < 0.005)
                granularityId = JavaDateCalendarManager.Granularities.Year
                        .toInt();
            else if (Math.abs(frequency - 4.0) < 0.005)
                granularityId = JavaDateCalendarManager.Granularities.Quarter
                        .toInt();
            else if (Math.abs(frequency - 12.0) < 0.005)
                granularityId = JavaDateCalendarManager.Granularities.Month
                        .toInt();
            else if (Math.abs(frequency - 52.0) < 0.005)
                granularityId = JavaDateCalendarManager.Granularities.Week
                        .toInt();
            else
                throw new TemporalDataException(
                        "Unknown granularity. frequency: " + frequency);

//            System.out.println("Granularity ID: "+granularityId);
            // prepare a calendar
            GregorianCalendar cal = new GregorianCalendar(
                    TimeZone.getTimeZone("UTC"));
            cal.setTimeInMillis(0);
            cal.set(GregorianCalendar.MONTH, 0);
            cal.set(GregorianCalendar.DAY_OF_MONTH, 1);
            cal.set(GregorianCalendar.HOUR_OF_DAY, 0);
            cal.set(GregorianCalendar.MINUTE, 0);
            cal.set(GregorianCalendar.YEAR, start[0]);
            if (frequency - 12.0 < 0.05 && start.length > 1) {
                // GregorianCalendar.MONTH starts with 0
                cal.set(GregorianCalendar.MONTH, start[1] - 1);
            }

            // relational
            TemporalDataset tmpds = new timeBench.data.TemporalDataset();
            tmpds.addDataColumn(name, double.class, null);
            tmpds.addColumn("rindex", int.class, 0);
//            tmpds.addDataColumn("minConf", double.class, null);
//            tmpds.addDataColumn("maxConf", double.class, null);
//            tmpds.addDataColumn("labels", String.class, null);

            // time indices are only used in log file to check our code
//            double[] time = new double[0];
//            DateFormat df = null;
//            if (logger.isTraceEnabled()) {
//                time = re.parseAndEval(
//                    "as.vector(time(" + name + "))", null, true).asDoubles();
//                df = DateFormat.getDateTimeInstance();
//            }

            for (int i = 0; i < data.length; i++) {
                long inf = cal.getTimeInMillis();

                if (Math.abs(frequency - 12.0) < 0.005)
                    cal.add(GregorianCalendar.MONTH, 1);
                else if (Math.abs(frequency - 4.0) < 0.005)
                    cal.add(GregorianCalendar.MONTH, 3);
                else if (Math.abs(frequency - 0.1) < 0.005)
                    cal.add(GregorianCalendar.YEAR, 10);
                else
                	cal.add(GregorianCalendar.YEAR, 1);
                    //cal.add(GregorianCalendar.DAY_OF_YEAR, 1);
                    //cal.add(GregorianCalendar.YEAR, 1);

                long sup = cal.getTimeInMillis() - 1;
//                if (logger.isTraceEnabled())
//                    logger.trace("time from R: " + time[i] + " inf: "
//                            + df.format(new Date(inf)) + " sup: "
//                            + df.format(new Date(sup)) + " data: " + data[i]);

                tmpds.addTemporalElement(
                        i,
                        inf,
                        sup,
                        granularityId,
                        granularityContextId,
                        TemporalElementStore.PRIMITIVE_INTERVAL);
                TemporalObject to = tmpds.addTemporalObject(i, i);
                //to.set(name, data[i]);
                to.setInt("rindex", i+1);
                to.set(name, (double) (Math.round(data[i]*100.0)/100.0));
                if ( data[i] == REngineConnection.R_NA_int) {
                //if (Double.isNaN(data[i])){
                	to.setKind(name, 1);
                	if (missingEstimate != null && missingEstimate.length > i) {
                		double estimateValue = (double) (Math.round(missingEstimate[i]*100.0)/100.0);
                		to.set(name, (double) (Math.round(missingEstimate[i]*100.0)/100.0));
                		to.setMin(name, (double) estimateValue-100);
                		to.setMax(name, (double) estimateValue+100);
                	}
                }
//                if (Math.abs(data[i]-260) < 5) {
//                	to.set(name, Double.NaN);
////                	to.setMin(name, (Double) (Math.round((data[i]-20.0)*100.0)/100.0));
////                	to.setMax(name, (Double) (Math.round((data[i]+20.0)*100.0)/100.0));
//                	to.setKind(name, 1);
//                } else {
//                	to.setKind(name, 0);
////                	to.set("minConf", 0.0);
////                	to.set("maxConf", 0.0);
//                }
//                to.set("minConf", 0.0);
//            	to.set("maxConf", 0.0);
//                to.set("labels", new String(to.getDouble(name)+" | "+to.getDouble("minConf")+" - " + to.getDouble("maxConf")));
            }

            return tmpds;
        } else if ("zoo".equals(x.asString())) {
            // zoo ... feature-rich R time series class
        	String timeClass = engine.parseAndEval("class(time("+name+"))", null, true).asString();
        	
//            double start = re.parseAndEval(
//                    "as.numeric(start(" + name + "))", null, true).asDouble();
            double[] data = engine.parseAndEval("coredata(" + name + ")", null,
                    true).asDoubles();
            double[] time = engine.parseAndEval(
                    "as.numeric(time(" + name + "))", null, true).asDoubles();
//            logger.debug("ts with start: " + start + ", obs: " + data.length);

            // get calendar and granularity
//            int granularityContextId = calM.getTopGranularityIdentifier();
//            int granularityId = calM.getBottomGranularityIdentifier();

            granularityContextId = JavaDateCalendarManager.Granularities.Top
                    .toInt();
            
            if (userSetGranularityId >= 0) {
            	granularityId = userSetGranularityId;
            } else {
	            if (timeClass.equals("Date")) {
	            	granularityId = JavaDateCalendarManager.Granularities.Day.toInt();
	            } else {
	            	granularityId = JavaDateCalendarManager.Granularities.Millisecond.toInt();
	            }
            }
//            granularityId = JavaDateCalendarManager.Granularities.Quarter.toInt();
            // relational
            TemporalDataset tmpds = new timeBench.data.TemporalDataset();
            tmpds.addDataColumn(name, double.class, null);

            // time indices are only used in log file to check our code
//            DateFormat df = null;
//            if (logger.isTraceEnabled()) {
//                df = DateFormat.getDateTimeInstance();
//            }

            for (int i = 0; i < data.length; i++) {
            	// year * 365 * 24 * 60 * 60 * 1000
                long inf = 0;
                
                if (timeClass.equals("Date")) {
                	inf = Math.round(time[i] * 24 * 60 * 60 * 1000);
                }
                if (timeClass.equals("POSIXct")) {
                	inf = Math.round(time[i] * 1000);
                }
//                if (logger.isTraceEnabled())
//                    logger.trace("time from R: " + time[i] + " inf: "
//                            + df.format(new Date(inf)) + " data: " + data[i]);

                tmpds.addTemporalElement(
                        i,
                        inf,
                        inf,
                        granularityId,
                        granularityContextId,
                        TemporalElementStore.PRIMITIVE_INSTANT);
                TemporalObject to = tmpds.addTemporalObject(i, i);
                to.set(name, data[i]);
                if (Double.isNaN(data[i])) {
                	to.setKind(name, 3);
                	to.setMin(name, 0);
                	to.setMax(name, 0);
                }
//                if (Double.isNaN(data[i])) {
//                	System.out.println("DATA: "+data[i]);
//                	to.set(name, data[i-1]);
//                	to.setKind(name, 3);
//                	to.setMin(name, data[i-1]-2);
//                	to.setMax(name, data[i-1]+2);
//                } else to.set(name, data[i]);
            }

            return tmpds;
        } else
            throw new TemporalDataException("Unsupported R class: "
                    + x.asString());
    }
    
    public TemporalDataset getForecast(
            String name) throws REngineException, REXPMismatchException,
            TemporalDataException {
        // check R class
        REXP x = engine.parseAndEval("class(" + name + "$mean)",null, true);
        if (!x.isString())
            throw new TemporalDataException("Unsupported R class: " + x);

        // ts ... simplest R time series class
        if ("ts".equals(x.asString())) {
            int[] start = engine
                    .parseAndEval("start(" + name + "$mean)", null, true)
                    .asIntegers();
            double frequency = engine.parseAndEval("frequency(" + name + "$mean)",
                    null, true).asDouble();
            double[] data = engine.parseAndEval("as.vector(" + name + "$mean)",
                    null, true).asDoubles();
            double[] upperBounds = engine.parseAndEval(name + "$upper[,1]",
                    null, true).asDoubles();
            double[] lowerBounds = engine.parseAndEval(name + "$upper[,1]",
                    null, true).asDoubles();
            
            int granularityContextId = JavaDateCalendarManager.Granularities.Top
                    .toInt();
            int granularityId = -1;
            if (Math.abs(frequency - 1.0) < 0.005
                    || Math.abs(frequency - 0.1) < 0.005)
                granularityId = JavaDateCalendarManager.Granularities.Year
                        .toInt();
            else if (Math.abs(frequency - 4.0) < 0.005)
                granularityId = JavaDateCalendarManager.Granularities.Quarter
                        .toInt();
            else if (Math.abs(frequency - 12.0) < 0.005)
                granularityId = JavaDateCalendarManager.Granularities.Month
                        .toInt();
            else if (Math.abs(frequency - 52.0) < 0.005)
                granularityId = JavaDateCalendarManager.Granularities.Week
                        .toInt();
            else
                throw new TemporalDataException(
                        "Unknown granularity. frequency: " + frequency);

//            System.out.println("Granularity ID: "+granularityId);
            // prepare a calendar
            GregorianCalendar cal = new GregorianCalendar(
                    TimeZone.getTimeZone("UTC"));
            cal.setTimeInMillis(0);
            cal.set(GregorianCalendar.MONTH, 0);
            cal.set(GregorianCalendar.DAY_OF_MONTH, 1);
            cal.set(GregorianCalendar.HOUR_OF_DAY, 0);
            cal.set(GregorianCalendar.MINUTE, 0);
            cal.set(GregorianCalendar.YEAR, start[0]);
            if (frequency - 12.0 < 0.05 && start.length > 1) {
                // GregorianCalendar.MONTH starts with 0
                cal.set(GregorianCalendar.MONTH, start[1] - 1);
            }

            // relational
            TemporalDataset tmpds = new timeBench.data.TemporalDataset();
            tmpds.addDataColumn(name, double.class, null);
            tmpds.addColumn("rindex", int.class, 0);

            for (int i = 0; i < data.length; i++) {
                long inf = cal.getTimeInMillis();

                if (Math.abs(frequency - 12.0) < 0.005)
                    cal.add(GregorianCalendar.MONTH, 1);
                else if (Math.abs(frequency - 4.0) < 0.005)
                    cal.add(GregorianCalendar.MONTH, 3);
                else if (Math.abs(frequency - 0.1) < 0.005)
                    cal.add(GregorianCalendar.YEAR, 10);
                else
                    cal.add(GregorianCalendar.YEAR, 1);

                long sup = cal.getTimeInMillis() - 1;

                tmpds.addTemporalElement(
                        i,
                        inf,
                        sup,
                        granularityId,
                        granularityContextId,
                        TemporalElementStore.PRIMITIVE_INTERVAL);
                TemporalObject to = tmpds.addTemporalObject(i, i);
                to.setInt("rindex", i+1);
                to.set(name, (double) (Math.round(data[i]*100.0)/100.0));
                to.setMin(name, lowerBounds[i]);
                to.setMax(name, upperBounds[i]);
            }

            return tmpds;
        } else
            throw new TemporalDataException("Unsupported R class: "
                    + x.asString());
    }
    
	public String forecastSARIMA(String sarimaModel, Object key, int n_forecast) {
		String resultName = "forecast"+Integer.toHexString(key.hashCode());
		try {
			engine.parseAndEval("library(forecast)");
			REXP result = engine.parseAndEval(resultName + " <- forecast("+sarimaModel+", h= "+String.valueOf(n_forecast)+")", null, true);
			if (result.isNull()) {
				return null;
			}
		} catch (REXPMismatchException e) {
			e.printStackTrace();
		} catch (REngineException e) {
			e.printStackTrace();
		}
		return resultName;
	}
	
	public String generateLowerAndUpperBoundsForecast(String name) {
		String upper = "upper"+name;
		String lower = "lower"+name;
		try {
			REXP result = engine.parseAndEval(lower + " <- ts("+name+"$lower[,1], start=start("+name+
					"$mean), frequency=frequency("+name+"$mean))", null, true);
			if (result.isNull()) {
				return null;
			}
			result = engine.parseAndEval(upper + " <- ts("+name+"$upper[,1], start=start("+name+
					"$mean), frequency=frequency("+name+"$mean))", null, true);
			if (result.isNull()) {
				return null;
			}
		} catch (REXPMismatchException e) {
			e.printStackTrace();
		} catch (REngineException e) {
			e.printStackTrace();
		}
		return name;
	}
    
    private String getACF_PACF(String name, int diff, int seasondiff, int seasonFrequency, int maxlag, String cmd) throws REngineException, REXPMismatchException {
    	String r_command_lag = "";
    	String r_command_all = name;
    	String na_action = ", na.action = na.pass";
    	
		if (maxlag > 0)
			r_command_lag = ", lag.max="+maxlag;
		if (diff > 0)
			r_command_all = "diff("+name+",differences="+diff+")";
		if (seasondiff > 0)
			r_command_all = "diff("+r_command_all+", lag="+seasonFrequency+", differences="+seasondiff+")";
		return " <- "+cmd+"("+r_command_all+", plot=FALSE"+r_command_lag+na_action + ")";
    }
    
    public ACFDataObject getAutocorrelationFunction(String name, int diff, int seasondiff, int seasonFrequency, int maxlag) throws REngineException, REXPMismatchException {
    	String lags_id = "acflags"+diff+maxlag;
    	engine.parseAndEval(lags_id + getACF_PACF(name, diff, seasondiff, seasonFrequency, maxlag, "acf"), null, true);
 		
    	ACFDataObject ret = new ACFDataObject("acf", engine.parseAndEval(lags_id+"$lag[-1]",null, true).asDoubles(),
 				engine.parseAndEval(lags_id+"$acf[-1]",null, true).asDoubles(), 
 				engine.parseAndEval("2/sqrt(length("+name+"))",null, true).asDouble());

     	return ret;
    }
    
    public ACFDataObject getPartialAutocorrelationFunction(String name, int diff, int seasondiff, int seasonFrequency, int maxlag) throws REngineException, REXPMismatchException {
    	String lags_id = "pacflags"+diff+maxlag;
    	engine.parseAndEval(lags_id + getACF_PACF(name, diff, seasondiff, seasonFrequency, maxlag, "pacf"), null, true);
    	ACFDataObject ret = new ACFDataObject("pacf", engine.parseAndEval(lags_id+"$lag",null, true).asDoubles(),
 				engine.parseAndEval(lags_id+"$acf",null, true).asDoubles(), 
 				engine.parseAndEval("2/sqrt(length("+name+"))",null, true).asDouble());
    	
     	return ret;
    }

	public String setDatasetToR(SortedMap<Long, Double> selectedDataset, int granularityID) {
		double[] time = new double[selectedDataset.size()];
        double[] data = new double[selectedDataset.size()];
        int i = 0;
		String name = "dataset"+Integer.toHexString(selectedDataset.hashCode());
		for (Long key : selectedDataset.keySet()){
			time[i] = key / 1000d;
			data[i] = selectedDataset.get(key);
			i++;
		}
		
		try {
			if (!engine.parseAndEval(name, null, true).isNull()) {
				return name;
			}
			
//			engine.assign(name + ".time", time);
			engine.assign(name , data);
//			engine.parseAndEval(name + " <- zooreg(" + name + ".data)");
			
			
			System.out.println("time: "+Arrays.toString(time));
			System.out.println("data: "+Arrays.toString(data));
//			System.out.println("R CMD: "+name + " <- zoo(" + name + ".data)");
//			System.out.println("Class: "+engine.parseAndEval("class("+name+")", null, true).asString());
//			System.out.println("Length: "+engine.parseAndEval("length("+name+")", null, true).asInteger());
//
//			System.out.println("Time: "+Arrays.toString(engine.parseAndEval("time("+name+")", null, true).asIntegers()));
	        // work around a possible timezone bug in as.POSIXct.numeric
	        // cp. http://stackoverflow.com/questions/2457129/converting-unix-seconds-in-milliseconds-to-posixct-posixlt
	        // this.exec("time(" + name + ") <- as.POSIXct(" + name +
	        // ".time, origin=\"1970-01-01 GMT\")");
//	        engine.parseAndEval("time(" + name + ") <- as.POSIXct(as.POSIXlt(" + name
//	                + ".time, origin=\"1970-01-01\"))");
//	        engine.parseAndEval("time(" + name + ") <- as.Date(" + name
//	                + ".time / (24*60*60))");
	        
		} catch (REngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			name = null;
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			name = null;
		}
		return name;
	}
	
	public void setTemporalDatasetToR(String name,
            TemporalDataset tmpds, String dataField, Double missingIdent)
            throws TemporalDataException, REngineException {
        double[] time = new double[tmpds.getTemporalObjectCount()];
        double[] data = new double[tmpds.getTemporalObjectCount()];
        int i = 0;
        
//        int dataCol = tmpds.getNodeTable().getColumnNumber(dataField);
        int dataCol = tmpds.getDataColumnIndices()[0];

        for (TemporalObject cur : tmpds.temporalObjects()) { 
            if (cur.getTemporalElement().isAnchored()) {
                time[i] = ((double) cur.getTemporalElement().asGeneric()
                        .getInf()) / 1000.0d;
                if (missingIdent == null || missingIdent != cur.getDouble(dataCol))
                	data[i] = cur.getDouble(dataCol);
                else {
                	data[i] = Double.NaN;
                }
                i++;
            } 
        }

        if (i == 0)
            throw new TemporalDataException(
                    "TemporalDataSet did not contain any anchored objects.");

        // shorten arrays, if some temp.obj. were skipped
        if (i < time.length || i < data.length) {
            double[] temp = new double[i];
            System.arraycopy(time, 0, temp, 0, i);
            time = temp;
            System.arraycopy(data, 0, temp, 0, i);
            data = temp;
        }

        try {
        engine.assign(name + ".time", time);
        engine.assign(name + ".data", data);
        engine.parseAndEval(name + " <- zoo(" + name + ".data)");
        // work around a possible timezone bug in as.POSIXct.numeric
        // cp. http://stackoverflow.com/questions/2457129/converting-unix-seconds-in-milliseconds-to-posixct-posixlt
        // this.exec("time(" + name + ") <- as.POSIXct(" + name +
        // ".time, origin=\"1970-01-01 GMT\")");
        engine.parseAndEval("time(" + name + ") <- as.POSIXct(as.POSIXlt(" + name
                + ".time, origin=\"1970-01-01\"))");
        } catch (REngineException e) {
        	e.printStackTrace();
        } catch (REXPMismatchException e) {
			e.printStackTrace();
		}
    }

}
