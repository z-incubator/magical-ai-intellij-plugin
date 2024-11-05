package com.ai;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.ContentManagerEvent;
import com.intellij.ui.content.ContentManagerListener;
import com.ai.action.ChatModelsAction;
import com.ai.action.GitHubAction;
import com.ai.gui.tool.window.ChatToolWindowFactory;
import com.ai.topic.OpenAITopic;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class MainWindowFactory implements ToolWindowFactory, DumbAware {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // 创建AI助手面板
        new ChatToolWindowFactory().createToolWindowContent(project, toolWindow);

        // 设置默认显示的面板位置
        toolWindow.setShowStripeButton(true);
        toolWindow.setSplitMode(false, () -> {
        });

        // 默认选中请求面板
        updateTitleActions(toolWindow, project);

        // Add the selection listener
        toolWindow.addContentManagerListener(new ContentManagerListener() {
            @Override
            public void selectionChanged(@NotNull ContentManagerEvent event) {
                updateTitleActions(toolWindow, project);
            }
        });

        project.getMessageBus().connect().subscribe(OpenAITopic.TOPIC,
                (OpenAITopic) (event) -> reloadWin(project, toolWindow));
    }

    private void reloadWin(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        toolWindow.getContentManager().removeContent(Objects.requireNonNull(toolWindow.getContentManager().getContent(0)), true);
        new ChatToolWindowFactory().createToolWindowContent(project, toolWindow);
        toolWindow.getContentManager().setSelectedContent(Objects.requireNonNull(toolWindow.getContentManager().getContent(0)));
    }

    private void updateTitleActions(@NotNull ToolWindow toolWindow, Project project) {
        List<AnAction> actionList = new LinkedList<>();
        actionList.add(new ChatModelsAction(project));
        actionList.add(new GitHubAction());
        toolWindow.setTitleActions(actionList);
    }
}
