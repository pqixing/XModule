package com.pqixing.intellij.ui;

import com.intellij.openapi.project.Project;
import com.pqixing.intellij.adapter.JListInfo;
import com.pqixing.intellij.adapter.JListSelectAdapter;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class ToMavenDialog extends BaseJDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton btnReDo;
    private JList jList;
    private JCheckBox all;
    private JLabel jlProgress;
    private JTextField tvDesc;
    private JCheckBox branchCheckBox;
    private JCheckBox versionCheckBox;
    private JCheckBox changeCheckBox;
    JListSelectAdapter adapter;
    private Runnable onOk;

    public ToMavenDialog(Project project, List<JListInfo> datas, String moduleName) {
        super(project);
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
        setTitle("ToMaven : " + moduleName);

        adapter = new JListSelectAdapter(jList, true);
        adapter.setDatas(datas);
        all.addActionListener(e -> {
            boolean allSelect = all.isSelected();
            for (JListInfo i : datas) {
                i.setSelect(allSelect);
            }
            updateUI(buttonOK.isVisible());
        });
        btnReDo.addActionListener(e -> {
            //重置所有状态
            for (JListInfo data : datas) {
                data.setStaue(0);
            }
            onOK();
        });
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

    public String getDesc() {
        return tvDesc.getText().trim();
    }

    public String getUnCheckCode() {
        int unCheck = 1;
        if (branchCheckBox.isSelected()) unCheck |= 1 << 4;
        if (versionCheckBox.isSelected()) unCheck |= 1 << 5;
        if (changeCheckBox.isSelected()) unCheck |= 1 << 6;
        return (unCheck == 1 ? 0 : unCheck) + "";
    }

    private void onOK() {
        buttonOK.setVisible(false);
        onOk.run();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }
}
