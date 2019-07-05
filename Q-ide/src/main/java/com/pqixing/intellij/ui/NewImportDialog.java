package com.pqixing.intellij.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.project.Project;
import com.pqixing.intellij.adapter.JListInfo;
import com.pqixing.intellij.adapter.JListSelectAdapter;
import com.pqixing.intellij.utils.GradleUtils;
import com.pqixing.intellij.utils.UiUtils;
import com.pqixing.tools.PropertiesUtils;
import com.pqixing.tools.TextUtils;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.*;

public class NewImportDialog extends BaseJDialog {
    public static final String BING_KEY = "syncRoot";
    public static final String IMPORT_KEY = "IMPORT";
    public static final String VCS_KEY = "vcs";
    private static final String CODEROOTS_KEY = "codeRoots";
    private JPanel contentPane;
    private JButton buttonOK;
    private JComboBox<String> cbBranchs;
    private JCheckBox cbBind;
    private JComboBox tvCodeRoot;
    private JButton btnProjectXml;
    private JTextField tvImport;
    private JComboBox cbImportModel;
    private JComboBox cbDpModel;
    private JList JlNotSelect;
    private JList jlSelect;
    private JButton btnConfig;
    private JCheckBox cbVcs;
    private JCheckBox cbMore;
    private JPanel jpMore;
    private JComboBox<String> cbLoadBranch;

    //已选中导入的工程
    private List<String> imports;
    private Runnable onOk;
    Project project;
    private boolean syncBranch;
    private ImportSelectAdapter allAdapter;
    private JListSelectAdapter selectAdapter;
    private Properties properties;
    private final String TAG = "-------------------";

    public NewImportDialog(@NotNull Project project, @NotNull List<String> imports, @NotNull List<JListInfo> allInfos, @NotNull List<String> branchs, String dpModel, String codeRoot) {
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
        properties = PropertiesUtils.INSTANCE.readProperties(new File(project.getBasePath(), UiUtils.INSTANCE.getIDE_PROPERTIES()));
        syncBranch = properties.getProperty(BING_KEY, "N").equals("Y");
        cbVcs.setSelected("Y".equals(properties.getProperty(VCS_KEY, "Y")));
        initDpModel(dpModel);
        initCodeRoot(branchs.get(0), codeRoot);
        initBranchs(branchs);
        initJList(imports, allInfos);
        initImportAction();
        initMore();
    }

