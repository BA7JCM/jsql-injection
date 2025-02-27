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
package com.jsql.view.swing.tree.model;

import com.jsql.model.bean.database.Column;
import com.jsql.view.swing.tree.custom.JPopupMenuCustomExtract;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Model for default item used on an empty tree.
 */
public class NodeModelEmpty extends AbstractNodeModel {
    
    /**
     * Flat node for empty tree.
     */
    public NodeModelEmpty(String textNode) {
        super(textNode);
    }

    public NodeModelEmpty(Column column) {
        super(column);
    }

    @Override
    public Component getComponent(
        final JTree tree, Object nodeRenderer, final boolean isSelected, boolean isLeaf, boolean hasFocus
    ) {
        DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) nodeRenderer;
        var panelEmpty = new JPanel(new BorderLayout());
        var labelEmpty = new JLabel(currentNode.getUserObject().toString());
        panelEmpty.add(labelEmpty);
        labelEmpty.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        if (isSelected) {
            panelEmpty.setBackground(UIManager.getColor("Tree.selectionBackground"));
        } else {
            panelEmpty.setBackground(UIManager.getColor("Tree.selectionInactiveBackground"));
        }
        return panelEmpty;
    }

    @Override
    protected Icon getLeafIcon(boolean leaf) {
        // No icon for default node
        return null;
    }
    
    @Override
    public void runAction() {
        // Not used
    }
    
    @Override
    protected void buildMenu(JPopupMenuCustomExtract tablePopupMenu, TreePath path) {
        // Not used
    }
    
    @Override
    public void showPopup(final DefaultMutableTreeNode currentTableNode, TreePath path, MouseEvent e) {
        // Not used
    }
    
    @Override
    public boolean isPopupDisplayable() {
        return false;
    }
}
