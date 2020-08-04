package com.pqixing.intellij.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.pqixing.intellij.adapter.JListInfo;
import com.pqixing.intellij.adapter.JListSelectAdapter;
import com.pqixing.intellij.utils.UiUtils;
import com.pqixing.tools.PropertiesUtils;
import com.pqixing.tools.TextUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.event.*;
import java.io.File;
import java.util.*;

public class NewImportDialog extends BaseJDialog {
    public static final String BING_KEY = "syncRoot";
    public static final String IMPORT_KEY = "IMPORT";
    public static final String VCS_KEY = "vcs";
    public static final String FORMAT_KEY = "format";
    private JPanel contentPane;
    private JButton buttonOK;
    private JCheckBox cbBind;
    private JComboBox<String> tvCodeRoot;
    private JButton btnProjectXml;
    private JTextField tvImport;
    private JComboBox cbDpModel;
    private JList JlNotSelect;
    private JList jlSelect;
    private JButton btnConfig;
    private JCheckBox cbVcs;
    private JPanel jpSelect;
    private JScrollPane jpOthers;
    private JCheckBox cbFormat;

    //已选中导入的工程
    private List<String> imports;
    private List<String> branchs;
    private Runnable onOk;
    Project project;
    private boolean syncBranch;
    private ImportSelectAdapter allAdapter;
    private JListSelectAdapter selectAdapter;
    private Properties properties;
    private final String TAG = "-------------------";

    public NewImportDialog(@NotNull Project project, @NotNull List<String> imports, @NotNull List<JListInfo> allInfos, @NotNull List<String> branchs, String dpModel, String codeRoot) {
        super(project);
        setContentPane(contentPane);
        setModal(false);


//        getRootPane().setDefaultButton(buttonOK);
        setTitle("Import");
        buttonOK.addActionListener(e -> onOK());
        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
//        UiUtils.centerDialog(this);
        this.project = project;
        this.imports = imports;
        this.branchs = branchs;
        properties = PropertiesUtils.INSTANCE.readProperties(new File(project.getBasePath(), UiUtils.INSTANCE.getIDE_PROPERTIES()));
        syncBranch = properties.getProperty(BING_KEY, "N").equals("N");
        cbVcs.setSelected("Y".equals(properties.getProperty(VCS_KEY, "Y")));
        cbFormat.setSelected("Y".equals(properties.getProperty(FORMAT_KEY, "N")));

        initDpModel(dpModel);
        cbBind.setSelected(syncBranch);
        cbBind.addActionListener(c -> initCodeRoot(branchs, codeRoot));
        initCodeRoot(branchs, codeRoot);
        initJList(imports, allInfos);
        initImportAction();
    }

    public boolean format() {
        return cbFormat.isSelected();
    }

    public boolean syncVcs() {
        return cbVcs.isSelected();
    }

    private void initDpModel(String dpModel) {
        for (int i = 0; i < cbDpModel.getItemCount(); i++) {
            if (dpModel.equals(cbDpModel.getItemAt(i))) {
                cbDpModel.setSelectedIndex(i);
                break;
            }
        }

    }


    public JButton getBtnProjectXml() {
        return btnProjectXml;
    }

    public JButton getBtnConfig() {
        return btnConfig;
    }

    public String getSelectBranch() {
        return syncBranch ? getCodeRootStr().substring(3) : branchs.get(0);
    }


    private void initJList(List<String> imports, List<JListInfo> all) {
        //模拟选中,不然列表数据会异常
        for (int i = 0; i < all.size(); i++) imports.add(TAG);
        selectAdapter = new JListSelectAdapter(jlSelect, false);
        allAdapter = new ImportSelectAdapter(JlNotSelect, all);
        selectAdapter.setSelectListener((jList, adapter, items) -> {
            for (JListInfo info : items) {
                imports.remove(info.getTitle());
            }
            updateImports();
            return true;
        });
        allAdapter.setSelectListener((jList, adapter, items) -> {
            for (JListInfo info : items) {
                info.setSelect(false);
                addToImport(info.getTitle());
            }
            updateImports();
            return true;
        });
        repaintBorder(jpOthers, "Others", all.size());
        updateImports();
    }

    private void addToImport(String title) {
        addToImport(title, title.substring(0, title.indexOf("#") + 1));
    }

    private void addToImport(String title, String model) {
        title = model + title.substring(title.lastIndexOf("/") + 1);
        if (!imports.contains(title)) {
            imports.add(0, title);
        }
    }

