package render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import prefuse.data.Node;
import prefuse.util.ColorLib;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

public class ArcRenderer implements prefuse.render.Renderer {
	/**
	 * Draws arcs like for ArcDiagrams
	 * @see prefuse.render.Renderer#render(java.awt.Graphics2D, prefuse.visual.VisualItem)
	 */
	public void render(Graphics2D g, VisualItem item) {		
		if(item instanceof NodeItem) { 								
			// draw from ViusalItem to each child (no arcs for leaves)
			for (int i=0; i<((NodeItem)item).getChildCount(); i++) {			
				Node childNode = ((NodeItem)item).getChild(i);
				if (childNode instanceof VisualItem) {
					VisualItem child = (VisualItem)childNode;
				
					// color of parent
					g.setColor(ColorLib.getColor(item.getFillColor()));
					/*if(item.getInt("class") == 0 && child.getInt("class") == 0)
						g.setColor(ColorLib.getColor(255, 0, 0, 63));
					if(item.getInt("class") == 0 && child.getInt("class") == 1)
						g.setColor(ColorLib.getColor(170, 170, 0, 63));
					if(item.getInt("class") == 0 && child.getInt("class") == 2)
						g.setColor(ColorLib.getColor(170, 0, 170, 63));
					if(item.getInt("class") == 1 && child.getInt("class") == 0)
						g.setColor(ColorLib.getColor(170, 170, 0, 63));
					if(item.getInt("class") == 1 && child.getInt("class") == 1)
						g.setColor(ColorLib.getColor(0, 255, 0, 63));
					if(item.getInt("class") == 1 && child.getInt("class") == 2)
						g.setColor(ColorLib.getColor(0, 170, 170, 63));
					if(item.getInt("class") == 2 && child.getInt("class") == 0)
						g.setColor(ColorLib.getColor(170, 0, 170, 63));
					if(item.getInt("class") == 2 && child.getInt("class") == 1)
						g.setColor(ColorLib.getColor(0, 170, 170, 63));
					if(item.getInt("class") == 2 && child.getInt("class") == 2)
						g.setColor(ColorLib.getColor(0, 0, 255, 63));*/						
		
					// There is not function for what we want to do - so we draw a polygon with many nodes and calculate those here
					int[] x = new int[92];
					int[] y = new int[92];
		
					double outerRadius = (child.getDouble(VisualItem.X2)-item.getX())/2.0;		
					double innerRadius = (child.getX()-item.getDouble(VisualItem.X2))/2.0;							
					for(int j=0; j<=180; j+=4) {
						x[j/4] = (int)Math.round( item.getX() + outerRadius * Math.cos( ((double)j) / 180.0 * Math.PI ) + outerRadius );
						x[91-j/4] = (int)Math.round( item.getX() + innerRadius * Math.cos( ((double)j) / 180.0 * Math.PI ) + outerRadius );
						y[j/4] = (int)Math.round( -outerRadius * Math.sin( ((double)j) / 180.0 * Math.PI ) + g.getClipBounds().height - 5 );
						y[91-j/4] = (int)Math.round( -innerRadius * Math.sin( ((double)j) / 180.0 * Math.PI ) + g.getClipBounds().height - 5 );
					}
					
					g.fillPolygon(x, y, 92);
				}
			}
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
		item.setBounds(item.getStartX(), item.getStartY(), item.getEndX()-item.getStartX()+1, item.getEndY()-item.getStartY()+1);
	}
}
