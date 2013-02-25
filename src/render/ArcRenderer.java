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
		
					// There is not function for what we want to do - so we draw a polygon with many nodes and calculate those here
					int[] x = new int[92];
					int[] y = new int[92];
		
					double outerRadius = (child.getEndX()-item.getStartX()+1.0)/2.0;		
					double innerRadius = (child.getStartX()-item.getEndX()+1.0)/2.0;		
					for(int j=0; j<=180; j+=4) {
						x[j/4] = (int)Math.round( outerRadius * Math.cos( ((double)j) / 180.0 * Math.PI ) );
						x[91-j/4] = (int)Math.round( innerRadius * Math.cos( ((double)j) / 180.0 * Math.PI ) );
						y[j/4] = (int)Math.round( outerRadius * Math.sin( ((double)j) / 180.0 * Math.PI ) + outerRadius );
						y[91-j/4] = (int)Math.round( innerRadius * Math.sin( ((double)j) / 180.0 * Math.PI ) + outerRadius );
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
