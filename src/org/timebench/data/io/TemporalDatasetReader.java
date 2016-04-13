package org.timebench.data.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.timebench.data.TemporalDataException;
import org.timebench.data.TemporalDataset;

import prefuse.data.io.DataIOException;
import prefuse.data.parser.DataParseException;

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
     * @throws TemporalDataException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public TemporalDataset readData(String location) throws DataIOException,
            TemporalDataException;

    /**
     * Read in a temporal dataset from the given URL.
     * 
     * @param url
     *            the url to read the temporal dataset from
     * @return the loaded temporal dataset
     * @throws TemporalDataException
     * @throws IOException
     */
    public TemporalDataset readData(URL url) throws DataIOException,
            TemporalDataException;

    /**
     * Read in a temporal dataset from the given File.
     * 
     * @param f
     *            the file to read the temporal dataset from
     * @return the loaded temporal dataset
     * @throws TemporalDataException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public TemporalDataset readData(File f) throws DataIOException,
            TemporalDataException;

    /**
     * Read in a temporal dataset from the given InputStream.
     * 
     * @param is
     *            the InputStream to read the temporal dataset from
     * @return the loaded temporal dataset
     * @throws TemporalDataException
     * @throws IOException
     */
    public TemporalDataset readData(InputStream is) throws DataIOException,
            TemporalDataException;

}
