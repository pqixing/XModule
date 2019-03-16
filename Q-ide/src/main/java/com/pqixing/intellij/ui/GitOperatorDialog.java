package com.pqixing.intellij.ui;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.Key;
import com.pqixing.intellij.adapter.JListInfo;
import com.pqixing.intellij.adapter.JListSelectAdapter;
import com.pqixing.intellij.utils.UiUtils;

import java.awt.event.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;

import git4idea.commands.GitLineHandlerListener;

import org.jdesktop.swingx.JXRadioGroup;

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
    public JButton btnRevert;
    public Runnable onOk;
    public JRadioButton[] operators;

    public JListSelectAdapter adapter;
    public GitListener gitListener;
    public Runnable onOperatorChange;

    public void setOnOk(Runnable onOk) {
        this.onOk = onOk;
    }

    public GitOperatorDialog(String title, String curBranch, List<JListInfo> datas) {
        setContentPane(contentPane);
        setModal(false);
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

        initRadio();
        btnRevert.addActionListener(e -> {
            setLogModel(!pLog.isVisible());
        });
    }

    public void setOnOperatorChange(Runnable onOperatorChange) {
        this.onOperatorChange = onOperatorChange;
    }

    /**
     * 初始化按钮
     */
    private void initRadio() {
        ChangeListener l = e -> {
            JRadioButton b = (JRadioButton) e.getSource();
            if (onOperatorChange != null && b.isSelected()) onOperatorChange.run();
        };
        for (JRadioButton r : operators) r.addChangeListener(l);
    }

    public void setTargetBranch(List<String> comboxs, boolean visible) {
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

        return "";
    }

    public String getTargetBranch() {
        Object item = cbBranch.getSelectedItem();
        if (item == null) return "master";
        String trim = item.toString().trim();
        if (trim.isEmpty()) trim = "master";
        return trim;
    }

    private void setLogModel(boolean log) {
        // add your code here
        pLog.setVisible(log);
        pOk.setVisible(!log);
        pOpertator.setVisible(!log);
    }

    private void onOK() {
        setLogModel(true);
        if (onOk != null) onOk.run();
    }


    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static class GitListener implements GitLineHandlerListener {
        JTextComponent textField;
        ProgressIndicator indicator;
        private String lastPercentLine = null;

        public void setIndicator(ProgressIndicator indicator) {
            this.indicator = indicator;
        }

        public GitListener(JTextComponent textField) {
            this.textField = textField;
        }

        @Override
        public void onLineAvailable(String line, Key outputType) {
            if (line.startsWith("Picked up _JAVA_OPTIONS:")) return;
            int i = line.lastIndexOf(":");
            if (i > 0) {
                String start = line.substring(0, i);
                if (lastPercentLine != null && line.startsWith(start)) {
                    textField.setText(textField.getText().replace(lastPercentLine, line));
                } else {
                    textField.setText(textField.getText() + line + "\n");
                }
                lastPercentLine = line;
            } else {
                lastPercentLine = null;
                textField.setText(textField.getText()+line + "\n");
            }
            if (indicator != null) indicator.setText(line);
        }
    }
}