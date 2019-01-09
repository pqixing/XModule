package com.pqixing.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.module.Module;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AnActionEventWrap extends AnActionEvent {
    private Module target;

    public AnActionEventWrap(AnActionEvent actionEvent, Module target) {
        super(actionEvent.getInputEvent(), actionEvent.getDataContext(), actionEvent.getPlace(), actionEvent.getPresentation(), actionEvent.getActionManager(), actionEvent.getModifiers(), actionEvent.isFromContextMenu(), actionEvent.isFromActionToolbar());
        this.target = target;
    }

    @Nullable
    @Override
    public <T> T getData(@NotNull DataKey<T> key) {
        T data = super.getData(key);
        return data instanceof Module ? (T) target : data;
    }
}
