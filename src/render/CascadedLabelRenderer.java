package render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;

/**
 * 
 * 
 * <p>
 * Added:          / TL<br>
 * Modifications: 
 * </p>
 * 
 * @author Tim Lammarsch
 *
 */
public class CascadedLabelRenderer implements prefuse.render.Renderer {
	public void render(Graphics2D g, VisualItem item) {
		System.out.println("render " + item.getString(VisualItem.LABEL));
		if(item.getString(VisualItem.LABEL) != null) {
			g.setColor(Color.BLACK);
			g.drawString(item.getString(VisualItem.LABEL),(int)item.getX(),(int)item.getY());
		}
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
		// TODO
		item.setBounds(item.getX()-10, item.getY()-10, 20, 20);
        System.out.println("bounds " + item.getX() + " " + item.getY());
	}
}
