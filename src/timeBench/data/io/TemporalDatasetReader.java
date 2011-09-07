package timeBench.data.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import prefuse.data.io.DataIOException;
import timeBench.data.relational.TemporalDataSetDummy;

/**
 * interface for classes that read in a temporal dataset from a particular file
 * format.
 * 
 * Based on {@link prefuse.data.io.GraphReader}.
 * 
 * @author Alexander Rind
 */
public interface TemporalDatasetReader {

    /**
     * Read in a temporal dataset from the file at the given location. Though
     * not required by this interface, the String is typically resolved using
     * the {@link prefuse.util.io.IOLib#streamFromString(String)} method,
     * allowing URLs, classpath references, and files on the file system to be
     * accessed.
     * 
     * @param location
     *            the location to read the temporal dataset from
     * @return the loaded temporal dataset
     * @throws FileNotFoundException
     * @throws IOException
     */
    public TemporalDataSetDummy readData(String location)
            throws DataIOException;

    /**
     * Read in a temporal dataset from the given URL.
     * 
     * @param url
     *            the url to read the temporal dataset from
     * @return the loaded temporal dataset
     * @throws IOException
     */
    public TemporalDataSetDummy readData(URL url) throws DataIOException;

    /**
     * Read in a temporal dataset from the given File.
     * 
     * @param f
     *            the file to read the temporal dataset from
     * @return the loaded temporal dataset
     * @throws FileNotFoundException
     * @throws IOException
     */
    public TemporalDataSetDummy readData(File f) throws DataIOException;

    /**
     * Read in a temporal dataset from the given InputStream.
     * 
     * @param is
     *            the InputStream to read the temporal dataset from
     * @return the loaded temporal dataset
     * @throws IOException
     */
    public TemporalDataSetDummy readData(InputStream is) throws DataIOException;

}
