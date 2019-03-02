package com.pqixing.intellij.utils;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JDialog;

public class UiUtils {
    public static void centerDialog(JDialog dialog) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); //获取屏幕的尺寸
        dialog.setLocation((screenSize.width-450)/2, (screenSize.height-350)/2);//设置窗口居中显示
    }
}
