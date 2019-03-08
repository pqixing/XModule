package com.pqixing.intellij.ui;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.Key;
import com.pqixing.intellij.adapter.JListInfo;
import com.pqixing.intellij.adapter.JListSelectAdapter;
import com.pqixing.intellij.utils.UiUtils;

import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

import git4idea.commands.GitLineHandlerListener;

public class GitOperatorDialog extends JDialog {
    public JPanel contentPane;
    public JButton buttonOK;
    public JButton buttonCancel;
    public JComboBox cbBranch;
    public JRadioButton rb01;
    public JRadioButton rb02;
    public JRadioButton rb03;
    public JList jLDatas;
    public JLabel jLTarget;
    public JLabel jlBranch;
    public JButton allButton;
    public JLabel jlTips;
    public JTextArea tLog;
    public JScrollPane pLog;
    public JPanel pOpertator;
    public JPanel pOk;
    public JPanel pHeader;
    public Runnable onOk;
    public JRadioButton[] operators;

    public JListSelectAdapter adapter;
    public GitListener gitListener;

    public void setOnOk(Runnable onOk) {
        this.onOk = onOk;
    }

    public GitOperatorDialog(String title, String curBranch, List<JListInfo> datas) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        gitListener = new GitListener(tLog);
        UiUtils.centerDialog(this);
        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        operators = new JRadioButton[]{rb01, rb02, rb03};
        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        setTitle(title);
        jlBranch.setText("Current: " + curBranch);
        adapter = new JListSelectAdapter(jLDatas, true);
        adapter.setDatas(datas);
        allButton.addActionListener(actionEvent -> {
            for (JListInfo i : datas) {
                i.setSelect(!i.getSelect());
            }
            adapter.updateUI();
        });

        jlTips.setText("All " + datas.size() + " Project");
    }

    public void setTargetBranch(List<String> comboxs,boolean visible) {
        jLTarget.setVisible(visible);
        cbBranch.setVisible(visible);
        if (comboxs != null) for (int i = 0; i < comboxs.size(); i++) {
            cbBranch.addItem(comboxs.get(i));
        }
    }

    public void updateUI() {
        adapter.updateUI();
    }

    /**
     * 设置参数
     *
     * @param cmds
     */
    public void setOperator(String... cmds) {
        for (int i = 0; i < operators.length; i++) {
            String c = i < cmds.length ? cmds[i] : null;
            if (c == null) operators[i].setVisible(false);
            else operators[i].setText(c);
        }
    }

    /**
     * 获取选中的命令
     * 如果为空,则忽略
     *
     * @return
     */
    public String getOperatorCmd() {
        for (JToggleButton btn : operators) {
            String text = btn.getText();
            if (btn.isVisible() && !text.isEmpty() && btn.isSelected()) {
                return text;
            }
        }

        return null;
    }

    public String getTargetBranch() {
        String trim = cbBranch.getSelectedItem().toString().trim();
        if (trim.isEmpty()) trim = "master";
        return trim;
    }

    private void onOK() {
        // add your code here
        pLog.setVisible(true);
        pOk.setVisible(false);
        pOpertator.setVisible(false);
        if (onOk != null) onOk.run();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static class GitListener implements GitLineHandlerListener {
        JTextComponent textField;
        ProgressIndicator indicator;

        public void setIndicator(ProgressIndicator indicator) {
            this.indicator = indicator;
        }

        public GitListener(JTextComponent textField) {
            this.textField = textField;
        }

        @Override
        public void onLineAvailable(String line, Key outputType) {
            textField.setText(textField.getText() + "\n" + line);
            if (indicator != null) indicator.setText(line);
        }
    }
}