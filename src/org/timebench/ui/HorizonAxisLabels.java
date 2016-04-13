package org.timebench.ui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import prefuse.Display;
import prefuse.util.display.PaintListener;

/**
 * This class is responsible for controlling the labels of the variables.
 * @author Atanasov and Schindler
 */
public class HorizonAxisLabels implements PaintListener {
    
    private boolean isLegendVisible;
    private Object[] variables;

    public HorizonAxisLabels(Object[] variables, boolean isLegendVisible) {
        this.isLegendVisible = isLegendVisible;
        this.variables = variables;
    }

    @Override
    public void postPaint(Display d, Graphics2D g) {
        float displayHeight = d.getHeight();
        float bandHeight;

        if (isLegendVisible)
            displayHeight = (float) (d.getHeight() - HorizonLegend.HEIGHT);
        else
            displayHeight = (float) d.getHeight();

        bandHeight = displayHeight
                / ((0.1f * (variables.length - 1)) + variables.length);

        float y = bandHeight / 2;

        FontMetrics metrics = g.getFontMetrics();
        y += metrics.getHeight() / 2;

        g.setColor(new Color(0, 0, 0));

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        for (Object p : variables) {
            g.drawString(String.valueOf(p), 3, y);
            y += bandHeight * 11.0 / 10.0;
        }
    }

    @Override
    public void prePaint(Display d, Graphics2D g) {
        // nothing to do here
    }
}
