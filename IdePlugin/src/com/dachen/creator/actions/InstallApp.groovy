package com.dachen.creator.actions

import com.dachen.creator.ui.MultiBoxDialog
import com.dachen.creator.utils.AndroidUtils
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.NotNull

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSession

public class InstallApp extends AnAction {
    Project project
    private String baseUrl = "https://192.168.3.211:9000/android"
    private File apkFile

    @Override
    public void actionPerformed(AnActionEvent e) {
        project = e.getProject();

        List<String> item = new ArrayList<>()
        String update = ""

        String updateTag = "更新:"
        String downloadTag = "downloadUrl="
        try {
            def conn = new URL(baseUrl).openConnection()
            ((HttpsURLConnection) conn).setHostnameVerifier(new HostnameVerifier() {
                @Override
                boolean verify(String s, SSLSession sslSession) {
                    return true
                }
            })
            String page = conn.inputStream.text
            page.eachLine { l ->
                if (l.contains(updateTag)) {
                    update = l.substring(l.indexOf(updateTag), l.lastIndexOf("<"))
                } else if (l.contains(downloadTag)) {
                    String apkUrl = l.substring(l.indexOf(downloadTag) + downloadTag.size() + 1)
                    apkUrl = apkUrl.substring(0, apkUrl.indexOf("\""))
                    String t = "remote$apkUrl?$update"
                    item.add(t)
                }
            }
        } catch (Exception e1) {
        }
        MultiBoxDialog.builder(project)
                .setMode(false, true, true)
                .setMsg("选择安装包", "请选择本地文件或网络地址")
                .setInput(item[0])
                .setItems(item)
                .setInputButton(null, true)
                .setHint("请勾选需要App")
                .setListener(new MultiBoxDialog.Listener() {
            @Override
            void onOk(String input, List<String> items, boolean check) {
                input = input.trim()
                if (input.startsWith("remote")) {
                    downloadApk(input.replace("remote", baseUrl.replace("/android","")))
                } else AndroidUtils.installApk(project, new File(input))
            }

            @Override
            void onCancel() {

            }
        }).show()
    }

    private void downloadApk(String url) {
        int last = url.lastIndexOf("?")
        if (last > 0) url = url.substring(0, last)
        apkFile = new File(project.getBasePath(), "build/apk/${url.substring(url.lastIndexOf("/") + 1)}")
        if (!apkFile.exists()) apkFile.getParentFile().mkdirs()
        def download = new Task.Backgroundable(project, "Start Download", true) {
            @Override
            void run(@NotNull ProgressIndicator progressIndicator) {
                progressIndicator.start()
                progressIndicator.setText("正在下载Apk:")
                apkFile.delete()
                apkFile.createNewFile()
                long total = 0
                def conn = new URL(url).openConnection()
                try {
                    ((HttpsURLConnection) conn).setHostnameVerifier(new HostnameVerifier() {
                        @Override
                        boolean verify(String s, SSLSession sslSession) {
                            return true
                        }
                    })

                    def input = conn.inputStream
                    total = conn.contentLengthLong

                    def output = apkFile.newOutputStream()
                    byte[] buffer = new byte[1024 * 1024 * 10]
                    int len
                    long allDown = 0;
                    while ((len = input.read(buffer)) > 0) {
                        output.write(buffer, 0, len)
                        allDown += len
                        int progress = allDown * 100 / total
                        progressIndicator.setText("download:${progress}%")
                    }
                    output.flush()
                    try {
                        input.close()
                        output.close()
                    } catch (Exception e) {

                    }
                } catch (Exception e) {

                }
                ApplicationManager.getApplication().invokeLater {
                    if (total>0 && apkFile.size()==total) {
                        AndroidUtils.installApk(project, apkFile)
                    } else {
                        new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "下载文件异常", "下载文件失败", NotificationType.INFORMATION).notify(project)
                    }
                }
            }
        }
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(download, new BackgroundableProcessIndicator(download))
    }
}
