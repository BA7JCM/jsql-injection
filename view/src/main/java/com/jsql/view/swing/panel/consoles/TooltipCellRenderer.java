package com.jsql.view.swing.panel.consoles;

import com.jsql.view.swing.util.UiUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class TooltipCellRenderer extends DefaultTableCellRenderer {
    
    @Override
    public Component getTableCellRendererComponent(
        JTable table,
        Object value,
        boolean isSelected,
        boolean hasFocus,
        int row,
        int column
    ) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        label.setToolTipText(
            String.format(
                "<html><div style=\"font-size:10px;font-family:'%s'\">%s</div></html>",
                UiUtil.FONT_NAME_MONO_NON_ASIAN,
                label.getText().replaceAll("(.{100})(?!$)", "$1<br>")  // linebreak any 100 chars
            )
        );
        return label;
    }
}