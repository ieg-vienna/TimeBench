//package timeBench.calendar;
//
//import timeBench.data.TemporalDataException;
//
///**
// * 
// * 
// * <p>
// * Added:          / TL<br>
// * Modifications: 
// * </p>
// * 
// * @author Tim Lammarsch
// *
// */
//public enum CalendarManagers {
//	JavaDate (0);
//	
//	private int intValue;
//	
//	CalendarManagers(int toInt) {
//		intValue = toInt;
//	}
//		
//		
//	public int toInt()
//	{
//		return intValue;
//	}
//		
//		
//	public static CalendarManagers fromInt(int intValue) throws TemporalDataException {
//		switch(intValue) {
//			case 0: return CalendarManagers.JavaDate;
//			default: throw new TemporalDataException("Unknown CalendarManager");
//		}
//	}
//}
