package timeBench.controls;

import java.awt.geom.Point2D;

import javax.swing.BoundedRangeModel;

import prefuse.Display;
import prefuse.controls.ZoomControl;
import timeBench.action.layout.timescale.RangeAdapter;

/**
 * Zooms a given {@link RangeAdapter}, by in/decreasing the interval between
 * it's value and extent, as declared in {@link BoundedRangeModel}.
 * 
 * Zooming is achieved by pressing the right mouse button on the background of
 * the {@link Display} and dragging the mouse up or down.
 * 
 * @author peterw
 * @see Display
 */
public class RangeZoomControl extends ZoomControl {
    private RangeAdapter timeScale;
    
    public RangeZoomControl(){
        this(null);
    }
    
    public RangeZoomControl(RangeAdapter timeScale){
        this.timeScale = timeScale;
    }
    
    protected int zoom(Display display, Point2D p, double zoom, boolean abs) {
        if(timeScale != null){
            int newExtent = (int) (timeScale.getExtent()+(zoom-1)*100);
            if(newExtent > 0){
                timeScale.setExtent(newExtent);
            }
            return ZOOM;
        }
        return NO_ZOOM;
    }

    public RangeAdapter getTimeScale() {
        return timeScale;
    }

    public void setTimeScale(RangeAdapter timeScale) {
        this.timeScale = timeScale;
    }
}
