package com.pqixing.intellij.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class TestDialog extends DialogWrapper {
    public TestDialog(@Nullable Project project) {
        super(project,false,IdeModalityType.MODELESS);
    }

    public Point location(){
        show();
        Point location = getLocation();
        dispose();
        return location;
    }
    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return null;
    }
}
