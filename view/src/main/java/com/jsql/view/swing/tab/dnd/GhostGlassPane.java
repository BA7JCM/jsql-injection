package com.jsql.view.swing.tab.dnd;

import javax.swing.*;
import java.awt.*;

public class GhostGlassPane extends JComponent {
    
    private DnDTabbedPane tabbedPane;
    
    protected GhostGlassPane(DnDTabbedPane tabbedPane) {
        this.tabbedPane = tabbedPane;
        this.setOpaque(false);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        this.tabbedPane.getDropLineRect().ifPresent(rect -> {
            Graphics2D g2 = (Graphics2D) g.create();
            var r = SwingUtilities.convertRectangle(this.tabbedPane, rect, this);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
            g2.setPaint(Color.RED);
            g2.fill(r);
            g2.dispose();
        });
    }
    
    public void setTargetTabbedPane(DnDTabbedPane tab) {
        this.tabbedPane = tab;
    }
}