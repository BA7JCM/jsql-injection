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
package com.jsql.view.swing.list;

import com.jsql.util.I18nUtil;
import com.jsql.util.LogLevelUtil;
import com.jsql.view.swing.util.MediatorHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Action to export a JList.
 */
public class MenuActionExport implements ActionListener {
    
    private static final Logger LOGGER = LogManager.getRootLogger();

    /**
     * List to export.
     */
    private final DnDList myList;
    
    /**
     * Create action to export a list.
     * @param myList List to export.
     */
    public MenuActionExport(DnDList myList) {
        this.myList = myList;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        final JFileChooser importFileDialog = new JFileChooser(MediatorHelper.model().getMediatorUtils().getPreferencesUtil().getPathFile()) {
            @Override
            public void approveSelection() {
                var file = this.getSelectedFile();
                if (file.exists() && this.getDialogType() == JFileChooser.SAVE_DIALOG) {
                    int replace = JOptionPane.showConfirmDialog(
                        MediatorHelper.frame(),
                        String.format("%s %s", file.getName(), I18nUtil.valueByKey("LIST_EXPORT_CONFIRM_LABEL")),
                        I18nUtil.valueByKey("LIST_EXPORT_CONFIRM_TITLE"),
                        JOptionPane.YES_NO_OPTION
                    );
                    
                    switch (replace) {
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        case JOptionPane.NO_OPTION:
                        case JOptionPane.CLOSED_OPTION:
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            this.cancelSelection();
                            return;
                        default:
                            break;
                    }
                } else {
                    super.approveSelection();
                }
            }
        };
        
        importFileDialog.setDialogTitle(I18nUtil.valueByKey("LIST_EXPORT_TITLE"));
        int choice = importFileDialog.showSaveDialog(this.myList.getTopLevelAncestor());
        if (choice != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try (
            var file = new FileOutputStream(importFileDialog.getSelectedFile());
            var out = new PrintStream(file, false, StandardCharsets.UTF_8)
        ) {
            int len = this.myList.getModel().getSize();
            for (var i = 0 ; i < len ; i++) {
                out.println(this.myList.getModel().getElementAt(i).toString());
            }
        } catch (IOException e) {
            LOGGER.log(LogLevelUtil.CONSOLE_JAVA, e, e);
        }
    }
}
