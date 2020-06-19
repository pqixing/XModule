package com.pqixing.intellij.ui;

import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.event.*;

public class OpenNewProjectDialog extends BaseJDialog {
    public JButton buttonOK;
    public JButton buttonCancel;
    public JTextField tvDir;
    public JPanel contentPane;
    public JButton tvFilePick;
    public JTextField tvGitUrl;
     Runnable onOk;


    public void setOnOk(Runnable onOk) {
        this.onOk = onOk;
    }


    public OpenNewProjectDialog(Project project) {
        super(project);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle("Open New Project");
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        // add your code here
        dispose();
        onOk.run();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }
}
