//package timeBench.data.oo;
//
//import timeBench.calendar.Granularity;
//import timeBench.data.TemporalDataException;
//
///**
// * UnanchoredTemporalPrimitive is the base class for Unanchored Temporal Primitives.
// * (Spans). 
// * 
// * <p>
// * Added:         2011-07-13 / TL<br>
// * Modifications: 
// * </p>
// * 
// * @author Tim Lammarsch
// *
// */
//public class UnanchoredTemporalElement extends TemporalElement {
//	
//	protected UnanchoredTemporalElement(timeBench.data.relational.TemporalElement relationalTemporalElement) {
//		super(relationalTemporalElement);
//	}
//	
//	/** 
//	 * The Instant which happens this temporal primitive before a given instant.
//	 * Always throws an exception in base class. 	
//	 * @param anchor The original instant.
//	 * @return The resulting instant.
//	 * @throws TemporalDataException
//	 */
//	public Instant before(Instant anchor) throws TemporalDataException {
//		return before(anchor,null);
//	}
//	
//	
//	/** 
//	 * The Instant which happens this temporal primitive before a given instant in a given granularity.
//	 * Always throws an exception in base class, overwritten in Span. 	
//	 * @param anchor The original instant.
//	 * @param granularity The granularity used for calculation.
//	 * @return The resulting instant.
//	 * @throws TemporalDataException
//	 */
//	public Instant before(Instant anchor,Granularity granularity) throws TemporalDataException {
//		throw new TemporalDataException("No general way to calculate before() for unanchored temporal primitive.");
//	}
//	
//	
//	/** 
//	 * The Instant which happens this temporal primitive after a given instant.
//	 * Always throws an exception in base class. 	
//	 * @param anchor The original instant.
//	 * @return The resulting instant.
//	 * @throws TemporalDataException
//	 */
//	public Instant after(Instant anchor) throws TemporalDataException {
//		return after(anchor,null);
//	}
//	 
//	 
//	/** 
//	 * The Instant which happens this temporal primitive after a given instant in a given granularity.
//	 * Always throws an exception in base class, overwritten in Span. 	
//	 * @param anchor The original instant.
//	 * @param granularity The granularity used for calculation.
//	 * @return The resulting instant.
//	 * @throws TemporalDataException
//	 */	public Instant after(Instant anchor,Granularity granularity) throws TemporalDataException {
//		throw new TemporalDataException("No general way to calculate before() for unanchored temporal primitive.");
//	}
//	 
//	 
//	public long getDuration() throws TemporalDataException {
//		long duration = 0;
//		
//		for(TemporalElement iElement : parts ) {
//			if (iElement instanceof Span)
//				duration += ((Span)iElement).getDuration();
//			else if (iElement instanceof UnanchoredTemporalElement)
//				duration += ((UnanchoredTemporalElement)iElement).getDuration();
//			else 
//				throw new TemporalDataException("Illegal parts in Unanchored Temporal element while calculating total duration.");
//		}
//		
//		return duration;
//	}
//}
