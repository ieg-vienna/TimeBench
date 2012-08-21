package timeBench.action.layout;

import prefuse.Constants;

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
public class GranularityTreeLayoutSettings {
	private boolean ignore = false;
    private int fitting = 0;
    private int targetAxis = Constants.X_AXIS;
    private double border = 0.0;

	public GranularityTreeLayoutSettings(boolean ignore,int fitting,int targetAxis,double border) {
		this.ignore = ignore;
		this.fitting = fitting;
		this.setTargetAxis(targetAxis);
		this.setBorder(border);
	}
	
	/**
	 * @return the ignore
	 */
	public boolean isIgnore() {
		return ignore;
	}

	/**
	 * @param ignore the ignore to set
	 */
	public void setmIgnore(boolean ignore) {
		this.ignore = ignore;
	}
	
    /**
     * @return the way of fitting into space
     */
    public int getFitting() {
        return fitting;
    }

    /**
     * @param fitting
     *            the way of fitting into space
     */
    public void setFitting(int fitting) {
        this.fitting = fitting;
    }

	/**
	 * @return the borders
	 */
	public double getBorder() {
		return border;
	}

	/**
	 * @param borders the borders to set
	 */
	public void setBorder(double border) {
		this.border = border;
	}

	/**
	 * @return the targetAxis
	 */
	public int getTargetAxis() {
		return targetAxis;
	}

	/**
	 * @param targetAxis the targetAxis to set
	 */
	public void setTargetAxis(int targetAxis) {
		this.targetAxis = targetAxis;
	}
}
