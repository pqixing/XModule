package com.dachen.creator.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MultiBoxDialog extends DialogWrapper implements ListCellRenderer<String>, ChangeListener, ListSelectionListener {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField tvInput;
    private JList jlItems;
    private JCheckBox cbForAll;
    private JTextArea result;
    private JLabel lbMsg;
    private JScrollPane jpSroll;
    private JPanel jpInput;
    private JPanel buttomContent;

    private boolean multi, inputText, associateInputAndItem, check,inputAble = true;
    private String inputTxt, hint, checkTxt, msg;
    private List<String> items = new ArrayList<>();
    private List<String> selectItems = new ArrayList<>();
    private Listener listener;


    public MultiBoxDialog setMode(boolean multi, boolean inputText, boolean associateInputAndItem) {
        this.multi = multi;
        this.inputText = inputText;
        this.associateInputAndItem = associateInputAndItem;
        return this;
    }

    public MultiBoxDialog setInput(String txt) {
        this.inputTxt = txt;
        return this;
    }

    public MultiBoxDialog setInputAble(boolean inputAble) {
        this.inputAble = inputAble;
        return this;
    }
    public MultiBoxDialog setItems(Collection<String> items) {
        if (items != null) {
            this.items.clear();
            this.items.addAll(items);
        }
        return this;
    }

    public MultiBoxDialog setHint(String hint) {
        this.hint = hint;
        return this;
    }

    public MultiBoxDialog setCheck(boolean check, String checkTxt) {
        this.check = check;
        this.checkTxt = checkTxt;
        return this;
    }

    public MultiBoxDialog setListener(Listener listener) {
        this.listener = listener;
        return this;
    }

    public MultiBoxDialog setMsg(String title, String msg) {
        setTitle(title);
        this.msg = msg;
        return this;
    }

    private MultiBoxDialog(@Nullable Project project) {
        super(project, true);
//        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
//        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
//        addWindowListener(new WindowAdapter() {
//            public void windowClosing(WindowEvent e) {
//                onCancel();
//            }
//        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        // add your code here
        if (listener != null) listener.onOk(tvInput.getText(), selectItems, cbForAll.isSelected());
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        if (listener != null) listener.onCancel();
        dispose();

    }

    @Override
    public void show() {
        init();
        initView();
        super.show();
        int exitCode = getExitCode();
        if (exitCode == 0) {
            if (listener != null) listener.onOk(tvInput.getText(), selectItems, cbForAll.isSelected());
        } else {
            if (listener != null) listener.onCancel();
        }
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    private void initView() {
        buttomContent.setVisible(false);
        jpInput.setVisible(inputText);
        tvInput.setText(inputTxt);
        tvInput.setEnabled(inputAble);
        cbForAll.setVisible(multi);
        cbForAll.setSelected(check);
        lbMsg.setText(msg);

        cbForAll.addChangeListener(this);
        result.setText(hint);

        if (multi && check) selectItems.addAll(items);
        jlItems.setCellRenderer(this);
        jlItems.setModel(new StringModel(items));
        jlItems.addListSelectionListener(this);
    }

    public static final MultiBoxDialog builder(Project project) {
        return new MultiBoxDialog(project);
    }

    @Override
    public Component getListCellRendererComponent(JList jList, String value, int i, boolean b, boolean b1) {
        boolean select = selectItems.contains(value);

        JCheckBox jCheckBox = new JCheckBox();
        jCheckBox.setText(value);
        jCheckBox.setSelected(select);
        jCheckBox.setEnabled(select);

        return jCheckBox;
    }

    @Override
    public void stateChanged(ChangeEvent changeEvent) {
        boolean all = cbForAll.isSelected();
        selectItems.clear();
        if (all) selectItems.addAll(items);

        jlItems.setModel(new StringModel(items));
        result.setText(selectItems.isEmpty() ? hint : selectItems.toString());
    }

    @Override
    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        String value = (String) jlItems.getSelectedValue();
        if (listSelectionEvent.getValueIsAdjusting() || value == null) return;

        if (!selectItems.remove(value)) {
            if (!multi) selectItems.clear();
            selectItems.add(value);
        }
        if (associateInputAndItem) tvInput.setText(value);
        jlItems.clearSelection();

        result.setText(selectItems.isEmpty() ? hint : selectItems.toString());
    }

    private void createUIComponents() {

    }

    public interface Listener {
        void onOk(String input, List<String> items, boolean check);

        void onCancel();
    }
}
