package timeBench.data.io.schema;

import java.util.Date;
import java.util.Map;

import prefuse.data.Tuple;
import timeBench.calendar.Granule;
import timeBench.data.GenericTemporalElement;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;

public abstract class InstantEncoding extends TemporalObjectEncoding {

    InstantEncoding() {
    }

    protected abstract Date buildDate(Tuple tuple) throws TemporalDataException;

    @Override
    public void buildTemporalElement(TemporalDataset tmpds, Tuple tuple,
            Map<String, GenericTemporalElement> elements) throws TemporalDataException {
        Granule granule = super.getGranularity().parseDateToGranule(
                buildDate(tuple));

        GenericTemporalElement elem = tmpds.addInstant(granule).asGeneric();

        elements.put(this.getKey(), elem);
    }
}