    private void initMore() {
        cbMore.addActionListener(actionEvent -> {
            jpMore.setVisible(cbMore.isSelected());
        });
        jpMore.setVisible(cbMore.isSelected());
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

    private long lastLoadBranch = 0L;

    private void loadBranchModules(String branch) {
        long taskId = System.currentTimeMillis();
        if (taskId - lastLoadBranch < 500) return;
        lastLoadBranch = taskId;
        Map<String, String> envs = new HashMap<>(GradleUtils.INSTANCE.getDefEnvs());
        envs.put("taskBranch", branch);
        GradleUtils.INSTANCE.runTask(project, Arrays.asList(":LoadAllBranchModule"), ProgressExecutionMode.IN_BACKGROUND_ASYNC, false, taskId + "", envs, (s, l) -> {
            if (!s) return;
            String[] strings = l.replace("#", ",").split(",");
            if (strings.length > 0) {
                imports.clear();
                for (int i = 0; i < strings.length; i++) {
                    if (!strings[i].isEmpty()) imports.add(strings[i]);
                }
                updateImports();
            }
        });
    }

    public String getSelectBranch() {
        return cbBranchs.getSelectedItem().toString();
    }


    private void initJList(List<String> imports, List<JListInfo> allInfos) {
        //模拟选中,不然列表数据会异常
        for (int i = 0; i < allInfos.size(); i++) imports.add(TAG);
        selectAdapter = new JListSelectAdapter(jlSelect, false);
        allAdapter = new ImportSelectAdapter(JlNotSelect, allInfos, imports);
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
                if (!imports.contains(info.getTitle())) {
                    imports.add(0, info.getTitle());
                }
            }
            updateImports();
            return true;
        });
        updateImports();
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            while (imports.remove(TAG)) {
            }
            updateImports();
        }
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
    }

    private void initCodeRoot(String branch, String codeRoot) {
        String root = syncBranch ? "../" + branch : codeRoot;
        cbBind.setSelected(syncBranch);
//        tvCodeRoot.setText(root);
        for (String s : str2List(properties.getProperty(CODEROOTS_KEY, ""))) {
            tvCodeRoot.addItem(s);
        }
        tvCodeRoot.setSelectedItem(root);
        tvCodeRoot.setEnabled(!syncBranch);
        cbBind.addItemListener(changeEvent -> {
            syncBranch = cbBind.isSelected();
            tvCodeRoot.setEnabled(!syncBranch);
            tvCodeRoot.setSelectedItem(syncBranch ? "../" + cbBranchs.getSelectedItem().toString() : codeRoot);
        });
    }

    private void initBranchs(List<String> branchs) {
        cbLoadBranch.addItem("");
        for (String b : branchs) {
            cbBranchs.addItem(b);
            cbLoadBranch.addItem(b);
        }
        cbBranchs.addItemListener(e -> {
            if (syncBranch) {
                tvCodeRoot.setSelectedItem("../" + getSelectBranch());
            }
            List<String> list = str2List(properties.getProperty(IMPORT_KEY + TextUtils.INSTANCE.numOrLetter(getSelectBranch())));
            if (!list.isEmpty()) {
                imports.clear();
                imports.addAll(list);
                updateImports();
            }
        });
        cbLoadBranch.addItemListener(itemEvent -> {
            loadBranchModules(cbLoadBranch.getSelectedItem().toString().trim());
        });
    }

    private void initImportAction() {
        tvImport.addKeyListener(new KeyListener() {
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
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {//如果敲下的是回车键,清空当前输入内容,并且自动选择待选框的文本
                    JListInfo find = allAdapter.filterDatas(filterKey);
                    if (find != null) {
                        String importKey = key.replace(filterKey, "") + find.getTitle();
                        if (!imports.contains(importKey)) {
                            imports.add(0, importKey);
                            updateImports();
                        }
                        find.setSelect(false);
                    }
                    allAdapter.filterDatas("");
                    tvImport.setText("");
                    return;
                } else allAdapter.filterDatas(filterKey);
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

        String importKey = IMPORT_KEY + TextUtils.INSTANCE.numOrLetter(getSelectBranch());
        String newImport = list2Str(getImports());
        String oldImports = properties.getProperty(importKey);
        properties.setProperty(importKey, newImport);

        List<String> codeRoots = str2List(properties.getProperty(CODEROOTS_KEY, ""));
        String rootStr = getCodeRootStr();
        boolean exists = codeRoots.remove(rootStr);
        codeRoots.add(0, rootStr);
        while (codeRoots.size() > 6) {
            codeRoots.remove(codeRoots.size() - 1);
        }
        properties.setProperty(CODEROOTS_KEY, list2Str(codeRoots));

        if (!newVcs.equals(oldVcs) || !newKey.equals(oldBind) || !newImport.equals(oldImports) || !exists)
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

    public String getImportModel() {
        return cbImportModel.getSelectedItem().toString();
    }

    public String getCodeRootStr() {
        String item = tvCodeRoot.getSelectedItem().toString().trim();
        return item.isEmpty() ? "../CodeSrc" : item;
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
        private List<String> imports;

        public ImportSelectAdapter(JList jList, List<JListInfo> datas, List<String> imports) {
            super(jList, false);
            setDatas(datas);
            this.imports = imports;
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

        public JListInfo filterDatas(String key) {
            this.filterKey = key == null ? "" : key.trim();
            List<JListInfo> datas = new LinkedList<>();
            datas.clear();
            JListInfo select = null;
            int lastScore = 0;
            if (!filterKey.isEmpty()) for (JListInfo p : sources) {
                p.setSelect(false);
                if (!p.toString().toLowerCase().contains(filterKey.toLowerCase()))
                    continue;
                int s = findLikeScore(p, filterKey);
                if (s > lastScore) {
                    lastScore = s;
                    select = p;
                    datas.add(0, p);
                } else datas.add(p);
            }
            else datas.addAll(sources);
            super.setDatas(datas);
            if (select != null) select.setSelect(true);
            return select;
        }

        private int findLikeScore(JListInfo p, String key) {
            String title = p.getTitle();
            int match = 100 - title.length();
            if (title.contains(key)) {
                return 10000 * match;
            }
            if (p.getTitle().toLowerCase().contains(key.toLowerCase())) {
                return 100 * match;
            }
            return match;
        }

        public void addDatas(List<JListInfo> datas) {
            addDatas(datas, filterKey);
        }

        public void addDatas(List<JListInfo> datas, String filterKey) {
            if (datas != null) {
                this.sources.addAll(0, datas);
                filterDatas(filterKey);
            }
        }

        public void removeDatas(List<JListInfo> datas) {
            removeDatas(datas, filterKey);
        }

        public void removeDatas(List<JListInfo> datas, String filterKey) {
            if (datas != null) {
                sources.removeAll(datas);
                filterDatas(filterKey);
            }
        }
    }

}
