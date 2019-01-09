package com.pqixing.intellij.ui;

import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListModel;

import kotlin.Pair;

public class ImportDialog extends JDialog {
    private JPanel rootPanel;
    private JButton btnOK;
    private JButton btnCancel;
    private JTextField tfImport;
    private JComboBox dpModel;
    private JList jlSelect;
    private JList jlOther;
    private JList jlHistory;
    private JComboBox importModel;

    public List<Pair<String, String>> select = new ArrayList<>();
    public List<Pair<String, String>> history = new ArrayList<>();
    public List<Pair<String, String>> other = new ArrayList<>();


    private Runnable onOk;

    public ImportDialog() {
        this(null, null, null);
    }

    public ImportDialog(List<Pair<String, String>> s, List<Pair<String, String>> h, List<Pair<String, String>> o) {
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

        if (select != null) this.select.addAll(s);
        if (history != null) this.history.addAll(h);
        if (other != null) this.other.addAll(o);

        SelctModel selctModel = new SelctModel(jlSelect, select);
        SelctModel historyModel = new SelctModel(jlHistory, history);
        SelctModel otherModel = new SelctModel(jlOther, other);
        setJListModel(jlSelect, selctModel, historyModel);
        setJListModel(jlHistory, historyModel, selctModel);
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
                historyModel.addFilter(key);
                otherModel.addFilter(key);
            }
        });
        dpModel.addItem("dpModel");
        dpModel.addItem("mavenOnly");
        dpModel.addItem("mavenFirst");
        dpModel.addItem("localFirst");
        dpModel.addItem("localOnly");
    }

    private ListModel setJListModel(JList list, SelctModel model, SelctModel targetModel) {
        list.setModel(model);
        list.setPreferredSize(new Dimension(list.getWidth(), model.getDatas().size() * 25));
        list.setLayoutOrientation(JList.VERTICAL);
        list.setFixedCellHeight(25);
        list.addListSelectionListener(event -> {
            int[] index = model.getIndex();
            int nowSelect = list.getSelectedIndex();
            if (index[0] == -1) {
                index[0] = nowSelect;
            }
            if (event.getValueIsAdjusting()) {
                index[1] = nowSelect;
                list.validate();
                return;
            }
            int start = Math.min(index[0], index[1]);
            int end = Math.max(index[0], index[1]);
            index[0] = -1;
            index[1] = -1;
            if (start < 0) return;
            List<Pair<String, String>> datas = model.getDatas();
            ArrayList selectItems = new ArrayList<Pair<String, String>>();
            for (int i = start; i <= end; i++) {
                selectItems.add(datas.remove(start));
            }
            targetModel.addDatas(selectItems);
            list.setPreferredSize(new Dimension(list.getWidth(), model.getDatas().size() * 25));
            list.setModel(model);
        });
        return model;
    }


    private static class SelctModel extends AbstractListModel<String> {
        List<Pair<String, String>> datas;
        int[] index = new int[]{-1, -1};
        JList jList;
        private String filterKey;
        List<Pair<String, String>> filters = new ArrayList<>();

        public int[] getIndex() {
            return index;
        }

        public SelctModel(JList jList, List<Pair<String, String>> datas) {
            this.datas = datas == null ? new ArrayList<>() : datas;
            this.jList = jList;
        }

        @Override
        public boolean equals(Object o) {
            return false;
        }

        public List<Pair<String, String>> getDatas() {
            return filterKey == null || filterKey.isEmpty() ? datas : filters;
        }

        public void setDatas(List<Pair<String, String>> datas) {
            this.datas.clear();
            addDatas(datas);

        }

        public void addFilter(String key) {
            this.filterKey = key;
            filters.clear();
            if (key != null && !key.isEmpty()) {
                for (Pair<String, String> p : datas) {
                    if (p.toString().toLowerCase().contains(key)) {
                        filters.add(p);
                    }
                }
            }
            jList.setPreferredSize(new Dimension(jList.getWidth(), getDatas().size() * 25));
            jList.setModel(this);
        }

        public void addDatas(List<Pair<String, String>> datas) {
            if (datas != null) this.datas.addAll(0, datas);
            addFilter(filterKey);
        }

        @Override
        public int getSize() {
            return getDatas().size();
        }

        @Override
        public String getElementAt(int i) {
            Pair<String, String> pair = getDatas().get(i);
            String prefix = (Math.min(index[0], index[1]) <= i && i <= Math.max(index[0], index[1])) ? "> " : "";
            return "  " + prefix + pair.getFirst() + "-" + pair.getSecond();
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
