package com.pqixing.intellij.ui;

import com.pqixing.intellij.adapter.JListInfo;
import com.pqixing.intellij.adapter.JListSelectAdapter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class CleanDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JList jlProjects;
    private JCheckBox cbGradle;
    private JCheckBox cbIml;
    private Runnable onOk;
    public JListSelectAdapter adapter;

    public CleanDialog(List<JListInfo> datas) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        adapter = new JListSelectAdapter(jlProjects, true);
        adapter.setDatas(datas);
    }

    public void setOnOk(Runnable onOk) {
        this.onOk = onOk;
    }

    private void onOK() {
        // add your code here
        dispose();
        if (onOk != null) onOk.run();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }
}
