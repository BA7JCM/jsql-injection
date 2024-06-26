/*******************************************************************************
 * Copyhacked (H) 2012-2020.
 * This program and the accompanying materials
 * are made available under no term at all, use it like
 * you want, but share and discuss about it
 * every time possible with every body.
 * 
 * Contributors:
 *      ron190 at ymail dot com - initial implementation
 ******************************************************************************/
package com.jsql.view.swing.tree;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.image.ImageObserver;

/**
 * Observer to update tree node composed by the animated GIF.
 */
public class ImageObserverAnimated implements ImageObserver {
    
    private final JTree tree;
    private final TreePath path;
    
    /**
     * Build GIF animator for tree node.
     * @param tree Tree containing GIF node
     * @param node Node with a GIF to animate
     */
    public ImageObserverAnimated(JTree tree, TreeNode node) {
        
        this.tree = tree;
        
        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        this.path = new TreePath(treeModel.getPathToRoot(node));
    }

    @Override
    public boolean imageUpdate(Image image, int flags, int x, int y, int w, int h) {
        
        if ((flags & (FRAMEBITS | ALLBITS)) != 0) {
            
            Rectangle rectangle = this.tree.getPathBounds(this.path);
            
            if (rectangle != null) {
                this.tree.repaint(rectangle);  // Unhandled StackOverflowError #92723
            }
        }
        
        return (flags & (ALLBITS | ABORT)) == 0;
    }
}
