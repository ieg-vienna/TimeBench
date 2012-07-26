package timeBench.util;

import ieg.util.color.HCL;

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
public class ColorLib {
	public static int[][] getCategoryPalette(int size1, int size2) {
		return getCategoryPalette(size1,size2,60.0,2.2);
	}
	public static int[][] getCategoryPalette(int size1, int size2,double chroma,double gamma) {
		int[][] result = new int[size1][size2];
		
		double min = HCL.getMinLuminanceForChroma(chroma, gamma);
		double max = HCL.getMaxLuminanceForChroma(chroma, gamma);
		double range = max-min;
		for(int i=0; i<size1; i++) {
			for(int j=0; j<size2; j++) {
				int[] color = HCL.hcl2rgb((double)i/(double)size1,chroma,range*(double)j/size2+min,gamma);
				result[i][j] = prefuse.util.ColorLib.rgb(color[0],color[1],color[2]);						
			}			
		}
		
		return result;
	}

	public static int[][] getOrdinalPalette(int size1, int size2) {
		return getOrdinalPalette(size1, size2,60.0,2.2);
	}
	public static int[][] getOrdinalPalette(int size1, int size2,double chroma,double gamma) {
		int[][] result = new int[size1][size2];
		
		double min = HCL.getMinLuminanceForChroma(chroma, gamma);
		double max = HCL.getMaxLuminanceForChroma(chroma, gamma);
		double range = max-min;
		for(int i=0; i<size1; i++) {
			for(int j=0; j<size2; j++) {
				int[] color = HCL.hcl2rgb((double)i/3.0/(double)size1+2.0/3.0,chroma,range*(double)j/size2+min,gamma);
				result[i][j] = prefuse.util.ColorLib.rgb(color[0],color[1],color[2]);						
			}			
		}
		
		return result;
	}
}
