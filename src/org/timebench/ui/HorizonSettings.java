package org.timebench.ui;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
//import prefuse.data.Table;
//import prefuse.data.Tuple;
//import prefuse.util.collections.IntIterator;
//import timeBench.action.layout.HorizonGraphAction;

import org.timebench.util.HorizonColorPalette;


/**
 * The main settings class for the HorizonGraph.
 * In this class the default settings are loaded from the XML-file.
 */
@XmlRootElement(name = "horizon-settings")
@XmlAccessorType(XmlAccessType.NONE)
public class HorizonSettings {
    public static final String HORIZON_TYPE_MIRROR = "mirror"; 
    public static final String HORIZON_TYPE_OFFSET = "offset"; 
    	
	@XmlElement(name = "bandsCount", required = false)
	public int BandsCount = 2;	
	
	@XmlElement(name = "base", required = false)
	private double _base;
	
	@XmlElement(name = "indexingBase", required = false)
	private double _indexingBase;
	
	public double getBase()
	{
		return Indexing ? _indexingBase : _base;
	}
	public double getBase(boolean indexing)
	{
		return indexing ? _indexingBase : _base;
	}
	public void setBase(double value)
	{
		if(Indexing)
			_indexingBase = value;
		else
			_base = value;
	}
	public void setBase(double value, boolean indexing)
	{
		if(indexing)
			_indexingBase = value;
		else
			_base = value;
	}
	
	@XmlElement(name = "bandWidth", required = false)
	double _bandWidth;	
	@XmlElement(name = "indexingBandWidth", required = false)
	double _indexingBandWidth;
	public double getBandWidth()
	{
		return Indexing ? _indexingBandWidth : _bandWidth;
	}
	public double getBandWidth(boolean indexing)
	{
		return indexing ? _indexingBandWidth : _bandWidth;
	}
	public void setBandWidth(double value)
	{
		if(Indexing)
			_indexingBandWidth = value;
		else
			_bandWidth = value;
	}
	public void setBandWidth(double value, boolean indexing)
	{
		if(indexing)
			_indexingBandWidth = value;
		else
			_bandWidth = value;
	}
	
	@XmlElement(name = "indexing", required = false)	
	public boolean Indexing = true;
	
	@XmlElement(name = "type", required = false)
	public String Type = HORIZON_TYPE_MIRROR;	// mirror or offset
	
	@XmlElement(name = "indexingPoint", required = false)
	public Date IndexingPoint; 
	
	@XmlElement(name = "colorPalette", required = false)
	public String ColorPalette = "default";
	
	@XmlElement(name = "stepchart", required = false)
	public boolean Stepchart;
	
	@XmlElement(name = "isLegendVisible", required = false)
	public boolean IsLegendVisible = true;

	public int[] getColorPalette()
	{
		return HorizonColorPalette.getColorPalette(ColorPalette).getColors(BandsCount);
	}
	
//	/**
//	 * Determines the default values from the data table
//	 * if the settings are not loaded from a session file.
//	 * @param table the data table
//	 */
//	public void determinateDefaultSettings(Table table)
//	{
//		if(_bandWidth == 0)
//		{		
//			// basis:
//			// if min <= 0: 0, else min
//			double min = getMin(table, HorizonGraphAction.COL_Y_POSITION);
//			_base = min <= 0 ? 0 : min;
//			
//			// bandWidth:
//			double max = getMax(table, HorizonGraphAction.COL_Y_POSITION);
//			_bandWidth = Math.max(_base - min, max - _base) / BandsCount;
//					
//			// indexingPoint:
//			IndexingPoint = getMinDate(table, HorizonGraphAction.COL_Y_POSITION, HorizonVisualization.COL_X_TIMESTAMP);
//		}
//	}
//	
//	/** 
//	 * @param table data table
//	 * @param column the data column
//	 * @return the maximum not null value (double)
//	 */
//	static double getMax(Table table, String column)
//	{
//		double max = Double.MIN_VALUE;
//		
//		IntIterator rows = table.rows();
//		
//		while(rows.hasNext())
//		{
//			Tuple current = table.getTuple(rows.nextInt());
//			String nullableValue = current.getString(column);
//						
//			double value = HorizonHelper.convertStringToDouble(nullableValue);
//			if(!Double.isNaN(value))
//				max = value > max ? value : max;
//		}
//
//		return max;
//	}
//	
//	/** 
//	 * @param table data table
//	 * @param column the data column
//	 * @return the minimum not null value (double)
//	 */
//	static double getMin(Table table, String column)
//	{
//		double min = Double.MAX_VALUE;
//		
//		IntIterator rows = table.rows();
//		
//		while(rows.hasNext())
//		{
//			Tuple current = table.getTuple(rows.nextInt());
//			String nullableValue = current.getString(column);
//			
//			double value = HorizonHelper.convertStringToDouble(nullableValue);
//			if(!Double.isNaN(value))
//				min = value < min ? value : min;
//		}
//
//		return min;
//	}
//	
//	/**
//	 * Determines the minimum date of the data values for the indexing point.
//	 * @param table data table
//	 * @param valueColumn
//	 * @param dateColumn
//	 * @return Date
//	 */
//	static Date getMinDate(Table table, String valueColumn, String dateColumn)
//	{
//		Date startDate = new Date(0);
//		Date minDate = startDate;
//		
//		IntIterator rows = table.rows();
//		
//		while(rows.hasNext())
//		{
//			Tuple current = table.getTuple(rows.nextInt());
//			Date currentDate = current.getDate(dateColumn);
//			String nullableValue = current.getString(valueColumn);
//			
//			if(!nullableValue.toLowerCase().equals("null") && (currentDate.before(minDate) || minDate == startDate))
//				minDate = currentDate;
//		}
//
//		return minDate;
//	}
}
