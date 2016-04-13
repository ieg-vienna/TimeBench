package org.timebench.R;

import org.rosuda.REngine.REngine;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

public class RserveEngineConnection implements REngineConnection {
    private static RserveEngineConnection instance;
    private REngine engine;

    private RserveEngineConnection() throws RserveException {
			engine = new RConnection();
    }
    
    public static synchronized RserveEngineConnection getInstance() throws RserveException {
        if (null == instance) {
            instance = new RserveEngineConnection();
        }
        return instance;
    }
    
    public REngine getREngine() {
    	return engine;
    }
}
