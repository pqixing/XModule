package com.pqixing.intellij.ui;

import com.pqixing.intellij.adapter.JListInfo;
import com.pqixing.intellij.adapter.JListSelectAdapter;
import com.pqixing.intellij.utils.UiUtils;

import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ToMavenDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JList jList;
    private JButton all;
    private JLabel jlProgress;
    public JLabel jlTitle;
    JListSelectAdapter adapter;
    private Runnable onOk;
    private boolean allSelect;

    public ToMavenDialog(List<JListInfo> datas) {
        setContentPane(contentPane);
        setModal(false);
        setLocation(400, 300);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        setTitle("ToMaven");

        adapter = new JListSelectAdapter(jList, true);
        adapter.setDatas(datas);
        jList.setModel(adapter);
        all.addActionListener(e -> {
            allSelect = !allSelect;
            all.setText(allSelect ? "None" : "All");
            for (JListInfo i : datas) {
                i.setSelect(allSelect);
            }
            updateUI(buttonOK.isVisible());
        });

        UiUtils.centerDialog(this);
    }

    public void setOnOk(Runnable onOk) {
        this.onOk = onOk;
    }

    /**
     * 刷新UI
     *
     * @param okVisible
     */
    public void updateUI(boolean okVisible) {
        buttonOK.setVisible(okVisible);
        jlProgress.setVisible(!okVisible);
        if (!okVisible) {

            List<JListInfo> datas = adapter.getDatas();
            int all = 0;
            int done = 0;
            for (JListInfo i : datas) {
                if (i.getSelect()) all++;
                if (i.getStaue() == 1) done++;
            }
            jlProgress.setText(done + "/" + all);
        }
        adapter.updateUI();

    }

    private void onOK() {
        buttonOK.setVisible(false);
        onOk.run();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        ToMavenDialog dialog = new ToMavenDialog(Collections.emptyList());
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
