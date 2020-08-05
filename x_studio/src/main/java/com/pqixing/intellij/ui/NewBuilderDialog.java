package com.pqixing.intellij.ui;

import com.intellij.openapi.project.Project;
import com.pqixing.intellij.actions.BuildParam;
import com.pqixing.intellij.adapter.JListInfo;
import com.pqixing.intellij.adapter.JListSelectAdapter;
import com.pqixing.intellij.utils.UiUtils;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NewBuilderDialog extends BaseJDialog {
    private JPanel contentPane;
    private JButton btnOK;
    public JButton btnInstall;
    private JCheckBox keepCheckBox;
    private JList jlHistory;
    private JComboBox cbModule;
    private JComboBox cbType;
    private JComboBox cbDepend;
    public JButton btnVersion;
    public JTextField tvVersion;
    private JTextField tvInstall;
    private JTextField tvLog;
    private Runnable onOk;
    private BuildParam param;
    private List<BuildParam> historys;
    private JListSelectAdapter adapter;
    ActionListener e = e -> resetBtn(false);
    ChangeListener c = e -> resetBtn(false);

    public void setOnOk(Runnable onOk) {
        this.onOk = onOk;
    }

    public NewBuilderDialog(Project project, BuildParam param, List<String> modules, List<BuildParam> historys) {
        super(project);
        setContentPane(contentPane);
        setTitle("Builder");
        setModal(false);
        getRootPane().setDefaultButton(btnOK);

        this.param = param;
        this.historys = historys;
        btnOK.addActionListener(e -> onOK());
        for (String m : modules) cbModule.addItem(m);

        resetBtn(true);

        adapter = new JListSelectAdapter(jlHistory, false);
        adapter.setSelectListener((jList, adapter, items) -> {
            if (!items.isEmpty()) {
                NewBuilderDialog.this.param = (BuildParam) items.get(items.size() - 1).getData();
                resetBtn(true);
            }
            return true;
        });
        mockData();
    }

    private void mockData() {
        List<JListInfo> datas = new ArrayList<>(50);
        JListInfo info = new JListInfo();
        info.setTitle("------------------");
        info.setLog("------------------");
        for (int i = 0; i < 80; i++) datas.add(info);
        adapter.setDatas(datas);
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) UiUtils.INSTANCE.addTask(100, this::refreshHistory);
    }

    public void resetBtn(boolean init) {
        if (init) {
            cbDepend.removeActionListener(e);
            cbModule.removeActionListener(e);
            cbType.removeActionListener(e);
            keepCheckBox.removeChangeListener(c);

            cbDepend.setSelectedItem(param.getDepend());
            cbModule.setSelectedItem(param.getModule());
            cbType.setSelectedItem(param.getBuildType());
            keepCheckBox.setSelected("Y".equals(param.getKeep()));
            tvVersion.setText(param.getVersion());
            tvInstall.setText(param.getInstall());

            cbDepend.addActionListener(e);
            cbModule.addActionListener(e);
            cbType.addActionListener(e);
            keepCheckBox.addChangeListener(c);
        } else {
            param.setModule(cbModule.getSelectedItem().toString());
            param.setDepend(cbDepend.getSelectedItem().toString());
            param.setBuildType(cbType.getSelectedItem().toString());
            param.setKeep(keepCheckBox.isSelected() ? "Y" : "N");
            param.setVersion(tvVersion.getText());
            param.setInstall(tvInstall.getText());
        }
        tvLog.setText(param.getResult());
        tvLog.setVisible(!param.getResult().isEmpty());
        btnInstall.setVisible(!param.getResult().isEmpty());
    }

    public void refreshHistory() {
        List<JListInfo> datas = new ArrayList<>(historys.size());
        int i = 0;
        for (BuildParam history : historys) {
            String result = history.getResult();
            JListInfo info = new JListInfo();
            info.setLog(history.getTime());
            info.setTitle((i++) + "," + history.getModule() + "-" + history.getBuildType());
            info.setStaue(result.isEmpty() ? 0 : new File(result).exists() ? 1 : 3);
            info.setData(history);
            datas.add(info);
        }
        adapter.setDatas(datas);
    }

    public BuildParam getParam() {
        return param;
    }

    private void onOK() {
        if (onOk != null) onOk.run();
        else dispose();
    }
}
