package org.timebench.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;

public class RectangleRenderer implements prefuse.render.Renderer {
	/**
	 * Draws a filled rectangle as fast as possible.
	 * @see prefuse.render.Renderer#render(java.awt.Graphics2D, prefuse.visual.VisualItem)
	 */
	public void render(Graphics2D g, VisualItem item) {
		if(item.isHover() || item.isHighlighted()) {
			item.setStrokeColor(item.getStartFillColor());			
		} else {
			item.setStrokeColor(item.getFillColor());
		}

		g.setColor(ColorLib.getColor(item.getFillColor()));
		g.fillRect((int)item.getStartX(), (int)item.getStartY(), (int)(item.getEndX()-item.getStartX()+1), (int)(item.getEndY()-item.getStartY()+1));
		
		g.setColor(ColorLib.getColor(item.getStrokeColor()));
		if(item.isHighlighted())
			g.setStroke(new BasicStroke(5));
		else
			g.setStroke(new BasicStroke(1));
		g.drawRect((int)item.getStartX(), (int)item.getStartY(), (int)(item.getEndX()-item.getStartX()+1), (int)(item.getEndY()-item.getStartY()+1));
	}

	/**
	 * Checks whether a point is inside the bounds of the item.
	 * @see prefuse.render.Renderer#locatePoint(java.awt.geom.Point2D, prefuse.visual.VisualItem)
	 */
	public boolean locatePoint(Point2D p, VisualItem item) {
        return item.getBounds().contains(p);
	}

	/**
	 * Sets the bounds of the item to the bounds of the rectangle.
	 * @see prefuse.render.Renderer#setBounds(prefuse.visual.VisualItem)
	 */
	public void setBounds(VisualItem item) {
		item.setBounds(item.getStartX(), item.getStartY(), item.getEndX()-item.getStartX()+1, item.getEndY()-item.getStartY()+1);
	}
}
