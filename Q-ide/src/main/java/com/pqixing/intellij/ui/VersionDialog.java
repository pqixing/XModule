package com.pqixing.intellij.ui;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.pqixing.intellij.adapter.JListInfo;
import com.pqixing.intellij.adapter.JListSelectAdapter;
import com.pqixing.intellij.utils.GitHelper;
import com.pqixing.intellij.utils.GradleUtils;
import com.pqixing.intellij.utils.UiUtils;

import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import git4idea.GitLocalBranch;
import git4idea.GitRemoteBranch;
import git4idea.branch.GitBranchesCollection;
import git4idea.repo.GitRepository;
import kotlin.Pair;

public class VersionDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox cbTarget;
    private JList jlBrans;
    private JButton allButton;
    boolean all = false;

    Project project;
    JListSelectAdapter adapter;

    public VersionDialog(Project project, String targetBranch) {
        setContentPane(contentPane);
        setModal(false);
        getRootPane().setDefaultButton(buttonOK);
        this.project = project;
        UiUtils.centerDialog(this);
        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        GitRepository repo = GitHelper.getRepo(new File(project.getBasePath(), "templet"), project);
        if (repo == null) {
            return;
        }
        GitBranchesCollection branches = repo.getBranches();
        HashSet<String> branchs = new HashSet<>();
        for (GitLocalBranch b : branches.getLocalBranches()) {
            branchs.add(b.getName());
        }
        for (GitRemoteBranch b : branches.getRemoteBranches()) {
            branchs.add(b.getName());
        }
        LinkedList<JListInfo> datas = new LinkedList<>();
        if (targetBranch != null) branchs.add(targetBranch);
        for (String b : branchs) {
            cbTarget.addItem(b);
            datas.add(new JListInfo(b, "", 0, false));
            if (b.equals(targetBranch)) cbTarget.setSelectedIndex(cbTarget.getItemCount() - 1);
        }
        adapter = new JListSelectAdapter(jlBrans, true);
        adapter.setDatas(datas);
        allButton.addActionListener(e -> {
            all = !all;
            for (JListInfo j : datas) {
                j.setSelect(all);
            }
            adapter.updateUI();
        });
    }

    private void onOK() {
        String targetBranch = cbTarget.getSelectedItem().toString().trim();
        if (targetBranch.isEmpty()) return;
        String runTaskId = System.currentTimeMillis() + "";
        HashMap<String, String> envs = new HashMap<>(GradleUtils.INSTANCE.getDefEnvs());
        envs.put("taskBranch", targetBranch);
        StringBuilder tagBrans = new StringBuilder();
        for (JListInfo j : adapter.getDatas()) {
            if (j.getSelect()) tagBrans.append(j.getTitle()).append(",");
        }
        envs.put("tagBranchs", tagBrans.toString());

        GradleUtils.INSTANCE.runTask(project, Arrays.asList(":VersionIndex", ":VersionTag"),
                ProgressExecutionMode.IN_BACKGROUND_ASYNC, false
                , runTaskId, envs
                , () -> {
                    Pair<Boolean, String> result = GradleUtils.INSTANCE.getResult(project, runTaskId);
                    if (!result.getFirst()) {
                        new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "VersionTag", "Gradle Task Error " + result.getSecond(), NotificationType.WARNING).notify(project);

                    } else ApplicationManager.getApplication().invokeLater(() -> {
                        FileEditorManager.getInstance(project).openFile(Objects.requireNonNull(VfsUtil.findFileByIoFile(new File(result.getSecond()), true)), true);
                    });

                });
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }
}
