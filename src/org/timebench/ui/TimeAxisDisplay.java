package org.timebench.ui;

import java.awt.geom.Point2D;

import org.timebench.action.layout.timescale.AdvancedTimeScale;

import prefuse.Display;
import prefuse.Visualization;

/**
 * Specialized prefuse {@link Display} that integrates panning and zooming a
 * {@link AdvancedTimeScale}.
 * 
 * 
 * <p>
 * The time scale must be set with {@link #setTimeScale(AdvancedTimeScale)}.
 * 
 * <p>
 * Added: 2012-06-13 / AR (based on work by Peter Weishapl, Thomas Turic, and
 * Stephan Hoffmann)<br>
 * Modifications: 2012-0X-XX / XX / ...
 * </p>
 * 
 * @author Alexander Rind (based on work of Peter Weishapl, Thomas Turic and
 *         Stephan Hoffmann)
 */
public class TimeAxisDisplay extends Display {

    private static final long serialVersionUID = 8996278214078774333L;

    private AdvancedTimeScale timeScale;

//    private boolean panNonTimeAxis = true;

    public TimeAxisDisplay() {
        super();
    }

    public TimeAxisDisplay(Visualization visualization) {
        super(visualization);
    }

    public AdvancedTimeScale getTimeScale() {
        return timeScale;
    }

    public void setTimeScale(AdvancedTimeScale timeScale) {
        this.timeScale = timeScale;
    }

    /*
     * Zoom & Pan: Instead of changing the Display's AffineTransform, just
     * change the TimeScale here so we can use the built-in prefuse Zoom & Pan
     * Controls. Note: Changing of the Display's AffineTransfrom is not
     * supported by timevis (except y-panning)!
     */
    public synchronized void pan(double dx, double dy) {
        panAbs(dx, dy);
    }

    public synchronized void panAbs(double dx, double dy) {
        if (timeScale != null) {
            timeScale.pan((int) dx);
        }
//        if (panNonTimeAxis) {
//            super.panAbs(0, dy);
//        }
    }

    public synchronized void zoom(Point2D p, double scale) {
        zoomAbs(p, scale);
    }

    public synchronized void zoomAbs(Point2D p, double scale) {
        if (timeScale != null) {
            timeScale.zoom(scale);
        }
    }
    /* End of Zoom & Pan */
}
