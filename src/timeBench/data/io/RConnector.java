package timeBench.data.io;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.REngineStdOutput;

import timeBench.calendar.JavaDateCalendarManager;
import timeBench.data.TemporalDataException;
import timeBench.data.oo.TemporalDataset;


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

    // could be replaced by REngine.getLastEngine()
    private REngine engine;

    RConnector() {
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

    public timeBench.data.relational.TemporalDataset getTemporalDataset(
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
//            Calendar calendar = CalendarManagerFactory.getSingleton(
//                    CalendarManagers.JavaDate).getDefaultCalendar();
            int granularityContextId = JavaDateCalendarManager.Granularities.Top
                    .toInt();
            int granularityId = -1;
            if (Math.abs(frequency - 1.0) < 0.005
                    || Math.abs(frequency - 0.1) < 0.005)
                granularityId = JavaDateCalendarManager.Granularities.Year
                        .toInt();
            else if (Math.abs(frequency - 12.0) < 0.005)
                granularityId = JavaDateCalendarManager.Granularities.Month
                        .toInt();
            else
                throw new TemporalDataException(
                        "Unknown granularity. frequency: " + frequency);

            // prepare a calendar
            GregorianCalendar cal = new GregorianCalendar();
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
            timeBench.data.relational.TemporalDataset tmpds = new timeBench.data.relational.TemporalDataset();
            tmpds.getDataElements().addColumn(name, double.class);

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

                int te = tmpds
                        .addTemporalElement(
                                inf,
                                sup,
                                granularityId,
                                granularityContextId,
                                timeBench.data.relational.TemporalDataset.PRIMITIVE_INTERVAL);
                int dataRow = tmpds.getDataElements().addRow();
                tmpds.getDataElements().set(dataRow, name, data[i]);
                tmpds.addOccurrence(dataRow, te);
            }

            return tmpds;
        } else
            throw new TemporalDataException("Unsupported R class: "
                    + x.asString());
    }

    public void putTemporalDataset(String name, TemporalDataset tmpds) {
        throw new RuntimeException("not yet implemented");
    }

    public void exec(String command) throws REngineException {
        try {
            engine.parseAndEval(command, null, false);
        } catch (REXPMismatchException e) {
            logger.error("unexpected exception", e);
        }
    }

    public boolean close() {
        return engine.close();
    }

}
