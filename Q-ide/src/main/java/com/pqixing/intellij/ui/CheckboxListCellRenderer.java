package com.pqixing.intellij.ui;

import javax.swing.*;
import java.awt.*;


public class CheckboxListCellRenderer  implements ListCellRenderer {

    public CheckboxListCellRenderer(){
    }


    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JCheckBox jCheckBox = new JCheckBox();
        jCheckBox.setComponentOrientation(list.getComponentOrientation());
        jCheckBox.setFont(list.getFont());
        jCheckBox.setBackground(list.getBackground());
        jCheckBox.setForeground(list.getForeground());
        jCheckBox.setSelected(isSelected);
        jCheckBox.setEnabled(list.isEnabled());
        jCheckBox.setText(value == null ? "" : value.toString());

        return jCheckBox;
    }
}