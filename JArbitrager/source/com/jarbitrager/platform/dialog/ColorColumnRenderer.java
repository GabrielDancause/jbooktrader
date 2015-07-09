package com.jarbitrager.platform.dialog;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

public class ColorColumnRenderer extends DefaultTableCellRenderer {
    private final Color fgndColor;

    public ColorColumnRenderer(Color foregnd) {
        super();
        fgndColor = foregnd;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        cell.setForeground(fgndColor);
        return cell;
    }
}