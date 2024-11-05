
package com.ai.gui.tool.window;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.ai.chat.AssistantType;
import com.ai.settings.state.GeneralSettings;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

public class ChatToolWindow {

    public static final String TOOL_WINDOW_ID = "MagicalAI";

    public static ToolWindow locate(Project project) {
        return ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID);
    }

    public static void ensureActivated(Project project) {
        var toolWindow = locate(project);
        if (toolWindow == null) {
            throw new AssertionError("Unable to find " + TOOL_WINDOW_ID + " Tool Window");
        }
        if (!toolWindow.isActive()) {
            ApplicationManager.getApplication().invokeLater(() -> {
                ContentManager contentManager = toolWindow.getContentManager();
                contentManager.setSelectedContent(Objects.requireNonNull(contentManager.getContent(0)));

                toolWindow.show();
                // 激活工具窗口
                toolWindow.activate(null);
            });
        }
    }

    public static void synchronizeContents() {
        synchronizeContents(GeneralSettings.getInstance().selectedModel());
    }

    public static void synchronizeContents(Set<? extends AssistantType> assistantTypes) {
        var openProjects = ProjectManager.getInstance().getOpenProjects();

        for (Project project : openProjects) {
            var assistantTypesToAdd = new ArrayList<>(assistantTypes);
            var toolWindow = locate(project);
            var contentManager = toolWindow.getContentManagerIfCreated();

            if (contentManager != null) {
                removeDisabledChatSystems(assistantTypes, contentManager, assistantTypesToAdd);
                if (!assistantTypesToAdd.isEmpty()) {
                    addEnabledChatSystems(assistantTypesToAdd, toolWindow, contentManager);
                }
            }
        }
    }

    private static void removeDisabledChatSystems(Set<? extends AssistantType> assistantTypes, ContentManager contentManager, ArrayList<? extends AssistantType> assistantTypesToAdd) {
        for (var content : contentManager.getContents()) {
            var assistantType = content.getUserData(ChatToolWindowFactory.ACTIVE_TAB);
            if (assistantType != null) {
                if (assistantTypes.contains(assistantType))
                    assistantTypesToAdd.remove(assistantType);
                else
                    contentManager.removeContent(content, true);
            }
        }
    }

    private static void addEnabledChatSystems(ArrayList<? extends AssistantType> assistantTypesToAdd, ToolWindow toolWindow, ContentManager contentManager) {
        var settings = GeneralSettings.getInstance();
        assistantTypesToAdd.forEach(type -> ChatToolWindowFactory.addToolWindowContent(toolWindow, type, settings));

        Content[] contents = contentManager.getContents();
        if (contents.length > 0) {
            contentManager.setSelectedContent(contents[contents.length - 1]);
        }
    }
}
