package com.pqixing.intellij.ui;

import javax.swing.*;

public class BaseJDialog extends JDialog {

    private static JDialog mThis;

    @Override
    public void pack() {
        super.pack();
        if (mThis != null) mThis.dispose();
        mThis = this;
        setLocationRelativeTo(null);
    }

    @Override
    public void dispose() {
        super.dispose();
        mThis = null;
    }
}
