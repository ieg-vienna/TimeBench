package timeBench.calendar.util;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import timeBench.calendar.Granularity;
import timeBench.data.TemporalDataException;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class GranularityAssociation<T extends Enum<T>> {
//	DualHashBidiMap<T , Granularity> associationMap = new DualHashBidiMap<>();

	private Hashtable<T, Granularity> enumGranularityMap = new Hashtable<>();
	private Hashtable<Granularity, T> granularityEnumMap = new Hashtable<>();

	public void associateGranularity(T enumGranularity, Granularity granularity) throws TemporalDataException {
		if (enumGranularityMap.containsKey(enumGranularity) || enumGranularityMap.containsValue(granularity) ||
			granularityEnumMap.containsKey(granularity) || granularityEnumMap.containsValue(enumGranularity)){

			throw new TemporalDataException("Multiple granularities with same label found: " + enumGranularity + " , " + granularity);
		}
		else{
			enumGranularityMap.put(enumGranularity, granularity);
			granularityEnumMap.put(granularity, enumGranularity);
		}
	}

	public T getAssociation(Granularity granularity){
		if (granularityEnumMap == null)
			return null;
		return granularityEnumMap.get(granularity);
	}

	public Granularity getAssociation(T granularity){
		if (enumGranularityMap == null)
			return null;
		return enumGranularityMap.get(granularity);
	}

	public int getAssociationCount(){
		if (enumGranularityMap == null)
			return 0;
		return enumGranularityMap.size();
	}
}
