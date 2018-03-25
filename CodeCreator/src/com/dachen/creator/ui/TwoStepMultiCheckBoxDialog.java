package com.dachen.creator.ui;

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

public class TwoStepMultiCheckBoxDialog extends DialogWrapper implements ListCellRenderer<String>, ListSelectionListener {
    JPanel jComponent;
    private final static int WIDTH = 400;
    private JPanel topContent;
    private JList checkBoxs;
    private JCheckBox stepTwo;
    private JScrollPane centerPanel;
    private JLabel topMsg;
    private JTextArea selectArea;

    private List<String> datas = new ArrayList<>();

    private List<String> selectItems = new ArrayList<>();

    public TwoStepMultiCheckBoxDialog(@Nullable Project project) {
        super(project, true);
        init();
        setTitle("啊哈哈哈");
        checkBoxs.setCellRenderer(this);
        stepTwo.addChangeListener(changeEvent -> {
//            centerPanel.setSize(-1, stepTwo.isSelected() ? 20*datas.size() : 0);
            centerPanel.setVisible(stepTwo.isSelected());
            selectArea.setVisible(stepTwo.isSelected());
            updateSize();
        });
        setLocation(0,0);
        checkBoxs.addListSelectionListener(this);
    }

    protected void updateSize(){
        setSize(WIDTH,60+(stepTwo.isSelected()?Math.min(360,datas.size()*20):0));
    }
    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        updateSize();
        return jComponent;
    }

    @Override
    public void show() {
        super.show();
        updateSize();
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
        TwoStepMultiCheckBoxDialog checkBoxDialog = new TwoStepMultiCheckBoxDialog(project);
        checkBoxDialog.setTitle(title);
        checkBoxDialog.topMsg.setText(msg);
        checkBoxDialog.stepTwo.setVisible(false);
        checkBoxDialog.stepTwo.setSize(0, 0);
        checkBoxDialog.datas.addAll(options);

        if(defaultItem>=0) checkBoxDialog.selectItems.add(options.get(defaultItem));
        else if(defaultItem == -1) checkBoxDialog.selectItems.addAll(options);
        checkBoxDialog.checkBoxs.setModel(new StringModel(checkBoxDialog.datas));
//        checkBoxDialog.selectArea.setText(checkBoxDialog.selectItems.toString());
        checkBoxDialog.show();
        if(checkBoxDialog.getExitCode() == 0) return new Pair<>(checkBoxDialog.stepTwo.isSelected(),checkBoxDialog.selectItems);
        else return null;
    }

    public static final Pair<Boolean, List<String>> showTwoStepSelect(@Nullable Project project, String title, String msg, List<String> options, int defaultItem, String checkTxt, boolean check) {
        if(options == null) options = new ArrayList<>();

        TwoStepMultiCheckBoxDialog checkBoxDialog = new TwoStepMultiCheckBoxDialog(project);
        checkBoxDialog.setTitle(title);
        checkBoxDialog.topMsg.setText(msg);
        checkBoxDialog.stepTwo.setText(checkTxt);
        checkBoxDialog.stepTwo.setSelected(check);
        checkBoxDialog.datas.addAll(options);

        if(defaultItem>=0) checkBoxDialog.selectItems.add(options.get(defaultItem));
        else if(defaultItem == -1) checkBoxDialog.selectItems.addAll(options);


        checkBoxDialog.selectArea.setText(checkBoxDialog.selectItems.toString());
        checkBoxDialog.checkBoxs.setModel(new StringModel(checkBoxDialog.datas));
        if (!check) {
            checkBoxDialog.centerPanel.setVisible(false);
        }
        checkBoxDialog.show();
        if(checkBoxDialog.getExitCode() == 0) return new Pair<>(checkBoxDialog.stepTwo.isSelected(),checkBoxDialog.selectItems);
        else return null;
    }

    @Override
    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        String value = (String) checkBoxs.getSelectedValue();
        if(listSelectionEvent.getValueIsAdjusting()||value == null) return;


        checkBoxs.clearSelection();

        if (!selectItems.remove(value)) {
            selectItems.add(value);
        }

        selectArea.setText(selectItems.toString());
    }
}
