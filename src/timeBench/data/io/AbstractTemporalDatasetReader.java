package timeBench.data.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import prefuse.data.io.DataIOException;
import prefuse.data.parser.DataParseException;
import prefuse.util.io.IOLib;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;

/**
 * Abstract base class implementation of the TemporalDatasetReader interface. Provides
 * implementations for all but the
 * {@link timeBench.data.io.TemporalDatasetReader#readData(InputStream)} method.
 * 
 * Based on {@link prefuse.data.io.AbstractGraphReader}.
 * 
 * @author Alexander Rind
 */
public abstract class AbstractTemporalDatasetReader implements
        TemporalDatasetReader {

    @Override
    public TemporalDataset readData(String location) throws DataIOException,
            TemporalDataException {
        try {
            InputStream is = IOLib.streamFromString(location);
            if (is == null)
                throw new DataIOException("Couldn't find " + location
                        + ". Not a valid file, URL, or resource locator.");
            return readData(is);
        } catch (IOException e) {
            throw new DataIOException(e);
        }
    }

    @Override
    public TemporalDataset readData(URL url) throws DataIOException,
            TemporalDataException {
        try {
            return readData(url.openStream());
        } catch (IOException e) {
            throw new DataIOException(e);
        }
    }

    @Override
    public TemporalDataset readData(File f) throws DataIOException,
            TemporalDataException {
        try {
            return readData(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            throw new DataIOException(e);
        }
    }

    @Override
    public abstract TemporalDataset readData(InputStream is)
            throws DataIOException, TemporalDataException;

}
