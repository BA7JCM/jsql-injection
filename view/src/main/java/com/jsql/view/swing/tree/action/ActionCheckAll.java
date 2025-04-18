package com.jsql.view.swing.tree.action;

import com.jsql.view.swing.tree.model.AbstractNodeModel;
import com.jsql.view.swing.util.MediatorHelper;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Listener to check or uncheck every children menu items.
 * Usually required from a table node to un/check every column
 */
public class ActionCheckAll implements ActionListener {
    
    private final boolean isCheckboxesSelected;
    private final TreePath path;
    
    public ActionCheckAll(boolean isCheckboxesSelected, TreePath path) {
        this.isCheckboxesSelected = isCheckboxesSelected;
        this.path = path;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        DefaultMutableTreeNode currentTableNode = (DefaultMutableTreeNode) this.path.getLastPathComponent();
        AbstractNodeModel currentTableModel = (AbstractNodeModel) currentTableNode.getUserObject();
        DefaultTreeModel treeModel = (DefaultTreeModel) MediatorHelper.treeDatabase().getModel();
        int tableChildCount = treeModel.getChildCount(currentTableNode);
        for (var i = 0 ; i < tableChildCount ; i++) {
            DefaultMutableTreeNode currentChild = (DefaultMutableTreeNode) treeModel.getChild(currentTableNode, i);
            if (currentChild.getUserObject() instanceof AbstractNodeModel) {
                AbstractNodeModel columnTreeNodeModel = (AbstractNodeModel) currentChild.getUserObject();
                columnTreeNodeModel.setSelected(this.isCheckboxesSelected);
                
                currentTableModel.setIsAnyCheckboxSelected(this.isCheckboxesSelected);
            }
        }

        treeModel.nodeChanged(currentTableNode);
    }
}