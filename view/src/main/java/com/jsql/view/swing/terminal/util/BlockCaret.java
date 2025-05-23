/*******************************************************************************
 * Copyhacked (H) 2012-2025.
 * This program and the accompanying materials
 * are made available under no term at all, use it like
 * you want, but share and discuss it
 * every time possible with every body.
 * 
 * Contributors:
 *      ron190 at ymail dot com - initial implementation
 ******************************************************************************/
package com.jsql.view.swing.terminal.util;

import com.jsql.util.LogLevelUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import java.awt.*;

/**
 * A caret in a block shape.
 */
public class BlockCaret extends DefaultCaret {
    
    private static final Logger LOGGER = LogManager.getRootLogger();
    
    /**
     * Create a caret shaped for terminal.
     */
    public BlockCaret() {
        this.setBlinkRate(500);  // half a second
    }

    @Override
    protected synchronized void damage(Rectangle r) {
        if (r == null) {
            return;
        }

        // give values to x,y,width,height (inherited from java.awt.Rectangle)
        this.x = r.x;
        this.y = r.y;
        this.height = r.height;
        
        // A value for width was probably set by paint(), which we leave alone.
        // But the first call to damage() precedes the first call to paint(), so
        // in this case we must be prepared to set a valid width, or else
        // paint()
        // will receive a bogus clip area and caret will not get drawn properly.
        if (this.width <= 0) {
            this.width = this.getComponent().getWidth();
        }

        //Calls getComponent().repaint(x, y, width, height) to erase
        this.repaint();
        
        // previous location of caret. Sometimes one call isn't enough.
        this.repaint();
    }

    @Override
    public void paint(Graphics g) {
        JTextComponent comp = this.getComponent();
        if (comp == null) {
            return;
        }

        int dot = this.getDot();
        Rectangle r = null;
        char dotChar;
        
        try {
            if (comp.modelToView2D(dot) != null) {
                r = comp.modelToView2D(dot).getBounds();
            }
            dotChar = comp.getText(dot, 1).charAt(0);
        } catch (BadLocationException e) {
            LOGGER.log(LogLevelUtil.CONSOLE_JAVA, e, e);
            return;
        }

        if (r == null) {
            return;
        }
        if (Character.isWhitespace(dotChar)) {
            dotChar = '_';
        }
        if (this.x != r.x || this.y != r.y) {
            // paint() has been called directly, without a previous call to
            // damage(), so do some cleanup. (This happens, for example, when
            // the text component is resized.)
            this.damage(r);
            return;
        }

        g.setColor(new Color(0, 255, 0));
        g.setXORMode(comp.getBackground()); // do this to draw in XOR mode

        this.width = g.getFontMetrics().charWidth(dotChar);
        if (this.isVisible()) {
            g.fillRect(r.x, r.y, this.width, r.height);
        }
    }
}
