package com.pqixing.intellij.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.pqixing.intellij.adapter.JListInfo;
import com.pqixing.intellij.adapter.JListSelectAdapter;
import com.pqixing.intellij.utils.GradleUtils;
import com.pqixing.intellij.utils.UiUtils;
import com.pqixing.tools.FileUtils;
import com.pqixing.tools.PropertiesUtils;
import com.pqixing.tools.TextUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.*;

public class NewImportDialog extends BaseJDialog {
    private static boolean moreVisible = false;
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
    private JButton loadAppButton;
    private JButton loadBranchButton;
    private JButton loadDpsButton;
    private JPanel jpSelect;
    private JScrollPane jpOthers;

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
        initLoadButtons(branchs);
        initJList(imports, allInfos);
        initImportAction();
        initMore();
    }

    private void initMore() {
        cbMore.setSelected(moreVisible);
        cbMore.addActionListener(a -> {
            moreVisible = cbMore.isSelected();
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


    /**
     * 加载指定分支的模块
     *
     * @param branch
     * @param app    加载app
     */
    private void loadBranchModules(String branch, boolean app) {
        if (branch == null) return;

        setVisible(false);
        Map<String, String> envs = new HashMap<>(GradleUtils.INSTANCE.getDefEnvs());
        envs.put("taskBranch", branch);
        GradleUtils.INSTANCE.runTask(project, Collections.singletonList(":LoadAllBranchModule")
                , ProgressExecutionMode.IN_BACKGROUND_ASYNC
                , false, System.currentTimeMillis() + "", envs, (s, l) -> {
                    if (!s) {
                        setVisible(true);
                        return;
                    }
                    imports.clear();
                    for (String string : l.split("#")[app ? 0 : 1].split(",")) {
                        if (!string.isEmpty()) imports.add(string);
                    }
                    setVisible(true);
                    updateImports();
                });
    }

    public String getSelectBranch() {
        return cbBranchs.getSelectedItem().toString();
    }


    private void initJList(List<String> imports, List<JListInfo> allInfos) {
        //模拟选中,不然列表数据会异常
        for (int i = 0; i < allInfos.size(); i++) imports.add(TAG);
        selectAdapter = new JListSelectAdapter(jlSelect, false);
        allAdapter = new ImportSelectAdapter(JlNotSelect, allInfos);
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
        repaintBorder(jpOthers, "Others", allInfos.size());
        updateImports();
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            while (true) if (!imports.remove(TAG)) break;
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
        repaintBorder(jpSelect, "Select", infos.size());
    }

    void repaintBorder(JComponent jpSelect, String key, int size) {
        ((TitledBorder) jpSelect.getBorder()).setTitle(key + " (" + size + ")");
        jpSelect.revalidate();
        jpSelect.repaint();
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

    /**
     * 选择分支
     */
    private String selectItems(List<String> items, String initValue) {
        if (items == null || items.isEmpty()) return null;
        return Messages.showEditableChooseDialog("", "Select Item", null, items.toArray(new String[]{}), initValue == null ? items.get(0) : initValue, null);
    }

    private void initLoadButtons(List<String> branchs) {
        loadAppButton.addActionListener(actionEvent -> loadBranchModules(getSelectBranch(), true));
        loadBranchButton.addActionListener(actionEvent -> loadBranchModules(selectItems(branchs, getSelectBranch()), false));
        loadDpsButton.addActionListener(actionEvent -> {
            ArrayList<String> items = new ArrayList<>();
            for (String m : getImports()) {
                if (!m.contains("#")) items.add(m);
            }
            String dpsItem = selectItems(items, null);
            //需要加载Dps依赖的项目
            loadDpsItem(dpsItem);

        });
        for (String b : branchs) cbBranchs.addItem(b);

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
    }

    /**
     * 加载给定的模块的依赖
     *
     * @param dpsItem
     */
    private void loadDpsItem(String dpsItem) {
        if (dpsItem == null) return;
        long runTime = System.currentTimeMillis();
        Map<String, String> envs = new HashMap<>();
        envs.put("dependentModel", "");
        setVisible(false);

        GradleUtils.INSTANCE.runTask(project, Collections.singletonList(":" + dpsItem + ":DpsAnalysis")
                , ProgressExecutionMode.IN_BACKGROUND_ASYNC, true, runTime + ""
                , envs, (s, l) -> ApplicationManager.getApplication().invokeLater(() -> {
                    File file = new File(project.getBasePath(), "build/dps/" + dpsItem + ".dp");
                    if (!file.exists()) Messages.showMessageDialog(dpsItem + " dps import failure", "ERROR", null);
                    else if (file.lastModified() > runTime || Messages.OK == Messages.showOkCancelDialog("The local configuration is available and still imported?", "ERROR", null)) {
                        imports.remove(dpsItem);
                        imports.add(0, "D#" + dpsItem);


                        String readText = FileUtils.readText(file);
                        if (readText != null) Messages.showMessageDialog(
                                "Import dps for " + dpsItem + "\n" + readText
                                        .replace(" ", "")
                                        .replace(",,", "")
                                        .replace(",", "\n")
                                , "All dps for " + dpsItem, null);
                    }
                    setVisible(true);
                    updateImports();
                }));
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
                            if (!imports.contains(importKey)) {
                                imports.add(0, importKey);
                                updateImports();
                            }
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
