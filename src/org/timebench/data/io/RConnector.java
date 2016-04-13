package org.timebench.data.io;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.REngineStdOutput;
import org.timebench.calendar.Calendar;
import org.timebench.calendar.CalendarFactory;
import org.timebench.calendar.Granularity;
import org.timebench.calendar.JavaDateCalendarManager;
import org.timebench.data.TemporalDataException;
import org.timebench.data.TemporalDataset;
import org.timebench.data.TemporalElement;
import org.timebench.data.TemporalElementStore;
import org.timebench.data.TemporalObject;

/**
 * XXX Should we (a) encapsulate REngine or (b) provide some static helper methods?
 * 
 * TODO How handle R Exceptions?
 * 
 * @author Rind
 */
public class RConnector {

    public static final String RENGINE_IMPL = "org.rosuda.REngine.JRI.JRIEngine";
    private static Logger logger = Logger.getLogger(RConnector.class);

    private static RConnector instance;
    
    private Calendar calendar = JavaDateCalendarManager.getSingleton().getDefaultCalendar();

    // could be replaced by REngine.getLastEngine()
    private REngine engine;  
    
    private RConnector() {
        try {
            if (logger.isInfoEnabled())
                // show R console output in System.out
                engine = REngine.engineForClass(RENGINE_IMPL, new String[] {
                        "--no-save", "--no-restore" }, new REngineStdOutput(),
                        false);
            else
                engine = REngine.engineForClass(RENGINE_IMPL);
        } catch (ClassNotFoundException e) {
            logger.error("unexpected exception", e);
        } catch (NoSuchMethodException e) {
            logger.error("unexpected exception", e);
        } catch (IllegalAccessException e) {
            logger.error("unexpected exception", e);
        } catch (InvocationTargetException e) {
            logger.error("unexpected exception", e);
        }
    }

    public static synchronized RConnector getInstance(Calendar calendar) {
    	RConnector instance = getInstance();
    	instance.calendar = calendar;
    	return instance;
    }
    
    public static synchronized RConnector getInstance() {
        if (null == instance)
            instance = new RConnector();
        return instance;
    }

    public double getDouble(String name) throws REXPMismatchException,
            REngineException {
        REXP x = engine.parseAndEval(name, null, true);
        return x.asDouble();
    }

    public TemporalDataset getTemporalDataset(
            String name) throws REngineException, REXPMismatchException,
            TemporalDataException {
        // check R class
        REXP x = engine.parseAndEval("class(" + name + ")", null, true);
        if (!x.isString())
            throw new TemporalDataException("Unsupported R class: " + x);
        logger.trace(x.asString());

        // ts ... simplest R time series class
        if ("ts".equals(x.asString())) {
            int[] start = engine
                    .parseAndEval("start(" + name + ")", null, true)
                    .asIntegers();
            double frequency = engine.parseAndEval("frequency(" + name + ")",
                    null, true).asDouble();
            double[] data = engine.parseAndEval("as.vector(" + name + ")",
                    null, true).asDoubles();
            logger.debug("ts with start: " + Arrays.toString(start)
                    + ", frequency: " + frequency + ", obs: " + data.length);

            // get calendar and granularity
            Calendar calendar = JavaDateCalendarManager.getSingleton().getDefaultCalendar();
            Granularity granularity = null;
            if (Math.abs(frequency - 1.0) < 0.005
                    || Math.abs(frequency - 0.1) < 0.005)
            	granularity = CalendarFactory.getSingleton().getGranularity(calendar,"Year","Top");
            else if (Math.abs(frequency - 12.0) < 0.005)
                granularity = CalendarFactory.getSingleton().getGranularity(calendar,"Month","Top");
            else
                throw new TemporalDataException(
                        "Unknown granularity. frequency: " + frequency);

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
            TemporalDataset tmpds = new org.timebench.data.TemporalDataset();
            tmpds.addDataColumn(name, double.class, null);

            // time indices are only used in log file to check our code
            double[] time = new double[0];
            DateFormat df = null;
            if (logger.isTraceEnabled()) {
                time = engine.parseAndEval(
                    "as.vector(time(" + name + "))", null, true).asDoubles();
                df = DateFormat.getDateTimeInstance();
            }

            for (int i = 0; i < data.length; i++) {
                long inf = cal.getTimeInMillis();

                if (Math.abs(frequency - 12.0) < 0.005)
                    cal.add(GregorianCalendar.MONTH, 1);
                else if (Math.abs(frequency - 0.1) < 0.005)
                    cal.add(GregorianCalendar.YEAR, 10);
                else
                    cal.add(GregorianCalendar.YEAR, 1);

                long sup = cal.getTimeInMillis() - 1;
                if (logger.isTraceEnabled())
                    logger.trace("time from R: " + time[i] + " inf: "
                            + df.format(new Date(inf)) + " sup: "
                            + df.format(new Date(sup)) + " data: " + data[i]);

                tmpds.addTemporalElement(
                        i,
                        inf,
                        sup,
                        granularity,
                        TemporalElementStore.PRIMITIVE_INTERVAL);
                TemporalObject to = tmpds.addTemporalObject(i, i);
                to.set(name, data[i]);
            }

            return tmpds;
        } else if ("zoo".equals(x.asString())) {
            // zoo ... feature-rich R time series class
            double start = engine.parseAndEval(
                    "as.numeric(start(" + name + "))", null, true).asDouble();
            double[] data = engine.parseAndEval("coredata(" + name + ")", null,
                    true).asDoubles();
            double[] time = engine.parseAndEval(
                    "as.numeric(time(" + name + "))", null, true).asDoubles();
            logger.debug("ts with start: " + start + ", obs: " + data.length);


            Granularity granularity = calendar.getBottomGranularity();
            
            // relational
            TemporalDataset tmpds = new org.timebench.data.TemporalDataset();
            tmpds.addDataColumn(name, double.class, null);

            // time indices are only used in log file to check our code
            DateFormat df = null;
            if (logger.isTraceEnabled()) {
                df = DateFormat.getDateTimeInstance();
            }

            for (int i = 0; i < data.length; i++) {
                long inf = Math.round(time[i] * 1000);
                if (logger.isTraceEnabled())
                    logger.trace("time from R: " + time[i] + " inf: "
                            + df.format(new Date(inf)) + " data: " + data[i]);

                tmpds.addTemporalElement(
                        i,
                        inf,
                        inf,
                        granularity,
                        TemporalElementStore.PRIMITIVE_INSTANT);
                TemporalObject to = tmpds.addTemporalObject(i, i);
                to.set(name, data[i]);
            }

            return tmpds;
        } else
            throw new TemporalDataException("Unsupported R class: "
                    + x.asString());
    }

