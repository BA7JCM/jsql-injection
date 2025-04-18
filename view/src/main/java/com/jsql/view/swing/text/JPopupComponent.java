/*******************************************************************************
 * Copyhacked (H) 2012-2025.
 * This program and the accompanying materials
 * are made available under no term at all, use it like
 * you want, but share and discuss it
 * every time possible with every body.
 *
 * Contributors:
 *      ron190 at ymail dot com - initial implementation
 *******************************************************************************/
package com.jsql.view.swing.text;

import com.jsql.view.swing.popupmenu.JPopupMenuComponent;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Add a popup menu to Decorated component.
 * @param <T> Component like JTextField or JTextArea to decorate
 */
public class JPopupComponent<T extends JTextComponent> extends JComponent implements DecoratorJComponent<T> {
    
    /**
     * Decorated component.
     */
    private final T proxy;

    /**
     * Get the decorated component, add popup menu Select All and Copy.
     * @param proxy Swing component to decorate
     */
    public JPopupComponent(final T proxy) {
        this.proxy = proxy;
        this.proxy.setComponentPopupMenu(new JPopupMenuComponent(this.proxy));
        this.proxy.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                // Left button will unselect text after selectAll, so only for right click
                if (SwingUtilities.isRightMouseButton(e)) {
                    JPopupComponent.this.proxy.requestFocusInWindow();
                }
            }
        });
    }

    @Override
    public T getProxy() {
        return this.proxy;
    }
}
