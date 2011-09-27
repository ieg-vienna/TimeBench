package timeBench.data.relational;

import prefuse.data.tuple.TableNode;

public class TemporalElement extends TableNode {
    
    public long getInf() {
        return super.getLong(TemporalDataset.INF);
    }

    public long getSup() {
        return super.getLong(TemporalDataset.SUP);
    }

    public long getKind() {
        return super.getLong(TemporalDataset.KIND);
    }

    public long getGranularityId() {
        return super.getLong(TemporalDataset.GRANULARITY_ID);
    }

    @Override
    public String toString() {
        return "TemporalElement[inf=" + getInf() + ", sup=" + getSup() + ", kind=" + getKind() + ", granularityId=" + getGranularityId() + "]";
    }
}
