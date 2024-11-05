
package com.ai.action.editor;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.ai.icons.AIIcons;
import com.ai.settings.CustomAction;
import com.ai.settings.state.GeneralSettings;
import com.ai.util.ThemeTool;

public class ActionsUtil {

    public static void refreshActions() {

        // 初始化customActionsPrefix
        if (GeneralSettings.getInstance().getCustomActionsPrefix().isEmpty()) {
            GeneralSettings.getInstance().getCustomActionsPrefix().add(new CustomAction("Code optimization", "optimize according to the latest syntax standards"));
            GeneralSettings.getInstance().getCustomActionsPrefix().add(new CustomAction("Generate Tests", "Add test case for this code"));
            GeneralSettings.getInstance().getCustomActionsPrefix().add(new CustomAction("Simplify this", "simplify this code"));
            GeneralSettings.getInstance().getCustomActionsPrefix().add(new CustomAction("Generate Docs", "Generate docs for this code"));
            GeneralSettings.getInstance().getCustomActionsPrefix().add(new CustomAction("Fix this", "Fix this code"));
            GeneralSettings.getInstance().getCustomActionsPrefix().add(new CustomAction("Explain this", "Explain this code"));
        }

        ActionManager actionManager = ActionManager.getInstance();
        AnAction existingActionGroup = actionManager.getAction("RightClickAction");
        if (existingActionGroup instanceof DefaultActionGroup group) {
            group.removeAll();
            group.add(new CustomPromptAction());
            group.addSeparator();
            group.add(new FixCompilationErrorsAction());
            group.addSeparator();
            // 添加自定义action
            for (CustomAction customAction : GeneralSettings.getInstance().getCustomActionsPrefix()) {
                group.add(new GenericEditorAction(customAction::getName, customAction.getCommand(), ThemeTool.isDark() ? AIIcons.AI_QUESTION_DARK : AIIcons.AI_QUESTION_LIGHT));
            }
        }
    }
}
