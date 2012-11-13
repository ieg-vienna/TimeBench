package timeBench.action.analytical;

import java.util.ArrayList;
import java.util.Hashtable;

import prefuse.action.Action;
import prefuse.data.expression.BinaryExpression;
import prefuse.data.expression.CompositePredicate;
import prefuse.data.expression.Expression;
import prefuse.data.expression.Predicate;
import timeBench.data.TemporalDataException;
import timeBench.data.TemporalDataset;
import timeBench.data.TemporalDatasetProvider;
import timeBench.data.TemporalElement;
import timeBench.data.TemporalObject;
import timeBench.data.expression.TemporalElementArrayExpression;

/**
 * 
 * 
 * <p>
 * Added:          / TL<br>
 * Modifications: 
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public class MultiPredicatePatternDiscovery extends Action implements TemporalDatasetProvider {

	TemporalDataset sourceDataset;
	TemporalDataset resultDataset;
	TemporalDataset eventDataset;		
	Predicate[] predicates;	// Bertone: Intervals
	int coherenceSettings;
	Hashtable<Long,Long> sourceToResult = new Hashtable<Long,Long>();
	ArrayList<Long> resultRoots = new ArrayList<Long>();

	public static int ONLY_COHERENT 		  = 0;
	public static int SPACING_ALLOWED		  = 1;
	public static int OVERLAP_ALLOWED 		  = 2;
	public static int SPACING_OVERLAP_ALLOWED = 3; // 1&2

	public static String predicateColumn = "_predicate";
	
	public MultiPredicatePatternDiscovery(TemporalDataset sourceDataset,TemporalDataset eventDataset,Predicate[] predicates) {
		this(sourceDataset,eventDataset,predicates,ONLY_COHERENT);
	}
	
	public MultiPredicatePatternDiscovery(TemporalDataset sourceDataset,TemporalDataset eventDataset,Predicate[] predicates, int coherenceSettings) {
		this.sourceDataset = sourceDataset;
		this.eventDataset = eventDataset;
		this.predicates = predicates;
		this.coherenceSettings = coherenceSettings;		
	}
	
	/* (non-Javadoc)
	 * @see prefuse.action.Action#run(double)
	 */
	@Override
	public void run(double frac) {
		try {					

		resultDataset = new TemporalDataset(sourceDataset.getDataColumnSchema());
		
		if (resultDataset.getEdgeTable().getColumn(predicateColumn) == null)
			resultDataset.getEdgeTable().addColumn(predicateColumn, long.class);
		
			// for all patterns of existing length (might be events)
			for (TemporalObject iSource : sourceDataset.temporalObjects()) {
				// and for all predicates
				for (int i=0; i<predicates.length; i++) {
					// as well as all events to be added
					for (TemporalObject iEvent : sourceDataset.temporalObjects()) {					
						ArrayList<TemporalObject> checkList = new ArrayList<TemporalObject>();
						// check if this combination is to be added
						check(checkList,iSource,iEvent,i);
					}
				}
			}
			
			long[] resultRootsForJavaHasNoBoxingOfValueTypes = new long[resultRoots.size()];
			for(int i=0; i<resultRootsForJavaHasNoBoxingOfValueTypes.length; i++)
				resultRootsForJavaHasNoBoxingOfValueTypes[i] = resultRoots.get(i);
			resultDataset.setRoots(resultRootsForJavaHasNoBoxingOfValueTypes);
		
		} catch (TemporalDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void check(ArrayList<TemporalObject> checkList,TemporalObject currentObject,TemporalObject newObject,int predicate) throws TemporalDataException {
		// recursively build list of existing pattern
		checkList.add(currentObject);
		if(currentObject.getChildCount() > 0) {
			for(TemporalObject iChild : currentObject.childObjects()) {
				check((ArrayList<TemporalObject>)checkList.clone(),iChild,newObject,predicate);
			}
		} else {
			// add new event
			checkList.add(newObject);
			// if this is an acceptable pattern
			if (satisfies(checkList,predicates[predicate])) {
				// check whether a pattern based on the same pattern exists in result
				for(int i=0; i<checkList.size()-1; i++) {
					if (!sourceToResult.containsKey(checkList.get(i).getId())) {
						TemporalObject newReference = resultDataset.addCloneOf((checkList.get(i)));
						if (i==0) {
							resultRoots.add(newReference.getId());
						}
						sourceToResult.put(checkList.get(i).getId(), newReference.getId());
						// if we did not add a first event of a pattern, add it as child of event before
						if (i>0) {
							int rowNumber = checkList.get(i).linkWithChild(newReference).getRow();
							resultDataset.getEdgeTable().setLong(rowNumber, predicateColumn,
								sourceDataset.getEdge(newReference,checkList.get(i)).getLong(predicateColumn)); 
						}
					}
				}
				// add new event to pattern
				TemporalObject newReference = resultDataset.addCloneOf(newObject);
				int rowNumber = resultDataset.getTemporalObject(sourceToResult.get(checkList.get(checkList.size()-2).getId())).linkWithChild(newReference).getRow();
				resultDataset.getEdgeTable().setLong(rowNumber, predicateColumn, predicate);
			}
		}
	}

	/* (non-Javadoc)
	 * @see timeBench.data.TemporalDatasetProvider#getTemporalDataset()
	 */
	@Override
	public TemporalDataset getTemporalDataset() {		
		return resultDataset;
	}
	
	/**
	 * @param iSource
	 * @param temporalObject
	 * @param instant
	 * @return
	 * @throws TemporalDataException
	 */
	private boolean satisfies(ArrayList<TemporalObject> checkedObjects, Predicate template)
			throws TemporalDataException {
		
		if (checkedObjects.size() > 1) {
			// check for coherence		
			if ( coherenceSettings != SPACING_OVERLAP_ALLOWED) {
				// check only last against others; assume this is called once for each new TemporalObject
				TemporalElement last = checkedObjects.get(checkedObjects.size()-1).getTemporalElement();
				if ( (coherenceSettings & SPACING_ALLOWED) == 0) {
					if (last.getFirstInstant().getInf() - checkedObjects.get(checkedObjects.size()-2).getTemporalElement().getLastInstant().getSup() > 1)
						return false;
				}
				if ( (coherenceSettings & OVERLAP_ALLOWED) == 0) {
					for(int i=0; i<checkedObjects.size()-1;i++) {
						if(checkedObjects.get(i).getTemporalElement().getLastInstant().getSup() >= last.getFirstInstant().getInf())
							return false;
					}
				}
			}
		}
		
		updateTemporalElementExpressions(template,checkedObjects);			
		
 		return template.getBoolean(checkedObjects.get(checkedObjects.size()-1));
	}
	
	/**
	 * @param template
	 * @param checkedObjects
	 */
	private void updateTemporalElementExpressions(Expression template,
			ArrayList<TemporalObject> checkedObjects) {
		if(template instanceof TemporalElementArrayExpression)
			((TemporalElementArrayExpression)template).updateBuffer(checkedObjects);
		else if (template instanceof BinaryExpression) {
			updateTemporalElementExpressions(((BinaryExpression)template).getLeftExpression(), checkedObjects);
			updateTemporalElementExpressions(((BinaryExpression)template).getRightExpression(), checkedObjects);
		} else if(template instanceof CompositePredicate) {
			CompositePredicate composite = (CompositePredicate)template;
			for(int i=0; i<composite.size(); i++)
				updateTemporalElementExpressions(composite.get(i), checkedObjects);			
		}
	}
}
