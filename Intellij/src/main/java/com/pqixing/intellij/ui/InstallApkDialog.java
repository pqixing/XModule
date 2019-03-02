package com.pqixing.intellij.ui;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.tools.idea.explorer.adbimpl.AdbPathUtil;
import com.android.tools.idea.explorer.adbimpl.AdbShellCommandResult;
import com.android.tools.idea.explorer.adbimpl.AdbShellCommandsUtil;
import com.intellij.debugger.memory.utils.AndroidUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.pqixing.intellij.adapter.JListInfo;
import com.pqixing.intellij.adapter.JListSelectAdapter;
import com.pqixing.intellij.utils.DachenHelper;
import com.pqixing.shell.Shell;

import org.jetbrains.android.sdk.AndroidSdkUtils;
import org.jetbrains.android.util.AndroidUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class InstallApkDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton refresh;
    private JList jlDevices;
    private JButton btnPick;
    private JComboBox jcPaths;
    private JTextField jfParams;
    private JLabel jlResult;
    protected AndroidDebugBridge androidDebugBridge;
    JListSelectAdapter adapter;
    private LinkedHashMap<String, String> apkUrls = new LinkedHashMap<>();
    private HashMap<JListInfo, IDevice> devices = new HashMap<>();
    private Project project;
    private boolean loadNetApks = false;

    public InstallApkDialog(Project project, String apkPath, AndroidDebugBridge androidDebugBridge) {
        this.project = project;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        this.androidDebugBridge = androidDebugBridge;
        buttonOK.addActionListener(e -> onOK());
        setTitle("Install Apk");
        setLocation(400, 300);

        refresh.addActionListener(e -> refreshDatas());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        jcPaths.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
                if (!loadNetApks) loadApkUrls();
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent popupMenuEvent) {

            }
        });
        btnPick.addActionListener(actionEvent -> {
            VirtualFile[] files = FileChooser.chooseFiles(new FileChooserDescriptor(true, false, false, false, false, false), project, VfsUtil.findFileByIoFile(new File(apkPath), false));
            if (files.length == 0) return;
            String path = VfsUtil.virtualToIoFile(files[0]).getAbsolutePath();
            addApksUrls(path);
        });

        adapter = new JListSelectAdapter(jlDevices, true);
        jlDevices.setModel(adapter);
        refreshDatas();
        if (apkPath != null) addApksUrls(apkPath);
        else loadApkUrls();
    }

    /**
     * 刷新数据
     */
    private void refreshDatas() {
        if (androidDebugBridge == null) return;
        devices.clear();
        for (IDevice d : androidDebugBridge.getDevices()) {
            devices.put(new JListInfo(d.getAvdName() + "  " + d.getSerialNumber() + "  " + d.getState(), "", 0, false), d);
        }
        adapter.setDatas(new LinkedList<>(devices.keySet()));
    }

    private void loadApkUrls() {
        for (Map.Entry<String, String> key : DachenHelper.INSTANCE.loadApksForNet().entrySet()) {
            apkUrls.put(key.getKey(), key.getValue());
            jcPaths.addItem(key.getKey());
        }
    }

    private void addApksUrls(String url) {
        String key = url.length() > 50 ? ".." + url.substring(url.length() - 50) : url;
        apkUrls.put(key, url);
        jcPaths.addItem(key);
        jcPaths.setSelectedIndex(jcPaths.getItemCount() - 1);
    }

    private String getPath(ProgressIndicator indicator) {
        String key = jcPaths.getSelectedItem().toString().trim();
        String apkUrl = apkUrls != null && apkUrls.containsKey(key) ? apkUrls.get(key) : key;
        if (apkUrl.startsWith("http")) {//网络地址
            jlResult.setText("Download...");
            indicator.setText("Downloading " + apkUrl);
            apkUrl = DachenHelper.INSTANCE.downloadApk(project, key, apkUrl);
        }
        File apkFile = new File(apkUrl);
        if (!apkFile.exists() || !apkFile.isFile()) {
            jlResult.setText("Apk file not exists  + apkUrl");
            return null;
        }
        return apkUrl;
    }

    private void install(String apkUrl, ProgressIndicator indicator) {
        jlResult.setText("Install...");
        File adb = AndroidSdkUtils.getAdb(project);
        for (Map.Entry<JListInfo, IDevice> jd : devices.entrySet()) {
            if (!jd.getKey().getSelect()) continue;
            boolean success;
            String output;
            try {
                jd.getKey().setLog("install...");
                adapter.updateUI();
                indicator.setText("install to " + jd.getKey().getTitle());
                LinkedList<String> list = Shell.runSync(adb.getAbsolutePath() + " -s " + jd.getValue().getSerialNumber() + " install " + jfParams.getText() + " " + apkUrl);
                output = list.getLast();
                success = output.trim().startsWith("Success");
            } catch (Exception e) {
                success = false;
                output = e.toString();
            }
            jd.getKey().setLog(output);
            jd.getKey().setStaue(success ? 1 : 3);
            adapter.updateUI();
        }
    }

    private void onOK() {
        Task.Backgroundable install = new Task.Backgroundable(project, "Start Install") {

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                int select = 0;
                for (JListInfo j : devices.keySet()) {
                    if (j.getSelect()) select++;
                }
                if (select <= 0) {
                    jlResult.setText("No Devices");
                    return;
                }
                buttonOK.setVisible(false);
                String path = getPath(indicator);
                if (path != null) install(path, indicator);
                indicator.setText("Install Finish");
                jlResult.setText("");
                buttonOK.setVisible(true);
                new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "Install Finish", path, NotificationType.INFORMATION).notify(project);
            }
        };
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(install, new BackgroundableProcessIndicator(install));

    }

    private void onCancel() {
        dispose();
    }
}
