package com.ai.action;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.ai.chat.AssistantType;
import com.ai.settings.state.GeneralSettings;
import com.ai.topic.OpenAITopic;

import static com.ai.chat.AssistantType.System.OnlineTool;

public class ModelValidationUtils {
    public static boolean isModelValid(Project model) {
        String defaultModel = GeneralSettings.getInstance().defaultModel;

        if (OnlineTool.name().equals(defaultModel)) {
            // 提示信息和按钮文本使用英文
            boolean switchToAPI = MessageDialogBuilder.yesNo(
                            "Operation Notice",
                            "The current online mirroring tool is not supported. Would you like to switch to the API mode?")
                    .yesText("Switch to Qianfan API")
                    .noText("Not Now").ask(model);

            if (switchToAPI) {
                GeneralSettings.getInstance().defaultModel = AssistantType.System.Qianfan.name();
                model.getMessageBus()
                        .syncPublisher(OpenAITopic.TOPIC)
                        .change(AssistantType.System.Qianfan);
                return false;
            } else {
                return true;
            }
        }
        // 如果不是 OnlineTool，默认认为模型有效
        return false;
    }

}
