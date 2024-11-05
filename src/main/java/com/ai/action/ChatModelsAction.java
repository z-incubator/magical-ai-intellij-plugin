package com.ai.action;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.ui.awt.RelativePoint;
import com.ai.chat.AssistantType;
import com.ai.icons.AIIcons;
import com.ai.settings.state.GeneralSettings;
import com.ai.topic.OpenAITopic;
import com.ai.util.ThemeTool;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;

public class ChatModelsAction extends DumbAwareAction {
    private final Project project;

    public ChatModelsAction(Project project) {
        super(() -> "Switch Mode", ThemeTool.isDark() ? AIIcons.AI_MODEL_DARK : AIIcons.AI_MODEL_LIGHT);
        this.project = project;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // Obtain the mouse event
        InputEvent inputEvent = e.getInputEvent();
        if (!(inputEvent instanceof MouseEvent mouseEvent)) {
            return; // If it's not a mouse event, we can't proceed with positioning
        }
        Component sourceComponent = mouseEvent.getComponent();

        if (sourceComponent instanceof JComponent) {
            ListPopup actionGroupPopup = JBPopupFactory.getInstance().createActionGroupPopup(
                    "Switch Mode",
                    new ChatActionGroup(project),
                    e.getDataContext(),
                    true,
                    null,
                    Integer.MAX_VALUE
            );

            // Show the popup relative to the mouse click
            RelativePoint popupPoint = new RelativePoint(mouseEvent.getLocationOnScreen());
            actionGroupPopup.show(popupPoint);
        }
    }

    static class ChatActionGroup extends ActionGroup {
        private final Project project;

        public ChatActionGroup(Project project) {
            super("Chat", true);
            this.project = project;
        }

        @Override
        public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
            AnAction[] chatModelsActions = new AnAction[AssistantType.System.values().length * 2]; // 分配足够的空间
            int i = 0;
            for (AssistantType.System assistantType : AssistantType.System.values()) {
                chatModelsActions[i++] = new ChatModelAction(project, assistantType);
                chatModelsActions[i++] = new Separator(); // 添加分隔符
            }
            if (i > 0 && chatModelsActions[i - 1] instanceof Separator) {
                i--; // 如果最后一个是分隔符，去掉它
            }
            return Arrays.copyOf(chatModelsActions, i); // 返回正确大小的数组
        }
    }

    static class ChatModelAction extends DumbAwareAction {
        private final Project project;
        private final AssistantType assistantType;

        public ChatModelAction(Project project, AssistantType assistantType) {
            super(assistantType::name, assistantType == AssistantType.System.OnlineTool ?
                    ThemeTool.isDark() ? AIIcons.ONLINE_DARK : AIIcons.ONLINE_LIGHT :
                    ThemeTool.isDark() ? AIIcons.API_DARK : AIIcons.API_LIGHT);
            this.project = project;
            this.assistantType = assistantType;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            GeneralSettings.getInstance().defaultModel = assistantType.name();
            this.project.getMessageBus().syncPublisher(OpenAITopic.TOPIC).change(assistantType);
        }
    }
}
