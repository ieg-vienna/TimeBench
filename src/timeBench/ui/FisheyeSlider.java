package timeBench.ui;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import timeBench.action.layout.timescale.FisheyeTimeScale;

/**
 * Let's the user change the fisheye distortion intensity of a given
 * {@link FisheyeTimeScale} from 0 to 5.
 * 
 * @author peterw
 * @see FisheyeTimeScale
 */
public class FisheyeSlider extends JSlider {
    
    private static final long serialVersionUID = 6041848922729348421L;

    public FisheyeSlider(final FisheyeTimeScale timeScale) {
		super(0, 50, 0);
		setPaintLabels(false);
		setPaintTicks(false);
		setPaintTrack(true);
		addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				timeScale.setFisheyeIntensity(getValue() / 10d);
			}
		});
	}
}
