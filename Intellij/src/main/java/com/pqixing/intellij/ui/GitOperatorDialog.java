package com.pqixing.intellij.ui;

import com.pqixing.intellij.adapter.JListSelectAdapter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

public class GitOperatorDialog extends JDialog implements ActionListener {
    private JPanel rootPanel;
    private JProgressBar progress;
    private JList jlRecords;
    private JTextArea tvLog;
    private JRadioButton clone;
    private JRadioButton merge;
    private JRadioButton checkout;
    private JRadioButton update;
    private JRadioButton create;
    private JRadioButton delete;
    private JButton buttonOK;
    private JCheckBox all;
    private JCheckBox reelection;
    private JLabel jlTitle;
    private JCheckBox handle;

    public String operatorCommond = "";
    public JListSelectAdapter adapter;

    public GitOperatorDialog() {
        setContentPane(rootPanel);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        buttonOK.addActionListener(e -> onOK());
        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        // call onCancel() on ESCAPE
        rootPanel.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);


        clone.addActionListener(this);
        merge.addActionListener(this);
        checkout.addActionListener(this);
        update.addActionListener(this);
        create.addActionListener(this);
        delete.addActionListener(this);
        adapter = new JListSelectAdapter(jlRecords,true);
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            list.add("item" + i);
        }
        adapter.setDatas(adapter.cover(list));
        jlRecords.setModel(adapter);
    }

    private void onOK() {
        dispose();
    }


    public static void main(String[] args) {
        GitOperatorDialog dialog = new GitOperatorDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        Object source = actionEvent.getSource();
        if (source instanceof JRadioButton) {
            JRadioButton r = ((JRadioButton) source);
            if (r.isSelected()) operatorCommond = r.getText();
        }
    }
}
