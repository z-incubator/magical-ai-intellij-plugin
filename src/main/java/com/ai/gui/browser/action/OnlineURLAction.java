package com.ai.gui.browser.action;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.ai.chat.AssistantType;
import com.ai.settings.state.GeneralSettings;
import com.ai.topic.OpenAITopic;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

public class OnlineURLAction extends ComboBoxAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        String onlineUrl = GeneralSettings.getInstance().getOnlineUrl();
        String selectedActionName = switch (onlineUrl) {
            case "https://chat.openai.com/" -> "Online Tool [ ChatGPT ]";
            case "https://yiyan.baidu.com/" -> "Online Tool [ ERNIE Bot ]";
            case "https://github.com/copilot/" -> "Online Tool [ GitHub Copilot ]";
            case "https://bard.google.com/" -> "Online Tool [ Google Bard ]";
            case "https://www.bing.com/chat/" -> "Online Tool [ Microsoft Bing Chat ]";
            case "https://www.ibm.com/cloud/watson-assistant/" -> "Online Tool [ IBM Watson Assistant ]";
            case "https://huggingface.co/models/" -> "Online Tool [ Hugging Face Transformers ]";
            case "http://docs.deeppavlov.ai/en/master/" -> "Online Tool [ DeepPavlov ]";
            case "https://rasa.com/" -> "Online Tool [ Rasa ]";
            case "https://ai.facebook.com/" -> "Online Tool [ Meta AI ]";
            case "https://xinghuo.xfyun.cn/" -> "Online Tool [ 讯飞星火 ]";
            default -> "Online Tool [ Unknown ]";
        };
        e.getPresentation().setText(selectedActionName);
    }

    @Override
    protected @NotNull DefaultActionGroup createPopupActionGroup(@NotNull JComponent button, @NotNull DataContext dataContext) {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        addAction(actionGroup, "ChatGPT", "https://chat.openai.com/");
        addAction(actionGroup, "ERNIE Bot", "https://yiyan.baidu.com/");
        addAction(actionGroup, "GitHub Copilot", "https://github.com/copilot/");
        addAction(actionGroup, "Google Bard", "https://bard.google.com/");
        addAction(actionGroup, "Microsoft Bing Chat", "https://www.bing.com/chat/");
        addAction(actionGroup, "IBM Watson Assistant", "https://www.ibm.com/cloud/watson-assistant/");
        addAction(actionGroup, "Hugging Face Transformers", "https://huggingface.co/models/");
        addAction(actionGroup, "DeepPavlov", "http://docs.deeppavlov.ai/en/master/");
        addAction(actionGroup, "Rasa", "https://rasa.com/");
        addAction(actionGroup, "Meta AI", "https://ai.facebook.com/");
        addAction(actionGroup, "讯飞星火", "https://xinghuo.xfyun.cn/");
        return actionGroup;
    }

    private void addAction(DefaultActionGroup actionGroup, String name, String url) {
        actionGroup.add(new AnAction(name) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                GeneralSettings.getInstance().setOnlineUrl(url);
                Objects.requireNonNull(e.getProject()).getMessageBus().syncPublisher(OpenAITopic.TOPIC)
                        .change(AssistantType.System.OnlineTool);
            }
        });
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        // 如果这个操作是后台处理任务，使用BGT
        return ActionUpdateThread.BGT;
    }

}
