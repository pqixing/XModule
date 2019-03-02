package com.pqixing.intellij.ui;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.tools.idea.explorer.adbimpl.AdbPathUtil;
import com.android.tools.idea.explorer.adbimpl.AdbShellCommandResult;
import com.android.tools.idea.explorer.adbimpl.AdbShellCommandsUtil;
import com.intellij.debugger.memory.utils.AndroidUtil;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.pqixing.intellij.adapter.JListInfo;
import com.pqixing.intellij.adapter.JListSelectAdapter;
import com.pqixing.intellij.utils.DachenHelper;
import com.pqixing.shell.Shell;

import org.jetbrains.android.sdk.AndroidSdkUtils;
import org.jetbrains.android.util.AndroidUtils;

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
    private JLabel jlHint;
    protected AndroidDebugBridge androidDebugBridge;
    JListSelectAdapter adapter;
    private LinkedHashMap<String, String> apkUrls = null;
    private HashMap<JListInfo, IDevice> devices = new HashMap<>();
    private Project project;

    public InstallApkDialog(Project project, String apkPath, AndroidDebugBridge androidDebugBridge) {
        this.project = project;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        this.androidDebugBridge = androidDebugBridge;
        buttonOK.addActionListener(e -> onOK());

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
                if (apkUrls == null) loadApkUrls();
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
            jcPaths.addItem(path);
            jcPaths.setSelectedIndex(jcPaths.getItemCount() - 1);
        });

        adapter = new JListSelectAdapter(jlDevices, true);
        jlDevices.setModel(adapter);
        refreshDatas();
        if (apkPath != null) jcPaths.addItem(apkPath);
        else loadApkUrls();
    }

    /**
     * 刷新数据
     */
    private void refreshDatas() {
        if (androidDebugBridge == null) return;
        devices.clear();
        for (IDevice d : androidDebugBridge.getDevices()) {
            devices.put(new JListInfo(d.getAvdName() + "  " + d.getSerialNumber(), "", 0, false), d);
        }
        adapter.setDatas(new LinkedList<>(devices.keySet()));
    }

    private void loadApkUrls() {
        apkUrls = DachenHelper.INSTANCE.loadApksForNet();
        for (String key : apkUrls.keySet()) {
            jcPaths.addItem(key);
        }
    }

    private String getPath() {
        String key = jcPaths.getSelectedItem().toString().trim();
        String apkUrl = apkUrls != null && apkUrls.containsKey(key) ? apkUrls.get(key) : key;
        if (apkUrl.startsWith("http")) {//网络地址
            jlHint.setText("Downloading " + apkUrl);
            apkUrl = DachenHelper.INSTANCE.downloadApk(project, key, apkUrl);
        }
        File apkFile = new File(apkUrl);
        if (!apkFile.exists()) {
            jlHint.setText("Apk file not exists " + apkUrl);
            return null;
        }
        return apkUrl;
    }

    private void install(String apkUrl) {
        File adb = AndroidSdkUtils.getAdb(project);
        for (Map.Entry<JListInfo, IDevice> jd : devices.entrySet()) {
            if (!jd.getKey().getSelect()) continue;
            boolean success;
            String output;
            try {
                jlHint.setText("install to " + jd.getKey().getTitle());
                LinkedList<String> list = Shell.runSync("adb install " + jfParams.getText() + " " + apkUrl, adb.getParentFile());
                success = list.isEmpty();
                output = list.toString();
            } catch (Exception e) {
                success = false;
                output = e.toString();
            }
            jd.getKey().setLog(output);
            jd.getKey().setStaue(success ? 1 : 3);
            adapter.updateUI();
        }
//        jlHint.setText("install finish " + apkUrl);
    }

    private void onOK() {
        int select = 0;
        for (JListInfo j : devices.keySet()) {
            if (j.getSelect()) select++;
        }
        if (select <= 0) {
            jlHint.setText("Not Target Devices");
            return;
        }
        String path = getPath();
        if (path != null) install(path);
    }

    private void onCancel() {
        dispose();
    }
}