    @Override
    public void setVisible(boolean b) {
        if (b) UiUtils.INSTANCE.addTask(100, () -> {
            boolean remove = false;
            while (imports.remove(TAG)) remove = true;
            if (remove) updateImports();
        });
        super.setVisible(b);
    }

    public void setOnOk(Runnable onOk) {
        this.onOk = onOk;
    }

    private void updateImports() {
        ArrayList<JListInfo> infos = new ArrayList<>(imports.size() * 2);
        for (String i : imports) {
            infos.add(new JListInfo(i, "", 0, false));
        }
        selectAdapter.setDatas(infos);
        repaintBorder(jpSelect, "Select", infos.size());
    }

    void repaintBorder(JComponent jpSelect, String key, int size) {
        ((TitledBorder) jpSelect.getBorder()).setTitle(key + " (" + size + ")");
        jpSelect.revalidate();
        jpSelect.repaint();
    }


    ItemListener codeRootListener = e -> syncImportByBranch();

    private void initCodeRoot(List<String> branch, String codeRoot) {
        syncBranch = cbBind.isSelected();
        tvCodeRoot.removeItemListener(codeRootListener);
        if (tvCodeRoot.getItemCount() > 0) tvCodeRoot.removeAllItems();
        if (!syncBranch) tvCodeRoot.addItem(codeRoot);
        else for (String s : branch) tvCodeRoot.addItem("../" + s);
        tvCodeRoot.setSelectedIndex(0);
        tvCodeRoot.addItemListener(codeRootListener);
        tvCodeRoot.setEditable(!syncBranch);
    }

    private void syncImportByBranch() {
        if (!syncBranch) return;
        List<String> list = str2List(properties.getProperty(IMPORT_KEY + TextUtils.INSTANCE.numOrLetter(getCodeRootStr())));
        if (!list.isEmpty()) {
            imports.clear();
            imports.addAll(list);
            updateImports();
        }
    }

    private void initImportAction() {
        tvImport.addKeyListener(new KeyListener() {
            int lastKeyCode = 0;

            @Override
            public void keyTyped(KeyEvent keyEvent) {
            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                String key = tvImport.getText().trim();
                String filterKey = key.replaceAll(".*#", "");
                int keyCode = keyEvent.getKeyCode();
                if (keyCode == KeyEvent.VK_ENTER) {//如果敲下的是回车键,清空当前输入内容,并且自动选择待选框的文本
                    if (lastKeyCode == KeyEvent.VK_CONTROL) {
                        onOK();
                    } else {
                        JListInfo find = null;
                        for (JListInfo i : allAdapter.getDatas()) {
                            if (i.getSelect()) {
                                find = i;
                                break;
                            }
                        }
                        if (find != null) {
                            String importKey = key.replace(filterKey, "") + find.getTitle();
                            addToImport(importKey);
                            updateImports();
                            find.setSelect(false);
                        }
                        allAdapter.filterDatas("");
                        tvImport.setText("");
                    }
                } else if (keyCode == KeyEvent.VK_DOWN) {
                    Iterator<JListInfo> iterator = allAdapter.getDatas().iterator();
                    JListInfo last = null;
                    while (iterator.hasNext()) {
                        JListInfo next = iterator.next();
                        if (last == null || !last.getSelect()) {
                            last = next;
                            continue;
                        }
                        last.setSelect(false);
                        next.setSelect(true);
                        allAdapter.updateUI();
                        break;
                    }
                } else if (keyCode == KeyEvent.VK_UP) {
                    Iterator<JListInfo> iterator = allAdapter.getDatas().iterator();
                    JListInfo last = null;
                    while (iterator.hasNext()) {
                        JListInfo next = iterator.next();
                        if (!next.getSelect()) {
                            last = next;
                            continue;
                        }
                        if (last != null) {
                            last.setSelect(true);
                            next.setSelect(false);
                            allAdapter.updateUI();
                        }
                        break;
                    }
                } else allAdapter.filterDatas(filterKey);

                repaintBorder(jpOthers, "Others", allAdapter.getDatas().

                        size());
                lastKeyCode = keyCode;
            }
        });
    }

    private void onOK() {
        saveBindKey();
        // add your code here
        dispose();

        if (onOk != null) onOk.run();
    }

