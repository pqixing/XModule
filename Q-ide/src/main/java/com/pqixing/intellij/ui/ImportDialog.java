package com.pqixing.intellij.ui;

import com.pqixing.intellij.adapter.JListInfo;
import com.pqixing.intellij.adapter.JListSelectAdapter;
import com.pqixing.intellij.utils.UiUtils;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

public class ImportDialog extends JDialog {
    private JPanel rootPanel;
    private JButton btnOK;
    private JButton btnCancel;
    private JTextField tfImport;
    private JComboBox dpModel;
    private JList jlSelect;
    private JList jlOther;
    private JComboBox importModel;
    public JTextField specificInclude;
    public JTextField codeRoot;
    private JLabel inputHint;
    public JButton btnConfig;
    public JButton btnXml;
    private HashSet<JListInfo> allInfos = new HashSet<>();

    public ImportSelectAdapter selctModel;
    public ImportSelectAdapter otherModel;
    private Runnable onOk;

    public ImportDialog() {
        this(Collections.emptyList(), Collections.emptyList(), "", "../CodeSrc", "mavenOnly");
    }

    public ImportDialog(List<JListInfo> select, List<JListInfo> other, String more, String codeRootStr, String dependentModel) {
        setContentPane(rootPanel);
        setModal(false);
//        getRootPane().setDefaultButton(btnOK);
        setTitle("Import Module");

        btnOK.addActionListener(e -> onOK());

        btnCancel.addActionListener(e -> dispose());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        // call onCancel() on ESCAPE
        rootPanel.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        selctModel = new ImportSelectAdapter(jlSelect, select);
        otherModel = new ImportSelectAdapter(jlOther, other);
        setJListModel(jlSelect, selctModel, otherModel);
        setJListModel(jlOther, otherModel, selctModel);

        for (JListInfo i : select) allInfos.add(i);
        for (JListInfo i : other) allInfos.add(i);


        tfImport.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                String key = tfImport.getText().trim();
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {//如果敲下的是回车键,清空当前输入内容,并且自动选择待选框的文本
                    String prepareAdd = inputHint.getText().trim();
                    JListInfo info = null;
                    for (JListInfo i : allInfos) {
                        if (i.getTitle().equals(prepareAdd)) {
                            info = i;
                            break;
                        }
                    }
                    if (info != null) {//如果该模块存在,则快速添加
                        if (selctModel.sources.contains(info)) {
                            selctModel.removeDatas(Arrays.asList(info),"");
                            otherModel.addDatas(Arrays.asList(info),"");
                        } else {
                            selctModel.addDatas(Arrays.asList(info),"");
                            otherModel.removeDatas(Arrays.asList(info),"");
                        }
                    }

                    inputHint.setText("");
                    tfImport.setText("");
                    return;
                }
                String result = null;
                for (JListInfo i : allInfos) {
                    String title = i.getTitle();
                    if (!title.toLowerCase().contains(key.toLowerCase())) continue;
                    if (key.equals(title) || title.length() < (result == null ? 100 : result.length())) {
                        result = title;
                    }
                }
                if (result != null) inputHint.setText(result);
                selctModel.filterDatas(key);
                otherModel.filterDatas(key);
            }
        });
        dpModel.addItem(dependentModel);
        List<String> asList = Arrays.asList("mavenOnly", "mavenFirst", "localFirst", "localOnly");
        for (String s : asList) {
            if (s.equals(dependentModel)) continue;
            dpModel.addItem(s);
        }
        specificInclude.setText(more);
        codeRoot.setText(codeRootStr);

        UiUtils.centerDialog(this);
    }

    private void setJListModel(JList jList, ImportSelectAdapter model, ImportSelectAdapter targetModel) {
        jList.setModel(model);
        model.setSelectListener((jList1, adapter, items) -> {
            targetModel.addDatas(items);
            model.removeDatas(items);
            return true;
        });
    }

    public String getCodeRootStr() {
        String item = codeRoot.getText().trim();
        return item.isEmpty() ? "../CodeSrc" : item;
    }

    public static class ImportSelectAdapter extends JListSelectAdapter {
        List<JListInfo> sources = new ArrayList<>();
        private String filterKey;

        public ImportSelectAdapter(JList jList, List<JListInfo> datas) {
            super(jList,false);
            setDatas(datas);
        }

        public List<JListInfo> getSelectItems() {
            return super.getDatas();
        }

        @Override
        public boolean equals(Object o) {
            return false;
        }

        @Override
        public void setDatas(List<JListInfo> datas) {
            this.sources.clear();
            addDatas(datas);
        }

        public void filterDatas(String key) {
            this.filterKey = key == null ? "" : key.trim().toLowerCase();
            List<JListInfo> datas = new LinkedList<>();
            datas.clear();
            if (!filterKey.isEmpty()) for (JListInfo p : sources) {
                if (p.toString().toLowerCase().contains(filterKey)) {
                    datas.add(p);
                }
            }
            else datas.addAll(sources);
            super.setDatas(datas);
        }

        public void addDatas(List<JListInfo> datas) {
           addDatas(datas,filterKey);
        }
        public void addDatas(List<JListInfo> datas,String filterKey) {
            if (datas != null) {
                this.sources.addAll(0, datas);
                filterDatas(filterKey);
            }
        }

        public void removeDatas(List<JListInfo> datas) {
            removeDatas(datas,filterKey);
        }
        public void removeDatas(List<JListInfo> datas,String filterKey) {
            if (datas != null) {
                sources.removeAll(datas);
                filterDatas(filterKey);
            }
        }
    }

    public String getImportModel() {
        return importModel.getSelectedItem().toString();
    }

    public String getDpModel() {
        return dpModel.getSelectedItem().toString();
    }

    public ImportDialog setOkListener(Runnable runnable) {
        this.onOk = runnable;
        return this;
    }

    protected void onOK() {
        dispose();
        if (onOk != null) onOk.run();
    }

    public static void main(String[] args) {
        ImportDialog dialog = new ImportDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
