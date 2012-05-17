package timeBench.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Calendar;

import javax.swing.JComponent;

import org.apache.log4j.Logger;

import timeBench.action.layout.timescale.TimeScale;
import timeBench.action.layout.timescale.TimeUnit;
import timeBench.action.layout.timescale.TimeUnitProvider;
import timeBench.util.DateUtil;

/**
 * Provides methods for painting a {@link TimeScale} upon a component.
 * 
 * Update Alex Rind: make lines and weekend controllable.
 * 
 * @author peterw
 * 
 */
public class TimeScalePainter {
	private final Logger log = Logger.getLogger(getClass());
	private Color weekendColor = new Color(235, 243, 250);
	private Color lineColor = Color.LIGHT_GRAY;
	
	private boolean paintLine = true;
	private boolean paintWeekend = true;

	private TimeScale timeScale;
	private JComponent component;

	/**
	 * Create a {@link TimeScalePainter} to paint upon the given
	 * {@link JComponent}.
	 * 
	 * @param comp
	 *            the component to paint upon
	 */
	public TimeScalePainter(JComponent comp) {
		this.component = comp;
	}

	/**
	 * Renders the provided {@link TimeScale}.
	 * 
	 * For each pixel of the {@link JComponent} representing a date that 'fits'
	 * into the {@link TimeScale}s current {@link TimeUnit},
	 * {@link TimeScalePainter#drawUnits(Graphics, TimeUnit, boolean)} is used
	 * to draw a thin vertical line.
	 * 
	 * If there is a longer {@link TimeUnit} than the current {@link TimeUnit}
	 * provided by {@link TimeUnitProvider}, it is used to draw thicker lines
	 * in the way mentioned above.
	 * 
	 * @param g
	 *            the graphics object
	 */
	public void paint(Graphics2D g) {
		if (timeScale == null) {
			log.debug("got no timeScale");
			return;
		}
		TimeUnit timeUnit = timeScale.getTimeUnit();
		drawUnits(g, timeUnit, true);

		timeUnit = timeScale.getTimeUnitProvider().getLonger(timeUnit);
		if (timeUnit != null) {
			((Graphics2D) g).setStroke(new BasicStroke(2));
			drawUnits(g, timeUnit, false);
		}
	}

	/**
	 * For each pixel of the {@link JComponent} representing a date that 'fits'
	 * into the provided {@link TimeUnit}, a vertical line is drawn, as
	 * implemented by
	 * {@link TimeScalePainter#drawUnit(Graphics, TimeUnit, long, int, boolean)}.
	 * 
	 * @param g
	 *            the graphics object
	 * @param timeUnit
	 *            the {@link TimeUnit} to draw lines for
	 * @param smallUnit
	 *            true to draw a thin line, otherwise a thicker line will be
	 *            drawn
	 */
	public void drawUnits(Graphics g, TimeUnit timeUnit, boolean smallUnit) {
		int x = 0;
		int weekendBegin = 0;
		boolean weekend = false;
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timeUnit.previous(timeScale.getStartDate()));

		while (x < component.getWidth()) {
			g.setColor(lineColor);
			x = (int) timeScale.getPixelForDate(cal.getTimeInMillis());
			if (paintLine)
				drawUnit(g, timeUnit, cal.getTimeInMillis(), x, smallUnit);

			if (smallUnit) {
				if (paintWeekend && weekend && timeUnit.compareTo(timeScale.getTimeUnitProvider().getWeekUnit()) < 0) {
					highlightWeekend(g, x, weekendBegin);
				}
				if (DateUtil.isWeekend(cal)) {
					weekend = true;
					weekendBegin = x;
				} else {
					weekend = false;
				}
			}

			cal.setTimeInMillis(timeUnit.next(cal.getTimeInMillis()));
		}
	}

	/**
	 * Highlights dates representing weekends with color.
	 * 
	 * @param g
	 *            the graphics object
	 * @param x
	 *            draw from weekendBegin to this pixel
	 * @param weekendBegin
	 *            the pixel, where the weekend begins
	 */
	protected void highlightWeekend(Graphics g, int x, int weekendBegin) {
		g.setColor(weekendColor);
		g.fillRect(weekendBegin + 1, 0, x - weekendBegin - 1, component.getHeight());
	}

	/**
	 * Draws a vertical line representing a date 'fitting' into a
	 * {@link TimeUnit} at a given position.
	 * 
	 * @param g
	 *            the graphics object
	 * @param timeUnit
	 *            the {@link TimeUnit}
	 * @param date
	 *            this is the date 'fitting' into {@link TimeUnit} and
	 *            represented by pixel x
	 * @param x
	 *            draw the line at this pixel
	 * @param smallUnit
	 *            true to draw a thin line, otherwise a thicker line will be
	 *            drawn
	 */
	protected void drawUnit(Graphics g, TimeUnit timeUnit, long date, int x, boolean smallUnit) {
		g.drawLine(x, 0, x, component.getHeight());
	}

	public JComponent getComponent() {
		return component;
	}

	public TimeScale getTimeScale() {
		return timeScale;
	}

	public void setTimeScale(TimeScale newTimeScale) {
		this.timeScale = newTimeScale;
	}

	public Color getLineColor() {
		return lineColor;
	}

	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
	}

	public Color getWeekendColor() {
		return weekendColor;
	}

	public void setWeekendColor(Color weekendColor) {
		this.weekendColor = weekendColor;
	}

	public boolean isPaintLine() {
		return paintLine;
	}

	/**
	 * control whether to paint lines to mark time units (default = true).
	 * @param paintLine if true lines will be painted
	 */
	public void setPaintLine(boolean paintLine) {
		this.paintLine = paintLine;
	}

	public boolean isPaintWeekend() {
		return paintWeekend;
	}

	/**
	 * control whether to color the  background to mark weekends (default = true).
	 * @param paintWeekend if true background will be colored
	 */
	public void setPaintWeekend(boolean paintWeekend) {
		this.paintWeekend = paintWeekend;
	}
}
