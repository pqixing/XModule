package com.pqixing.intellij.ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;

public class BaseJDialog extends JDialog {

    private static HashMap<String, JDialog> showDialogs = new HashMap<>();

    @Override
    public void pack() {
        super.pack();
        JDialog put = showDialogs.put(getClass().getName(), this);
        if (put != null) put.dispose();
        setLocationRelativeTo(null);

        getRootPane().registerKeyboardAction(a -> onCapsLockClick(), KeyStroke.getKeyStroke(KeyEvent.VK_CAPS_LOCK, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    long lastOnCapsLockClick = 0;
    private void onCapsLockClick() {
        if(System.currentTimeMillis() - lastOnCapsLockClick <300) {
            setModal(!isModal());
            setVisible(false);
            setVisible(true);
        }else {
            lastOnCapsLockClick = System.currentTimeMillis();
        }
    }

    public BaseJDialog showAndPack() {
        pack();
        setVisible(true);
        return this;
    }

    @Override
    public void dispose() {
        super.dispose();
        showDialogs.remove(getClass().getName(), this);
    }
}
