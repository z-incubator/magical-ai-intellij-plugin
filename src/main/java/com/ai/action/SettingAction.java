package com.ai.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;


public class SettingAction extends DumbAwareAction {
    private final boolean isAI;

    public SettingAction(@NotNull @Nls String text, boolean isAI) {
        super(() -> text, AllIcons.General.Settings);
        this.isAI = isAI;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ShowSettingsUtil.getInstance().showSettingsDialog(e.getProject(), isAI ? "Chat Models Settings" : "API Configuration");
    }
}
