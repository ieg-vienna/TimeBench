package timeBench.action.layout;

public class GranularityGROOVELayoutSettings {
    // private static final long serialVersionUID = -707716457554852086L;

    private int orientation;
    private boolean visible;
    private int[] borderWith;
    private int sourceColumn;
    private int fitting;

    @Deprecated
    public GranularityGROOVELayoutSettings() {
    }

    public GranularityGROOVELayoutSettings(int sourceColumn, int orientation,
            boolean visible, int[] borderWidth, int colorCalculation,
            int colorOverlayLevel,int fitting) {
        this.sourceColumn = sourceColumn;
        this.orientation = orientation;
        this.visible = visible;
        this.borderWith = borderWidth;
        this.fitting = fitting;
    }

    /**
     * @return the sourceColumn
     */
    public int getSourceColumn() {
        return sourceColumn;
    }

    /**
     * @param sourceColumn
     *            the sourceColumn to set
     */
    public void setSourceColumn(int sourceColumn) {
        this.sourceColumn = sourceColumn;
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
    
    public int getOrientation() {
        return orientation;
    }

    /**
     * @param orientation
     *            the orientation to set
     */
    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    /**
     * @return the visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * @param visible
     *            the visible to set
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * @return the borderWith
     */
    public int[] getBorderWith() {
        return borderWith;
    }

    /**
     * @param borderWith
     *            the borderWith to set
     */
    public void setBorderWith(int[] borderWith) {
        this.borderWith = borderWith;
    }
}
