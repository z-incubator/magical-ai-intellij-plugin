
package com.ai.chat;

import com.ai.core.TextSubstitutor;
import com.ai.text.TextContent;
import com.ai.gui.prompt.context.DefaultInputContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.ai.topic.FileTopic;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.model.Media;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChatLinkService extends AbstractChatLink {

    private final Project project;
    private final InputContext inputContext;
    private final ConversationHandler conversationHandler;
    private final ChatLinkState conversationContext;

    public ChatLinkService(Project project, ConversationHandler engine, AssistantConfiguration configuration) {
        this.project = project;
        this.conversationHandler = engine;
        this.conversationContext = new ChatLinkState(configuration);
        this.conversationContext.setTextSubstitutor(project.getService(TextSubstitutor.class));
        this.inputContext = new DefaultInputContext();
    }

    @Override
    public Project getProject() {
        return project;
    }

    @Override
    public InputContext getInputContext() {
        return inputContext;
    }

    @Override
    public ConversationContext getConversationContext() {
        return conversationContext;
    }

    @Override
    public void pushMessage(String prompt, List<? extends TextContent> textContents) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> pushMessage(prompt, textContents, getInputContext()));
    }

    public void pushMessage(String prompt, List<? extends TextContent> textContents, InputContext inputContext) {
        ChatMessageComposer composer = ApplicationManager.getApplication().getService(ChatMessageComposer.class);
        List<TextContent> mergedCtx = mergeContext(textContents, inputContext);
        List<Media> mediaList = getMediaAttachments(inputContext);
        UserMessage message = composer.compose(conversationContext, prompt, mergedCtx, mediaList);
        if (message.getContent().isEmpty()) {
            return;
        }

        inputContext.clear();
        // 推送消息
        project.getMessageBus().syncPublisher(FileTopic.TOPIC).fileChange();

        ChatMessageListener listener = this.chatMessageListeners.fire();
        ChatMessageEvent.Starting event = ChatMessageEvent.starting(this, message);
        try {
            listener.exchangeStarting(event);
            conversationHandler.push(conversationContext, event, listener);
        } catch (ChatExchangeAbortException ex) {
            listener.exchangeCancelled(event.cancelled());
            getConversationContext().setLastPostedCodeFragments(List.of());
        } catch (Throwable x) {
            listener.exchangeFailed(event.failed(x));
            getConversationContext().setLastPostedCodeFragments(List.of());
        }
    }

    private static List<TextContent> mergeContext(List<? extends TextContent> textContents, InputContext inputContext) {
        if (inputContext.getAttachments().isEmpty()) {
            return List.copyOf(textContents);
        }

        List<TextContent> list = new ArrayList<>();
        Optional<TextContent> code;
        for (var contextEntry : inputContext.getAttachments())
            if ((code = contextEntry.getTextContentIfPresent()).isPresent())
                list.add(code.get());

        for (var codeFragment : textContents)
            if (!list.contains(codeFragment))
                list.add(codeFragment);

        return list;
    }

    private static List<Media> getMediaAttachments(InputContext inputContext) {
        if (inputContext.getAttachments().isEmpty()) {
            return List.of();
        }

        List<Media> list = new ArrayList<>();
        for (var attachment : inputContext.getAttachments())
            attachment.getMediaContentIfPresent().ifPresent(list::add);

        return list;
    }
}
