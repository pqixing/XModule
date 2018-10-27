package com.dachen.creator.actions


import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.pqixing.tools.Logger
import com.pqixing.tools.Shell

public class ClearCache extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        int exitCode = Messages.showOkCancelDialog("是否清除所有缓存", "ClearCache", null);
        if(exitCode!=0) return
//        GradleUtils.runTask(project, ["CleanCache"])

        Shell.logger = new Logger() {
            @Override
            void log(String s) {
                System.out.println(s)
            }
        }
        Object result = Shell.runSync("adb install -r /Users/Dev/Code/MyApplication/app/build/outputs/apk/debug/app-debug.apk",null,null)
        System.out.println("result $result")
       Shell.testRun("this is test println")
    }
}
