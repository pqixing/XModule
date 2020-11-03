package com.pqixing.intellij.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;

public class BaseJDialog extends JDialog {
    Project pj0;

    public BaseJDialog(Project project) {
        this.pj0 = project;
    }

    private static HashMap<String, JDialog> showDialogs = new HashMap<>();

    @Override
    public void pack() {
        super.pack();
        JDialog put = showDialogs.put(getClass().getName(), this);
        if (put != null) put.dispose();
        GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        TestDialog td = null;
        if (devices != null && devices.length > 1) {
            td = new TestDialog(pj0);
            td.show();
            Point location = td.getLocation();
            setLocation(location.x - getWidth() / 2, location.y - getHeight() / 2);
        } else setLocationRelativeTo(null);
        if (td != null) td.unShow();
        getRootPane().registerKeyboardAction(a -> onCapsLockClick(), KeyStroke.getKeyStroke(KeyEvent.VK_CAPS_LOCK, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    long clickTime = 0;
    long lastOnCapsLockClick = 0;

    private void onCapsLockClick() {
        clickTime = System.currentTimeMillis() - lastOnCapsLockClick < 300 ? clickTime + 1 : 0;
        lastOnCapsLockClick = System.currentTimeMillis();
        if (clickTime > 0 && clickTime % 2 == 0) {
            setModal(!isModal());
            setVisible(false);
            setVisible(true);
        }
        if (clickTime == 5) {
            String s = Messages.showInputDialog("输入debug调试端口", "Gradle调试", null, "5005", null);
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
