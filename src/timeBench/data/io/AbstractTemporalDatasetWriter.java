package timeBench.data.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import prefuse.data.io.DataIOException;
import timeBench.data.relational.TemporalDataset;

/**
 * Abstract base class implementation of the TemporalDatasetWriter interface.
 * Provides implementations for all but the
 * {@link timeBench.data.io.TemporalDatasetWriter#writeData(TemporalDataset,OutputStream)}
 * method.
 * 
 * Based on {@link prefuse.data.io.AbstractGraphWriter}.
 * 
 * @author Alexander Rind
 */
public abstract class AbstractTemporalDatasetWriter implements
        TemporalDatasetWriter {

    @Override
    public void writeData(TemporalDataset tmpds, String filename)
            throws DataIOException {
        writeData(tmpds, new File(filename));
    }

    @Override
    public void writeData(TemporalDataset tmpds, File f) throws DataIOException {
        try {
            writeData(tmpds, new FileOutputStream(f));
        } catch (FileNotFoundException e) {
            throw new DataIOException(e);
        }
    }
}
