package com.pqixing.intellij.ui;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.pqixing.intellij.adapter.JListInfo;
import com.pqixing.intellij.adapter.JListSelectAdapter;
import com.pqixing.intellij.adapter.JlistSelectListener;
import com.pqixing.intellij.utils.UiUtils;
import javafx.util.Pair;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FileListDialog extends JDialog {
    private JPanel contentPane;
    private JList jlDatas;
    private Project project;


    public FileListDialog(Project project, List<JListInfo> datas, List<File> filePath) {
        setContentPane(contentPane);
        setModal(false);
        UiUtils.centerDialog(this);
        this.project = project;
        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        setTitle("Files");

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        JListSelectAdapter adapter = new JListSelectAdapter(jlDatas, false);
        adapter.setDatas(datas);
        adapter.setSelectListener((jList, adapter1, items) -> {
            JListInfo info = items.get(items.size() - 1);
            File file = filePath.get(datas.indexOf(info));
            FileEditorManager.getInstance(project).openFile(VfsUtil.findFileByIoFile(file, true), true);
            return true;
        });

    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }
}
