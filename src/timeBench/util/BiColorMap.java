package timeBench.util;

public class BiColorMap {

    private int[][] palette;
    private double minValue1, maxValue1;
    private double minValue2, maxValue2;
    
    public BiColorMap(int[][] map, double min1, double max1, double min2, double max2) {
        palette = map;
        minValue1 = min1;
        maxValue1 = max1;
        minValue2 = min2;
        maxValue2 = max2;
    }
    
    public int getColor(double val1,double val2) {
    	if (val1 < minValue1)
    		val1 = minValue1;
    	else if (val1 > maxValue1)
    		val1 = maxValue1;
    	if (val2 < minValue2)
    		val2 = minValue2;
    	else if (val2 > maxValue2)
    		val2 = maxValue2;

        int idx1 = (int)(palette.length * (val1-minValue1)/(maxValue1-minValue1));
        int idx2 = (int)(palette[0].length * (val2-minValue2)/(maxValue2-minValue2));
        return palette[idx1][idx2];
    }
    
    public int getColor(int baseColor,double val2) {
    	if (val2 < minValue2)
    		val2 = minValue2;
    	else if (val2 > maxValue2)
    		val2 = maxValue2;

        int idx1 = (int)(palette.length * (val1-minValue1)/(maxValue1-minValue1));
        int idx2 = (int)(palette[0].length * (val2-minValue2)/(maxValue2-minValue2));
        return palette[idx1][idx2];
    }

    public int[][] getColorPalette() {
        return palette;
    }

    public void setColorPalette(int[][] palette) {
        this.palette = palette;
    }

    public double getMaxValue1() {
        return maxValue1;
    }
    public double getMaxValue2() {
        return maxValue2;
    }

    public void setMaxValue1(double maxValue1) {
        this.maxValue1 = maxValue1;
    }
    public void setMaxValue2(double maxValue2) {
        this.maxValue2 = maxValue2;
    }

    public double getMinValue1() {
        return minValue1;
    }
    public double getMinValue2() {
        return minValue2;
    }

    public void setMinValue1(double minValue1) {
        this.minValue1 = minValue1;
    }
    public void setMinValue2(double minValue2) {
        this.minValue2 = minValue2;
    }
    
}
