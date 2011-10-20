package timeBench.data.io;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.io.DataIOException;
import prefuse.data.io.TableReader;
import prefuse.util.collections.IntIterator;
import timeBench.calendar.Calendar;
import timeBench.calendar.CalendarManagerFactory;
import timeBench.calendar.CalendarManagers;
import timeBench.data.TemporalDataException;
import timeBench.data.io.schema.DateInstantEncoding;
import timeBench.data.io.schema.TemporalDataColumnSpecification;
import timeBench.data.io.schema.TemporalObjectEncoding;
import timeBench.data.relational.TemporalDataset;

public class TextTableTemporalDatasetReader extends
        AbstractTemporalDatasetReader {

    private static final Logger logger = Logger
            .getLogger(TextTableTemporalDatasetReader.class);

    private TemporalDataColumnSpecification spec = null;

    public TextTableTemporalDatasetReader() {
        this.spec = null;
    }
    
    public TextTableTemporalDatasetReader(TemporalDataColumnSpecification spec) {
        super();
        this.spec = spec;
    }

    @Override
    public TemporalDataset readData(InputStream is) throws DataIOException,
            TemporalDataException {

        TemporalDataColumnSpecification spec = (this.spec != null) ? this.spec
                : new TemporalDataColumnSpecification();

        TableReader tableReader = spec.getTableFormat().getTableReader();
        Table table = tableReader.readTable(is);

        if (this.spec == null)
            scanTableForSpecification(table, spec);

        TemporalDataset tmpds = new TemporalDataset();
        importTable(table, tmpds, spec);
        table = null;
        return tmpds;
    }

    // TODO should the class remember the spec or stay in auto-detection mode  
    private static void scanTableForSpecification(Table table,
            TemporalDataColumnSpecification spec) throws TemporalDataException {

        Calendar calendar = CalendarManagerFactory.getSingleton(
                CalendarManagers.JavaDate).getDefaultCalendar();
        spec.setCalendar(calendar);

        String temporalColumn = null;
        LinkedList<String> dataColumns = new LinkedList<String>();
        for (int i = 0; i < table.getColumnCount(); i++) {
            String col = table.getColumnName(i);
            if (table.canGetDate(col) && temporalColumn == null)
                temporalColumn = col;
            else
                dataColumns.add(col);
        }

        if (temporalColumn == null)
            throw new TemporalDataException(
                    "Imported data table does not have a recognized temporal column.");

        DateInstantEncoding enc = new DateInstantEncoding("default",
                temporalColumn);
        enc.setDataColumns(dataColumns.toArray(new String[0]));
        enc.setGranularity(calendar.getDiscreteTimeDomain());
        spec.addEncoding(enc);
    }

    public static void importTable(Table table, TemporalDataset tmpds,
            TemporalDataColumnSpecification spec) throws TemporalDataException {

        // 1. analyze & prepare schemata
        if (logger.isDebugEnabled())
            logger.debug("start import " + table);
        spec.init();
        TreeMap<String, Integer> elements = new TreeMap<String, Integer>();

        // 1.2. prepare table for data elements
        for (TemporalObjectEncoding encoding : spec.getEncodings()) {
            try {
                prepareDataColumns(tmpds, table, encoding);
            } catch (TemporalDataException e) {
                // this is safe if the data element table has no column
                logger.fatal("Failed to build data columns on import", e);
                System.exit(1);
            }
        }

        // 2. for each data row
        IntIterator rows = table.rows();
        while (rows.hasNext()) {
            Tuple tuple = (Tuple) table.getTuple(rows.nextInt());

            try {
                // 2.1. for each schema
                for (TemporalObjectEncoding encoding : spec.getEncodings()) {
                    logger.trace(encoding.getKey());

                    // 2.1.1. extract temporal element & append to TempDS
                    encoding.buildTemporalElement(tmpds, tuple, elements);

                    // 2.1.2. if it has data columns
                    if (encoding.getDataColumns().length > 0) {
                        // 2.1.2.1. extract data element & append to TempDS
                        // (optional)
                        int dataRow = addDataElement(tmpds, tuple, encoding);
                        logger.debug("data row " + dataRow);

                        // 2.1.2.2. link temporal element with data element in
                        // TempDS
                        tmpds.addOccurrence(dataRow,
                                elements.get(encoding.getKey()));
                    }
                }
            } catch (TemporalDataException e) {
                if (spec.isFailOnIllegalRows())
                    throw e;
                else {
                    if (logger.isInfoEnabled())
                        logger.info("skip row import: " + tuple + " Reason: ",
                                e);
                }
            }
            // 2.2 clear cached temporal elements
            elements.clear();
        }
    }

    private static void prepareDataColumns(TemporalDataset tmpds, Table table,
            TemporalObjectEncoding schema) throws TemporalDataException {
        Table dataElements = tmpds.getDataElements();

        for (String col : schema.getDataColumns()) {
            if (dataElements.getColumnNumber(col) == -1) {
                // column does not exist yet --> add it
                if (logger.isDebugEnabled())
                    logger.debug("prepare data col \"" + col + "\" type "
                            + table.getColumnType(col));
                dataElements.addColumn(col, table.getColumnType(col));
            } else if (dataElements.getColumnType(col) != table
                    .getColumnType(col)) {
                throw new TemporalDataException("Data column " + col
                        + " already exists with a different type: is "
                        + dataElements.getColumnType(col) + " expected "
                        + table.getColumnType(col));
            } else if (logger.isDebugEnabled())
                logger.debug("skip data col \"" + col + "\" type "
                        + table.getColumnType(col));
        }
    }

    private static int addDataElement(TemporalDataset tmpds, Tuple tuple,
            TemporalObjectEncoding schema) {
        Table dataElements = tmpds.getDataElements();

        int rowNumber = dataElements.addRow();
        for (String col : schema.getDataColumns()) {
            if (logger.isTraceEnabled())
                logger.trace("add data item " + col + " value "
                        + tuple.get(col));
            // TODO insert switch (columnType) and call setInt(getInt())
            // if we are more serious about performance
            dataElements.set(rowNumber, col, tuple.get(col));
        }
        return rowNumber;
    }
    
    public TemporalDataColumnSpecification getSpecification() {
        return spec;
    }

    public void setSpecification(TemporalDataColumnSpecification spec) {
        this.spec = spec;
    }
}
