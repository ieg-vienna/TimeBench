package org.timebench.data.io;

import java.io.File;
import java.io.OutputStream;

import org.timebench.data.TemporalDataset;

import prefuse.data.io.DataIOException;

/**
 * interface for classes that write a temporal dataset to a particular file
 * format.
 * 
 * Based on {@link prefuse.data.io.GraphWriter}.
 * 
 * @author Alexander Rind
 */
public interface TemporalDatasetWriter {

    /**
     * Write a {@link TemporalDataset} to the file with the given filename.
     * 
     * @param tmpds
     *            the {@link TemporalDataset} to write
     * @param filename
     *            the file to write the temporal dataset to
     * @throws DataIOException
     */
    public void writeData(TemporalDataset tmpds, String filename)
            throws DataIOException;

    /**
     * Write a {@link TemporalDataset} to the given {@link File}.
     * 
     * @param tmpds
     *            the {@link TemporalDataset} to write
     * @param f
     *            the file to write the temporal dataset to
     * @throws DataIOException
     */
    public void writeData(TemporalDataset tmpds, File f)
            throws DataIOException;

    /**
     * Write a {@link TemporalDataset} to the given {@link OutputStream}.
     * 
     * @param tmpds
     *            the {@link TemporalDataset} to write
     * @param os
     *            the OutputStream to write the temporal dataset to
     * @throws DataIOException
     */
    public void writeData(TemporalDataset tmpds, OutputStream os)
            throws DataIOException;
}
