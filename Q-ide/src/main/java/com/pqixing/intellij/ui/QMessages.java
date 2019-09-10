// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.pqixing.intellij.ui;

import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QMessages extends Messages {
    @Nullable
    public static QChooseDialog getChooseDialog(String message,
                                                String title,
                                                String[] values,
                                                String initialValue) {
        QChooseDialog dialog = new QChooseDialog(message, title, values, initialValue);
        dialog.getComboBox().setEditable(true);
        dialog.getComboBox().getEditor().setItem(initialValue);
        dialog.getComboBox().setSelectedItem(initialValue);
        return dialog;
    }

    private static class QChooseDialog extends ChooseDialog {

        public QChooseDialog(String message, @Nls(capitalization = Nls.Capitalization.Title) String title, @NotNull String[] values, String initialValue) {
            super(message, title, null, values, initialValue);
        }
    }
}
