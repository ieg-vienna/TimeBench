package timeBench.action.layout.timescale;

import java.text.DateFormat;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * <p>
 * {@link BoundedRangeModel} implementation that manages the relationship
 * between two {@link AdvancedTimeScale}s, like in a overview-detail
 * relationship.
 * </p>
 * 
 * @author peterw
 * @see AdvancedTimeScale
 * @see BoundedRangeModel
 */
@SuppressWarnings("serial")
public class RangeAdapter extends DefaultBoundedRangeModel {
	protected AdvancedTimeScale fullScale;
	protected AdvancedTimeScale actualScale;

	/**
	 * <p>
	 * Creates a {@link RangeAdapter} managing a overview-detail relationship
	 * between fullScale and actualScale where fullScale is represents the
	 * overview and actualScale the detail.
	 * </p>
	 * 
	 * <p>
	 * The {@link RangeAdapter} listens for changes in the fullScale and updates
	 * the {@link RangeAdapter}s {@link BoundedRangeModel} accordingly. Changes
	 * to the {@link RangeAdapter}s {@link BoundedRangeModel} are reflected in
	 * the actualScale's interval.
	 * </p>
	 * 
	 * @param fullScale
	 *            the scale representing the overview
	 * @param actualScale
	 *            the detail scale modified by this {@link RangeAdapter}
	 */
	public RangeAdapter(AdvancedTimeScale fullScale, AdvancedTimeScale actualScale) {
		this.fullScale = fullScale;
		this.actualScale = actualScale;

		maximize();

		addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				updateRangeValues();
			}
		});

		fullScale.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setMaximum(RangeAdapter.this.fullScale.getDisplayWidth());
				updateRangeValues();
			}
		});
	}

	/**
	 * Set the interval of the actualScale according to this
	 * {@link RangeAdapter}s rangeStart and rangeEnd.
	 * 
	 * @see RangeAdapter#getRangeStart()
	 * @see RangeAdapter#getRangeEnd()
	 */
	public void updateRangeValues() {
		actualScale.changeInterval(getRangeStart(), getRangeEnd());
	}

	/**
	 * Maximize this {@link RangeAdapter} and therefore match the actualScale's
	 * interval to the fullScale.
	 */
	public void maximize() {
		setRangeProperties(0, fullScale.getDisplayWidth(), 0, fullScale.getDisplayWidth(), false);
	}
	
	/** 
	 * pan the detail timescale with its own resolution
	 * @author Alex Rind 
	 */
	public int panActual(int pixels) {
		int dx = Math.round((float) pixels * actualScale.millisPerPixel / fullScale.millisPerPixel); 
		
		setValue(getValue() - dx);
		
		return dx;
	}

	private long getRangeStart() {
		return getDateAtPixel(fullScale, getValue());
	}

	private long getRangeEnd() {
		return getDateAtPixel(fullScale, getValue() + getExtent());
	}

	private long getDateAtPixel(AdvancedTimeScale scale, int x) {
		return scale.getDateAtPixel(x, false);
	}

	public AdvancedTimeScale getFullScale() {
        return fullScale;
    }

    public AdvancedTimeScale getActualScale() {
        return actualScale;
    }

    public String toString() {
		DateFormat df = DateFormat.getDateTimeInstance();
		StringBuilder sb = new StringBuilder();
		sb.append(df.format(fullScale.getStartDate())).append("[").append(df.format(getRangeStart())).append("]");
		sb.append(" - ");
		sb.append("[").append(df.format(getRangeEnd())).append("]").append(df.format(fullScale.getEndDate()));
		sb.append(", ").append(super.toString());

		return sb.toString();
	}
}
