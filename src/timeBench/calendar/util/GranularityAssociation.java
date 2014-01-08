package timeBench.calendar.util;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import timeBench.calendar.Granularity;
import timeBench.data.TemporalDataException;

public class GranularityAssociation<Enum> {
	DualHashBidiMap<Enum, Granularity> associationMap = new DualHashBidiMap<>();

	public void associateGranularities(Enum enumGranularity, Granularity granularity) throws TemporalDataException {
		if (!associationMap.containsKey(enumGranularity) && !associationMap.containsValue(granularity)){
			associationMap.put(enumGranularity, granularity);
		}
		else{
			throw new TemporalDataException("Multiple granularities with same label found.");
		}
	}

	public Enum getAssociation(Granularity granularity){
		return associationMap.getKey(granularity);
	}

	public Granularity getAssociation(Enum granularity){
		return associationMap.get(granularity);
	}

	public int getAssociationCount(){
		return associationMap.size();
	}
}
