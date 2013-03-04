package render;

import ieg.prefuse.data.ParentChildNode;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Iterator;

import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.util.ColorLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

public class ArcRenderer implements prefuse.render.Renderer {
	/**
	 * Draws arcs like for ArcDiagrams
	 * @see prefuse.render.Renderer#render(java.awt.Graphics2D, prefuse.visual.VisualItem)
	 */
	public void render(Graphics2D g, VisualItem item) {		
		if(item instanceof EdgeItem) {
			NodeItem childNode = ((EdgeItem)item).getSourceItem();
			NodeItem parentNode = ((EdgeItem)item).getTargetItem();
			if (childNode instanceof VisualItem && parentNode instanceof VisualItem) {
				VisualItem child = (VisualItem)childNode;
				VisualItem parent = (VisualItem)parentNode;						
	
				// There is not function for what we want to do - so we draw a polygon with many nodes and calculate those here
				int[] x = new int[128];
				int[] y = new int[128];
	
				double outerRadius = (child.getDouble(VisualItem.X2)-parent.getX())/2.0;		
				double innerRadius = (child.getX()-parent.getDouble(VisualItem.X2))/2.0;
				if ( parent.getColumnIndex(ParentChildNode.DEPTH) == -1 ) {
					for(int j=0; j<=95; j++) {
						x[j] = (int)Math.round( parent.getX() + outerRadius * Math.cos( ((double)j) / 95.0 * Math.PI ) + outerRadius );
						y[j] = (int)Math.round( -outerRadius * Math.sin( ((double)j) / 95.0 * Math.PI ) + g.getClipBounds().height - 5 );
					}
					for(int j=0; j<=31; j++) {
						x[96+j] = (int)Math.round( parent.getDouble(VisualItem.X2) - innerRadius * Math.cos( ((double)j) / 31.0 * Math.PI ) + innerRadius );
						y[96+j] = (int)Math.round( -innerRadius * Math.sin( ((double)j) / 31.0 * Math.PI ) + g.getClipBounds().height - 5 );
					}
				} else {						
					int ydirect = parent.getInt(ParentChildNode.DEPTH) % 2 == 1 ? -1 : 1;
					for(int j=0; j<=95; j++) {
						x[j] = (int)Math.round( parent.getX() + outerRadius * Math.cos( ((double)j) / 95.0 * Math.PI ) + outerRadius );
						y[j] = (int)Math.round( -outerRadius * ydirect * Math.sin( ((double)j) / 95.0 * Math.PI ) + g.getClipBounds().height / 2 );
					}
					for(int j=0; j<=31; j++) {
						x[96+j] = (int)Math.round( parent.getDouble(VisualItem.X2) - innerRadius * Math.cos( ((double)j) / 31.0 * Math.PI ) + innerRadius );
						y[96+j] = (int)Math.round( -innerRadius * ydirect * Math.sin( ((double)j) / 31.0 * Math.PI ) + g.getClipBounds().height / 2 );
					}
				}
				
				/*g.setColor(ColorLib.getColor(ColorLib.red(item.getFillColor()),
						ColorLib.green(item.getFillColor()),
						ColorLib.blue(item.getFillColor()),
						(int)Math.round((1.0-(outerRadius-innerRadius)/outerRadius)*170.0)));*/

				VisualItem firstParent = parent;
				while(firstParent.getInt(ParentChildNode.DEPTH) > 0) {
					Iterator iN = ((NodeItem)firstParent).outNeighbors();
					firstParent = (VisualItem)iN.next();
				}
				
				g.setColor(ColorLib.getColor(ColorLib.red(firstParent.getFillColor()),
						ColorLib.green(firstParent.getFillColor()),
						ColorLib.blue(firstParent.getFillColor()),
						(int)Math.round((1.0-(outerRadius-innerRadius)/outerRadius)*170.0)));
				
				/*if (firstParent.getInt("class") == 0 )
					g.setColor(ColorLib.getColor(255,0,0,(int)Math.round((1.0-(outerRadius-innerRadius)/outerRadius)*170.0)));
				if (firstParent.getInt("class") == 1 )
					g.setColor(ColorLib.getColor(0,255,0,(int)Math.round((1.0-(outerRadius-innerRadius)/outerRadius)*170.0)));
				if (firstParent.getInt("class") == 2 )
					g.setColor(ColorLib.getColor(0,0,255,(int)Math.round((1.0-(outerRadius-innerRadius)/outerRadius)*170.0)));*/
	
				/*if(parent.getInt("class") == 0 && child.getInt("class") == 0)
					g.setColor(ColorLib.getColor(255, 0, 0, 63));
				if(parent.getInt("class") == 0 && child.getInt("class") == 1)
					g.setColor(ColorLib.getColor(170, 170, 0, 63));
				if(parent.getInt("class") == 0 && child.getInt("class") == 2)
					g.setColor(ColorLib.getColor(170, 0, 170, 63));
				if(parent.getInt("class") == 1 && child.getInt("class") == 0)
					g.setColor(ColorLib.getColor(170, 170, 0, 63));
				if(parent.getInt("class") == 1 && child.getInt("class") == 1)
					g.setColor(ColorLib.getColor(0, 255, 0, 63));
				if(parent.getInt("class") == 1 && child.getInt("class") == 2)
					g.setColor(ColorLib.getColor(0, 170, 170, 63));
				if(parent.getInt("class") == 2 && child.getInt("class") == 0)
					g.setColor(ColorLib.getColor(170, 0, 170, 63));
				if(parent.getInt("class") == 2 && child.getInt("class") == 1)
					g.setColor(ColorLib.getColor(0, 170, 170, 63));
				if(parent.getInt("class") == 2 && child.getInt("class") == 2)
					g.setColor(ColorLib.getColor(0, 0, 255, 63));*/
				
				g.fillPolygon(x, y, 128);
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
