package org.timebench.action.layout.timescale;

import javax.swing.event.ChangeListener;

/**
 * Decorator pattern for horizontally offset time scales.
 * 
 * Calls to instances of this class will be delegated to the instance of 
 * TimeScale, which is passed in the constructor.
 * 
 * @author Alexander Rind
 *
 */
public class OffsetTimeScale implements TimeScale {
	
	private TimeScale m_timeScale;
	private int m_offsetPixels;

	public OffsetTimeScale(TimeScale timeScale, int offsetPixels) {
		m_timeScale = timeScale;
		m_offsetPixels = offsetPixels;
	}

	/* (non-Javadoc)
	 * @see at.ac.tuwien.cs.timevis.TimeScale#getPixelForDate(long)
	 */
	@Override
	public int getPixelForDate(long date) {
		return this.m_timeScale.getPixelForDate(date) - this.m_offsetPixels;
	}

	/* (non-Javadoc)
	 * @see at.ac.tuwien.cs.timevis.TimeScale#getDateAtPixel(int)
	 */
	@Override
	public long getDateAtPixel(int pixel) {
		return this.m_timeScale.getDateAtPixel(pixel + this.m_offsetPixels);
	}

	/* (non-Javadoc)
	 * @see at.ac.tuwien.cs.timevis.TimeScale#getDateAtPixel(int, boolean)
	 */
	@Override
	public long getDateAtPixel(int pixel, boolean adjustToUnit) {
		return this.m_timeScale.getDateAtPixel(pixel + this.m_offsetPixels, true);
	}

	/* (non-Javadoc)
	 * @see at.ac.tuwien.cs.timevis.TimeScale#getStartDate()
	 */
	@Override
	public long getStartDate() {
		return this.getDateAtPixel(0);
	}

	/* (non-Javadoc)
	 * @see at.ac.tuwien.cs.timevis.TimeScale#getMillisPerPixel()
	 */
	@Override
	public long getMillisPerPixel() {
		return this.m_timeScale.getMillisPerPixel();
	}

	/* (non-Javadoc)
	 * @see at.ac.tuwien.cs.timevis.TimeScale#getMinimumPixelPerUnit()
	 */
	@Override
	public int getMinimumPixelPerUnit() {
		return this.m_timeScale.getMinimumPixelPerUnit();
	}

	/* (non-Javadoc)
	 * @see at.ac.tuwien.cs.timevis.TimeScale#getTimeUnit()
	 */
	@Override
	public TimeUnit getTimeUnit() {
		return this.m_timeScale.getTimeUnit();
	}

	/* (non-Javadoc)
	 * @see at.ac.tuwien.cs.timevis.TimeScale#getTimeUnitProvider()
	 */
	@Override
	public TimeUnitProvider getTimeUnitProvider() {
		return this.m_timeScale.getTimeUnitProvider();
	}

	/* (non-Javadoc)
	 * @see at.ac.tuwien.cs.timevis.TimeScale#addChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public void addChangeListener(ChangeListener cl) {
		this.m_timeScale.addChangeListener(cl);
	}

	/* (non-Javadoc)
	 * @see at.ac.tuwien.cs.timevis.TimeScale#removeChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public void removeChangeListener(ChangeListener cl) {
		this.m_timeScale.removeChangeListener(cl);
	}

}
