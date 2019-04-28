package com.pqixing.intellij.ui;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
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
import com.pqixing.intellij.utils.UiUtils;
import com.pqixing.shell.Shell;

import org.jetbrains.android.sdk.AndroidSdkUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
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

public class InstallApkDialog extends BaseJDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton refresh;
    private JList jlDevices;
    private JButton btnPick;
    private JComboBox jcPaths;
    private JTextField jfParams;
    private JLabel jlResult;
    JListSelectAdapter adapter;
    private LinkedHashMap<String, String> apkUrls = new LinkedHashMap<>();
    private HashMap<JListInfo, IDevice> devices = new HashMap<>();
    private Project project;
    private boolean loadNetApks = false;

    public InstallApkDialog(Project project, String apkPath) {
        this.project = project;
        setContentPane(contentPane);
        setModal(false);
        getRootPane().setDefaultButton(buttonOK);
        buttonOK.addActionListener(e -> onOK());
        setTitle("Install Apk");

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
        mockData();
        if (apkPath != null) addApksUrls(apkPath);
        else loadApkUrls();

    }

    private void mockData() {
        ArrayList<JListInfo> infos = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            infos.add(new JListInfo("------------------------", "", 0, false));
        }
        adapter.setDatas(infos);
    }

    /**
     * 刷新数据
     */
    private void refreshDatas() {
        devices.clear();
        AndroidDebugBridge bridge = AndroidSdkUtils.getDebugBridge(project);
        int i = 0;
        if (bridge != null) for (IDevice d : bridge.getDevices()) {
            String avdName = d.getAvdName();
            if (avdName == null)
                avdName = UiUtils.adbShellCommon(d, "getprop ro.product.brand", true) + "-" + UiUtils.adbShellCommon(d, "getprop ro.product.model", true);
            devices.put(new JListInfo(avdName + "  " + d.getSerialNumber() + "  " + d.getState(), "", 0, i++ == 0), d);
        }
        adapter.setDatas(new LinkedList<>(devices.keySet()));
    }

    @Override
    public void setVisible(boolean b) {
        if (b) refreshDatas();
        super.setVisible(b);
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
        jlResult.setText(url);
    }

    private String getPath(ProgressIndicator indicator) {
        String apkUrl = jcPaths.getSelectedItem().toString().trim();
        String key = apkUrl;
        for (Map.Entry<String, String> entry : apkUrls.entrySet()) {
            if (entry.getKey().replace(" ", "").equals(key.replace(" ", ""))) {
                apkUrl = entry.getValue();
            }
        }
        if (apkUrl.startsWith("http")) {//网络地址
            jlResult.setText("Download..." + apkUrl);
            int of = key.lastIndexOf(" ");
            indicator.setText("Download..." + apkUrl);
            apkUrl = DachenHelper.INSTANCE.downloadApk(project, key.substring(Math.max(0, of)).trim(), apkUrl);
            final String u = apkUrl;
            ApplicationManager.getApplication().invokeLater(() -> addApksUrls(u));
        }
        File apkFile = new File(apkUrl);
        if (!apkFile.exists() || !apkFile.isFile()) {
            jlResult.setText("Apk file not exists " + apkUrl);
            buttonOK.setVisible(true);
            return null;
        }
        return apkUrl;
    }

    private void install(String apkUrl, ProgressIndicator indicator) {
        File adb = AndroidSdkUtils.getAdb(project);
        for (Map.Entry<JListInfo, IDevice> jd : devices.entrySet()) {
            if (!jd.getKey().getSelect()) continue;
            boolean success;
            String output;
            try {
                jd.getKey().setLog("install...");
                adapter.updateUI();
                indicator.setText("install to " + jd.getKey().getTitle() + " " + apkUrl);
                jlResult.setText("Install Apk: " + apkUrl);
                LinkedList<String> list = Shell.runSync(adb.getAbsolutePath() + " -s " + jd.getValue().getSerialNumber() + " install " + jfParams.getText() + " " + apkUrl);
                output = list.getLast();
                success = output.trim().startsWith("Success");
            } catch (Exception e) {
                success = false;
                output = e.toString();
                jlResult.setText(output);
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
                if (path == null) return;
                install(path, indicator);
                indicator.setText("Install Finish");
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
