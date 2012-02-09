package timeBench.data.io.schema;

import java.util.Date;
import java.util.Map;

import prefuse.data.Tuple;
import timeBench.calendar.Granule;
import timeBench.data.TemporalDataException;
import timeBench.data.relational.GenericTemporalElement;
import timeBench.data.relational.TemporalDataset;

public abstract class InstantEncoding extends TemporalObjectEncoding {

    InstantEncoding() {
    }

    protected abstract Date buildDate(Tuple tuple) throws TemporalDataException;

    @Override
    public void buildTemporalElement(TemporalDataset tmpds, Tuple tuple,
            Map<String, GenericTemporalElement> elements) throws TemporalDataException {
        Granule granule = super.getGranularity().parseDateToGranule(
                buildDate(tuple));

        // TODO efficient way to remember last added ID
        int row = tmpds.addTemporalElement(granule.getInf(), granule.getSup(),
                super.getGranularity().getIdentifier(), super.getGranularity().getGranularityContextIdentifier(),
                TemporalDataset.PRIMITIVE_INSTANT);

        elements.put(this.getKey(), tmpds.getTemporalElementByRow(row));
    }
}
