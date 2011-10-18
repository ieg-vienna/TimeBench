package timeBench.data.io.schema;

import java.util.Date;
import java.util.Map;

import prefuse.data.Tuple;
import timeBench.calendar.Granule;
import timeBench.data.TemporalDataException;
import timeBench.data.oo.TemporalElement;
import timeBench.data.relational.TemporalDataset;

public abstract class InstantEncoding extends TemporalObjectEncoding {

    InstantEncoding() {
    }

    protected abstract Date buildDate(Tuple tuple) throws TemporalDataException;

    @Override
    public void buildTemporalElement(TemporalDataset tmpds, Tuple tuple,
            Map<String, Integer> elements) throws TemporalDataException {
        Granule granule = super.getGranularity().parseDateToGranule(
                buildDate(tuple));

        int row = tmpds.addTemporalElement(granule.getInf(), granule.getSup(),
                super.getGranularity().getIdentifier(),
                TemporalDataset.PRIMITIVE_INSTANT);

        elements.put(super.getKey(), row);
    }

    @Override
    public void buildTemporalElement(Tuple tuple,
            Map<String, TemporalElement> elements) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }
}
