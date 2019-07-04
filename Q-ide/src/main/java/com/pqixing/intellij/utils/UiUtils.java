package com.pqixing.intellij.utils;

import com.android.ddmlib.IDevice;
import com.android.tools.idea.explorer.adbimpl.AdbShellCommandsUtil;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.List;

import javax.swing.JDialog;

public class UiUtils {
    public static final String IDE_PROPERTIES = ".idea/modularization.properties";
//    public static void centerDialog(JDialog dialog) {
////        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); //获取屏幕的尺寸
////        dialog.setLocation((screenSize.width-450)/2, (screenSize.height-350)/2);//设置窗口居中显示
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                dialog.setLocationRelativeTo(null);
//            }
//        }).start();
//    }

    public static String adbShellCommon(IDevice device, String cmd,boolean firstLine){
        try {
            List<String> output = AdbShellCommandsUtil.executeCommand(device, cmd).getOutput();
            if(firstLine||output.size()==1) return output.get(0);
            return output.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
