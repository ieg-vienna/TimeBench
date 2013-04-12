package timeBench.action.analytical;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalObject;

/**
 * Converts temporal data stored in columns of a pivot table to multiples
 * temporal objects (key/value pairs).
 * 
 * How call it: in TIS "dissolve pivot table"
 * 
 * @author Rind
 */
public class ColumnToRowsTemporalDataTransformation {

    private final Logger logger = Logger.getLogger(this.getClass());

    public TemporalDataset toRows(TemporalDataset input)
            throws TemporalDataException {
        List<String> colsFlatten = new LinkedList<String>();
        List<String> colsPassed = new LinkedList<String>();
        for (int i : input.getDataColumnIndices()) {
            colsFlatten.add(input.getNodeTable().getColumnName(i));
            if (logger.isTraceEnabled())
                logger.trace("data col: "
                        + input.getNodeTable().getColumnName(i));
        }

        return toRows(input, "category", "value", colsFlatten, colsPassed);
    }

    public TemporalDataset toRows(TemporalDataset input, String colCategory,
            String colValue, List<String> colsFlatten, List<String> colsPassed)
            throws TemporalDataException {
        TemporalDataset result = new TemporalDataset(
                input.getTemporalElements());

        // TODO determine data type of colsFlatten
        for (String col : colsFlatten) {
            if (!input.getNodeTable().canGetDouble(col))
                throw new UnsupportedOperationException(
                        "Flatten only supports double columns yet.");
        }

        // TODO prepare for passed columns
        if (colsPassed.size() > 0) {
            throw new UnsupportedOperationException(
                    "Flatten does not yet support passed columns.");
        }

        // prepare columns
        result.addDataColumn(colCategory, String.class, null);
        result.addDataColumn(colValue, double.class, -1);

        for (TemporalObject rowObj : input.temporalObjects()) {
            for (String col : colsFlatten) {
                TemporalObject flatObj = result.addTemporalObject(rowObj
                        .getTemporalElement());
                flatObj.setString(colCategory, col);
                flatObj.setDouble(colValue, rowObj.getDouble(col));
            }
        }

        return result;
    }

}
