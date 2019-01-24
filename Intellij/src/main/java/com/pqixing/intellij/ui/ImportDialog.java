package com.pqixing.intellij.ui;

import com.pqixing.intellij.adapter.JListInfo;
import com.pqixing.intellij.adapter.JListSelectAdapter;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
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
    public JComboBox codeRoot;

    public ImportSelectAdapter selctModel;
    public ImportSelectAdapter otherModel;
    private Runnable onOk;

    public ImportDialog() {
        this(null, null, null);
    }

    public ImportDialog(List<JListInfo> select, List<JListInfo> other, List<String> codeRoots) {
        setContentPane(rootPanel);
        setModal(false);
        getRootPane().setDefaultButton(btnOK);
        setTitle("Import Module");
        setLocation(400, 300);

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
                selctModel.filterDatas(key);
                otherModel.filterDatas(key);
            }
        });
        dpModel.addItem("dpModel");
        dpModel.addItem("mavenOnly");
        dpModel.addItem("mavenFirst");
        dpModel.addItem("localFirst");
        dpModel.addItem("localOnly");
        if (codeRoot != null) codeRoots.forEach(s -> this.codeRoot.addItem(s));
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
        Object item = codeRoot.getSelectedItem();
        return item == null || item.toString().trim().isEmpty() ? "main" : item.toString();
    }

    public static class ImportSelectAdapter extends JListSelectAdapter {
        List<JListInfo> sources = new ArrayList<>();
        private String filterKey;

        public ImportSelectAdapter(JList jList, List<JListInfo> datas) {
            super(jList);
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
            if (datas != null) {
                this.sources.addAll(0, datas);
                filterDatas(filterKey);
            }
        }

        public void removeDatas(List<JListInfo> datas) {
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
        if (onOk != null) onOk.run();
        dispose();
    }

    public static void main(String[] args) {
        ImportDialog dialog = new ImportDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
