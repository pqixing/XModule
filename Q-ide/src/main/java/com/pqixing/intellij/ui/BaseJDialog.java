package com.pqixing.intellij.ui;

import javax.swing.*;
import java.util.HashMap;

public class BaseJDialog extends JDialog {

    private static HashMap<String, JDialog> showDialogs = new HashMap<>();

    @Override
    public void pack() {
        super.pack();
        JDialog put = showDialogs.put(getClass().getName(), this);
        if (put != null) put.dispose();
        setLocationRelativeTo(null);
    }

    @Override
    public void dispose() {
        super.dispose();
        showDialogs.remove(getClass().getName(), this);
    }
}
