package org.timebench.data.io;

import ieg.prefuse.data.DataHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.timebench.calendar.Granularity;
import org.timebench.calendar.JavaDateCalendarManager;
import org.timebench.data.GenericTemporalElement;
import org.timebench.data.TemporalDataException;
import org.timebench.data.TemporalDataset;
import org.timebench.data.TemporalObject;
import org.timebench.data.io.schema.DateInstantEncoding;
import org.timebench.data.io.schema.TemporalDataColumnSpecification;
import org.timebench.data.io.schema.TemporalObjectEncoding;
import org.timebench.util.xml.JaxbMarshaller;

import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.io.DataIOException;
import prefuse.data.io.TableReader;
import prefuse.util.collections.IntIterator;

/**
 * Reads a TemporalDataset and either tries to automatically detect the temporal
 * column or use a given data format specification.
 * 
 * <p>
 * Added: 2011-09-27 / AR<br>
 * Modifications: 2013-03-25 / AR / autoGranularity
 * </p>
 * 
 * @author Alexander Rind
 */
public class TextTableTemporalDatasetReader extends
        AbstractTemporalDatasetReader {

    private static final Logger logger = Logger
            .getLogger(TextTableTemporalDatasetReader.class);

    private TemporalDataColumnSpecification spec = null;
    
    private Granularity autoGranularity = null;

    /**
     * Reads a TemporalDataset and tries to automatically detect the temporal
     * column. Granules of the bottom granularity are assumed.
     */
    public TextTableTemporalDatasetReader() {
        this.spec = null;
    }
    
    /**
     * Reads a TemporalDataset and tries to automatically detect the temporal
     * column. The temporal elements will be instants based on the given
     * granularity.
     * 
     * @param granularity
     */
    public TextTableTemporalDatasetReader(Granularity granularity) {
        super();
        this.autoGranularity = granularity;
    }

    public TextTableTemporalDatasetReader(TemporalDataColumnSpecification spec) {
        super();
        this.spec = spec;
    }

    public TextTableTemporalDatasetReader(String xmlFile) throws IOException,
            JAXBException {
        super();
        this.spec = (TemporalDataColumnSpecification) JaxbMarshaller.loadUser(
                xmlFile, TemporalDataColumnSpecification.class);
    }

    @Override
    public TemporalDataset readData(InputStream is) throws DataIOException,
            TemporalDataException {

        TemporalDataColumnSpecification spec = (this.spec != null) ? this.spec
                : new TemporalDataColumnSpecification();

        TableReader tableReader = spec.getTableFormat().getTableReader();
        // XXX hack to handle prefuse String -> Date conversion in UTC
        TimeZone oldDefault = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Table table = tableReader.readTable(is);
        TimeZone.setDefault(oldDefault);

        if (logger.isInfoEnabled()) {
            logger.info("Metadata of raw table:");
            DataHelper.printMetadata(System.out, table);
        }
        if (this.spec == null)
            scanTableForSpecification(table, spec);

        TemporalDataset tmpds = new TemporalDataset();
        importTable(table, tmpds, spec);
        table = null;
        return tmpds;
    }

    // TODO should the class remember the spec or stay in auto-detection mode  
    private void scanTableForSpecification(Table table,
            TemporalDataColumnSpecification spec) throws TemporalDataException {

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
        enc.setGranularity(autoGranularity != null ? autoGranularity : 
                JavaDateCalendarManager.getSingleton().getDefaultCalendar().getBottomGranularity());
        spec.addEncoding(enc);
    }

    public static void importTable(Table table, TemporalDataset tmpds,
            TemporalDataColumnSpecification spec) throws TemporalDataException {

        // 1. analyze & prepare schemata
        if (logger.isDebugEnabled())
            logger.debug("start import " + table);
        spec.init();

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
        // map of temporal elements extracted from 1 row: specKey -> row 
        TreeMap<String, GenericTemporalElement> elements = new TreeMap<String, GenericTemporalElement>();
        long temporalObjectId = 0;
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
                        if (encoding.isTemporalObjectIdIncluded()) { 
                            temporalObjectId = tuple.getLong(TemporalObject.ID);
                        } else {
                            temporalObjectId++;
                        }
                        logger.debug("temporal object id " + temporalObjectId);
                            
                        // 2.1.2.1. link temporal element with data element in TempDS
                        TemporalObject obj = tmpds.addTemporalObject(
                                temporalObjectId,
                                elements.get(encoding.getKey()).getId());
                        
                        // 2.1.2.2. extract data element & append to TempDS
                        copyDataElements(obj, tuple, encoding);
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
        Table dataElements = tmpds.getNodeTable();

        for (String col : schema.getDataColumns()) {
            if (dataElements.getColumnNumber(col) == -1) {
                // column does not exist yet --> add it
                if (logger.isDebugEnabled())
                    logger.debug("prepare data col \"" + col + "\" type "
                            + table.getColumnType(col));
                tmpds.addDataColumn(col, table.getColumnType(col), null);
            } else if (dataElements.getColumnType(col) != table
                    .getColumnType(col)) {
                // column exists but wrong data type
                throw new TemporalDataException("Data column " + col
                        + " already exists with a different type: is "
                        + dataElements.getColumnType(col) + " expected "
                        + table.getColumnType(col));
            } else if (col.equals(TemporalObject.TEMPORAL_ELEMENT_ID)) {
                // try to put data in foreign key column
                throw new TemporalDataException("Data column " + col
                        + " is reserved for TimeBench.");
            } else if (logger.isDebugEnabled())
                logger.debug("skip data col \"" + col + "\" type "
                        + table.getColumnType(col));
        }
    }

    private static void copyDataElements(TemporalObject obj, Tuple tuple,
            TemporalObjectEncoding schema) {
        for (String col : schema.getDataColumns()) {
            if (logger.isTraceEnabled())
                logger.trace("add data item " + col + " value "
                        + tuple.get(col));
            // TODO insert switch (columnType) and call setInt(getInt())
            // if we are more serious about performance
            obj.set(col, tuple.get(col));
        }
    }
    
    public TemporalDataColumnSpecification getSpecification() {
        return spec;
    }

    public void setSpecification(TemporalDataColumnSpecification spec) {
        this.spec = spec;
    }
}
