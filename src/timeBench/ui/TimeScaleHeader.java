package timeBench.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import timeBench.action.layout.timescale.TimeScale;
import timeBench.action.layout.timescale.TimeUnit;

public class TimeScaleHeader extends JPanel {
    
    private static final long serialVersionUID = -6190939165401154694L;
    
    private TimeScale timeScale;
	private TimeScalePainter timeScalePainter;
	private ChangeHandler changeHandler = new ChangeHandler();

	public TimeScaleHeader() {
		this(null);
	}
	
	public TimeScaleHeader(TimeScale timeScale) {
		setVisible(false);
		setForeground(Color.GRAY);
		setOpaque(true);
		setBackground(Color.WHITE);
		
		timeScalePainter = new TimeScaleHeaderPainter(this);
		
		if(timeScale != null){
			setTimeScale(timeScale);
		}
	}

	public TimeScale getTimeScale() {
		return timeScale;
	}

	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	public Dimension getPreferredSize() {
		int height = getFontMetrics(getFont()).getHeight() * 2;
		return new Dimension(20, height);
	}

	public void setTimeScale(TimeScale newTimeScale) {
		if (timeScale != null) {
			timeScale.removeChangeListener(changeHandler);
		}

		this.timeScale = newTimeScale;

		if (timeScale != null) {
			timeScale.addChangeListener(changeHandler);
		}
		timeScalePainter.setTimeScale(newTimeScale);
		setVisible(newTimeScale != null);
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
//		g.drawLine(getX(), getHeight() - 1, getWidth(), getHeight() - 1);
//		g.drawLine(getX(), getY(), getWidth(), getY());
		timeScalePainter.paint((Graphics2D) g);
	}

	class ChangeHandler implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			revalidate();
			repaint();
		}
	}

	static class TimeScaleHeaderPainter extends TimeScalePainter {
		private long lastDate;
		private int lastX;
		private int lastY;
		
		public TimeScaleHeaderPainter(JComponent comp) {
			super(comp);
			setLineColor(comp.getForeground());
		}

		public void paint(Graphics2D g) {
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
	                RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
			
			super.paint(g);
			int y = getComponent().getHeight() / 2;
			g.setStroke(new BasicStroke());
			g.drawLine(0, y, getComponent().getWidth(), y);
		}

		protected void drawUnit(Graphics g, TimeUnit timeUnit, long date, int x, boolean smallUnit) {
			int y = 0;
			if (smallUnit) {
				y = getComponent().getHeight() / 2;
			}
			g.drawLine(x, y, x, getComponent().getHeight());

			DateFormat format = (smallUnit ? timeUnit.getShortFormat() : timeUnit.getLongFormat());
			
			String dateString = format.format(new Date(lastDate));
			int stringWidth = g.getFontMetrics().stringWidth(dateString);
			
			if(lastX + stringWidth <= x){
				//TODO: Write a more sophisticated Algorithm: Try to cut String, adjust Font, whatever. 
				g.drawString(dateString, lastX + 2, g.getFontMetrics().getAscent() + lastY);
			}
			
			lastDate = date;
			lastX = x;
			lastY = y;
		}

		protected void highlightWeekend(Graphics g, int x, int weekendBegin) {
		}
	}
}
