package timeBench.calendar.util;

import timeBench.calendar.Granularity;
import timeBench.data.TemporalDataException;

import java.util.Hashtable;

public class GranularityAssociation<T extends Enum<T>> {
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
		return granularityEnumMap.get(granularity);
	}

	public Granularity getAssociation(T granularity){
		return enumGranularityMap.get(granularity);
	}

	public int getAssociationCount(){
		return enumGranularityMap.size();
	}
}
