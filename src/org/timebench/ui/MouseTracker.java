package org.timebench.ui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Date;

import javax.swing.JComponent;

import org.apache.log4j.Logger;
import org.timebench.action.layout.timescale.TimeScale;

/**
 * Tracks the mouse position on a given {@link JComponent}.
 * {@link MouseTracker#paintTimeAtPosition(Graphics2D)} paints a vertical line
 * with a text label representing the date at the current mouse position. The
 * date is provided by {@link TimeScale}.
 * 
 * @author peterw
 * 
 */
public class MouseTracker extends MouseMotionAdapter {
	protected int lastX;
	private TimeScale timeScale;
	private JComponent component;

	protected final static Logger logger = Logger.getLogger(MouseTracker.class);

	/**
	 * offset on the left edge of the component, which the MouseTracker can not
	 * enter
	 */
	private int m_offsetLeft = 0;

	/**
	 * Create a {@link MouseTracker} which tracks the mouse position on the
	 * given component.
	 * 
	 * @param component
	 *            the component
	 */
	public MouseTracker(JComponent component) {
		this(component, null);
	}

	/**
	 * Create a {@link MouseTracker} which tracks the mouse position on the
	 * given component. The {@link TimeScale} is used to get the date on a given
	 * position.
	 * 
	 * @param component
	 *            the component
	 */
	public MouseTracker(JComponent component, TimeScale timeScale) {
		this.timeScale = timeScale;
		this.component = component;
		component.addMouseMotionListener(this);
	}

	/**
	 * Paint a vertical line at the mouse position.
	 * 
	 * Typically this method should be called with a graphics object provided by
	 * the {@link JComponent#paint(java.awt.Graphics)} method of the tracked
	 * component.
	 * 
	 * @param g
	 *            the graphics object used for painting the tracked component
	 */
	public void paintTimeAtPosition(Graphics2D g) {
		if (timeScale == null || component == null) {
			return;
		}

		Graphics2D g2d = (Graphics2D) g;
        String dateText = timeScale.getTimeUnit().formatFull(
                new Date(timeScale.getDateAtPixel(lastX)));
		// String dateTextRaw =
		// timeScale.getUnit().getFullFormat().format(timeScale.getDateAtPixel(mouseTracker.lastX,
		// false));
		// int textHeight = g.getFontMetrics().getHeight() -
		// g.getFontMetrics().getDescent() + 1;
		int textWidth = g.getFontMetrics().stringWidth(dateText);
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				0.5f));
		// g.setColor(Color.WHITE);
		// g.fillRect(mouseTracker.lastX, 0, textWidth, textHeight);
		g.setColor(Color.BLACK);
		g.drawLine(lastX, 0, lastX, component.getHeight());
		g2d.setComposite(AlphaComposite.Src);
		g.setColor(Color.GRAY);

		// g.drawString(dateTextRaw, (mouseTracker.lastX + 1)
		// - Math.max(0, ((mouseTracker.lastX + textWidth) - getWidth())),
		// getHeight()
		// -
		// g.getFontMetrics().getDescent()-g.getFontMetrics().getHeight());
		g.drawString(dateText, (lastX + 1)
				- Math.max(0, ((lastX + textWidth) - component.getWidth())),
				component.getHeight() - g.getFontMetrics().getDescent());
		// g.drawString("" + getWorldCoordinate(mouseTracker.lastX),
		// mouseTracker.lastX + 1, getHeight());
	}

	/**
	 * Paint a vertical line at the mouse position.
	 * 
	 * Typically this method should be called with a graphics object provided by
	 * the {@link JComponent#paint(java.awt.Graphics)} method of the tracked
	 * component.
	 * 
	 * @param g
	 *            the graphics object used for painting the tracked component
	 * @author TT support
	 */
	public void paintTimeAtPosition(Graphics2D g, Component c,
			boolean containsMouse) {
		if (timeScale == null || component == null) {
			return;
		}

		Composite cpst = g.getComposite();

		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				0.5f));
		
		g.setColor(new Color(228, 88, 0));
		g.setStroke(new BasicStroke(1f));
		g.drawLine(lastX, 0, lastX, c.getHeight());

		g.setComposite(cpst);

		if (!containsMouse)
			return;

        String dateText = timeScale.getTimeUnit().formatFull(
                new Date(timeScale.getDateAtPixel(lastX)));
		// String dateTextRaw =
		// timeScale.getUnit().getFullFormat().format(timeScale.getDateAtPixel(mouseTracker.lastX,
		// false));
		// int textHeight = g.getFontMetrics().getHeight() -
		// g.getFontMetrics().getDescent() + 1;
		int textWidth = g.getFontMetrics().stringWidth(dateText);
		// g.setColor(Color.WHITE);
		// g.fillRect(mouseTracker.lastX, 0, textWidth, textHeight);
		// g.setColor(Color.BLACK);
		// g.setColor(Color.GRAY);
		// g.drawString(dateTextRaw, (mouseTracker.lastX + 1)
		// - Math.max(0, ((mouseTracker.lastX + textWidth) - getWidth())),
		// getHeight()
		// -
		// g.getFontMetrics().getDescent()-g.getFontMetrics().getHeight());
		g.drawString(dateText, (lastX + 1)
				- Math.max(0, ((lastX + textWidth) - c.getWidth())), c
				.getHeight()
				- g.getFontMetrics().getDescent());
		// g.drawString("" + getWorldCoordinate(mouseTracker.lastX),
		// mouseTracker.lastX + 1, getHeight());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseMotionAdapter#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {
		if (component != null) {
			// if mouse is left of offset --> set on offset
			lastX = Math.max(e.getX(), this.m_offsetLeft);
			component.repaint();
		}
	}

	public TimeScale getTimeScale() {
		return timeScale;
	}

	public void setTimeScale(TimeScale timeScale) {
		this.timeScale = timeScale;
	}

	public JComponent getComponent() {
		return component;
	}

	/**
	 * get width of the left offset. The offset is a part on the left edge of
	 * the component, which the MouseTracker can not enter.
	 * 
	 * @return width of the left offset
	 */
	public int getOffsetLeft() {
		return m_offsetLeft;
	}

	/**
	 * set width of the left offset. The offset is a part on the left edge of
	 * the component, which the MouseTracker can not enter.
	 * 
	 * @param offsetLeft
	 */
	public void setOffsetLeft(int offsetLeft) {
		this.m_offsetLeft = offsetLeft;
		this.lastX = this.m_offsetLeft;
	}
}