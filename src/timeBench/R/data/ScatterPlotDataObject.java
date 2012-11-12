package timeBench.R.data;

import java.util.Arrays;

import prefuse.data.Table;
import prefuse.data.util.Index;

public class ScatterPlotDataObject {
	private double[] x,y;
	private int[] xint;
	private double intercept, slope;
	private double minX,maxX,minY,maxY;
	
	
	public ScatterPlotDataObject(double[] x, double[] y, double intercept, double slope) {
		super();
		this.x = x;
		this.y = y;
		this.intercept = intercept;
		this.slope = slope;
		minX = maxX = minY = maxY = 0.0;
	}
	
	public ScatterPlotDataObject(int[] x, double[] y, double intercept, double slope) {
		super();
		this.xint = x;
		this.y = y;
		this.intercept = intercept;
		this.slope = slope;
		minX = maxX = minY = maxY = 0.0;
	}
	public int[] getIntX() {
		return xint;
	}
	public double[] getX() {
		return x;
	}
	public void setX(double[] x) {
		this.x = x;
	}
	public double[] getY() {
		return y;
	}
	public void setY(double[] y) {
		this.y = y;
	}
	public double getIntercept() {
		return intercept;
	}
	public void setIntercept(double intercept) {
		this.intercept = intercept;
	}
	public double getSlope() {
		return slope;
	}
	public void setSlope(double slope) {
		this.slope = slope;
	}
	
	public double getMinX() {
		return minX;
	}

	public void setMinX(double minX) {
		this.minX = minX;
	}

	public double getMaxX() {
		return maxX;
	}

	public void setMaxX(double maxX) {
		this.maxX = maxX;
	}

	public double getMinY() {
		return minY;
	}

	public void setMinY(double minY) {
		this.minY = minY;
	}

	public double getMaxY() {
		return maxY;
	}

	public void setMaxY(double maxY) {
		this.maxY = maxY;
	}
	
	public double getMaxDataY() {
		double max = 0.0;
		for (int i = 0; i < y.length; i++) {
			if (y[i]>max) max = y[i];
		}
		return max;
	}
	
	public double getMinDataY() {
		double min = y[(int)Math.floor(y.length/2)];
		for (int i = 0; i < y.length; i++) {
			if (y[i]<min) min = y[i];
		}
		return min;
	}

	@Override
	public String toString() {
		if (x != null)
			return "ScatterPlotDataObject [x=" + Arrays.toString(x) + ", y=" + Arrays.toString(y)
				+ ", intercept=" + intercept + ", slope=" + slope + "]";
		return "ScatterPlotDataObject [x=" + Arrays.toString(xint) + ", y=" + Arrays.toString(y)
				+ ", intercept=" + intercept + ", slope=" + slope + "]";
	}
	
	public Table getDataTable() {
		Table ret = new Table(y.length,0);
		
		//ret.addRows(y.length);
		//if (x != null)
			ret.addColumn("x", double.class);
		//else ret.addColumn("x", int.class);
		ret.addColumn("y", double.class);
		
		for (int i = 0 ; i < y.length; i++) {
			if (x!=null) ret.set(i,0,x[i]);
			else ret.set(i, 0, (double) xint[i]);
			ret.set(i,1,y[i]);	
		}
		
		return ret;
	}
	
	public Table getLines() {
		Table ret = new Table(2,0);
		//ret.addRows(2);
		ret.addColumn("x", double.class);
		ret.addColumn("y", double.class);
		
		Table data = getDataTable();
		Index i = data.index("x");
				
		minX = Math.floor(data.getDouble(i.minimum(),"x")*10.0)/10.0;
		maxX = Math.ceil(data.getDouble(i.maximum(),"x")*10.0)/10.0;
		minX -= 0.1;
		maxX += 0.1;
		
		minY = (minX*slope)+intercept;
		maxY = (maxX*slope)+intercept;
		
		ret.set(0, 0, minX);
		ret.set(0, 1, minY);
		ret.set(1, 0, maxX);
		ret.set(1, 1, maxY);

		return ret;
	}

}
