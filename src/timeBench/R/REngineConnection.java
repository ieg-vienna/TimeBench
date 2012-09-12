package timeBench.R;

import org.rosuda.REngine.REngine;

public interface REngineConnection {
	public double R_NA = Double.longBitsToDouble(0x7ff00000000007a2L);
	public int R_NA_int = -2147483648; 
	
	public REngine getREngine();
}
