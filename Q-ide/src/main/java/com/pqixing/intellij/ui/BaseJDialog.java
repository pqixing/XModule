package com.pqixing.intellij.ui;

import javax.swing.*;

public class BaseJDialog extends JDialog {

    @Override
    public void pack() {
        super.pack();
        setLocationRelativeTo(null);
    }
}
