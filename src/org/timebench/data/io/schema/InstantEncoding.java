package org.timebench.data.io.schema;

import java.util.Date;
import java.util.Map;

import org.timebench.calendar.Granule;
import org.timebench.data.GenericTemporalElement;
import org.timebench.data.TemporalDataException;
import org.timebench.data.TemporalDataset;

import prefuse.data.Tuple;

public abstract class InstantEncoding extends TemporalObjectEncoding {

    InstantEncoding() {
    }

    protected abstract Date buildDate(Tuple tuple) throws TemporalDataException;

    @Override
    public void buildTemporalElement(TemporalDataset tmpds, Tuple tuple,
            Map<String, GenericTemporalElement> elements) throws TemporalDataException {
        Granule granule = new Granule(buildDate(tuple),super.getGranularity());

        GenericTemporalElement elem = tmpds.addInstant(granule).asGeneric();

        elements.put(this.getKey(), elem);
    }
}
