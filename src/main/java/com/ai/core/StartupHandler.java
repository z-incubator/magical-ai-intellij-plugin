package com.ai.core;

import com.ai.settings.state.GeneralSettings;
import com.ai.action.editor.ActionsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class StartupHandler implements StartupActivity {

    private @Getter static volatile boolean fullyStarted;

    @Override
    public void runActivity(@NotNull Project project) {
        try {
            GeneralSettings.getInstance();
            ActionsUtil.refreshActions();
        } finally {
            fullyStarted = true;
        }
    }
}
