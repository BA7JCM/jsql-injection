package com.jsql.view.swing.combomenu;

import javax.swing.*;
import java.awt.*;

public class BlankIcon implements Icon {
    
    private final Color fillColor;
    
    private final int size;

    public BlankIcon() {
        
        this(null, 11);
    }

    public BlankIcon(Color color, int size) {

        this.fillColor = color;
        this.size = size;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (this.fillColor != null) {
            
            g.setColor(this.fillColor);
            g.drawRect(x, y, this.size-1, this.size-1);
        }
    }

    @Override
    public int getIconWidth() {
        return this.size;
    }

    @Override
    public int getIconHeight() {
        return this.size;
    }
}