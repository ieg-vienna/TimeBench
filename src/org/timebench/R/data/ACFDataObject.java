package org.timebench.R.data;

import java.util.Arrays;

import prefuse.data.Table;

public class ACFDataObject {
	private static final String CORFUNC_COLUMN = "corfunc";
	private static final String LAG_COLUMN = "lag";
	private double m_boundaries;
	private double[] m_lags, m_corrfunct;
	private String m_groupname;
	
	public ACFDataObject(String name, double[] lags, double[] correlationf, double boundaries) {
		m_lags = lags.clone();
		m_corrfunct = correlationf.clone();
		m_boundaries = boundaries;
		m_groupname = name;
	}
	
	public void setLags(double[] l) {
		m_lags = l.clone();
	}
	
	public double[] getLags() {
		return m_lags;
	}
	
	public void setAutocorrelationFunction(double[] a) {
		m_corrfunct = a.clone();
	}
	
	public double[] getAutocorrelationFunction() {
		return m_corrfunct;
	}
	
	public double getBoundaries() {
		return m_boundaries;
	}

	public void setBoundaries(double boundaries) {
		this.m_boundaries = boundaries;
	}
	
	public String getGroupName() {
		return m_groupname;
	}
	
	public void setGroupName(String name) {
		m_groupname = name;
	}
	
	public String getLagColumnName() {
		return LAG_COLUMN;
	}
	
	public String getCorrFunctionColumnName() {
		return CORFUNC_COLUMN;
	}
	
	public Table getDataTable() {
		Table ret = new Table(m_lags.length, 0);
		ret.addColumn(LAG_COLUMN, double.class);
		ret.addColumn(CORFUNC_COLUMN, double.class);
		
		for (int i = 0 ; i < m_lags.length; i++) {
			ret.setDouble(i,0,m_lags[i]);
			ret.setDouble(i,1,m_corrfunct[i]);		
		}
		return ret;
	}
	
	public double getMaxLag() {
		return m_lags[m_lags.length-1];
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(m_boundaries);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + Arrays.hashCode(m_corrfunct);
		result = prime * result
				+ ((m_groupname == null) ? 0 : m_groupname.hashCode());
		result = prime * result + Arrays.hashCode(m_lags);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ACFDataObject other = (ACFDataObject) obj;
		if (Double.doubleToLongBits(m_boundaries) != Double
				.doubleToLongBits(other.m_boundaries))
			return false;
		if (!Arrays.equals(m_corrfunct, other.m_corrfunct))
			return false;
		if (m_groupname == null) {
			if (other.m_groupname != null)
				return false;
		} else if (!m_groupname.equals(other.m_groupname))
			return false;
		if (!Arrays.equals(m_lags, other.m_lags))
			return false;
		return true;
	}
}