    /**
     * 保存切换框的值
     */
    private void saveBindKey() {
        String newKey = syncBranch ? "Y" : "N";
        String oldBind = properties.getProperty(BING_KEY);
        properties.setProperty(BING_KEY, newKey);

        String newVcs = syncVcs() ? "Y" : "N";
        String oldVcs = properties.getProperty(VCS_KEY);
        properties.setProperty(VCS_KEY, newVcs);

        String newFormat = format() ? "Y" : "N";
        String oldFormat = properties.getProperty(FORMAT_KEY);
        properties.setProperty(FORMAT_KEY, newFormat);

        String importKey = IMPORT_KEY + TextUtils.INSTANCE.numOrLetter(getSelectBranch());
        String newImport = list2Str(getImports());
        String oldImports = properties.getProperty(importKey);
        properties.setProperty(importKey, newImport);

        //更新监听
        UiUtils.INSTANCE.getFtModules().put(project.getBasePath(), newFormat.equals("Y"));

        if (!newFormat.equals(oldFormat) || !newVcs.equals(oldVcs) || !newKey.equals(oldBind) || !newImport.equals(oldImports))
            ApplicationManager.getApplication().runWriteAction(() -> PropertiesUtils.INSTANCE.writeProperties(new File(project.getBasePath(), UiUtils.INSTANCE.getIDE_PROPERTIES()), properties));
    }

    private List<String> str2List(String str) {
        List<String> ls = new ArrayList<>();
        if (str == null) return ls;
        for (String s : str.split(",")) {
            if (s != null && !s.isEmpty()) ls.add(s);
        }
        return ls;
    }

    private String list2Str(List<String> ls) {
        StringBuilder sb = new StringBuilder();
        for (String s : ls) {
            sb.append(s).append(",");
        }
        return sb.toString();
    }

    public String getCodeRootStr() {
        Object item = tvCodeRoot.getSelectedItem();
        return item == null ? "" : item.toString().trim();
    }

    public List<String> getImports() {
        return imports;
    }

    public String getDpModel() {
        return cbDpModel.getSelectedItem().toString();
    }


    public static class ImportSelectAdapter extends JListSelectAdapter {
        List<JListInfo> sources = new ArrayList<>();
        private String filterKey;

        public ImportSelectAdapter(JList jList, List<JListInfo> datas) {
            super(jList, false);
            setDatas(datas);
        }

        @Override
        public boolean equals(Object o) {
            return false;
        }

        @Override
        public void setDatas(List<JListInfo> datas) {
            this.sources.clear();
            if (datas != null) {
                this.sources.addAll(0, datas);
                filterDatas(filterKey);
            }
        }

        public JListInfo filterDatas(String key) {
            this.filterKey = key == null ? "" : key.trim();
            List<JListInfo> datas = new LinkedList<>();
            JListInfo select = null;
            if (!filterKey.isEmpty()) {
                loadDataByScope(datas);
            } else datas.addAll(sources);
            super.setDatas(datas);
            if (!datas.isEmpty()) {
                select = datas.get(0);
                select.setSelect(true);
            }
            return select;
        }

        private void loadDataByScope(List<JListInfo> datas) {
            for (JListInfo p : sources) {
                p.setSelect(false);
                int s = findLikeScore(p, filterKey);
                if (s == -1) continue;
                p.setData(s);
                datas.add(p);
            }
            if (!datas.isEmpty()) Collections.sort(datas, (t0, t1) -> {
                String t0s = t0.getData() == null ? "0" : t0.getData().toString();
                String t1s = t1.getData() == null ? "0" : t1.getData().toString();
                return t1s.compareTo(t0s);
            });
        }

        private int findLikeScore(JListInfo p, String key) {
            String title = p.getTitle();
            int match = 100 - title.length();
            if (title.contains(key)) {
                return 10000 * match;
            }
            if (title.toLowerCase().contains(key.toLowerCase())) {
                return 100 * match;
            }

            if (checkMath(title.toLowerCase(), key.toLowerCase())) {
                return 10 * match;
            }
            StringBuilder mb = new StringBuilder();
            for (char k : key.toCharArray()) mb.append(".*").append(k);

            if (title.matches(mb.toString())) {
            }
            if (p.getLog().toLowerCase().contains(key.toLowerCase()))
                return match;
            return -1;
        }

        private boolean checkMath(String title, String key) {
            char[] chars = title.toCharArray();
            int length = chars.length;
            int queryIndex = -1;

            outer:
            for (char k : key.toCharArray()) {
                while (++queryIndex < length) if (k == chars[queryIndex]) continue outer;
                return false;
            }
            return true;
        }

    }

}