    public void putTemporalDataset(String name,
            TemporalDataset tmpds, String dataField)
            throws TemporalDataException, REngineException {
        double[] time = new double[tmpds.getTemporalObjectCount()];
        double[] data = new double[tmpds.getTemporalObjectCount()];
        int i = 0;
        
        int dataCol = tmpds.getNodeTable().getColumnNumber(dataField);

        for (TemporalObject cur : tmpds.temporalObjects()) { 
            if (cur.getTemporalElement().isAnchored()) {
                time[i] = ((double) cur.getTemporalElement().asGeneric()
                        .getInf()) / 1000.0d;
                data[i] = cur.getDouble(dataCol);
                i++;
            } else {
                logger.debug("skip temp.o. with temp.el. "
                        + cur.getTemporalElement());
            }
        }
        logger.trace(i);

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

        engine.assign(name + ".time", time);
        engine.assign(name + ".data", data);
        this.exec(name + " <- zoo(" + name + ".data)");
        // work around a possible timezone bug in as.POSIXct.numeric
        // cp. http://stackoverflow.com/questions/2457129/converting-unix-seconds-in-milliseconds-to-posixct-posixlt
        // this.exec("time(" + name + ") <- as.POSIXct(" + name +
        // ".time, origin=\"1970-01-01 GMT\")");
        this.exec("time(" + name + ") <- as.POSIXct(as.POSIXlt(" + name
                + ".time, origin=\"1970-01-01\"))");
    }

    public void putTemporalDataset(String name,
            TemporalDataset tmpds, String dataField,
            int startYear, int startMonth, double frequency, int length)
            throws REngineException {
        double[] data = new double[length];
        Arrays.fill(data, REXPDouble.NA);

        if (frequency != 12.0)
            throw new RuntimeException("only months supported");

        Granularity granularity = CalendarFactory.getSingleton().getGranularity(
        		JavaDateCalendarManager.getSingleton().getDefaultCalendar(),
        		"Month","Top");

        int dataCol = tmpds.getNodeTable().getColumnNumber(dataField);

        for (TemporalObject cur : tmpds.temporalObjects()) { 
            TemporalElement elem = cur.getTemporalElement();
            if (!elem.isAnchored()) {
                logger.debug("skip temp.o. with unanchored temp.el. " + elem);
            } else if (elem.getGranularityId() != granularity.getIdentifier()
                    || elem.getGranularityContextId() != granularity.getGranularityContextIdentifier()) {
                logger.debug("skip temp.o. with wrong granularity " + elem);
            } else {
                long inf = elem.asGeneric().getInf();

                GregorianCalendar cal = new GregorianCalendar(
                        TimeZone.getTimeZone("UTC"));
                cal.setTimeInMillis(inf);
                int year = cal.get(GregorianCalendar.YEAR);
                int month = cal.get(GregorianCalendar.MONTH) + 1;
                int index = (year - startYear) * 12 + month - startMonth;

                if (index >= 0 && index < length) {
                    data[index] = cur.getDouble(dataCol);
                } else {
                    logger.debug("skip temp.o. out of temporal bounds " + index
                            + " " + elem);
                }
            }
        }

        // copy data to R and build ts object
        engine.assign(name + ".data", data);
        this.exec(name + " <- ts(" + name + ".data, start = c(" + startYear
                + ", " + startMonth + "), frequency = " + frequency + ")");
    }

    public void exec(String command) throws REngineException {
        try {
            logger.debug("R> " + command);
            engine.parseAndEval(command, null, false);
        } catch (REXPMismatchException e) {
            logger.error("unexpected exception", e);
        }
    }

    public boolean close() {
        return engine.close();
    }

}
