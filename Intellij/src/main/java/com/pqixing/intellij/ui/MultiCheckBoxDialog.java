package com.pqixing.intellij.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MultiCheckBoxDialog extends DialogWrapper implements ListCellRenderer<String>, ListSelectionListener {
    JPanel jComponent;
    private final static int WIDTH = 400;
    private JPanel topContent;
    private JList cbItems;
    private JScrollPane centerContent;
    private JLabel tvMsg;
    private JTextArea tvItemSelect;
    private JTextField tvInput;
    private JCheckBox cbSelectAll;

    private List<String> datas = new ArrayList<>();

    private List<String> selectItems = new ArrayList<>();

    public MultiCheckBoxDialog(@Nullable Project project) {
        super(project, true);
        init();
        setTitle("啊哈哈哈");
        cbItems.setCellRenderer(this);
        cbSelectAll.addChangeListener(changeEvent -> {
//            centerContent.setSize(-1, cbSelectAll.isSelected() ? 20*datas.size() : 0);
            centerContent.setVisible(cbSelectAll.isSelected());
            tvItemSelect.setVisible(cbSelectAll.isSelected());
            updateSize();
        });
        setLocation(0,0);
        cbItems.addListSelectionListener(this);
    }

    protected void updateSize(){
    }
    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return jComponent;
    }

    @Override
    public Component getListCellRendererComponent(JList jList, String value, int i, boolean b, boolean b1) {
        if(getSize().width!=WIDTH) updateSize();

        boolean select = selectItems.contains(value);

        JCheckBox jCheckBox = new JCheckBox();
        jCheckBox.setText(value);
        jCheckBox.setSelected(select);
        jCheckBox.setEnabled(select);

        return jCheckBox;
    }


    public static final Pair<Boolean, List<String>> showMultiSelect(@Nullable Project project, String title, String msg, List<String> options,int defaultItem) {
        if(options == null) options = new ArrayList<>();
        MultiCheckBoxDialog checkBoxDialog = new MultiCheckBoxDialog(project);
        checkBoxDialog.setTitle(title);
        checkBoxDialog.tvMsg.setText(msg);
        checkBoxDialog.cbSelectAll.setVisible(false);
        checkBoxDialog.cbSelectAll.setSize(0, 0);
        checkBoxDialog.datas.addAll(options);

        if(defaultItem>=0) checkBoxDialog.selectItems.add(options.get(defaultItem));
        else if(defaultItem == -1) checkBoxDialog.selectItems.addAll(options);
        checkBoxDialog.cbItems.setModel(new StringModel(checkBoxDialog.datas));
        checkBoxDialog.tvItemSelect.setText(checkBoxDialog.selectItems.toString());
        checkBoxDialog.show();
        if(checkBoxDialog.getExitCode() == 0) return new Pair<>(checkBoxDialog.cbSelectAll.isSelected(),checkBoxDialog.selectItems);
        else return null;
    }

    public static final Pair<Boolean, List<String>> showTwoStepSelect(@Nullable Project project, String title, String msg, List<String> options, int defaultItem, String checkTxt, boolean check) {
        if(options == null) options = new ArrayList<>();

        MultiCheckBoxDialog checkBoxDialog = new MultiCheckBoxDialog(project);
        checkBoxDialog.setTitle(title);
        checkBoxDialog.tvMsg.setText(msg);
        checkBoxDialog.cbSelectAll.setText(checkTxt);
        checkBoxDialog.cbSelectAll.setSelected(check);
        checkBoxDialog.datas.addAll(options);

        if(defaultItem>=0) checkBoxDialog.selectItems.add(options.get(defaultItem));
        else if(defaultItem == -1) checkBoxDialog.selectItems.addAll(options);


        checkBoxDialog.tvItemSelect.setText(checkBoxDialog.selectItems.toString());
        checkBoxDialog.cbItems.setModel(new StringModel(checkBoxDialog.datas));
        if (!check) {
            checkBoxDialog.centerContent.setVisible(false);
        }
        checkBoxDialog.show();
        if(checkBoxDialog.getExitCode() == 0) return new Pair<>(checkBoxDialog.cbSelectAll.isSelected(),checkBoxDialog.selectItems);
        else return null;
    }

    @Override
    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        String value = (String) cbItems.getSelectedValue();
        if(listSelectionEvent.getValueIsAdjusting()||value == null) return;


        cbItems.clearSelection();

        if (!selectItems.remove(value)) {
            selectItems.add(value);
        }

        tvItemSelect.setText(selectItems.toString());
    }

    private void createUIComponents() {
    }
}
