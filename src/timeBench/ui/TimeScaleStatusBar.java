package timeBench.ui;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import timeBench.action.layout.timescale.AdvancedTimeScale;

/**
 * Displays start date, the name of the current {@link timeBench.action.layout.timescale.TimeUnit} and end date of
 * a given {@link AdvancedTimeScale}.
 * 
 * @author peterw
 * 
 */
public class TimeScaleStatusBar extends JPanel implements ChangeListener {
    
    private static final long serialVersionUID = -7368354080719114701L;
    
    private JLabel lblStartDate;
	private JLabel lblEndDate;
	private JLabel lblUnitName;
	private AdvancedTimeScale timeScale;

	/**
	 * Create a {@link TimeScaleStatusBar} with no {@link AdvancedTimeScale}. Set the
	 * {@link AdvancedTimeScale} by calling
	 * {@link TimeScaleStatusBar#setTimeScale(AdvancedTimeScale)}.
	 */
	public TimeScaleStatusBar() {
		this(null);
	}

	/**
	 * Create a {@link TimeScaleStatusBar} to display data of the given
	 * {@link AdvancedTimeScale}.
	 * 
	 * @param timeScale
	 *            the data of this {@link AdvancedTimeScale} will be displayed
	 */
	public TimeScaleStatusBar(AdvancedTimeScale timeScale) {
		// setBorder(new OneLineBorder(OneLineBorder.TOP, Color.LIGHT_GRAY));
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		lblStartDate = new JLabel();
		add(lblStartDate);
		add(Box.createHorizontalGlue());
		lblUnitName = new JLabel();
		add(lblUnitName);
		add(Box.createHorizontalGlue());
		lblEndDate = new JLabel();
		add(lblEndDate);
		// add(Box.createHorizontalStrut(20));
		if (timeScale != null) {
			setTimeScale(timeScale);
		}
	}

	public void setTimeScale(AdvancedTimeScale newTimeScale) {
		if (timeScale != null) {
			timeScale.removeChangeListener(this);
		}
		timeScale = newTimeScale;
		newTimeScale.addChangeListener(this);
		stateChanged(new ChangeEvent(timeScale));
	}

	public AdvancedTimeScale getTimeScale() {
		return timeScale;
	}

	public void stateChanged(ChangeEvent e) {
		lblStartDate.setText(timeScale.getTimeUnit().getFullFormat().format(timeScale.getStartDateAdjusted()));
		lblUnitName.setText(timeScale.getTimeUnit().getName());
		lblEndDate.setText(timeScale.getTimeUnit().getFullFormat().format(timeScale.getEndDateAdjusted()));
	}
}
