package timeBench.ui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import prefuse.Display;
import prefuse.util.display.PaintListener;
import timeBench.action.layout.HorizonGraphAction;

/**
 * This class contains the control of the legend of HorizonGraph.
 * Shown: 
 *  - color bands
 *  - outliers
 *  - null values
 * @author Atanasov and Schindler
 */
public class HorizonLegend implements PaintListener
{
    static final int LEGEND_HEIGHT = 15;	// the height of the legend (excluding text) in px
	static final int OUTLIER_WIDTH = 20;	// the width of the outlier bands in the legend in px
	static final int BAND_WIDTH =    50; 	// the distance between to bands (e.g. -50 to -25) in px
	static final int NULL_VALUE_WIDTH = 52;	
	
	public static final int HEIGHT = LEGEND_HEIGHT + 20; // 20 (font-height, borders)

//    private boolean _settings.IsLegendVisible;
//    private int _settings.BandsCount;
//    private boolean _settings.Indexing;
    
    HorizonSettings _settings;
    
    public HorizonLegend(HorizonSettings settings)
    {
        _settings = settings;
    }

	@Override
	public void postPaint(Display d, Graphics2D g)
	{
		if(! _settings.IsLegendVisible) return;
		// attention Point (0, 0) is the TOP, left corner
		
		drawLegend(d, g);
		drawText(d, g);
	}

	
	/**
	 * This method draws the legend.
	 * @param display the main display
	 * @param graphics the graphics object
	 */
	private void drawLegend(Display d, Graphics2D g)
	{	
		int xOffset = (d.getWidth() - (OUTLIER_WIDTH + (BAND_WIDTH * _settings.BandsCount)) * 2 ) / 2,
			yOffset = d.getHeight() - LEGEND_HEIGHT - g.getFontMetrics().getHeight();
		
		int xMid = xOffset + OUTLIER_WIDTH + (BAND_WIDTH * _settings.BandsCount);
		int xMax = xOffset + (OUTLIER_WIDTH + (BAND_WIDTH * _settings.BandsCount)) * 2;
		
		int[] colors = _settings.getColorPalette();		
		
		// draw the negative bands
		if(_settings.Type.equals(HorizonSettings.HORIZON_TYPE_MIRROR))
			for(int i = 0; i < _settings.BandsCount; i++)
			{
				g.setColor(new Color(colors[HorizonGraphAction.MAX_BANDS - i]));
				g.fillPolygon(new int[] { xOffset, xOffset, xOffset + OUTLIER_WIDTH + (_settings.BandsCount - i - 1) * BAND_WIDTH, xOffset + OUTLIER_WIDTH + (_settings.BandsCount - i) * BAND_WIDTH }, 
						      new int[] { yOffset + LEGEND_HEIGHT, yOffset, yOffset, yOffset + LEGEND_HEIGHT}, 4);
			}			
		else
			for(int i = 0; i < _settings.BandsCount; i++)
			{
				g.setColor(new Color(colors[HorizonGraphAction.MAX_BANDS - i]));
				g.fillPolygon(new int[] { xOffset, xOffset, xOffset + OUTLIER_WIDTH + (_settings.BandsCount - i) * BAND_WIDTH, xOffset + OUTLIER_WIDTH + (_settings.BandsCount - i - 1) * BAND_WIDTH }, 
						      new int[] { yOffset + LEGEND_HEIGHT, yOffset, yOffset, yOffset + LEGEND_HEIGHT}, 4);
			}		
		
		// draw the positive bands
		for(int i = 0; i < _settings.BandsCount; i++)
		{
			g.setColor(new Color(colors[HorizonGraphAction.MAX_BANDS + i + 2]));			
			g.fillPolygon(new int[] { xMid + (i * BAND_WIDTH), xMid + ((i + 1) * BAND_WIDTH), xMax, xMax }, 
					      new int[] { yOffset + LEGEND_HEIGHT, yOffset, yOffset, yOffset + LEGEND_HEIGHT}, 4);
		}
		
		// draw negative outlier rectangle
		g.setColor(new Color(colors[0]));
		g.fillPolygon(new int[] { xOffset, xOffset + OUTLIER_WIDTH, xOffset + OUTLIER_WIDTH, xOffset }, 
					  new int[] { yOffset, yOffset, yOffset + LEGEND_HEIGHT, yOffset + LEGEND_HEIGHT}, 4);
			
		// draw positive outlier rectangle
		g.setColor(new Color(colors[colors.length - 1]));
		g.fillPolygon(new int[] { xMax - OUTLIER_WIDTH, xMax, xMax , xMax - OUTLIER_WIDTH }, 
					  new int[] { yOffset, yOffset, yOffset + LEGEND_HEIGHT, yOffset + LEGEND_HEIGHT}, 4);
		
		
		// draw null value rectangle
		g.setColor(new Color(colors[HorizonGraphAction.MAX_BANDS + 1]));
		g.fillPolygon(new int[] { xMax + NULL_VALUE_WIDTH / 2, xMax + NULL_VALUE_WIDTH, xMax + NULL_VALUE_WIDTH, xMax + NULL_VALUE_WIDTH/2 }, 
				  new int[] { yOffset, yOffset, yOffset + LEGEND_HEIGHT, yOffset + LEGEND_HEIGHT}, 4);
	}
	
	
	/**
	 * Draws the captions of the legend.
	 * @param display the main display
	 * @param graphics the graphics object
	 */
	private void drawText(Display display, Graphics2D graphics)
	{
		graphics.setColor(Color.BLACK);
		FontMetrics metrics = graphics.getFontMetrics();
			
		double val = _settings.Indexing ? 0 : _settings.getBase();
		val -= (_settings.getBandWidth() * _settings.BandsCount);
		
		int x = (display.getWidth() - (OUTLIER_WIDTH + (BAND_WIDTH * _settings.BandsCount)) * 2 ) / 2 + OUTLIER_WIDTH, y = display.getHeight() - 3;
		
		if(_settings.Indexing)
			graphics.drawString("% values", x - 78, y);
						
		for(int i = 0; i < _settings.BandsCount * 2 + 1; i++)
		{
			double val2 = _settings.Indexing ? val * 100 : val;
			String valStr = String.valueOf(Math.round(val2));
			valStr = _settings.Indexing && val > 0 ? "+" + valStr : valStr;
			
			graphics.drawString(valStr, x - (metrics.stringWidth(valStr) / 2), y);
			
			val += _settings.getBandWidth();
			x += BAND_WIDTH;
		}
		
		x -= BAND_WIDTH;
		x += OUTLIER_WIDTH;
		x += 3;
		
		graphics.drawString("missing values", x, y);
	}

    @Override
    public void prePaint(Display d, Graphics2D g) {
        // nothing to do here
    }
}
